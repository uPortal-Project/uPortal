/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events;

/**
 * Interface of classes that know how to handle a specific event. The concept of
 * handling an event usually implies that the handler will perform some form of
 * logging such as using Log4j or writing the information to a database.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface EventHandler {

    /**
     * Method to handle any processing of the event that is needed.
     * 
     * @param event the event to handle.
     */
    void handleEvent(PortalEvent event);

    /**
     * Method to check if this handler will be able to process the event.
     * 
     * @param event the event we want to check if we support.
     * @return true if the event is supported, false otherwise.
     */
    boolean supports(PortalEvent event);
}
