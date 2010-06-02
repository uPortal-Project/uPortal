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

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortletPortalUrl;

/**
 * Encapsulates the logic for writing to and reading from URLs for portlets. This should hide the actual paramter
 * namespacing, encoding and related logic for both URL generation and parsing.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletUrlSyntaxProvider {
    /**
     * Generates a full portlet URL for the current request, passed portlet window & portlet URL data object. The
     * generated URL will be complete and ready for rendering.
     * 
     * @param request The current request
     * @param portletWindow The portlet window the parameters are for
     * @param portletUrl The Portlet URL data to be written.
     * @return A fully generated portlet URL ready to be rendered
     * @throws IllegalArgumentException if request, portletWindowId, or portletUrl are null
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletWindow portletWindow, PortalPortletURLProvider portletUrl);
    
    /**
     * Parses out PortletUrl data from the request.
     * 
     * @param request The request to parse parameters from
     * @return The PortletUrl data for the request, null if no portlet is targeted by this request
     * @throws IllegalArgumentException if request is null.
     */
    public IPortletPortalUrl parsePortletUrl(HttpServletRequest request);
    
}
