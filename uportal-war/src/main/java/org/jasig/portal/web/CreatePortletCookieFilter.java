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
package org.jasig.portal.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter to trigger {@link IPortalCookie} creation.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class CreatePortletCookieFilter extends OncePerRequestFilter {

	private IPortletCookieDao portletCookieDao;
	/**
	 * @param portletCookieDao the portletCookieDao to set
	 */
	@Autowired
	public void setPortletCookieDao(IPortletCookieDao portletCookieDao) {
		this.portletCookieDao = portletCookieDao;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		boolean hasCookie = false;
		Cookie [] cookies = request.getCookies();
		if (cookies != null) {  // getCookies() returns null if there aren't any
	        for(Cookie cookie: cookies) {
	            if(IPortalCookie.PORTAL_COOKIE_NAME.equals(cookie.getName())) {
	                hasCookie = true;
	                break;
	            }
	        }
		}
		
		if(!hasCookie) {
			IPortalCookie portalCookie = this.portletCookieDao.createPortalCookie();
		
			// TODO construct proper mechanism for setting PORTLET_COOKIE_TOKEN expires time
    	
			response.addCookie(portalCookie.toMasterCookie());
		}
		
		filterChain.doFilter(request, response);
	}

	

}
