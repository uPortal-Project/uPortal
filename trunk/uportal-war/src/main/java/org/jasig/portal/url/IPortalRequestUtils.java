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
     * Gets the HttpServletRequest passed into Pluto to initiate this portlet request 
     * 
     * @param portletRequest The request targeted to the portlet
     * @return The portlet's request, scoped to the portlet.
     */
    public HttpServletRequest getPortletHttpRequest(PortletRequest portletRequest);
    
    /**
     * Useful for container service callbacks and service portlets that are provided with
     * the portlet's request but need access to the portal's HttpServletRequest. 
     * 
     * @param request The request targeted to the portlet
     * @return The portal's request, not scoped to a particular portlet
     */
    public HttpServletRequest getOriginalPortalRequest(HttpServletRequest request);
    
    /**
     * @see #getPortletHttpRequest(PortletRequest)
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
    
    /**
     * Useful for container service callbacks and service portlets that are provided with a {@link HttpServletRequest}
     * but need either the root portlet request or the root portal request (depending on where processing is happening at that point)
     */
    public HttpServletRequest getOriginalPortletOrPortalRequest(HttpServletRequest portletRequest);
}
