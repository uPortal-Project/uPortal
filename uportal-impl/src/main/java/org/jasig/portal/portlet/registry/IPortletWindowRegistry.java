/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides methods for creating and accessing {@link IPortletWindow} and related objects.
 * 
 * Methods require a {@link HttpServletRequest} object due to the nature of IPortletWindows and how they are tracked.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletWindowRegistry {
    /**
     * Get the IPortletWindow specified by the IPortletWindowId.
     * 
     * @param request The current request.
     * @param portletWindowId The ID of the IPortletWindow to return.
     * @return The requested IPortletWindow, if no window exists for the ID null will be returned.
     * @throws IllegalArgumentException if request or portletWindowId are null.
     */
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId);
    
    /**
     * Converts a Pluto {@link PortletWindow} object to a uPortal {@link IPortletWindow}.
     * 
     * @param request The request related to the window objects
     * @param portletWindow The Pluto {@link PortletWindow} to convert from
     * @return The corresponding uPortal {@link IPortletWindow}, will not be null.
     * @throws IllegalArgumentException if request or portletWindow are null
     */
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow portletWindow);

    /**
     * Creates an IPortletWindowId for the specified string identifier
     * 
     * @param portletWindowId The string represenation of the portlet window ID.
     * @return The IPortletWindowId for the string
     * @throws IllegalArgumentException If portletWindowId is null
     */
    public IPortletWindowId getPortletWindowId(String portletWindowId);
}
