/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
