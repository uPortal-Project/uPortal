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

@Deprecated
public abstract class AbstractEventListener implements ApplicationListener, InitializingBean {

	private EventHandler[] handlers;
	
	public final void onApplicationEvent(ApplicationEvent event) {
        if (!PortalEvent.class.isAssignableFrom(event.getClass())) {
            return;
        }
        
        final PortalEvent portalEvent = (PortalEvent) event;

        for (int i = 0; i < handlers.length; i++) {
        	final EventHandler current = handlers[i];
        	
        	if (current.supports(portalEvent)) {
        		onApplicationEventInternal(portalEvent, current);
        	}
        }
	}
	
	protected abstract void onApplicationEventInternal(PortalEvent event, EventHandler handler);
	
	protected void afterPropertiesSetInternal() throws Exception {};
	
	public final void setEventHandlers(final EventHandler[] handlers) {
		this.handlers = handlers;
	}
	
	public final void afterPropertiesSet() throws Exception {
		Assert.notEmpty(this.handlers, "handlers array cannot be empty");
		afterPropertiesSetInternal();
	}
}
