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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class exposes some limited functions around the provided {@link CacheManager}. It is used
 * within SpEL expressions by the Cache Manager portlet.
 */
@Service
public class CacheManagementHelper {
    private static final class CaseInsenstivieStringComparator implements Comparator<String> {
        public static final CaseInsenstivieStringComparator INSTANCE =
                new CaseInsenstivieStringComparator();

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    protected final Log logger = LogFactory.getLog(this.getClass());

    private CacheManager cacheManager;

    /** @param cacheManager the cacheManager to set */
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Map<String, CacheStatistics> getAllCacheStatistics() {
        final Map<String, CacheStatistics> allCacheStatistics =
                new TreeMap<String, CacheStatistics>(CaseInsenstivieStringComparator.INSTANCE);

        for (final String cacheName : this.cacheManager.getCacheNames()) {
            final Cache cache = this.cacheManager.getCache(cacheName);

            if (null != cache && Status.STATUS_ALIVE.equals(cache.getStatus())) {
                final CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
                final Statistics statistics = cache.getStatistics();

                final CacheStatistics cacheStatistics = new CacheStatistics();

                cacheStatistics.hits = statistics.getCacheHits();
                cacheStatistics.misses = statistics.getCacheMisses();
                cacheStatistics.size = statistics.getObjectCount();
                cacheStatistics.maxSize =
                        cacheConfiguration.getMaxElementsInMemory()
                                + cacheConfiguration.getMaxElementsOnDisk();

                allCacheStatistics.put(cacheName, cacheStatistics);
            }
        }

        return allCacheStatistics;
    }

    /**
     * @see Status#STATUS_ALIVE
     * @see Cache#getStatus()
     * @see Cache#getStatistics()
     * @param cacheName
     * @return the {@link Statistics} for the specified cache; returns null of cache is not alive or
     *     doesn't exist
     */
    public Statistics getCacheStatistics(String cacheName) {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (null != cache && Status.STATUS_ALIVE.equals(cache.getStatus())) {
            Statistics result = cache.getStatistics();
            return result;
        }

        return null;
    }

    /**
     * Call {@link Cache#removeAll()} on the specified cache, if it exists and is alive.
     *
     * @see Status#STATUS_ALIVE
     * @see Cache#getStatus()
     * @param cacheName
     */
    public void clearCache(String cacheName) {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (null != cache && Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache.removeAll();
            logger.warn("finished removeAll for cache: " + cacheName);
        }
    }

    /** Call {@link #clearCache(String)} on ALL caches. */
    public void clearAllCaches() {
        logger.warn("beginning request to clear all caches");
        for (final String cacheName : this.cacheManager.getCacheNames()) {
            clearCache(cacheName);
        }
        logger.warn("completed request to clear all caches");
    }

    public static class CacheStatistics {
        public long hits;
        public long misses;
        public long size;
        public long maxSize;

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getSize() {
            return size;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public double getUsage() {
            if (this.maxSize == 0) {
                return 0;
            }

            return (double) this.size / (double) this.maxSize;
        }

        public double getEffectiveness() {
            final double requests = this.hits + this.misses;

            if (requests == 0) {
                return 0;
            }

            return (double) this.hits / requests;
        }
    }
}
