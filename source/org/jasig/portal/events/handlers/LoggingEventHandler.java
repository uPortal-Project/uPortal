/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import org.jasig.portal.events.PortalEvent;

/**
 * Instance of Event Handler that delegates to Commons Logging and writes out
 * events at the INFO level.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 * 
 */
public final class LoggingEventHandler extends AbstractLimitedSupportEventHandler {

	public void handleEvent(final PortalEvent event) {
		if (log.isInfoEnabled()) {
			log.info(getDefaultMessage(event));
		}
	}
}
