package org.jasig.portal.events;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

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
