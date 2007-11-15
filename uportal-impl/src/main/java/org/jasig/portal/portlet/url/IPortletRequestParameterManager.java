/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;

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
     * @throws IllegalStateException if {@link #setRequestType(HttpServletRequest, IPortletWindowId, RequestType)} has already been called.
     */
    public void setNoPortletRequest(HttpServletRequest request);
    
    /**
     * Set the type of request for this portlet.
     * 
     * @param request The current request.
     * @param portletId The ID of the portlet targeted
     * @param type The type of request for the targeted portlet
     * @throws IllegalArgumentException If request, portletId, or type are null.
     * @throws IllegalStateException if {@link #setNoPortletRequest(HttpServletRequest)} has already been called.
     */
    public void setRequestType(HttpServletRequest request, IPortletWindowId portletId, RequestType type);
    
    /**
     * If this request targets a portlet.
     * 
     * @param request The current request.
     * @return True if a portlet is targeted by this request 
     * @throws RequestParameterProcessingIncompleteException If {@link org.jasig.portal.url.processing.PortletParameterProcessor} has not completed on this request.
     */
    public boolean isPortletTargeted(HttpServletRequest request);
    
    /**
     * Get all portlet IDs that were targeted by this request.
     * 
     * @param request The current request.
     * @return A Set of channel ids that have had parameters associated with this request.
     * @throws IllegalArgumentException If request is null
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the necessary information associated with it yet to return the set
     */
    public Set<IPortletWindowId> getTargetedPortletWindowIds(HttpServletRequest request);
    
    /**
     * Gets the request type for the request that targeted the specified portlet window id.
     * 
     * @param request The current request.
     * @param portletId The ID of the portlet to get the request type for.
     * @return The type of the request that targeted the portlet id.
     * @throws IllegalArgumentException If request, or portletId
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the necessary information associated with it yet to return a request type
     */
    public RequestType getPortletRequestType(HttpServletRequest request, IPortletWindowId portletId);
}
