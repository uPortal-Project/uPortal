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

package org.jasig.portal.portlet.dao;

import javax.servlet.http.Cookie;

import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;

/**
 * Interface for creating/updating/deleting {@link IPortalCookie} and related {@link IPortletCookie}s.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public interface IPortletCookieDao {

	/**
	 * 
	 * @return a brand new {@link IPortalCookie}
	 */
	public IPortalCookie createPortalCookie(int maxAge);
	
	/**
	 * 
	 * @param portalCookie
	 * @return
	 */
	public IPortalCookie updatePortalCookieExpiration(IPortalCookie portalCookie, int maxAge);
	
	/**
	 * 
	 * @param value
	 * @return the matching {@link IPortalCookie} with the same value, or null if doesn't exist
	 */
	public IPortalCookie getPortalCookie(String value);

	/**
	 * Update the {@link IPortletCookie} in the specified {@link IPortalCookie}.
	 * 
	 * @param portalCookie
	 * @param cookie
	 * @return the updated {@link IPortalCookie}
	 */
	public IPortalCookie addOrUpdatePortletCookie(IPortalCookie portalCookie, Cookie cookie);
	
	
	/**
     * Intended for periodic execution, this method will delete all {@link IPortalCookie}s and {@link IPortletCookie}s
     * from persistence that have expired.
     */
    public void purgeExpiredCookies();
}
