/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.aggr;

import java.util.Map;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.session.EventSession;

/**
 * Defines a class that aggregates events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalEventAggregator<E extends PortalEvent> {
    /**
     * @return true if this aggregator supports the specified event type
     */
    boolean supports(Class<? extends PortalEvent> type);
    
    /**
     * Add the specified event to the aggregate
     * 
     * @param e The event to aggregate
     * @param eventSession Information about the event session associated with the event, MAY BE NULL!
     * @param currentIntervals Information about all of the intervals the event exists in.
     */
    void aggregateEvent(E e, EventSession eventSession, Map<AggregationInterval, AggregationIntervalInfo> currentIntervals);
    
    /**
     * Handle crossing over an interval boundary, called after the LAST event of the interval is processed.
     * 
     * @param interval The type of interval that was crossed
     * @param intervals Information about all intervals that the previous set of events was part of
     */
    void handleIntervalBoundary(AggregationInterval interval, Map<AggregationInterval, AggregationIntervalInfo> intervals);
}
