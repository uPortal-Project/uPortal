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

package org.jasig.portal.portlet.om;

import java.util.Date;
import java.util.Set;

/**
 * Interface describing the parent cookie used to relate all
 * Portlet Cookies to one HTTP Cookie.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public interface IPortalCookie {
	
	public static final String PORTAL_COOKIE_NAME = IPortalCookie.class.getPackage().getName() + ".PORTLET_COOKIE_TOKEN";
	/**
	 * 
	 * @return the unique value for this cookie
	 */
	String getValue();
	
	/**
	 * 
	 * @return the timestamp when this cookie was created
	 */
	Date getCreated();
	
	/**
	 * 
	 * @return the timestamp when this cookie is due to expire
	 */
	Date getExpires();
	
	/**
	 * Update the expiration timestamp.
	 *  
	 * @param expires
	 */
	void setExpires(Date expires);
	
	/**
	 * 
	 * @return the set of {@link IPortletCookie}s linked to this instance
	 */
	Set<IPortletCookie> getPortletCookies();

}
