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

package org.jasig.portal.portlet.dao.trans;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;

/**
 * Handles entity management for transient portlets, defined as portlets that aren't permanent parts of the user's layout. Portlet
 * preferences are still persisted but are associated with a transient rendering of the portlet. Transient portlets are
 * detected by checking the channel subscribe ID against the {@link TransientUserLayoutManagerWrapper#SUBSCRIBE_PREFIX}
 * prefix.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository
@Qualifier("main")
public class TransientPortletEntityDao implements IPortletEntityDao {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityDao delegatePortletEntityDao;
    private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;

    
    public IPortletEntityDao getDelegatePortletEntityDao() {
        return delegatePortletEntityDao;
    }
    /**
     * The IPortletEntityDao to delegate calls to for actualy persistence
     */
    @Autowired
    public void setDelegatePortletEntityDao(@Qualifier("persistence") IPortletEntityDao delegatePortletEntityDao) {
        this.delegatePortletEntityDao = delegatePortletEntityDao;
    }
    
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * Registry for looking up data related to portlet definitions
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    public IUserInstanceManager getUserInstanceManager() {
        return userInstanceManager;
    }
    /**
     * Used to get access to the user's layout manager
     */
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * Used to get access to the current portal request
     */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#createPortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        if (channelSubscribeId.startsWith(TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX)) {
            final String transientChannelSubscribeId = channelSubscribeId;
            channelSubscribeId = this.getDatabaseChannelSubscribeId(portletDefinitionId);
            
            final IPortletEntity portletEntity = this.delegatePortletEntityDao.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
            return new TransientPortletEntity(portletEntity, transientChannelSubscribeId);
        }
        
        return this.delegatePortletEntityDao.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#deletePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void deletePortletEntity(IPortletEntity portletEntity) {
        portletEntity = this.unwrapEntity(portletEntity);
        this.delegatePortletEntityDao.deletePortletEntity(portletEntity);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#getPortletEntities(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    public Set<IPortletEntity> getPortletEntities(IPortletDefinitionId portletDefinitionId) {
        final Set<IPortletEntity> portletEntities = this.delegatePortletEntityDao.getPortletEntities(portletDefinitionId);
        return this.wrapEntities(portletEntities);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#getPortletEntitiesForUser(int)
     */
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        final Set<IPortletEntity> portletEntities = this.delegatePortletEntityDao.getPortletEntitiesForUser(userId);
        return this.wrapEntities(portletEntities);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#getPortletEntity(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        final IPortletEntity portletEntity = this.delegatePortletEntityDao.getPortletEntity(portletEntityId);
        return this.wrapEntity(portletEntity);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#getPortletEntity(java.lang.String, int)
     */
    public IPortletEntity getPortletEntity(String channelSubscribeId, int userId) {
        if (channelSubscribeId.startsWith(TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX)) {
            final String databaseChannelSubscribeId = this.determineDatabaseChannelSubscribeId(channelSubscribeId);
            
            final IPortletEntity portletEntity = this.delegatePortletEntityDao.getPortletEntity(databaseChannelSubscribeId, userId);
            return this.wrapEntity(portletEntity);
        }
        
        return this.delegatePortletEntityDao.getPortletEntity(channelSubscribeId, userId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.dao.IPortletEntityDao#updatePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void updatePortletEntity(IPortletEntity portletEntity) {
        portletEntity = this.unwrapEntity(portletEntity);
        this.delegatePortletEntityDao.updatePortletEntity(portletEntity);
    }
    
    
    
    /**
     * Returns the unwrapped entity if it is an instance of TransientPortletEntity. If not the original entity is
     * returned.
     */
    protected IPortletEntity unwrapEntity(IPortletEntity portletEntity) {
        if (portletEntity instanceof TransientPortletEntity) {
            return ((TransientPortletEntity)portletEntity).getDelegatePortletEntity();
        }
        
        return portletEntity;
    }
    
    /**
     * Adds a TransientPortletEntity wrapper to the portletEntity if it is needed. If the specified entity is transient
     * but no transient subscribe id has been registered for it yet in the transientIdMap null is returned. If no
     * wrapping is needed the original entity is returned.
     */
    protected IPortletEntity wrapEntity(IPortletEntity portletEntity) {
        if (portletEntity == null) {
            return null;
        }

        final String databaseChannelSubscribeId = portletEntity.getChannelSubscribeId();
        if (databaseChannelSubscribeId.startsWith(TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX)) {
            final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
            final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
            final IChannelDefinition channelDefinition = portletDefinition.getChannelDefinition();
            final String fname = channelDefinition.getFName();
            
            final IUserLayoutManager userLayoutManager = this.getUserLayoutManager();
            if (userLayoutManager == null) {
                this.logger.warn("Could not find IUserLayoutManager when trying to wrap transient portlet entity: " + portletEntity);
                return portletEntity;
            }

            final String subscribeId = userLayoutManager.getSubscribeId(fname);
            return new TransientPortletEntity(portletEntity, subscribeId);
        }
        
        return portletEntity;
    }
    
    /**
     * Calles {@link #wrapEntity(IPortletEntity)} on each entry in the Set, wrap calls that return null result in the
     * entitiy being dropped from the returned Set
     */
    protected Set<IPortletEntity> wrapEntities(Set<IPortletEntity> portletEntities) {
        final Set<IPortletEntity> wrappedPortletEntities = new LinkedHashSet<IPortletEntity>(portletEntities.size());
        
        for (final IPortletEntity portletEntity : portletEntities) {
            final IPortletEntity wrappedEntity = this.wrapEntity(portletEntity);
            
            if (wrappedEntity != null) {
                wrappedPortletEntities.add(wrappedEntity);
            }
        }
        
        return wrappedPortletEntities;
    }
    
    protected String determineDatabaseChannelSubscribeId(String channelSubscribeId) {
        //Find the referenced Node in the user's layout
        final IUserLayoutManager userLayoutManager = this.getUserLayoutManager();
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(channelSubscribeId);
        
        //Lookup the IportletDefinition for the node
        final String channelPublishId = channelNode.getChannelPublishId();
        final int channelDefinitionId = Integer.valueOf(channelPublishId.startsWith("chan") ? channelPublishId.substring(4) : channelPublishId);
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(channelDefinitionId);

        //Generate the subscribe ID used for the database
        return this.getDatabaseChannelSubscribeId(portletDefinition.getPortletDefinitionId());
    }
    
    protected String getDatabaseChannelSubscribeId(IPortletDefinitionId portletDefinitionId) {
        return TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX + "." + portletDefinitionId.getStringId();
    }

    protected IUserLayoutManager getUserLayoutManager() {
        final HttpServletRequest portalRequest;
        try {
            portalRequest = this.portalRequestUtils.getCurrentPortalRequest();
        }
        catch (IllegalStateException ise) {
            //Assuming that we are running from the command line for something like data export
            return null;
        }
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(portalRequest);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        return preferencesManager.getUserLayoutManager();
    }
}
