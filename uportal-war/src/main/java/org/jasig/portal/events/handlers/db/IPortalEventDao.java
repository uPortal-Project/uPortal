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

package org.jasig.portal.events.handlers.db;

import java.util.Date;

import org.jasig.portal.events.PortalEvent;

import com.google.common.base.Function;

/**
 * Persists, retrieves and deletes portal events from a persistent store 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalEventDao {
    void storePortalEvent(PortalEvent portalEvent);
    void storePortalEvents(PortalEvent... portalEvents);
    void storePortalEvents(Iterable<PortalEvent> portalEvents);
    
    /**
     * @param startTime The inclusive start time to get events for
     * @param endTime The exclusive end time to get events for
     * @param handler Function which will be called for each event.
     */
    void getPortalEvents(Date startTime, Date endTime, Function<PortalEvent, Object> handler);
    
    /**
     * Delete events with timestamps from before the specified date (exclusive)
     */
    int deletePortalEventsBefore(Date endTime);
}
