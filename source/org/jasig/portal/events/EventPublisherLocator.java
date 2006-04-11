package org.jasig.portal.events;

import org.jasig.portal.spring.PortalApplicationContextFacade;
import org.springframework.context.ApplicationEventPublisher;

public final class EventPublisherLocator {
	/** Single instance of the ApplicationEventPublisher that uPortal will use. */
	private final static ApplicationEventPublisher applicationEventPublisher = (ApplicationEventPublisher) PortalApplicationContextFacade
			.getPortalApplicationContext();

	/**
	 * Method to retrieve the cache factory
	 * 
	 * @return the cache factory.
	 */
	public static final ApplicationEventPublisher getApplicationEventPublisher() {
		return applicationEventPublisher;
	}
}
