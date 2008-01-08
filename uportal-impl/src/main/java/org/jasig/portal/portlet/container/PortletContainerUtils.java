/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.url.AttributeScopingHttpServletRequestWrapper;

/**
 * Static utilities for working with the portlet container and related objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletContainerUtils {
    private PortletContainerUtils() {
    }

    /**
     * Useful for container service callbacks that are provided with the portlet's request
     * but need access to the portal's HttpServletRequest. 
     * 
     * @param portletRequest The request targeted to the portlet
     * @return The portal's request
     */
    public static HttpServletRequest getHttpServletRequest(PortletRequest portletRequest) {
        final HttpServletRequest portalRequest = (HttpServletRequest)portletRequest.getAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__PORTAL_HTTP_SERVLET_REQUEST);
        if (portalRequest != null) {
            return portalRequest;
        }
        
        throw new IllegalArgumentException("The Portal's HttpServletRequest is not available from the PorteltRequest using attribute '" + AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__PORTAL_HTTP_SERVLET_REQUEST + "'");
    }
}
