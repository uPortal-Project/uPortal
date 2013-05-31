package org.jasig.portal.events;

import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Tracks events that have been fired during this request and provides a thread-safe
 * read-only set of those events.
 * 
 * @author Eric Dalquist
 */
public interface RequestScopedEventsTracker {

    /**
     * Return a read-only set of all events that have been fired so far during the handling of the portal's request
     */
    Set<PortalEvent> getRequestEvents(PortletRequest portletRequest);

    /**
     * Return a read-only set of all events that have been fired so far during the handling of the portal's request
     */
    Set<PortalEvent> getRequestEvents(HttpServletRequest request);
}