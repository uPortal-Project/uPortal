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

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.portlet.om.IPortletCookie;

/**
 * {@link IPortletCookie} implementation for {@link IPortletCookie}s that
 * are only stored in the {@link HttpSession} (e.g. those with maxAge = -1).
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
class SessionOnlyPortletCookieImpl implements IPortletCookie, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7216859047141530039L;
	
	private final String name;
	private String value;
	private String comment;
	private String domain;
	private String path;
	private int version = 0;
	private boolean secure;
	private Date expires;
	
	/**
	 * 
	 */
	SessionOnlyPortletCookieImpl(String name) {
		this.name = name;
	}
	
	SessionOnlyPortletCookieImpl(Cookie cookie) {
		this.name = cookie.getName();
		this.value = cookie.getValue();
		this.comment = cookie.getComment();
		this.domain = cookie.getDomain();
		this.path = cookie.getPath();
		this.version = cookie.getVersion();
		this.secure = cookie.getSecure();
		
		setMaxAge(cookie.getMaxAge());
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the maxAge
	 */
	public int getMaxAge() {
		if (this.expires == null) {
		    return -1;
		}
		else {
		   return (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.expires.getTime());
		}
	}
	/**
	 * @param maxAge the maxAge to set
	 */
	public void setMaxAge(int maxAge) {
		if(maxAge < 0) {
			this.expires = null;
		} else {
			this.expires = DateUtils.addSeconds(new Date(), maxAge);
		}
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	/**
	 * @return the secure
	 */
	public boolean isSecure() {
		return secure;
	}
	/**
	 * @param secure the secure to set
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	/**
	 * @return the expires
	 */
	public Date getExpires() {
		return expires;
	}
	/**
	 * @param expires the expires to set
	 */
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.om.IPortletCookie#toCookie()
	 */
	@Override
	public Cookie toCookie() {
		Cookie cookie = new Cookie(name, value);
		cookie.setComment(comment);
		if(domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setMaxAge(getMaxAge());
		cookie.setPath(path);
		cookie.setSecure(secure);
		cookie.setVersion(version);
		return cookie;
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.om.IPortletCookie#updateFromCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public void updateFromCookie(Cookie cookie) {
		this.setComment(cookie.getComment());
        this.setDomain(cookie.getDomain());
        this.setExpires(DateUtils.addSeconds(new Date(), cookie.getMaxAge()));
        this.setPath(cookie.getPath());
        this.setSecure(cookie.getSecure());
        this.setValue(cookie.getValue());
	}
}
