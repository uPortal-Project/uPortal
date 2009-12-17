/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;

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
     * Parses the parameters on the request, providing a List of PortletUrls that were targeted with parameters. 
     * All PortletUrl objects returned will have their {@link RequestType} set.
     * 
     * @param request The request to parse parameters from
     * @return A list of PortletUrls in order. The first item is the primary portlet, subsequent items are delegated portlets. Empty if no portlets are targeted
     * @throws IllegalArgumentException if request is null.
     */
    public List<PortletUrl> parsePortletParameters(HttpServletRequest request);
}
