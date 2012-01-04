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

package org.jasig.portal.portlet.dao.jpa;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.servlet.http.Cookie;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;

/**
 * JPA annotated {@link IPortletCookie} implementation.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Entity
@Table(
		name = "UP_PORTLET_COOKIES",
        uniqueConstraints = @UniqueConstraint( columnNames = {"PORTAL_COOKIE_ID", "COOKIE_NAME"} )
	)
@SequenceGenerator(
        name="UP_PORTLET_COOKIES_GEN",
        sequenceName="UP_PORTLET_COOKIES_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_PORTLET_COOKIES_GEN",
        pkColumnValue="UP_PORTLET_COOKIES",
        allocationSize=5
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries(
        { 
            @NamedQuery(
                    name = PortletCookieImpl.UP_PORTLET_COOKIES__DELETE_EXPIRED, 
                    query = "delete from PortletCookieImpl c where c.expires <= :expirationDate"
            ) 
        }
    )
class PortletCookieImpl implements IPortletCookie {
    public static final String UP_PORTLET_COOKIES__DELETE_EXPIRED = "UP_PORTLET_COOKIES__DELETE_EXPIRED";

	@Id
    @GeneratedValue(generator = "UP_PORTLET_COOKIES_GEN")
    @Column(name = "PORTLET_COOKIE_ID")
    private final long internalPortletCookieId;
	
	@Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
	
	@SuppressWarnings("unused")
	@ManyToOne(targetEntity = PortalCookieImpl.class)
	private final IPortalCookie portalCookie;
    
    @Column(name = "COOKIE_NAME", length=500, nullable = false, updatable = false)
	private final String name;
	
	@Column(name = "COOKIE_COMMENT", length=1000, nullable = true, updatable = true)
	private String comment;
	@Column(name = "COOKIE_DOMAIN", length=500, nullable = true, updatable = true)
	private String domain;
	@Column(name = "EXPIRES", nullable = false, updatable = true)
    private Date expires;
	
	@Column(name = "COOKIE_PATH", length=1000, nullable = true, updatable = true)
	private String path;
	@Column(name = "COOKIE_VALUE", length=1000, nullable = false, updatable = true)
	private String value = "";
	@Column(name = "VERSION", nullable = false, updatable = true)
	private int version = 0;
	@Column(name = "SECURE", nullable = false, updatable = true)
	private boolean secure = false;
	
	/**
	 * For ORM internal use only
	 */
	@SuppressWarnings("unused")
	private PortletCookieImpl() {
		this.internalPortletCookieId = -1;
		this.entityVersion = -1;
		this.name = null;
		this.portalCookie = null;
	}
	/**
	 * 
	 * @param name
	 */
	PortletCookieImpl(IPortalCookie portalCookie, Cookie cookie) {
		this.internalPortletCookieId = -1;
		this.entityVersion = -1;
		this.name = cookie.getName();
		this.portalCookie = portalCookie;
		this.updateFromCookie(cookie);
	}
	
	/*
	 * (non-Javadoc)
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
    /**
	 * @return the comment
	 */
	@Override
    public String getComment() {
		return comment;
	}
	/**
	 * @return the domain
	 */
	@Override
    public String getDomain() {
		return domain;
	}
	/**
	 * @return
	 */
	@Override
	public Date getExpires() {
        return this.expires;
    }
    /**
	 * @return the name
	 */
	@Override
    public String getName() {
		return name;
	}
	/**
	 * @return the path
	 */
	@Override
    public String getPath() {
		return path;
	}
	/**
	 * @return the value
	 */
	@Override
    public String getValue() {
		return value;
	}
	/**
	 * @return the version
	 */
	@Override
    public int getVersion() {
		return version;
	}
	/**
	 * @return the secure
	 */
	@Override
    public boolean isSecure() {
		return secure;
	}
	/**
	 * @param comment the comment to set
	 */
	@Override
    public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @param domain the domain to set
	 */
	@Override
    public void setDomain(String domain) {
		this.domain = domain;
	}
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.om.IPortletCookie#setExpires(java.util.Date)
     */
	@Override
    public void setExpires(Date expires) {
        this.expires = expires;
    }
	/**
	 * @param path the path to set
	 */
	@Override
    public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @param value the value to set
	 */
	@Override
    public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @param version the version to set
	 */
	@Override
    public void setVersion(int version) {
		this.version = version;
	}
	/**
	 * @param secure the secure to set
	 */
	@Override
    public void setSecure(boolean secure) {
		this.secure = secure;
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.om.IPortletCookie#toCookie()
	 */
	@Override
	public Cookie toCookie() {
		Cookie cookie = new Cookie(this.name, this.value);
		cookie.setComment(this.comment);
		if(this.domain != null) {
			// FYI: setDomain requires non-null argument (requirement not documented)
			cookie.setDomain(this.domain);
		}
		
		final int maxAge;
		if (this.expires == null) {
		    maxAge = -1;
		}
		else {
		    maxAge = (int)TimeUnit.MILLISECONDS.toSeconds(this.expires.getTime() - System.currentTimeMillis());
		}
		cookie.setMaxAge(maxAge);
		cookie.setPath(this.path);
		cookie.setSecure(this.secure);
		cookie.setVersion(this.version);
		return cookie;
	}
    @Override
    public String toString() {
        return "PortletCookieImpl [internalPortletCookieId=" + this.internalPortletCookieId + ", entityVersion="
                + this.entityVersion + ", name=" + this.name + ", comment=" + this.comment + ", domain=" + this.domain
                + ", expires=" + this.expires + ", path=" + this.path + ", value=" + this.value + ", version="
                + this.version + ", secure=" + this.secure + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PortletCookieImpl other = (PortletCookieImpl) obj;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        return true;
    }
}
