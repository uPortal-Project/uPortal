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
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * JPA implementation of {@link IPortletCookieDao}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Repository("portletCookieDao")
@Qualifier("persistence")
public class JpaPortletCookieDaoImpl extends BasePortalJpaDao implements IPortletCookieDao {
	private final SecureRandom secureRandom = new SecureRandom();

	private String deletePortalCookieQueryString;
	private String deletePortletCookieQueryString;
	private CriteriaQuery<PortletCookieImpl> findExpiredByParentPortletCookiesQuery;
    private ParameterExpression<Date> nowParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.nowParameter = this.createParameterExpression(Date.class, "now");
        
        this.deletePortalCookieQueryString = 
                "DELETE FROM " + PortalCookieImpl.class.getName() + " e " +
                "WHERE e." + PortalCookieImpl_.expires.getName() + " <= :" + this.nowParameter.getName();
        
        this.deletePortletCookieQueryString = 
                "DELETE FROM " + PortletCookieImpl.class.getName() + " e " +
                "WHERE e." + PortletCookieImpl_.expires.getName() + " <= :" + this.nowParameter.getName();
        
        this.findExpiredByParentPortletCookiesQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletCookieImpl>>() {
            @Override
            public CriteriaQuery<PortletCookieImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletCookieImpl> criteriaQuery = cb.createQuery(PortletCookieImpl.class);
                final Root<PortletCookieImpl> typeRoot = criteriaQuery.from(PortletCookieImpl.class);
                criteriaQuery.select(typeRoot);
                criteriaQuery.where(cb.lessThanOrEqualTo(typeRoot.get(PortletCookieImpl_.portalCookie).get(PortalCookieImpl_.expires), nowParameter));
                
                return criteriaQuery;
            }
        });
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
	
	@Override
	@PortalTransactional
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
		this.getEntityManager().persist(portalCookie);
		
		return portalCookie;
	}

	@OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
	@Override
	public IPortalCookie getPortalCookie(String portalCookieValue) {
	    final NaturalIdQuery<PortalCookieImpl> query = this.createNaturalIdQuery(PortalCookieImpl.class);
	    query.using(PortalCookieImpl_.value, portalCookieValue);
	    return query.load();
	}

    @Override
    @PortalTransactional
    public IPortalCookie updatePortalCookieExpiration(IPortalCookie portalCookie, int maxAge) {
        
        //Calculate expiration date and update the portal cookie
        Date expiration = DateUtils.addSeconds(new Date(), maxAge);
        portalCookie.setExpires(expiration);
        
        this.getEntityManager().persist(portalCookie);
        
        return portalCookie;
    }
    
	@Override
	@PortalTransactional
	public void purgeExpiredCookies() {
		final Date now = new Date();
		
		logger.debug("begin portlet cookie expiration");
		
        final EntityManager entityManager = this.getEntityManager();
        final Query deletePortletCookieQuery = entityManager.createQuery(this.deletePortletCookieQueryString);
        deletePortletCookieQuery.setParameter(this.nowParameter.getName(), now);
        final int deletedPortletCookies = deletePortletCookieQuery.executeUpdate();
        
        if(logger.isDebugEnabled()) {
            logger.debug("finished purging " + deletedPortletCookies + " directly expired portlet cookies");
        }
        
        final TypedQuery<PortletCookieImpl> expiredByParentCookiesQuery = this.createQuery(findExpiredByParentPortletCookiesQuery);
        expiredByParentCookiesQuery.setParameter(this.nowParameter.getName(), now);
        final List<PortletCookieImpl> indirectlyExpiredCookies = expiredByParentCookiesQuery.getResultList();
        for (final PortletCookieImpl portletCookieImpl : indirectlyExpiredCookies) {
            entityManager.remove(portletCookieImpl);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("finished purging " + indirectlyExpiredCookies.size() + " indirectly expired portlet cookies");
        }

		logger.debug("begin portal cookie expiration");
		
		final Query deletePortalCookieQuery = entityManager.createQuery(this.deletePortalCookieQueryString);
        deletePortalCookieQuery.setParameter(this.nowParameter.getName(), now);
        final int deletedPortalCookies = deletePortalCookieQuery.executeUpdate();
        
		if(logger.isDebugEnabled()) {
			logger.debug("finished purging " + deletedPortalCookies + " portal cookies, begin portlet cookie expiration");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.dao.IPortletCookieDao#updatePortletCookie(org.jasig.portal.portlet.om.IPortalCookie, javax.servlet.http.Cookie)
	 */
	@Override
	@PortalTransactional
	public IPortalCookie addOrUpdatePortletCookie(IPortalCookie portalCookie, Cookie cookie) {
	    final Set<IPortletCookie> portletCookies = portalCookie.getPortletCookies();
	    
	    boolean found = false;
	    final String name = cookie.getName();
        final EntityManager entityManager = this.getEntityManager();
        for (final Iterator<IPortletCookie> portletCookieItr = portletCookies.iterator(); portletCookieItr.hasNext(); ) {
	        final IPortletCookie portletCookie = portletCookieItr.next();
	        if (name.equals(portletCookie.getName())) {
	            //Delete cookies with a maxAge of 0
	            if (cookie.getMaxAge() == 0) {
	                portletCookieItr.remove();
	                entityManager.remove(portletCookie);
	            }
	            else {
	                portletCookie.updateFromCookie(cookie);
	            }
	            
	            found = true;
	            break;
	        }
        }
        
        if (!found) {
            IPortletCookie newPortletCookie = new PortletCookieImpl(portalCookie, cookie);
            portletCookies.add(newPortletCookie);
        }
        
		entityManager.persist(portalCookie);
		
		return portalCookie;
	}
	
	
}
