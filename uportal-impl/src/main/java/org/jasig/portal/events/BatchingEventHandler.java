/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
