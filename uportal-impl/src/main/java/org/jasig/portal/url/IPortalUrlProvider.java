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

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Provides source for portal and portlet URL objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalUrlProvider {
    
    /**
     * Get the portal request information for the specified request.
     * 
     * @param request The current portal request
     * @return Information about the current request
     */
    public IPortalRequestInfo getPortalRequestInfo(HttpServletRequest request) throws InvalidPortalRequestException;
    
//  Not implemented until all folders have fnames?
//  public IBasePortalUrl getFolderUrlByFName(String folderFName);

    /**
     * Gets the default portal URL, this is equivalent to the first URL rendered by the portal when a user logs in. 
     * 
     * @param request The current portal request
     * @return Default {@link IBasePortalUrl}
     */
    public IBasePortalUrl getDefaultUrl(HttpServletRequest request);
    
    /**
     * @param request The current portal request
     * @param folderNodeId ID of the folder in the user's layout that should be targeted by the URL.
     * @return Folder targeted {@link IBasePortalUrl}
     * @throws IllegalArgumentException If the specified ID doesn't exist for a folder in the users layout.
     */
    public IBasePortalUrl getFolderUrlByNodeId(HttpServletRequest request, String folderNodeId);
    
//    public IPortalChannelUrl getChannelUrlByNodeId(HttpServletRequest request, String channelNodeId);
//    public IPortalChannelUrl getChannelUrlByFName(HttpServletRequest request, String channelFName);
    
    /**
     * @param request The current portal request
     * @param portletWindowId ID of the portlet window that should be targeted by the URL.
     * @return Portlet targeted {@link IBasePortalUrl}
     * @throws IllegalArgumentException If the specified ID doesn't exist for a channel in the users layout.
     */
    public IPortalPortletUrl getPortletUrl(HttpServletRequest request, IPortletWindowId portletWindowId);
    
    /**
     * @param request The current portal request
     * @param portletNodeId ID of the channel in the user's layout that should be targeted by the URL.
     * @return Portlet targeted {@link IBasePortalUrl}
     * @throws IllegalArgumentException If the specified ID doesn't exist for a channel in the users layout.
     */
    public IPortalPortletUrl getPortletUrlByNodeId(HttpServletRequest request, String portletNodeId);
    
    /**
     * @param request The current portal request
     * @param portletFName The fname of the portlet that should be targeted by the URL
     * @return Portlet targeted {@link IBasePortalUrl}
     * @throws IllegalArgumentException If the specified fname does not exist.
     */
    public IPortalPortletUrl getPortletUrlByFName(HttpServletRequest request, String portletFName);
}
