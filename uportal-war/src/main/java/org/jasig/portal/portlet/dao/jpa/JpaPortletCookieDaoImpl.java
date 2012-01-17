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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
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
@Repository("portletCookieDao")
@Qualifier("persistence")
public class JpaPortletCookieDaoImpl extends BaseJpaDao implements IPortletCookieDao {
    private static final String FIND_COOKIE_BY_VALUE_CACHE_REGION = PortalCookieImpl.class.getName() + ".query.FIND_COOKIE_BY_VALUE";
    
	private final SecureRandom secureRandom = new SecureRandom();
	private final Log log = LogFactory.getLog(this.getClass());

	private CriteriaQuery<PortalCookieImpl> findPortalCookieByValueQuery;
	private CriteriaQuery<PortalCookieImpl> findExpiredPortalCookieQuery;
    private ParameterExpression<String> valueParameter;
    private ParameterExpression<Date> nowParameter;
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.valueParameter = cb.parameter(String.class, "value");
        this.nowParameter = cb.parameter(Date.class, "now");
        
        this.findPortalCookieByValueQuery = this.buildFindPortalCookieByValueQuery(cb);
        this.findExpiredPortalCookieQuery = this.buildFindExpiredPortalCookieQuery(cb);
    }
    
    protected CriteriaQuery<PortalCookieImpl> buildFindPortalCookieByValueQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<PortalCookieImpl> criteriaQuery = cb.createQuery(PortalCookieImpl.class);
        final Root<PortalCookieImpl> typeRoot = criteriaQuery.from(PortalCookieImpl.class);
        criteriaQuery.select(typeRoot);
        typeRoot.fetch(PortalCookieImpl_.portletCookies, JoinType.LEFT);
        criteriaQuery.where(
            cb.equal(typeRoot.get(PortalCookieImpl_.value), this.valueParameter)
        );
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<PortalCookieImpl> buildFindExpiredPortalCookieQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<PortalCookieImpl> criteriaQuery = cb.createQuery(PortalCookieImpl.class);
        final Root<PortalCookieImpl> typeRoot = criteriaQuery.from(PortalCookieImpl.class);
        criteriaQuery.select(typeRoot);
        typeRoot.fetch(PortalCookieImpl_.portletCookies, JoinType.LEFT);
        criteriaQuery.where(
            cb.lessThanOrEqualTo(typeRoot.get(PortalCookieImpl_.expires), this.nowParameter)
        );
        
        return criteriaQuery;
    }
	
	/**
	 * Generates a 40 character unique value.
	 * @return
	 */
	private String generateNewCookieId() {
		final byte[] keyBytes = new byte[30];
		this.secureRandom.nextBytes(keyBytes);
		return Base64.encodeBase64URLSafeString(keyBytes);
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.cookies.IPortalCookieDao#createPortalCookie(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	@Transactional
	public IPortalCookie createPortalCookie(int maxAge) {
	    //Make sure our unique ID doesn't already exist by really small random chance
		String uniqueId;
		do {
		    uniqueId = generateNewCookieId();
		} while (this.getPortalCookie(uniqueId) != null);
		
		//Calculate the expiration date for the cookie
		final Date expiration = DateUtils.addSeconds(new Date(), maxAge);
		
		//Create and persist
		final IPortalCookie portalCookie = new PortalCookieImpl(uniqueId, expiration);
		this.entityManager.persist(portalCookie);
		
		return portalCookie;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.cookies.IPortalCookieDao#getPortalCookie(java.lang.String)
	 */
	@Override
	public IPortalCookie getPortalCookie(String portalCookieValue) {
	    final TypedQuery<PortalCookieImpl> query = this.createQuery(this.findPortalCookieByValueQuery, FIND_COOKIE_BY_VALUE_CACHE_REGION);
	    
		query.setParameter(this.valueParameter, portalCookieValue);
        
		final List<PortalCookieImpl> results = query.getResultList();
		return DataAccessUtils.uniqueResult(results);
	}

    @Override
    @Transactional
    public IPortalCookie updatePortalCookieExpiration(IPortalCookie portalCookie, int maxAge) {
        
        //Calculate expiration date and update the portal cookie
        Date expiration = DateUtils.addSeconds(new Date(), maxAge);
        portalCookie.setExpires(expiration);
        
        this.entityManager.persist(portalCookie);
        
        return portalCookie;
    }
    
	@Override
	@Transactional
	public void purgeExpiredCookies() {
		final Date now = new Date();

		log.debug("begin portal cookie expiration");
		//TODO Figure out a JPQL DELETE statement to do the purging instead of having to load every expird portal cookie from the DB first
//		final Query deletePortalCookies = this.entityManager.createNamedQuery(PortalCookieImpl.UP_PORTAL_COOKIES__DELETE_EXPIRED);

		final TypedQuery<PortalCookieImpl> deletePortalCookies = this.entityManager.createQuery(this.findExpiredPortalCookieQuery);
		deletePortalCookies.setParameter(this.nowParameter, now);
		
		final List<PortalCookieImpl> resultList = deletePortalCookies.getResultList();
		for (final PortalCookieImpl portalCookie : resultList) {
		    this.entityManager.remove(portalCookie);
		}
		if(log.isDebugEnabled()) {
			log.debug("finished purging " + resultList.size() + " portal cookies, begin portlet cookie expiration");
		}
        final Query deletePortletCookies = this.entityManager.createNamedQuery(PortletCookieImpl.UP_PORTLET_COOKIES__DELETE_EXPIRED);
        deletePortletCookies.setParameter("expirationDate", now);
        
        final int portletCookiesDeleted = deletePortletCookies.executeUpdate();
        if(log.isDebugEnabled()) {
        	log.debug("finished purging " + portletCookiesDeleted + " portlet cookies");
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.dao.IPortletCookieDao#updatePortletCookie(org.jasig.portal.portlet.om.IPortalCookie, javax.servlet.http.Cookie)
	 */
	@Override
	@Transactional
	public IPortalCookie addOrUpdatePortletCookie(IPortalCookie portalCookie, Cookie cookie) {
	    final Set<IPortletCookie> portletCookies = portalCookie.getPortletCookies();
	    
	    boolean found = false;
	    final String name = cookie.getName();
        for (final Iterator<IPortletCookie> portletCookieItr = portletCookies.iterator(); portletCookieItr.hasNext(); ) {
	        final IPortletCookie portletCookie = portletCookieItr.next();
	        if (name.equals(portletCookie.getName())) {
	            //Delete cookies with a maxAge of 0
	            if (cookie.getMaxAge() == 0) {
	                portletCookieItr.remove();
	                this.entityManager.remove(portletCookie);
	            }
	            else {
	                portletCookie.updateFromCookie(cookie);
	            }
	            
	            found = true;
	            break;
	        }
        }
        
        if (!found) {
            IPortletCookie newPortletCookie = new PortletCookieImpl(cookie);
            portletCookies.add(newPortletCookie);
        }
        
		this.entityManager.persist(portalCookie);
		
		return portalCookie;
	}
	
	
}
