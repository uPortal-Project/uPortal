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

import javax.persistence.FlushModeType;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;

/**
 * Defines a class that aggregates events. <br>
 * IMPORTANT: The AggrEventsDb EntityManager {@link BaseAggrEventsJpaDao} that is open during
 * execution is running in {@link FlushModeType#COMMIT}.
 *
 */
public interface IPortalEventAggregator<E extends PortalEvent> {
    /** @return true if this aggregator supports the specified event type */
    boolean supports(Class<? extends PortalEvent> type);

    /** @return true if this aggregator supports the specified event */
    boolean supports(PortalEvent event);
}
