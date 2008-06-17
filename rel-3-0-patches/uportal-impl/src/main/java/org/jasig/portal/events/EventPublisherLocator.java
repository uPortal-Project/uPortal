/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Simple wrapper to provide access to a {@link ApplicationEventPublisher} for classes that
 * publish events.
 * 
 * @version $Revision$
 */
public final class EventPublisherLocator {

	/**
	 * @return The ApplicationEventPublisher to use for publishing events.
	 */
	public static final ApplicationEventPublisher getApplicationEventPublisher() {
	    final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
		return applicationContext;
	}
}
