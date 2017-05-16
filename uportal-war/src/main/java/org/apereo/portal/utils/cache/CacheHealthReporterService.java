/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * This service supports custom {@link net.sf.ehcache.event.CacheEventListener} objects that
 * recognize when "something bad" is happening with a uPortal cache. Those listeners quickly contact
 * this service with the details. Periodically this service produces a report on bad things that are
 * occurring, if any, and writes it to the log.
 *
 */
@Service("cacheHealthReporterService")
public class CacheHealthReporterService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private enum Reports {
        CACHE_SIZE_NOT_LARGE_ENOUGH {
            @Override
            protected void doReportInternal(Logger logger, List<ReportTuple> list) {
                if (list.size() != 0) {
                    final StringBuilder report = new StringBuilder();
                    final Map<String, Integer> counts = new HashMap<>();
                    for (ReportTuple tuple : list) {
                        final String cacheName = tuple.getCache().getName();
                        Integer newCount = 1; // default
                        Integer oldCount = counts.get(cacheName);
                        if (oldCount != null) {
                            newCount = ++oldCount;
                        }
                        counts.put(cacheName, newCount);
                    }
                    report.append(
                            "The following cache(s) have insufficient maxElementsInMemory;  "
                                    + "there must be room in the cache to hold every object of "
                                    + "the corresponding type in the portal:");
                    for (Map.Entry<String, Integer> y : counts.entrySet()) {
                        report.append("\n\t- ")
                                .append(y.getKey())
                                .append(" (")
                                .append(y.getValue())
                                .append(
                                        " elements evicted due to insufficient size since last report)");
                    }
                    logger.warn(report.toString());
                }
            }
        };

        private final List<ReportTuple> entries =
                Collections.synchronizedList(new ArrayList<ReportTuple>());

        public final void add(ReportTuple tuple) {
            entries.add(tuple);
        }

        public final void writeReport(Logger logger) {

            // Copy & Reset the entries list
            final List<ReportTuple> list = new ArrayList<>(entries);
            entries.clear();
            doReportInternal(logger, list);
        }

        protected abstract void doReportInternal(Logger logger, List<ReportTuple> list);
    }

    public void generateReports() {
        for (Reports report : Reports.values()) {
            report.writeReport(logger);
        }
    }

    @Async
    public void reportCacheSizeNotLargeEnough(Ehcache cache, Element element) {
        final ReportTuple tuple = new ReportTuple(cache, element);
        Reports.CACHE_SIZE_NOT_LARGE_ENOUGH.add(tuple);
    }

    /*
     * Nested Types
     */

    private static final class ReportTuple {
        private final Ehcache cache;
        private final Element element;

        public ReportTuple(Ehcache cache, Element element) {
            this.cache = cache;
            this.element = element;
        }

        public Ehcache getCache() {
            return cache;
        }

        public Element getElement() {
            return element;
        }
    }
}
