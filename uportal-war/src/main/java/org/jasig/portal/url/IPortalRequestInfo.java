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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jasig.portal.portlet.om.IPortletWindowId;


/**
 * Provides information about the portal request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalRequestInfo {
    public static final String URL_TYPE_HEADER = UrlType.class.getName();
    
    /**
     * @return The state rendered by the URL
     */
    public UrlState getUrlState();
    
    /**
     * @return Type of url this request is for
     */
    public UrlType getUrlType();
    
    /**
     * @return Parameters targeting the portal itself
     */
    public Map<String, List<String>> getPortalParameters();
    
    /**
     * @return The layout node being targeted by the request. If the request isn't targeting a particular layout node null is returned.
     */
    public String getTargetedLayoutNodeId();
    
    /**
     * @return Parameters targeting the layout management system
     */
    public Map<String, List<String>> getLayoutParameters();
    
    /**
     * @return Information for a request targeting a portlet. If the request doesn't target a portlet null is returned.
     */
    public IPortletRequestInfo getPortletRequestInfo();
    
    /**
     * @return The canonical URL for this request information. The URL will be valid for use in markup or as a redirect
     * and will be absolute (starting with a / or a protocol)
     */
    public String getCanonicalUrl();
    
    /**
     * 
     * @return a never null, but possibly empty, {@link List} of additional {@link IPortletRequestInfo}s embedded in this portalrequestinfo
     */
    public Collection<IPortletRequestInfo> getAdditionalPortletRequestInfos();
    /**
     * 
     * @param portletWindowId
     * @return additional {@link IPortletRequestInfo} embbeded in this portalrequestinfo with the specified window id
     */
    public IPortletRequestInfo getAdditionalPortletRequestInfo(IPortletWindowId portletWindowId);
}
