/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.url;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.utils.web.PortletHttpServletRequestWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.context.PortletRequestAttributes;

/**
 * Provides access to the original portal and portlet requests using the {@link PortalHttpServletRequestWrapper#ATTRIBUTE__HTTP_SERVLET_REQUEST}
 * and {@link PortletHttpServletRequestWrapper#ATTRIBUTE__HTTP_SERVLET_REQUEST}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portalRequestUtils")
public class PortalRequestUtilsImpl implements IPortalRequestUtils {

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getOriginalPortalRequest(javax.portlet.PortletRequest)
     */
    @Override
    public HttpServletRequest getPortletHttpRequest(PortletRequest portletRequest) {
        final HttpServletRequest portalRequest = (HttpServletRequest)portletRequest.getAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (portalRequest != null) {
            return portalRequest;
        }
        
        throw new IllegalArgumentException("The orginal portlet HttpServletRequest is not available from the PortletRequest using attribute '" + PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST + "'");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getOriginalPortalRequest(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public HttpServletRequest getOriginalPortalRequest(HttpServletRequest portletRequest) {
        final HttpServletRequest portalRequest = (HttpServletRequest)portletRequest.getAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (portalRequest != null) {
            return portalRequest;
        }
        
        return portletRequest;
    }
    
    @Override
    public HttpServletRequest getOriginalPortletOrPortalRequest(HttpServletRequest request) {
        final HttpServletRequest portletRequest = (HttpServletRequest)request.getAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST);
        if (portletRequest != null) {
            return portletRequest;
        }
        
        return this.getOriginalPortalRequest(request);
    }

    @Override
    public HttpServletRequest getOriginalPortalRequest(WebRequest request) {
        final HttpServletRequest portalRequest = (HttpServletRequest)request.getAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, WebRequest.SCOPE_REQUEST);
        if (portalRequest != null) {
            return portalRequest;
        }
        
        if (request instanceof NativeWebRequest) {
            final NativeWebRequest nativeWebRequest = (NativeWebRequest)request;
            
            final Object nativeRequest = nativeWebRequest.getNativeRequest();
            if (nativeRequest instanceof HttpServletRequest) {
                return (HttpServletRequest)nativeRequest;
            }
        }
        
        throw new IllegalArgumentException("The orginal portal HttpServletRequest is not available from the WebRequest using attribute '" + PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST + "'");
    }

    @Override
    public HttpServletResponse getOriginalPortalResponse(PortletRequest portletRequest) {
        final HttpServletResponse portalResponse = (HttpServletResponse)portletRequest.getAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_RESPONSE);
        if (portalResponse != null) {
            return portalResponse;
        }
        
        throw new IllegalArgumentException("The orginal portal HttpServletResponse is not available from the PortletRequest using attribute '" + PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_RESPONSE + "'");
    }
    
    @Override
    public HttpServletResponse getOriginalPortalResponse(HttpServletRequest portletRequest) {
        final HttpServletResponse portalResponse = (HttpServletResponse)portletRequest.getAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_RESPONSE);
        if (portalResponse != null) {
            return portalResponse;
        }
        
        throw new IllegalArgumentException("The orginal portal HttpServletResponse is not available from the HttpServletRequest using attribute '" + PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_RESPONSE + "'");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalRequestUtils#getCurrentPortalRequest()
     */
    @Override
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
            return this.getPortletHttpRequest(request);
        }
        else {
            throw new IllegalStateException("No ServletRequestAttributes or PortletRequestAttributes available from the RequestContextHolder. " + (requestAttributes == null ? null : requestAttributes.getClass().getName()));
        }
    }

}
