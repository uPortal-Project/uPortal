/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Provides methods for creating and accessing {@link IPortletWindow} and related objects.
 * 
 * Methods require a {@link HttpServletRequest} object due to the nature of IPortletWindows and how they are tracked.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletWindowRegistry {
//    /**
//     * TODO determine entity specific information that needs to be passed in here to correctly create a IPortletWindow
//     * passing in some sort of entity identifier should allieviate the need to pass the contextPath and portletName here
//     * 
//     * Creates a new IPortletWindow, generating a globally unique indentifier for it.
//     * 
//     * @param request
//     * @param contextPath
//     * @param portletName
//     * @return
//     */
//    public IPortletWindow createPortletWindow(HttpServletRequest request, String contextPath, String portletName);
//    
//    public IPortletWindow clonePortletWindow(HttpServletRequest request, IPortletWindow portletWindow);
//    
//    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId);
//    
    /**
     * Converts a Pluto {@link PortletWindow} object to a uPortal {@link IPortletWindow}.
     * 
     * @param request The request related to the window objects
     * @param portletWindow The Pluto {@link PortletWindow} to convert from
     * @return The corresponding uPortal {@link IPortletWindow}, will not be null.
     * @throws IllegalArgumentException if request or portletWindow are null
     */
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow portletWindow);

//    public IPortletWindowId getPortletWindowId(String portletWindowId);
}
