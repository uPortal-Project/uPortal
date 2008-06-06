/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
