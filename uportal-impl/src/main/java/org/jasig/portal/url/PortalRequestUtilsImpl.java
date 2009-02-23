/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.portlet.context.PortletRequestAttributes;

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

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getCurrentPortalRequest()
     */
    public HttpServletRequest getCurrentPortalRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        
        if (requestAttributes instanceof ServletRequestAttributes) {
            final HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            try {
                return this.getOriginalPortalRequest(request);
            }
            catch (IllegalArgumentException iae) {
                return request;
            }
        }
        else if (requestAttributes instanceof PortletRequestAttributes) {
            final PortletRequest request = ((PortletRequestAttributes)requestAttributes).getRequest();
            return this.getOriginalPortalRequest(request);
        }
        else {
            throw new IllegalStateException("No ServletRequestAttributes or PortletRequestAttributes available from the RequestContextHolder. " + (requestAttributes == null ? null : requestAttributes.getClass().getName()));
        }
    }
    
}
