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
 * Generates URLs for the current request based on the portal or portlet URL objects and target ids.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUrlGenerator {
    /**
     * @param request The current request
     * @param basePortalUrl The URL object to create a URL string from
     * @param targetFolderId The targeted folder id from the user's layout
     * @return A URL to be used in markup or as a redirect. The URL will be absolute, starting with a / or with a protocol such as http://
     */
    public String generatePortalUrl(HttpServletRequest request, IBasePortalUrl basePortalUrl, String targetFolderId);
    
    /**
     * @param request The current request
     * @param portalPortletUrl The URL object to create a URL string from
     * @param portletWindowId The targeted portlet window ID
     * @return A URL to be used in markup or as a redirect. The URL will be absolute, starting with a / or with a protocol such as http://
     */
    public String generatePortletUrl(HttpServletRequest request, IPortalPortletUrl portalPortletUrl, IPortletWindowId portletWindowId);
}
