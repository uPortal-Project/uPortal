/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events;

/**
 * Extension of EventHandler that also knows how to deal with batches of events.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface BatchingEventHandler extends EventHandler {

    /**
     * Method to handle any processing of an array of events that is needed.
     * 
     * @param events the events to handle.
     */
    void handleEvents(PortalEvent... events);
}
