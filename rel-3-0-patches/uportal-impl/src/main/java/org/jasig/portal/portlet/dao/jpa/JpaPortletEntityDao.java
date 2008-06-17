/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JpaPortletEntityDao  implements IPortletEntityDao {
    private static final String FIND_PORTLET_ENT_BY_CHAN_SUB_AND_USER = 
        "from PortletEntityImpl portEnt " +
        "where portEnt.channelSubscribeId = :channelSubscribeId and portEnt.userId = :userId";

    private static final String FIND_PORTLET_ENTS_BY_PORTLET_DEF = 
        "from PortletEntityImpl portEnt " +
        "where portEnt.portletDefinition = :portletDefinition";
    
    private static final String FIND_PORTLET_ENTS_BY_USER_ID = 
        "from PortletEntityImpl portEnt " +
        "where portEnt.userId = :userId";

    private EntityManager entityManager;
    private IPortletDefinitionDao portletDefinitionDao;
    
    
    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    /**
     * @return the portletDefinitionDao
     */
    public IPortletDefinitionDao getPortletDefinitionDao() {
        return portletDefinitionDao;
    }
    /**
     * @param portletDefinitionDao the portletDefinitionDao to set
     */
    @Required
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#createPortletEntity(org.jasig.portal.om.portlet.IPortletDefinitionId, java.lang.String, int)
     */
    @Transactional
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        final IPortletDefinition portletDefinition = this.portletDefinitionDao.getPortletDefinition(portletDefinitionId);
        if (portletDefinition == null) {
            throw new DataRetrievalFailureException("No IPortletDefinition exists for IPortletDefinitionId='" + portletDefinitionId + "'");
        }
        
        IPortletEntity portletEntity = new PortletEntityImpl(portletDefinition, channelSubscribeId, userId);
        
        portletEntity = this.entityManager.merge(portletEntity);
        this.entityManager.persist(portletEntity);

        //Refresh the definition as it's internall collection of IPortletEntities may need to be updated
        this.entityManager.refresh(portletDefinition);
        
        return portletEntity;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#deletePortletEntity(org.jasig.portal.om.portlet.IPortletEntity)
     */
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

    @Transactional(readOnly = true)
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntity can not be null");
        
        final long internalPortletEntityId = Long.parseLong(portletEntityId.getStringId());
        final PortletEntityImpl portletEntity = this.entityManager.find(PortletEntityImpl.class, internalPortletEntityId);
        return portletEntity;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#getPortletEntity(java.lang.String, int)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public IPortletEntity getPortletEntity(String channelSubscribeId, int userId) {
        Validate.notNull(channelSubscribeId, "portletEntity can not be null");
        
        final Query query = this.entityManager.createQuery(FIND_PORTLET_ENT_BY_CHAN_SUB_AND_USER);
        query.setParameter("channelSubscribeId", channelSubscribeId);
        query.setParameter("userId", userId);
        query.setMaxResults(1);
        
        final List<IPortletEntity> portletEntities = query.getResultList();
        final IPortletEntity portletEntity = (IPortletEntity)DataAccessUtils.uniqueResult(portletEntities);
        return portletEntity;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#getPortletEntities(org.jasig.portal.om.portlet.IPortletDefinitionId)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Set<IPortletEntity> getPortletEntities(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletEntity can not be null");
        
        final IPortletDefinition portletDefinition = this.portletDefinitionDao.getPortletDefinition(portletDefinitionId);
        
        final Query query = this.entityManager.createQuery(FIND_PORTLET_ENTS_BY_PORTLET_DEF);
        query.setParameter("portletDefinition", portletDefinition);
        
        final List<IPortletEntity> portletEntities = query.getResultList();
        return new HashSet<IPortletEntity>(portletEntities);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#getPortletEntitiesForUser(int)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        final Query query = this.entityManager.createQuery(FIND_PORTLET_ENTS_BY_USER_ID);
        query.setParameter("userId", userId);
        
        final List<IPortletEntity> portletEntities = query.getResultList();
        return new HashSet<IPortletEntity>(portletEntities);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletEntityDao#updatePortletEntity(org.jasig.portal.om.portlet.IPortletEntity)
     */
    @Transactional
    public void updatePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        portletEntity = this.entityManager.merge(portletEntity);
        this.entityManager.persist(portletEntity);
    }

}
