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

/**
 * Defines a class that aggregates events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalEventAggregator<E extends PortalEvent> {
    /**
     * Add the specified event to the aggregate
     */
    void aggregateEvent(E e);
    
    /**
     * Handle crossing over an interval boundary, called after the LAST event of the interval is processed.
     * 
     * @param interval The type of interval that was crossed
     * @param intervals Information about all intervals that the previous set of events was part of
     */
    void handleIntervalBoundry(Interval interval, Map<Interval, IntervalInfo> intervals);
}
