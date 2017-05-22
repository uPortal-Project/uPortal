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
package org.apereo.portal.events.handlers.db;

import com.google.common.base.Function;
import org.apereo.portal.concurrency.FunctionWithoutResult;
import org.apereo.portal.events.PortalEvent;
import org.joda.time.DateTime;

/**
 * Persists, retrieves and deletes portal events from a persistent store
 *
 */
public interface IPortalEventDao {
    void storePortalEvent(PortalEvent portalEvent);

    void storePortalEvents(PortalEvent... portalEvents);

    void storePortalEvents(Iterable<PortalEvent> portalEvents);

    /**
     * Gets all persisted events in the time range. To deal with memory and data access issues the
     * results are not returned but passed in order to the provided {@link FunctionWithoutResult}
     * handler.
     *
     * @param startTime The inclusive start time to get events for
     * @param endTime The exclusive end time to get events for
     * @param maxEvents The maximum number events to retrieve. -1 means no limit
     * @param handler Function which will be called for each event.
     */
    void getPortalEvents(
            DateTime startTime,
            DateTime endTime,
            int maxEvents,
            FunctionWithoutResult<PortalEvent> handler);
    /** @see #getPortalEvents(DateTime, DateTime, int, FunctionWithoutResult) */
    void getPortalEvents(
            DateTime startTime, DateTime endTime, FunctionWithoutResult<PortalEvent> handler);

    /**
     * Gets all un-aggregated persisted events in the time range. After the handler is called on
     * each event it is marked as aggregated. To deal with memory and data access issues the results
     * are not returned but passed in order to the provided {@link Function} handler. If aggregation
     * should stop the handler should return false after processing an event. Only events processed
     * up to that point will be marked as aggregated.
     *
     * @param startTime The inclusive start time to get events for
     * @param endTime The exclusive end time to get events for
     * @param maxEvents The maximum number events to retrieve. -1 means no limit
     * @param handler Function which will be called for each event.
     * @return true if all events were handled successfully, false if the handler returns false for
     *     any event to signal processing should be stopped
     */
    boolean aggregatePortalEvents(
            DateTime startTime,
            DateTime endTime,
            int maxEvents,
            Function<PortalEvent, Boolean> handler);

    /** @return The timestamp of the oldest event in the persitent store */
    DateTime getOldestPortalEventTimestamp();

    /** @return The timestamp of the most recent event in the persitent store */
    DateTime getNewestPortalEventTimestamp();

    /** Delete events with timestamps from before the specified date (exclusive) */
    int deletePortalEventsBefore(DateTime endTime);
}
