/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Simple wrapper to provide access to a {@link ApplicationEventPublisher} for classes that
 * publish events.
 * 
 * @version $Revision$
 * @deprecated Use Spring managed beans and {@link org.springframework.context.ApplicationEventPublisherAware}
 */
@Deprecated
public final class EventPublisherLocator {

	/**
	 * @return The ApplicationEventPublisher to use for publishing events.
	 */
	public static final ApplicationEventPublisher getApplicationEventPublisher() {
	    return PortalApplicationContextLocator.getApplicationContext();
	}
}
