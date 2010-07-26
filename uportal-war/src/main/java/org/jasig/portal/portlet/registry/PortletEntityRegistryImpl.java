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

package org.jasig.portal.portlet.registry;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.user.IUserInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Provides access to IPortletEntity objects and convenience methods for creating
 * and converting them and related objects.
 * 
 * The portlet adaptor channel will be responsible for listenting to unsubscribe events and cleaning up entity objects
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletEntityRegistryImpl implements IPortletEntityRegistry {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletEntityDao portletEntityDao;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    
    
    /**
     * @return the portletEntityDao
     */
    public IPortletEntityDao getPortletEntityDao() {
        return portletEntityDao;
    }
    /**
     * @param portletEntityDao the portletEntityDao to set
     */
    @Autowired(required=true)
    public void setPortletEntityDao(@Qualifier("main") IPortletEntityDao portletEntityDao) {
        this.portletEntityDao = portletEntityDao;
    }

    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired(required=true)
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#createPortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        return this.portletEntityDao.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        return this.portletEntityDao.getPortletEntity(portletEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String)
     */
    public IPortletEntity getPortletEntity(String portletEntityIdString) {
        Validate.notNull(portletEntityIdString, "portletEntityId can not be null");
        
        final long portletEntityIdLong;
        try {
            portletEntityIdLong = Long.parseLong(portletEntityIdString);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("PortletEntityId must parsable as a long", nfe);
        }
        
        final PortletEntityIdImpl portletEntityId = new PortletEntityIdImpl(portletEntityIdLong);
        return this.portletEntityDao.getPortletEntity(portletEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String, int)
     */
    public IPortletEntity getPortletEntity(String channelSubscribeId, int userId) {
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        return this.portletEntityDao.getPortletEntity(channelSubscribeId, userId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntitiesForUser(int)
     */
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        return this.portletEntityDao.getPortletEntitiesForUser(userId);
    }
    
    public IPortletEntity getOrCreatePortletEntity(IUserInstance userInstance, String channelSubscribeId) {
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Find the channel and portlet definitions
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(channelSubscribeId);
        final int channelPublishId = Integer.valueOf(channelNode.getChannelPublishId());
        
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(channelPublishId);
        
        return this.getOrCreatePortletEntity(portletDefinition.getPortletDefinitionId(), channelSubscribeId, userInstance.getPerson().getID());
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getOrCreatePortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    public IPortletEntity getOrCreatePortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        final IPortletEntity portletEntity = this.getPortletEntity(channelSubscribeId, userId);
        if (portletEntity != null) {
            if (!portletDefinitionId.equals(portletEntity.getPortletDefinitionId())) {
                this.logger.warn("Found portlet entity '" + portletEntity + "' is not the correct entity for portlet definition id: " + portletDefinitionId + ". The entity will be deleted and a new one created.");
                this.deletePortletEntity(portletEntity);
            }
            else {
                return portletEntity;
            }
        }
        
        return this.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#storePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void storePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        this.portletEntityDao.updatePortletEntity(portletEntity);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#deletePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void deletePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        this.portletEntityDao.deletePortletEntity(portletEntity);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getParentPortletDefinition(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        final IPortletEntity portletEntity = this.getPortletEntity(portletEntityId);
        final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
        return this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    }
}

class PortletEntityIdImpl extends AbstractObjectId implements IPortletEntityId {
    private static final long serialVersionUID = 1L;

    public PortletEntityIdImpl(long portletEntityId) {
        super(Long.toString(portletEntityId));
    }
}

