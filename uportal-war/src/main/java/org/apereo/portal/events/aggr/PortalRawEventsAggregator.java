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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import org.apereo.portal.concurrency.locking.IClusterLockService;
import org.apereo.portal.events.PortalEvent;

/** Handles aggregation of portal raw events as well as cleanup of the generated aggregations */
public interface PortalRawEventsAggregator {
    /**
     * Name of the lock to use with {@link IClusterLockService} to call {@link
     * #doAggregateRawEvents()}
     */
    static final String AGGREGATION_LOCK_NAME =
            PortalRawEventsAggregator.class.getName() + ".AGGREGATION_LOCK";

    /**
     * Aggregates raw portal event data <br>
     * Note that this method MUST be called while the current thread & JVM owns the {@link
     * #AGGREGATION_LOCK_NAME} cluster wide lock via the {@link IClusterLockService}
     *
     * @return null if aggregation is not attempted due to some dependency being missing
     * @see IPortalEventAggregator#aggregateEvent(PortalEvent,
     *     org.apereo.portal.events.aggr.session.EventSession, EventAggregationContext, Map)
     */
    EventProcessingResult doAggregateRawEvents();

    /**
     * Close aggregations that were missed when crossing an interval boundary. <br>
     * Note that this method MUST be called while the current thread & JVM owns the {@link
     * #AGGREGATION_LOCK_NAME} cluster wide lock via the {@link IClusterLockService}
     *
     * @see IPortalEventAggregator#cleanUnclosedAggregations(org.joda.time.DateTime,
     *     org.joda.time.DateTime, AggregationInterval)
     */
    EventProcessingResult doCloseAggregations();

    /**
     * Evict cached data for the specified entities and keys.
     *
     * @param evictedEntities Map of entity type to collection of primary keys to be evicted
     */
    void evictAggregates(Map<Class<?>, Collection<Serializable>> evictedEntities);
}
