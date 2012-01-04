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

package org.jasig.portal.portlet.container.services;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Service interface for implementing Cookie support for Portlets.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletCookieService {
    public static final String DEFAULT_PORTAL_COOKIE_NAME = "org.jasig.portal.PORTLET_COOKIE";
    public static final String DEFAULT_PORTAL_COOKIE_COMMENT = "uPortal Portlet Master Cookie";
    
    /**
     * Check if the portal cookie already exists, if it does update its feilds, if not
     * create it.
     * Should be called for every request via a Servlet Filter.
     */
    public void updatePortalCookie(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Get all cookies that are visible to the portlet
     */
    public Cookie[] getAllPortletCookies(HttpServletRequest request, IPortletWindowId portletWindowId);
    
    /**
     * Adds the specified cookie for the specified portlet.
     */
    public void addCookie(HttpServletRequest request, IPortletWindowId portletWindowId, Cookie cookie);
    
    /**
     * Purges expired cookies
     */
    public boolean purgeExpiredCookies();
}
