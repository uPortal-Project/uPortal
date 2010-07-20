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

package org.jasig.portal.portlet.delegation;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortletPortalUrl;

/**
 * Provides some utility methods for dealing with delegate rendering of portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDelegationManager {
    public static final String DELEGATE_ACTION_REDIRECT_TOKEN = "DELEGATE_ACTION_REDIRECT";
    
    /**
     * Set the URL to use as parent data when generating delegate portlet URLs
     * 
     * @param parentPortletUrl The parent URL data
     */
    public void setParentPortletUrl(HttpServletRequest request, IPortletPortalUrl parentPortletUrl);
    
    /**
     * Get the URL data to use for the delegation parent window
     * 
     * @param parentPortletWindowId The ID of the portlet window to get the PortletUrl for
     * @return The PortletUrl for the parent window, null if no base URL data is provided
     */
    public IPortletPortalUrl getParentPortletUrl(HttpServletRequest request, IPortletWindowId parentPortletWindowId);
    
    /**
     * Pass the url generated after a delegates processAction 
     * 
     * @param request The portlet adaptor request for the delegate
     * @param portletUrl The URL to pass to the delegation dispatcher
     */
    public void setDelegatePortletActionRedirectUrl(HttpServletRequest request, IPortletPortalUrl portletUrl);
    
    /**
     * @param request The portlet adapter request for the delgate parent
     * @return The URL generated after the delegates processAction completes
     */
    public IPortletPortalUrl getDelegatePortletActionRedirectUrl(HttpServletRequest request);
    
    /**
     * @param request The portlet adapter request for the delgate parent
     * @return The URL generated after the delegates processAction completes
     */
    public IPortletPortalUrl getDelegatePortletActionRedirectUrl(PortletRequest request);
}
