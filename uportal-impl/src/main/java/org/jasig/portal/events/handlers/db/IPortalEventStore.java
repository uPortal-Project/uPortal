/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers.db;

import org.jasig.portal.events.PortalEvent;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalEventStore {
    public void storePortalEvents(PortalEvent... portalEvents);
}
