/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to the original portal and portlet requests using the {@link PortalHttpServletRequest#ATTRIBUTE__HTTP_SERVLET_REQUEST}
 * and {@link AttributeScopingHttpServletRequestWrapper#ATTRIBUTE__HTTP_SERVLET_REQUEST}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalRequestUtilsImpl implements IPortalRequestUtils {

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getOriginalPortalRequest(javax.portlet.PortletRequest)
     */
    public HttpServletRequest getOriginalPortalRequest(PortletRequest portletRequest) {
        final HttpServletRequest portalRequest = (HttpServletRequest)portletRequest.getAttribute(PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (portalRequest != null) {
            return portalRequest;
        }
        
        throw new IllegalArgumentException("The orginal portal HttpServletRequest is not available from the PortletRequest using attribute '" + PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST + "'");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getOriginalPortalRequest(javax.servlet.http.HttpServletRequest)
     */
    public HttpServletRequest getOriginalPortalRequest(HttpServletRequest portletRequest) {
        final HttpServletRequest portalRequest = (HttpServletRequest)portletRequest.getAttribute(PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (portalRequest != null) {
            return portalRequest;
        }
        
        throw new IllegalArgumentException("The orginal portal HttpServletRequest is not available from the HttpServletRequest using attribute '" + PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST + "'");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getOriginalPortletAdaptorRequest(javax.portlet.PortletRequest)
     */
    public HttpServletRequest getOriginalPortletAdaptorRequest(PortletRequest portletRequest) {
        final HttpServletRequest originalPortletRequest = (HttpServletRequest)portletRequest.getAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (originalPortletRequest != null) {
            return originalPortletRequest;
        }
        
        throw new IllegalArgumentException("The original portlet adaptor HttpServletRequest is not available from the PorteltRequest using attribute '" + AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST + "'");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getOriginalPortletAdaptorRequest(javax.servlet.http.HttpServletRequest)
     */
    public HttpServletRequest getOriginalPortletAdaptorRequest(HttpServletRequest portletRequest) {
        final HttpServletRequest originalPortletRequest = (HttpServletRequest)portletRequest.getAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (originalPortletRequest != null) {
            return originalPortletRequest;
        }
        
        throw new IllegalArgumentException("The original portlet adaptor HttpServletRequest is not available from the HttpServletRequest using attribute '" + AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST + "'");

    }

}
