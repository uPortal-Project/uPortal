/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalRequestUtils {
    /**
     * Useful for container service callbacks that are provided with the portlet's request
     * but need access to the HttpServletRequest passed into the portlet container. 
     * 
     * @param portletRequest The request targeted to the portlet
     * @return The portlet scoped request passed to the portlet container
     */
    public HttpServletRequest getOriginalPortletAdaptorRequest(PortletRequest portletRequest);
    
    /**
     * @see #getOriginalPortletAdaptorRequest(PortletRequest)
     */
    public HttpServletRequest getOriginalPortletAdaptorRequest(HttpServletRequest portletRequest);
    
    /**
     * Useful for container service callbacks and service portlets that are provided with
     * the portlet's request but need access to the portal's HttpServletRequest. 
     * 
     * @param portletRequest The request targeted to the portlet
     * @return The portal's request, not scoped to a particular portlet
     */
    public HttpServletRequest getOriginalPortalRequest(PortletRequest portletRequest);
    
    /**
     * @see #getOriginalPortalRequest(PortletRequest)
     */
    public HttpServletRequest getOriginalPortalRequest(HttpServletRequest portletRequest);
}
