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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@GenericGenerator(
        name = "UP_PORTAL_COOKIE_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_PORTAL_COOKIE_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_PORTAL_COOKIE_HI")
        }
    )
class PortalCookieImpl implements IPortalCookie {

	@Id
    @GeneratedValue(generator = "UP_PORTAL_COOKIE_GEN")
    @Column(name = "PORTAL_COOKIE_ID")
    private final long internalPortalCookieId;
	
	@Column(name = "CREATED", nullable = false, updatable = false)
	private final Date created;
	@Column(name = "EXPIRES", nullable = false, updatable = true)
	private Date expires;
	@Column(name = "VALUE", nullable = false, updatable = false, unique = true)
	private final String value;
	
	@OneToMany(targetEntity = PortletCookieImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private Set<IPortletCookie> portletCookies;
	
	/**
	 * For ORM internal use only
	 */
	@SuppressWarnings("unused")
	private PortalCookieImpl() {
		this.internalPortalCookieId = -1;
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
	public Date getCreated() {
		return created;
	}

	/**
	 * @return the expires
	 */
	public Date getExpires() {
		return expires;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the portletCookies
	 */
	public Set<IPortletCookie> getPortletCookies() {
		return portletCookies;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PortalCookieImpl [created=");
		builder.append(created);
		builder.append(", expires=");
		builder.append(expires);
		builder.append(", internalPortalCookieId=");
		builder.append(internalPortalCookieId);
		builder.append(", portletCookies=");
		builder.append(portletCookies);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((expires == null) ? 0 : expires.hashCode());
		result = prime
				* result
				+ (int) (internalPortalCookieId ^ (internalPortalCookieId >>> 32));
		result = prime * result
				+ ((portletCookies == null) ? 0 : portletCookies.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PortalCookieImpl)) {
			return false;
		}
		PortalCookieImpl other = (PortalCookieImpl) obj;
		if (created == null) {
			if (other.created != null) {
				return false;
			}
		} else if (!created.equals(other.created)) {
			return false;
		}
		if (expires == null) {
			if (other.expires != null) {
				return false;
			}
		} else if (!expires.equals(other.expires)) {
			return false;
		}
		if (internalPortalCookieId != other.internalPortalCookieId) {
			return false;
		}
		if (portletCookies == null) {
			if (other.portletCookies != null) {
				return false;
			}
		} else if (!portletCookies.equals(other.portletCookies)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
