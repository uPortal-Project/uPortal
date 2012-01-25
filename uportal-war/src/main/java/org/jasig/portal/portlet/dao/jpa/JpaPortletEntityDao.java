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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
@Qualifier("persistence")
public class JpaPortletEntityDao extends BaseJpaDao implements IPortletEntityDao {
    private CriteriaQuery<PortletEntityImpl> findEntitiesForDefinitionQuery;
    private CriteriaQuery<PortletEntityImpl> findEntitiesForUserIdQuery;
    private ParameterExpression<Integer> userIdParameter;
    private ParameterExpression<PortletDefinitionImpl> portletDefinitionParameter;

    private IPortletDefinitionDao portletDefinitionDao;
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    
    @Autowired
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.userIdParameter = this.createParameterExpression(Integer.class, "userId");
        this.portletDefinitionParameter = this.createParameterExpression(PortletDefinitionImpl.class, "portletDefinition");
        
        this.findEntitiesForDefinitionQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletEntityImpl>>() {
            @Override
            public CriteriaQuery<PortletEntityImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletEntityImpl> criteriaQuery = cb.createQuery(PortletEntityImpl.class);
                final Root<PortletEntityImpl> entityRoot = criteriaQuery.from(PortletEntityImpl.class);
                criteriaQuery.select(entityRoot);
                addFetches(entityRoot);
                criteriaQuery.where(
                    cb.equal(entityRoot.get(PortletEntityImpl_.portletDefinition), portletDefinitionParameter)
                );
                
                return criteriaQuery;
            }
        });

        
        this.findEntitiesForUserIdQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletEntityImpl>>() {
            @Override
            public CriteriaQuery<PortletEntityImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletEntityImpl> criteriaQuery = cb.createQuery(PortletEntityImpl.class);
                final Root<PortletEntityImpl> entityRoot = criteriaQuery.from(PortletEntityImpl.class);
                criteriaQuery.select(entityRoot);
                addFetches(entityRoot);
                criteriaQuery.where(
                    cb.equal(entityRoot.get(PortletEntityImpl_.userId), userIdParameter)
                );
                
                return criteriaQuery;
            }
        });
    }

    /**
     * Add all the fetches needed for completely loading the object graph
     */
    protected void addFetches(final Root<PortletEntityImpl> definitionRoot) {
        definitionRoot.fetch(PortletEntityImpl_.portletPreferences, JoinType.LEFT)
            .fetch(PortletPreferencesImpl_.portletPreferences, JoinType.LEFT)
            .fetch(PortletPreferenceImpl_.values, JoinType.LEFT);
        definitionRoot.fetch(PortletEntityImpl_.windowStates, JoinType.LEFT);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#createPortletEntity(org.jasig.portal.om.portlet.IPortletDefinitionId, java.lang.String, int)
     */
    @Override
    @Transactional
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String layoutNodeId, int userId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        Validate.notEmpty(layoutNodeId, "layoutNodeId can not be null");
        
        final IPortletDefinition portletDefinition = this.portletDefinitionDao.getPortletDefinition(portletDefinitionId);
        if (portletDefinition == null) {
            throw new DataRetrievalFailureException("No IPortletDefinition exists for IPortletDefinitionId='" + portletDefinitionId + "'");
        }
        
        IPortletEntity portletEntity = new PortletEntityImpl(portletDefinition, layoutNodeId, userId);
        
        this.entityManager.persist(portletEntity);

        return portletEntity;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#deletePortletEntity(org.jasig.portal.om.portlet.IPortletEntity)
     */
    @Override
    @Transactional
    public void deletePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        final IPortletEntity persistentPortletEntity;
        if (this.entityManager.contains(portletEntity)) {
            persistentPortletEntity = portletEntity;
        }
        else {
            persistentPortletEntity = this.entityManager.merge(portletEntity);
        }
        
        this.entityManager.remove(persistentPortletEntity);
    }

    @Override
    @Transactional(readOnly=true)
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        final long internalPortletEntityId = getNativePortletEntityId(portletEntityId);
        final PortletEntityImpl portletEntity = this.entityManager.find(PortletEntityImpl.class, internalPortletEntityId);
        return portletEntity;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean portletEntityExists(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        this.entityManager.clear();
        
        final long internalPortletEntityId = getNativePortletEntityId(portletEntityId);
        final PortletEntityImpl portletEntity = this.entityManager.find(PortletEntityImpl.class, internalPortletEntityId);
        return portletEntity != null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#getPortletEntity(java.lang.String, int)
     */
    @Override
    @Transactional(readOnly=true)
    public IPortletEntity getPortletEntity(String layoutNodeId, int userId) {
        Validate.notNull(layoutNodeId, "portletEntity can not be null");
        
        
		/* Since portal entities mostly are retrieved in batches (for each "channel" element in user's layout), it is
		 * faster to retrieve all portlet entities, so that persistence framework can place them in 2nd level cache, and
		 * iterate over them manually instead of retrieving single portlet entity one by one. */
        Set<IPortletEntity> entities = getPortletEntitiesForUser(userId);
        for (IPortletEntity entity : entities) {
            if (StringUtils.equals(entity.getLayoutNodeId(), layoutNodeId)) {
                return entity;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#getPortletEntities(org.jasig.portal.om.portlet.IPortletDefinitionId)
     */
    @Override
    @Transactional(readOnly=true)
    public Set<IPortletEntity> getPortletEntities(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletEntity can not be null");
        
        final IPortletDefinition portletDefinition = this.portletDefinitionDao.getPortletDefinition(portletDefinitionId);
        
        final TypedQuery<PortletEntityImpl> query = this.createCachedQuery(this.findEntitiesForDefinitionQuery);
        query.setParameter(this.portletDefinitionParameter, (PortletDefinitionImpl)portletDefinition);
        
        final List<PortletEntityImpl> portletEntities = query.getResultList();
        return new HashSet<IPortletEntity>(portletEntities);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#getPortletEntitiesForUser(int)
     */
    @Override
    @Transactional(readOnly=true)
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        final TypedQuery<PortletEntityImpl> query = this.createCachedQuery(this.findEntitiesForUserIdQuery);
        query.setParameter(this.userIdParameter, userId);
        
        final List<PortletEntityImpl> portletEntities = query.getResultList();
        return new HashSet<IPortletEntity>(portletEntities);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#updatePortletEntity(org.jasig.portal.om.portlet.IPortletEntity)
     */
    @Override
    @Transactional
    public void updatePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");

        this.entityManager.persist(portletEntity);
    }
    
    protected long getNativePortletEntityId(IPortletEntityId portletEntityId) {
        final long internalPortletEntityId = Long.parseLong(portletEntityId.getStringId());
        return internalPortletEntityId;
    }

}
