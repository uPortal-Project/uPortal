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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.servlet.http.Cookie;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.portlet.om.IPortletCookie;

/**
 * JPA annotated {@link IPortletCookie} implementation.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Entity
@Table(
		name = "UP_PORTLET_COOKIES"
	)
@GenericGenerator(
        name = "UP_PORTLET_COOKIE_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_PORTLET_COOKIE_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_PORTLET_COOKIE_HI")
        }
    )
class PortletCookieImpl implements IPortletCookie {

	@Id
    @GeneratedValue(generator = "UP_PORTLET_COOKIE_GEN")
    @Column(name = "PORTLET_COOKIE_ID")
    private final long internalPortletCookieId;
	
	@Column(name = "NAME", nullable = false, updatable = false)
	private final String name;
	
	@Column(name = "COMMENT", nullable = true, updatable = true)
	private String comment;
	@Column(name = "DOMAIN", nullable = true, updatable = true)
	private String domain;
	@Column(name = "MAX_AGE", nullable = false, updatable = true)
	private int maxAge = -1;
	
	@Column(name = "PATH", nullable = true, updatable = true)
	private String path;
	@Column(name = "VALUE", nullable = false, updatable = true)
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
		this.name = null;
	}
	/**
	 * 
	 * @param name
	 */
	PortletCookieImpl(String name) {
		this.internalPortletCookieId = -1;
		this.name = name;
	}
	
	
	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @return the maxAge
	 */
	public int getMaxAge() {
		return maxAge;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @return the secure
	 */
	public boolean isSecure() {
		return secure;
	}
	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @param maxAge the maxAge to set
	 */
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	/**
	 * @param secure the secure to set
	 */
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
		cookie.setMaxAge(this.maxAge);
		cookie.setPath(this.path);
		cookie.setSecure(this.secure);
		cookie.setVersion(this.version);
		return cookie;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PortletCookieImpl [comment=");
		builder.append(comment);
		builder.append(", domain=");
		builder.append(domain);
		builder.append(", internalPortletCookieId=");
		builder.append(internalPortletCookieId);
		builder.append(", maxAge=");
		builder.append(maxAge);
		builder.append(", name=");
		builder.append(name);
		builder.append(", path=");
		builder.append(path);
		builder.append(", secure=");
		builder.append(secure);
		builder.append(", value=");
		builder.append(value);
		builder.append(", version=");
		builder.append(version);
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
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime
				* result
				+ (int) (internalPortletCookieId ^ (internalPortletCookieId >>> 32));
		result = prime * result + maxAge;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (secure ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + version;
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
		if (!(obj instanceof PortletCookieImpl)) {
			return false;
		}
		PortletCookieImpl other = (PortletCookieImpl) obj;
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!comment.equals(other.comment)) {
			return false;
		}
		if (domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!domain.equals(other.domain)) {
			return false;
		}
		if (internalPortletCookieId != other.internalPortletCookieId) {
			return false;
		}
		if (maxAge != other.maxAge) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (secure != other.secure) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}
	
}
