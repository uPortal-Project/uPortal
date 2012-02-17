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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.portlet.dao.IPortletTypeDao;
import org.jasig.portal.portlet.om.IPortletType;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * JPA/Hibernate implementation of IChannelTypeDao.  This DAO handles 
 * channel types and is not yet integrated with the channel definition persistence
 * code.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
@Repository
public class JpaPortletTypeDao extends BaseJpaDao implements IPortletTypeDao {
    private CriteriaQuery<PortletTypeImpl> findAllTypesQuery;
    private CriteriaQuery<PortletTypeImpl> findTypeByNameQuery;
    private ParameterExpression<String> nameParameter;
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
    public void afterPropertiesSet() throws Exception {
        this.nameParameter = this.createParameterExpression(String.class, "name");
        
        this.findAllTypesQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletTypeImpl>>() {
            @Override
            public CriteriaQuery<PortletTypeImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletTypeImpl> criteriaQuery = cb.createQuery(PortletTypeImpl.class);
                criteriaQuery.from(PortletTypeImpl.class);
                return criteriaQuery;
            }
        });
        
        
        this.findTypeByNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletTypeImpl>>() {
            @Override
            public CriteriaQuery<PortletTypeImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletTypeImpl> criteriaQuery = cb.createQuery(PortletTypeImpl.class);
                final Root<PortletTypeImpl> typeRoot = criteriaQuery.from(PortletTypeImpl.class);
                criteriaQuery.select(typeRoot);
                criteriaQuery.where(
                    cb.equal(typeRoot.get(PortletTypeImpl_.name), nameParameter)
                );
                
                return criteriaQuery;
            }
        });
    }
    
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#deleteChannelType(org.jasig.portal.channel.IChannelType)
     */
    @Override
    @Transactional
	public void deletePortletType(IPortletType type) {
        Validate.notNull(type, "definition can not be null");
        
        final IPortletType persistentChanneltype;
        if (this.entityManager.contains(type)) {
            persistentChanneltype = type;
        }
        else {
            persistentChanneltype = this.entityManager.merge(type);
        }
    	this.entityManager.remove(persistentChanneltype);
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#createChannelType(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional
    public IPortletType createPortletType(String name, String cpdUri) {
        Validate.notEmpty(name, "name can not be null");
        Validate.notEmpty(cpdUri, "cpdUri can not be null");
        
        final PortletTypeImpl channelType = new PortletTypeImpl(name, cpdUri);
        
        this.entityManager.persist(channelType);
        
        return channelType;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelType(int)
     */
	@Override
    public IPortletType getPortletType(int id) {
		return this.entityManager.find(PortletTypeImpl.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelType(java.lang.String)
	 */
    @Override
	public IPortletType getPortletType(String name) {
        final TypedQuery<PortletTypeImpl> query = this.createCachedQuery(this.findTypeByNameQuery);
        query.setParameter(this.nameParameter, name);
        
        final List<PortletTypeImpl> channelTypes = query.getResultList();
        return DataAccessUtils.uniqueResult(channelTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#getChannelTypes()
	 */
    @Override
	public List<IPortletType> getPortletTypes() {
        final TypedQuery<PortletTypeImpl> query = this.createCachedQuery(this.findAllTypesQuery);
        final List<PortletTypeImpl> portletTypes = query.getResultList();
		return new ArrayList<IPortletType>(portletTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.channel.dao.IChannelTypeDao#saveChannelType(org.jasig.portal.channel.IChannelType)
	 */
	@Override
    @Transactional
	public IPortletType updatePortletType(IPortletType type) {
        Validate.notNull(type, "type can not be null");
        
        this.entityManager.persist(type);
        
        return type;
	}

}
