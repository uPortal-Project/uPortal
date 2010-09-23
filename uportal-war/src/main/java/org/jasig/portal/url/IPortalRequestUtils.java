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

import org.springframework.web.context.request.WebRequest;

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
     * @param portletRequest The request targeted to the portlet
     * @return The next request up the chain from a portlet adaptor request
     */
    public HttpServletRequest getPortletAdaptorParentRequest(HttpServletRequest portletRequest);
    
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
    
    /**
     * @see #getOriginalPortalRequest(PortletRequest)
     */
    public HttpServletRequest getOriginalPortalRequest(WebRequest request);
    
    /**
     * Useful for container service callbacks and service portlets that are provided with
     * the portlet's request but need access to the portal's HttpServletResponse. 
     * 
     * @param portletRequest The request targeted to the portlet
     * @return The portal's response, not scoped to a particular portlet
     */
    public HttpServletResponse getOriginalPortalResponse(PortletRequest portletRequest);
    
    /**
     * @see #getOriginalPortalResponse(PortletRequest)
     */
    public HttpServletResponse getOriginalPortalResponse(HttpServletRequest portletRequest);
    
    /**
     * Uses {@link org.springframework.web.context.request.RequestContextHolder} to retrieve the current
     * portal HttpServletRequest 
     */
    public HttpServletRequest getCurrentPortalRequest();
}
