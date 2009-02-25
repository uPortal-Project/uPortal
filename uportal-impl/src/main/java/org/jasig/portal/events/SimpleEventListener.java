/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events;

/**
 * Simple Implementation of EventListener that calls the appropriate
 * <code>EventHandler</code> without extra processing.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 * @deprecated Use {@link PortalEventListener}
 */
@Deprecated
public final class SimpleEventListener extends AbstractEventListener {

	protected void onApplicationEventInternal(final PortalEvent event,
			final EventHandler handler) {
		handler.handleEvent(event);
	}
}
