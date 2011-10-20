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

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.handlers.QueueingEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Hands off queued portal events for storage by the IPortalEventDao
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("PortalEventDaoQueuingEventHandler")
public class PortalEventDaoQueuingEventHandler extends QueueingEventHandler<PortalEvent> {
    private IPortalEventDao portalEventDao;
    
    /**
     * @param portalEventDao the portalEventDao to set
     */
    @Autowired
    public void setPortalEventDao(IPortalEventDao portalEventDao) {
        this.portalEventDao = portalEventDao;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.events.handlers.QueueingEventHandler#onApplicationEvents(java.lang.Iterable)
     */
    @Override
    protected void onApplicationEvents(Iterable<PortalEvent> events) {
        this.portalEventDao.storePortalEvents(events);
    }
}
