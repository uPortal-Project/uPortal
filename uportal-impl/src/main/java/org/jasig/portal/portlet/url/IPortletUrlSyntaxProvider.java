/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Encapsulates the logic for writing to and reading from URLs for portlets. This should hide the actual paramter
 * namespacing, encoding and related logic for both URL generation and parsing.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletUrlSyntaxProvider {
    /**
     * Generates a full portlet URL for the current request, passed portlet window & portlet URL data object. The
     * generated URL will be complete and ready for rendering.
     * 
     * @param request The current request
     * @param portletWindow The portlet window the parameters are for
     * @param portletUrl The Portlet URL data to be written.
     * @return A fully generated portlet URL ready to be rendered
     * @throws IllegalArgumentException if request, portletWindowId, or portletUrl are null
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletWindow portletWindow, PortletUrl portletUrl);
    
    /**
     * Parses the parameters on the request, providing a Map of portlet IDs that were targeted with parameters
     * and the populated PortletUrl object for that ID. All PortletUrl objects returned will have their {@link RequestType}
     * set.
     * 
     * @param request The request to parse parameters from
     * @return A Map of targeted IPortletWindowIds to populated PortletUrls, will never be null.
     * @throws IllegalArgumentException if request is null.
     */
    public Map<IPortletWindowId, PortletUrl> parsePortletParameters(HttpServletRequest request);
}
