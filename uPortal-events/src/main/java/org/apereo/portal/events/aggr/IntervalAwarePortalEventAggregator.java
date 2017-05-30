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
package org.apereo.portal.events.aggr;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.session.EventSession;
import org.joda.time.DateTime;

/**
 * Defines a class that aggregates events. <br>
 * IMPORTANT: The AggrEventsDb EntityManager that is open during execution is running in {@link
 * FlushModeType#COMMIT}. It is recommended that implementations make use of the {@link
 * EventAggregationContext} to track created/modified entities during {@link
 * #aggregateEvent(PortalEvent, EventSession, EventAggregationContext, Map)} calls and only call
 * {@link EntityManager#persist(Object)} during {@link #handleIntervalBoundary(AggregationInterval,
 * EventAggregationContext, Map)} calls. <br>
 * An explicit {@link EntityManager#flush()} call is made after all aggregators have had {@link
 * #handleIntervalBoundary(AggregationInterval, EventAggregationContext, Map)} called.
 *
 */
public interface IntervalAwarePortalEventAggregator<E extends PortalEvent>
        extends IPortalEventAggregator<E> {
    /**
     * Add the specified event to the aggregate
     *
     * @param e The event to aggregate
     * @param eventSession Information about the event session associated with the event
     * @param eventAggregationContext Context used to store stateful information for an event
     *     aggregation run
     * @param currentIntervals Information about all of the intervals the event exists in.
     */
    void aggregateEvent(
            E e,
            EventSession eventSession,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> currentIntervals);

    /**
     * Handle crossing over an interval boundary, called after the LAST event of the interval is
     * processed.
     *
     * @param interval The type of interval that was crossed
     * @param intervals Information about all intervals that the previous set of events was part of
     */
    void handleIntervalBoundary(
            AggregationInterval interval,
            EventAggregationContext eventAggregationContext,
            Map<AggregationInterval, AggregationIntervalInfo> intervals);

    /**
     * Due to cases where an interval boundary might be missed this method should contain the logic
     * to clean up and close all aggregations for the specified interval exist before the specified
     * DateTime
     *
     * @return the number of unclosed aggregations that were closed
     * @see BaseAggregationPrivateDao#getUnclosedAggregations(DateTime, DateTime,
     *     AggregationInterval)
     */
    int cleanUnclosedAggregations(DateTime start, DateTime end, AggregationInterval interval);
}
