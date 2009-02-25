/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
