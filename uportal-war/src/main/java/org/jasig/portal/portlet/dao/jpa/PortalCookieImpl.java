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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;

/**
 * JPA annotated {@link IPortalCookie} annotation.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Entity
@Table(
		name = "UP_PORTAL_COOKIES"
	)
@SequenceGenerator(
        name="UP_PORTAL_COOKIES_GEN",
        sequenceName="UP_PORTAL_COOKIES_SEQ",
        allocationSize=100
    )
@TableGenerator(
        name="UP_PORTAL_COOKIES_GEN",
        pkColumnValue="UP_PORTAL_COOKIES",
        allocationSize=100
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class PortalCookieImpl implements IPortalCookie {
	@Id
    @GeneratedValue(generator = "UP_PORTAL_COOKIES_GEN")
    @Column(name = "PORTAL_COOKIE_ID")
    private final long internalPortalCookieId;
	
	@Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    @Column(name = "CREATED", nullable = false, updatable = false)
	private final Date created;
	@Column(name = "EXPIRES", nullable = false, updatable = true)
	private Date expires;
	@NaturalId
	@Column(name = "COOKIE_VALUE", length=100, nullable = false)
	private final String value;
	
	@OneToMany(targetEntity = PortletCookieImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "PORTAL_COOKIE_ID")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Set<IPortletCookie> portletCookies;
	
	/**
	 * For ORM internal use only
	 */
	@SuppressWarnings("unused")
	private PortalCookieImpl() {
		this.internalPortalCookieId = -1;
		this.entityVersion = -1;
		this.created = new Date();
		this.expires = null;
		this.value = null;
		this.portletCookies = new HashSet<IPortletCookie>();
	}
	
	/**
	 * 
	 * @param value
	 */
	PortalCookieImpl(String value, Date expiration) {
		this.internalPortalCookieId = -1;
		this.entityVersion = -1;
		
		this.value = value;
		
		this.created = new Date();
		this.expires = expiration;
		this.portletCookies = new HashSet<IPortletCookie>();
	}

	/**
	 * @return the internalPortalCookieId
	 */
	public long getInternalPortalCookieId() {
		return internalPortalCookieId;
	}

	/**
	 * @return the created
	 */
	@Override
    public Date getCreated() {
		return created;
	}

	/**
	 * @return the expires
	 */
	@Override
    public Date getExpires() {
		return expires;
	}

	/**
	 * @return the value
	 */
	@Override
    public String getValue() {
		return value;
	}

	/**
	 * @return the portletCookies
	 */
	@Override
    public Set<IPortletCookie> getPortletCookies() {
		return portletCookies;
	}

	/**
	 * @param expires the expires to set
	 */
	@Override
    public void setExpires(Date expires) {
		this.expires = expires;
	}

    @Override
    public String toString() {
        return "PortalCookieImpl [internalPortalCookieId=" + this.internalPortalCookieId + ", entityVersion="
                + this.entityVersion + ", created=" + this.created + ", expires=" + this.expires + ", value="
                + this.value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
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
        PortalCookieImpl other = (PortalCookieImpl) obj;
        if (this.value == null) {
            if (other.value != null)
                return false;
        }
        else if (!this.value.equals(other.value))
            return false;
        return true;
    }
}
