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

import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.handlers.QueueingEventHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Hands off queued portal events for storage by the IPortalEventDao
 *
 */
public class PortalEventDaoQueuingEventHandler extends QueueingEventHandler<PortalEvent> {
    private IPortalEventDao portalEventDao;

    /** @param portalEventDao the portalEventDao to set */
    @Autowired
    public void setPortalEventDao(IPortalEventDao portalEventDao) {
        this.portalEventDao = portalEventDao;
    }

    @Override
    protected void onApplicationEvents(Iterable<PortalEvent> events) {
        this.portalEventDao.storePortalEvents(events);
    }
}
