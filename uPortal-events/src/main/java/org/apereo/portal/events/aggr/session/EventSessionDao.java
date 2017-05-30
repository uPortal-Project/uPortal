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
package org.apereo.portal.events.aggr.session;

import org.apereo.portal.events.PortalEvent;
import org.joda.time.DateTime;

/**
 * Tracks event session data during aggregation.
 *
 */
public interface EventSessionDao {

    /** Store a modified event session */
    void storeEventSession(EventSession eventSession);

    /**
     * Get the {@link EventSession} for the event.
     *
     * @param event The event to get the session for
     * @return The event session, will not return null
     */
    EventSession getEventSession(PortalEvent event);

    /**
     * @param eventSessionId The id of the session to delete, see {@link
     *     PortalEvent#getEventSessionId()}
     */
    void deleteEventSession(String eventSessionId);

    /** Purge expired events, expired is defined by the implementation */
    int purgeEventSessionsBefore(DateTime lastAggregatedEventDate);
}
