/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
     * Mark this request as not having any portlet request parameters associated with it.
     * 
     * @param request The current request.
     * @throws IllegalArgumentException If request is null
     * @throws IllegalStateException if {@link #setRequestInfo(HttpServletRequest, IPortletWindowId, PortletRequestInfo)} has already been called.
     */
    public void setNoPortletRequest(HttpServletRequest request);
    
    /**
     * Set the type of request for this portlet.
     * 
     * @param request The current request.
     * @param portletId The ID of the portlet targeted
     * @param portletRequestInfo Data about the request
     * @throws IllegalArgumentException If request, portletId, or portletRequest are null.
     * @throws IllegalStateException if {@link #setNoPortletRequest(HttpServletRequest)} has already been called.
     */
    public void setRequestInfo(HttpServletRequest request, IPortletWindowId portletId, PortletRequestInfo portletRequestInfo);
    
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
     * Gets the request information for the targeted portlet, returns null if no portlet was targeted.
     * 
     * @param request The current request.
     * @return The PortletRequestInfo for the targeted IPortletWindowId, null if no portlet was targeted.
     * @throws IllegalArgumentException If request is null
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the necessary information associated with it yet to return a request type
     */
    public PortletRequestInfo getPortletRequestInfo(HttpServletRequest request);
}
