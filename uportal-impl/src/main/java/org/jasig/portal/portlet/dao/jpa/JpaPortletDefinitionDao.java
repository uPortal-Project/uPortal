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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.dao.IChannelDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of the portlet definition DAO
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
public class JpaPortletDefinitionDao implements IPortletDefinitionDao {
    private static final String FIND_PORTLET_DEF_BY_CHANNEL_DEF = 
        "from PortletDefinitionImpl portDef " +
        "where portDef.channelDefinition = :channelDefinition";
    
    private static final String FIND_PORTLET_DEF_BY_CHANNEL_DEF_CACHE_REGION = PortletDefinitionImpl.class.getName() + ".query.FIND_PORTLET_DEF_BY_CHANNEL_DEF";
    
    private EntityManager entityManager;
    private IChannelDefinitionDao channelDefinitionDao;
    
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
    
    @Required
    public void setChannelDefinitionDao(IChannelDefinitionDao channelDefinitionDao) {
        this.channelDefinitionDao = channelDefinitionDao;
    }
    
    @Override
    public IPortletDefinition getPortletDefinition(int channelPublishId) {
        final IChannelDefinition channelDefinition = this.channelDefinitionDao.getChannelDefinition(channelPublishId);
        
        final Query query = this.entityManager.createQuery(FIND_PORTLET_DEF_BY_CHANNEL_DEF);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", FIND_PORTLET_DEF_BY_CHANNEL_DEF_CACHE_REGION);
        query.setParameter("channelDefinition", channelDefinition);
        
        final List portletEntities = query.getResultList();
        return (IPortletDefinition)DataAccessUtils.singleResult(portletEntities);
    }
    
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        
        final long internalPortletDefinitionId = Long.parseLong(portletDefinitionId.getStringId());
        final PortletDefinitionImpl portletDefinition = this.entityManager.find(PortletDefinitionImpl.class, internalPortletDefinitionId);
        
        return portletDefinition;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.dao.portlet.IPortletDefinitionDao#updatePortletDefinition(org.jasig.portal.om.portlet.IPortletDefinition)
     */
    @Transactional
    public void updatePortletDefinition(IPortletDefinition portletDefinition) {
        Validate.notNull(portletDefinition, "portletDefinition can not be null");
        
        this.entityManager.persist(portletDefinition);
    }
}
