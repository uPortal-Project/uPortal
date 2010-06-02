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

package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortletPortalUrl;

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
     */
    public void setNoPortletRequest(HttpServletRequest request);
    
    /**
     * Set the type of request for this portlet.
     * 
     * @param request The current request.
     * @param portletId The ID of the portlet targeted
     * @param portletRequestInfo Data about the request
     * @throws IllegalArgumentException If request, portletId, or portletRequest are null.
     */
    public void setRequestInfo(HttpServletRequest request, IPortletWindowId portletId, PortletRequestInfo portletRequestInfo);
    
    /**
     * Set the PortletUrl for the portlet that is targeted by the request
     * 
     * @param request The current request.
     * @param portletUrl The PortletUrl for the targeted portlet, null if no portlet was targeted for the request
     * @throws IllegalArgumentException If request or is null.
     */
    @Deprecated
    public void setTargetedPortletUrl(HttpServletRequest request, IPortletPortalUrl portletUrl);
    
    /**
     * Set the PortletUrl for portlets that have data in this request but are not targeted
     * 
     * @param request The current request
     * @param portletUrl The PortletUrl for the additional portlet
     * @throws IllegalArgumentException If request or portletUrl are null.
     */
     @Deprecated
    public void setAdditionalPortletUrl(HttpServletRequest request, IPortletPortalUrl portletUrl);
    
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
     * Gets the request information for the specified portlet, returns null if the specified portlet was not targeted.
     * 
     * @param request The current request.
     * @param portletWindowId The id of the portlet to get request information for
     * @return The PortletUrl for the specified IPortletWindowId, null if the specified portlet was not targeted.
     * 
     * @throws IllegalArgumentException If request is null
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the necessary information associated with it yet to return a request type
     */
     @Deprecated
    public IPortletPortalUrl getPortletRequestInfo(HttpServletRequest request, IPortletWindowId portletWindowId);
    
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
