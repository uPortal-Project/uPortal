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

package org.jasig.portal.events;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

/**
 * Base {@link ApplicationListener} for {@link PortalEvent}s. For each PortalEvent each configured
 * {@link EventHandler} will be executed.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalEventListener implements ApplicationListener, InitializingBean {
    private EventHandler eventHandler;
    
    /**
     * @return the eventHandler
     */
    public EventHandler getEventHandler() {
        return this.eventHandler;
    }
    /**
     * @param eventHandler the eventHandler to set
     */
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.eventHandler, "eventHandler must not be null");
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (!PortalEvent.class.isAssignableFrom(event.getClass())) {
            return;
        }

        final PortalEvent portalEvent = (PortalEvent) event;
        if (this.eventHandler.supports(portalEvent)) {
            this.eventHandler.handleEvent(portalEvent);
        }
    }
}
