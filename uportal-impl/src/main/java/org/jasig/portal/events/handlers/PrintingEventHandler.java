/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import org.jasig.portal.events.PortalEvent;

/**
 * Instance of Event Handler that merely writes out events to the
 * <code>System.out</code> PrintWriter.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 * 
 */
public final class PrintingEventHandler extends AbstractLimitedSupportEventHandler {

	public void handleEvent(final PortalEvent event) {
		System.out.println(event.toString());
	}
}
