/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletRequestParameterManager {
    /**
     * Mark this request as not having any portlet request parameters associated with it.
     * 
     * @param request The current request.
     * @throws IllegalArgumentException If request is null
     */
    public void setNoPortletParameters(HttpServletRequest request);
    
    /**
     * Set the type of request for this portlet.
     * 
     * @param request The current request.
     * @param portletId The ID of the portlet targeted
     * @param type The type of request for the targeted portlet
     * @throws IllegalArgumentException If request, portletId, or type are null.
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
}
