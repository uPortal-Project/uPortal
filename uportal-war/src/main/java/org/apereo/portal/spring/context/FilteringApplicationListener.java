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
package org.apereo.portal.spring.context;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.OrderComparator;

/**
 * Uses list of {@link ApplicationEventFilter} to decide if an event should be passed to the
 * subclass.
 *
 */
public abstract class FilteringApplicationListener<E extends ApplicationEvent>
        implements ApplicationListener<E> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private List<ApplicationEventFilter<E>> applicationEventFilters = Collections.emptyList();

    /** @param applicationEventFilters The list of filters to test each event with */
    public final void setApplicationEventFilters(
            List<ApplicationEventFilter<E>> applicationEventFilters) {
        applicationEventFilters = new ArrayList<ApplicationEventFilter<E>>(applicationEventFilters);
        Collections.sort(applicationEventFilters, OrderComparator.INSTANCE);
        this.applicationEventFilters = ImmutableList.copyOf(applicationEventFilters);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public final void onApplicationEvent(E event) {
        for (final ApplicationEventFilter<E> applicationEventFilter :
                this.applicationEventFilters) {
            if (!applicationEventFilter.supports(event)) {
                logger.debug(
                        "Filter {} does not support event {} it will be ignored",
                        applicationEventFilter,
                        event);
                return;
            }
        }

        this.onFilteredApplicationEvent(event);
    }

    /** @param event Event that has passed all configured {@link ApplicationEventFilter}s */
    protected abstract void onFilteredApplicationEvent(E event);
}
