/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

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
     * Set the PortletUrl for the portlet that is targeted by the request
     * 
     * @param request The current request.
     * @param portletUrl The PortletUrl for the targeted portlet, null if no portlet was targeted for the request
     * @throws IllegalArgumentException If request or is null.
     */
    public void setTargetedPortletUrl(HttpServletRequest request, PortletUrl portletUrl);
    
    /**
     * Set the PortletUrl for portlets that have data in this request but are not targeted
     * 
     * @param request The current request
     * @param portletUrl The PortletUrl for the additional portlet
     * @throws IllegalArgumentException If request or portletUrl are null.
     */
    public void setAdditionalPortletUrl(HttpServletRequest request, PortletUrl portletUrl);
    
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
