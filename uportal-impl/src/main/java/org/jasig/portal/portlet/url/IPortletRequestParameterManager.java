/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides access to portlet specific information for a HttpServletRequest.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRequestParameterManager {
    /**
     * Set the PortletUrls for this request
     * 
     * @param request The current request.
     * @param portletUrls List of PortletUrls parsed from the request. The first item in the list is assumed to be the targeted portlet, subsequent items are assumed to be delegate portlets in delegation order.
     * @throws IllegalArgumentException If request, portletId, or portletRequest are null.
     */
    public void setRequestInfo(HttpServletRequest request, List<PortletUrl> portletUrls);
    
    /**
     * Gets the portlet window ID targeted by the request, returns null if no portlet was targeted.
     * 
     * @param request The current request.
     * @return The IPortletWindowId of the portlet targeted by the request, null if no portlet was targeted.
     * @throws IllegalArgumentException If request is null
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the necessary information associated with it yet to return the set
     */
    public IPortletWindowId getTargetedPortletWindowId(HttpServletRequest request);
    
    /**
     * Gets the request information for the specified portlet, returns null if the specified portlet was not targeted.
     * 
     * @param request The current request.
     * @param portletWindowId The id of the portlet to get request information for
     * @return The PortletUrl for the specified IPortletWindowId, null if the specified portlet was not targeted.
     * 
     * @throws IllegalArgumentException If request is null
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the necessary information associated with it yet to return a request type
     */
    public PortletUrl getPortletRequestInfo(HttpServletRequest request, IPortletWindowId portletWindowId);
}
