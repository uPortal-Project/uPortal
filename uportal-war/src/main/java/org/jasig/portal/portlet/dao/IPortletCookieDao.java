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
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.jasig.portal.portlet.om.IPortletEntityId;

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
	 * @param cookie
	 * @return
	 */
	public IPortalCookie storePortletCookie(IPortalCookie portalCookie, IPortletEntityId portletEntityId, Cookie cookie);
	/**
	 * Update the {@link IPortletCookie} to the specified {@link IPortalCookie} for
	 * the {@link IPortletEntityId}.
	 * 
	 * @param portalCookie
	 * @param portletEntityId
	 * @param cookie
	 * @return the updated {@link IPortalCookie}
	 */
	public IPortalCookie updatePortletCookie(IPortalCookie portalCookie, IPortletEntityId portletEntityId, Cookie cookie);
	
	/**
	 * Remove the {@link IPortletCookie} from the specified {@link IPortalCookie} for
	 * the {@link IPortletEntityId}.
	 * 
	 * @param portalCookie
	 * @param portletEntityId
	 * @param cookie
	 * @return the updated {@link IPortalCookie}
	 */
	public IPortalCookie deletePortletCookie(IPortalCookie portalCookie, IPortletEntityId portletEntityId, Cookie cookie);
	
	/**
	 * 
	 * @param portletEntityId
	 * @return a never null, but possibly empty, {@link Set} of {@link IPortletCookie}s stored for the {@link IPortletEntityId}
	 */
	public List<IPortletCookie> getPortletCookiesForEntity(IPortletEntityId portletEntityId);
	
	/**
	 * 
	 * @param portletEntityId
	 * @return true if {@link IPortletCookie}s are stored for the specified {@link IPortletEntityId}.
	 */
	public boolean entityHasStoredPortletCookies(IPortletEntityId portletEntityId);
}
