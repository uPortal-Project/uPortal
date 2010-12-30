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

import java.util.Date;

import javax.servlet.http.Cookie;

import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletEntityId;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public interface IPortletCookieDao {

	/**
	 * 
	 * @return a brand new {@link IPortalCookie}
	 */
	public IPortalCookie createPortalCookie();
	
	/**
	 * 
	 * @param portalCookie
	 * @return
	 */
	public IPortalCookie updatePortalCookieExpiration(IPortalCookie portalCookie, Date expiration);
	
	/**
	 * 
	 * @param value
	 * @return the matching {@link IPortalCookie} with the same value, or null if doesn't exist
	 */
	public IPortalCookie getPortalCookie(String value);
	
	/**
	 * Delete the specified {@link IPortalCookie}.
	 * 
	 * @param portalCookie
	 */
	public void deletePortalCookie(IPortalCookie portalCookie);
	
	/**
	 * Add a {@link IPortletCookie} to the specified {@link IPortalCookie} for
	 * the {@link IPortletEntityId}.
	 * 
	 * @param portalCookie
	 * @param portletEntityId
	 * @param portletCookie
	 * @return
	 */
	public IPortalCookie storePortletCookie(IPortalCookie portalCookie, IPortletEntityId portletEntityId, Cookie portletCookie);
	/**
	 * Update the {@link IPortletCookie} to the specified {@link IPortalCookie} for
	 * the {@link IPortletEntityId}.
	 * 
	 * @param portalCookie
	 * @param portletEntityId
	 * @param portletCookie
	 * @return the updated {@link IPortalCookie}
	 */
	public IPortalCookie updatePortletCookie(IPortalCookie portalCookie, IPortletEntityId portletEntityId, Cookie portletCookie);
	
	/**
	 * Remove the {@link IPortletCookie} from the specified {@link IPortalCookie} for
	 * the {@link IPortletEntityId}.
	 * 
	 * @param portalCookie
	 * @param portletEntityId
	 * @param portletCookie
	 * @return the updated {@link IPortalCookie}
	 */
	public IPortalCookie deletePortletCookie(IPortalCookie portalCookie, IPortletEntityId portletEntityId, Cookie portletCookie);
	
}
