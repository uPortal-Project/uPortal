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

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of {@link IPortletCookieDao}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Repository
@Qualifier("persistence")
public class JpaPortletCookieDaoImpl implements IPortletCookieDao {

	private final SecureRandom secureRandom = new SecureRandom();

	private int portalCookieLifetimeMinutes = 60;
	private EntityManager entityManager;
	
	/**
	 * @param entityManager the entityManager to set
	 */
	@PersistenceContext(unitName="uPortalPersistence")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	/**
	 * Default value is 60 minutes.
	 * 
	 * @return the portalCookieLifetimeMinutes
	 */
	public int getPortalCookieLifetimeMinutes() {
		return portalCookieLifetimeMinutes;
	}
	/**
	 * @param portalCookieLifetimeMinutes the portalCookieLifetimeMinutes to set
	 */
	public void setPortalCookieLifetimeMinutes(int portalCookieLifetimeMinutes) {
		this.portalCookieLifetimeMinutes = portalCookieLifetimeMinutes;
	}

	/**
	 * Generates a 40 character unique value.
	 * @return
	 */
	private String generateNewCookieId() {
		final byte[] keyBytes = new byte[30];
		this.secureRandom.nextBytes(keyBytes);
		return new String(Base64.encodeBase64(keyBytes));
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.cookies.IPortalCookieDao#createPortalCookie(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	@Transactional
	public IPortalCookie createPortalCookie() {
		final String uniqueId = generateNewCookieId();
		Date expiration = DateUtils.addMinutes(new Date(), this.portalCookieLifetimeMinutes);
		
		IPortalCookie portalCookie = new PortalCookieImpl(uniqueId, expiration);
		this.entityManager.persist(portalCookie);
		
		IPortalCookie result = getPortalCookie(uniqueId);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.cookies.IPortalCookieDao#deletePortalCookie(org.jasig.portal.portlet.om.IPortalCookie)
	 */
	@Override
	@Transactional
	public void deletePortalCookie(IPortalCookie portalCookie) {
		IPortalCookie persisted;
		if(this.entityManager.contains(portalCookie)) {
			persisted = portalCookie;
		} else {
			persisted = this.entityManager.merge(portalCookie);
		}

		this.entityManager.remove(persisted);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.cookies.IPortalCookieDao#getPortalCookie(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IPortalCookie getPortalCookie(String portalCookieValue) {
		final Query query = this.entityManager.createQuery("from PortalCookieImpl portalCookie " +
		        "where portalCookie.value = :portalCookieValue");
		query.setParameter("portalCookieValue", portalCookieValue);
		query.setMaxResults(1);
		List<IPortalCookie> results = query.getResultList();
		IPortalCookie cookie = DataAccessUtils.uniqueResult(results);
		return cookie;
	}

	/**
	 * Intended for periodic execution, this method will delete all {@link IPortalCookie}s
	 * from persistence that have expired.
	 */
	public void purgeExpiredCookies() {
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.dao.IPortletCookieDao#deletePortletCookie(org.jasig.portal.portlet.om.IPortalCookie, javax.servlet.http.Cookie)
	 */
	@Override
	public IPortalCookie deletePortletCookie(IPortalCookie portalCookie,
			Cookie cookie) {
		
		IPortalCookie persisted;
		if(this.entityManager.contains(portalCookie)) {
			persisted = portalCookie;
		} else {
			persisted = this.entityManager.merge(portalCookie);
		}

		IPortletCookie persistedPortletCookie = null;
		for(IPortletCookie p: persisted.getPortletCookies()) {
			if(p.getName().equals(cookie.getName())) {
				persistedPortletCookie = p;
				break;
			}
		}
		if(persistedPortletCookie != null) {
			this.entityManager.remove(persistedPortletCookie);
			persisted.getPortletCookies().remove(persistedPortletCookie);
			
			persisted = this.entityManager.merge(persisted);
		}
		return persisted;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.dao.IPortletCookieDao#storePortletCookie(org.jasig.portal.portlet.om.IPortalCookie, javax.servlet.http.Cookie)
	 */
	@Override
	@Transactional
	public IPortalCookie storePortletCookie(IPortalCookie portalCookie,
			Cookie cookie) {
		IPortletCookie portletCookie = cloneCookieAsPortletCookie(cookie);
		
		IPortalCookie persisted;
		if(this.entityManager.contains(portalCookie)) {
			persisted = portalCookie;
		} else {
			persisted = this.entityManager.merge(portalCookie);
		}
		
		this.entityManager.persist(portletCookie);
		persisted.getPortletCookies().add(portletCookie);
		
		this.entityManager.persist(persisted);
		return persisted;
	}

	
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.dao.IPortletCookieDao#updatePortalCookieExpiration(org.jasig.portal.portlet.om.IPortalCookie, java.util.Date)
	 */
	@Override
	@Transactional
	public IPortalCookie updatePortalCookieExpiration(
			IPortalCookie portalCookie, Date expiration) {
		IPortalCookie persisted;
		if(this.entityManager.contains(portalCookie)) {
			persisted = portalCookie;
		} else {
			persisted = this.entityManager.merge(portalCookie);
		}
		
		persisted.setExpires(expiration);
		this.entityManager.persist(persisted);
		return persisted;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.dao.IPortletCookieDao#updatePortletCookie(org.jasig.portal.portlet.om.IPortalCookie, javax.servlet.http.Cookie)
	 */
	@Override
	@Transactional
	public IPortalCookie updatePortletCookie(IPortalCookie portalCookie,
			Cookie cookie) {
		
		IPortalCookie persisted;
		if(this.entityManager.contains(portalCookie)) {
			persisted = portalCookie;
		} else {
			persisted = this.entityManager.merge(portalCookie);
		}
		
		
		IPortletCookie persistedPortletCookie = null;
		for(IPortletCookie p: persisted.getPortletCookies()) {
			if(p.getName().equals(cookie.getName())) {
				persistedPortletCookie = p;
				break;
			}
		}
		
		if(persistedPortletCookie != null) {
			persistedPortletCookie = this.entityManager.merge(persistedPortletCookie);
			persisted.getPortletCookies().remove(persistedPortletCookie);
			
			IPortletCookie newPortletCookie = cloneCookieAsPortletCookie(cookie);
			persisted.getPortletCookies().add(newPortletCookie);
			
			this.entityManager.persist(portalCookie);
			this.entityManager.remove(persistedPortletCookie);
		}
		
		return persisted;
	}

	/**
	 * Helper method to convert a {@link Cookie} into a {@link IPortletCookie}.
	 * 
	 * @param cookie
	 * @return
	 */
	protected IPortletCookie cloneCookieAsPortletCookie(Cookie cookie) {
		PortletCookieImpl portletCookie = new PortletCookieImpl(cookie.getName());
		portletCookie.setComment(cookie.getComment());
		portletCookie.setDomain(cookie.getDomain());
		portletCookie.setMaxAge(cookie.getMaxAge());
		portletCookie.setPath(cookie.getPath());
		portletCookie.setSecure(cookie.getSecure());
		portletCookie.setValue(cookie.getValue());
		return portletCookie;
	}
}
