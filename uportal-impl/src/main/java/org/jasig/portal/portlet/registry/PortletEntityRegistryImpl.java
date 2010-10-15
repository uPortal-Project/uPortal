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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.WebUtils;

/**
 * Provides access to IPortletEntity objects and convenience methods for creating
 * and converting them and related objects.
 * 
 * The portlet adaptor channel will be responsible for listenting to unsubscribe events and cleaning up entity objects
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEntityRegistryImpl implements IPortletEntityRegistry {
    public static final String INTERIM_PORTLET_ENTITY_MAP_ATTRIBUTE = PortletEntityRegistryImpl.class.getName() + ".INTERIM_PORTLET_ENTITY_MAP";
    public static final String PORTLET_ENTITY_ID_MAP_ATTRIBUTE = PortletEntityRegistryImpl.class.getName() + ".PORTLET_ENTITY_ID_MAP";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletEntityDao portletEntityDao;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    
    
    /**
     * @return the portletEntityDao
     */
    public IPortletEntityDao getPortletEntityDao() {
        return portletEntityDao;
    }
    /**
     * @param portletEntityDao the portletEntityDao to set
     */
    @Required
    public void setPortletEntityDao(IPortletEntityDao portletEntityDao) {
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
    @Required
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#createPortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    @Override
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        final InterimPortletEntityImpl interimPortletEntity = new InterimPortletEntityImpl(portletDefinitionId, channelSubscribeId, userId);
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Created InterimPortletEntity " + interimPortletEntity.getPortletEntityId() + " for def=" + portletDefinitionId + ", sub=" + channelSubscribeId + ", usr=" + userId);
        }
        
        this.storeInterimPortletEntity(interimPortletEntity);
        return interimPortletEntity;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    @Override
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        final InterimPortletEntityImpl interimPortletEntity = this.getInterimPortletEntity(portletEntityId);
        if (interimPortletEntity != null) {
            return interimPortletEntity;
        }
        
        //Need to find a mapped persistent entity ID to do a DB lookup
        final IPortletEntityId persistentEntityId = this.getPersistentId(portletEntityId);
        if (persistentEntityId == null) {
            return null;
        }
        
        final IPortletEntity persistentPortletEntity = this.portletEntityDao.getPortletEntity(persistentEntityId);
        if (persistentPortletEntity == null) {
            return null;
        }
        
        return new PersistentPortletEntityWrapper(persistentPortletEntity);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String)
     */
    @Override
    public IPortletEntity getPortletEntity(String portletEntityIdString) {
        Validate.notNull(portletEntityIdString, "portletEntityId can not be null");
        return this.getPortletEntity(new PortletEntityIdImpl(portletEntityIdString));
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String, int)
     */
    @Override
    public IPortletEntity getPortletEntity(String channelSubscribeId, int userId) {
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        final InterimPortletEntityImpl interimPortletEntity = this.getInterimPortletEntity(channelSubscribeId, userId);
        if (interimPortletEntity != null) {
            return interimPortletEntity;
        }
        
        final IPortletEntity persistentPortletEntity = this.portletEntityDao.getPortletEntity(channelSubscribeId, userId);
        if (persistentPortletEntity == null) {
            return null;
        }
        
        final PersistentPortletEntityWrapper wrappedPortletEntity = new PersistentPortletEntityWrapper(persistentPortletEntity);
        this.setPersistentIdMapping(wrappedPortletEntity.getPortletEntityId(), persistentPortletEntity.getPortletEntityId());
        
        return wrappedPortletEntity;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntitiesForUser(int)
     */
    @Override
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        final Set<IPortletEntity> persistentPortletEntities = this.portletEntityDao.getPortletEntitiesForUser(userId);
        
        final Set<IPortletEntity> wrappedPortletEntities = new LinkedHashSet<IPortletEntity>();
        for (final IPortletEntity persistentPortletEntity : persistentPortletEntities) {
            final PersistentPortletEntityWrapper wrappedPortletEntity = new PersistentPortletEntityWrapper(persistentPortletEntity);
            this.setPersistentIdMapping(wrappedPortletEntity.getPortletEntityId(), persistentPortletEntity.getPortletEntityId());
            wrappedPortletEntities.add(wrappedPortletEntity);
        }
        
        final Set<InterimPortletEntityImpl> interimPortletEntities = this.getInterimPortletEntities(userId);
        if (interimPortletEntities != null) {
            wrappedPortletEntities.addAll(interimPortletEntities);
        }
        
        return wrappedPortletEntities;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getOrCreatePortletEntity(org.jasig.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    @Override
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
    @Override
    public void storePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
        
        if (portletEntity instanceof PersistentPortletEntityWrapper) {
            //Unwrap the persistent entity
            portletEntity = ((PersistentPortletEntityWrapper)portletEntity).getPersistentEntity();
            
            //Already persistent entity that still has prefs 
            if (preferences.size() > 0) {
                this.portletEntityDao.updatePortletEntity(portletEntity);
            }
            //Already persistent entity with no preferences, DELETE!
            else {
                this.portletEntityDao.deletePortletEntity(portletEntity);
                
                //remove the persistent ID mapping after the delete
                this.removePersistentId(portletEntityId);
                
                //Setup an interim entity in its place
                final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
                final String channelSubscribeId = portletEntity.getChannelSubscribeId();
                final int userId = portletEntity.getUserId();
                final IPortletEntity interimPortletEntity = this.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
                
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Persistent portlet entity " + portletEntityId + " no longer has preferences. Deleted it and created InterimPortletEntity " + interimPortletEntity.getPortletEntityId());
                }
            }
        }
        else {
            //There are preferences on the interim entity, create an store it
            if (preferences.size() > 0) {
                final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
                final String channelSubscribeId = portletEntity.getChannelSubscribeId();
                final int userId = portletEntity.getUserId();
                final IPortletEntity persistantEntity = this.portletEntityDao.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);

                persistantEntity.setPortletPreferences(portletPreferences);
                
                this.portletEntityDao.updatePortletEntity(persistantEntity);
                
                //Remove the in-memory interim entity
                this.removeInterimPortletEntity(portletEntityId);
                
                //Setup the persistent ID mapping
                this.setPersistentIdMapping(portletEntityId, persistantEntity.getPortletEntityId());
                
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("InterimPortletEntity " + portletEntityId + " now has preferences. Deleted it and created persistent portlet entity " + persistantEntity.getPortletEntityId());
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#deletePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    @Override
    public void deletePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        if (portletEntity instanceof PersistentPortletEntityWrapper) {
            //Unwrap the persistent entity
            portletEntity = ((PersistentPortletEntityWrapper)portletEntity).getPersistentEntity();
            
            this.portletEntityDao.deletePortletEntity(portletEntity);
            
            //remove the persistent ID mapping after the delete
            this.removePersistentId(portletEntity.getPortletEntityId());
        }
        else {
            this.removeInterimPortletEntity(portletEntity.getPortletEntityId());
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getParentPortletDefinition(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    @Override
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        final IPortletEntity portletEntity = this.getPortletEntity(portletEntityId);
        final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();

        return this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    }
    
    protected void setPersistentIdMapping(IPortletEntityId wrapperId, IPortletEntityId persistentId) {
        final HttpSession session = this.getSession();
        if (session == null) {
            return;
        }
        
        Map<IPortletEntityId, IPortletEntityId> idMapping;
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (WebUtils.getSessionMutex(session)) {
            idMapping = (Map<IPortletEntityId, IPortletEntityId>)session.getAttribute(PORTLET_ENTITY_ID_MAP_ATTRIBUTE);
            if (idMapping == null) {
                idMapping = new ConcurrentHashMap<IPortletEntityId, IPortletEntityId>();
                session.setAttribute(PORTLET_ENTITY_ID_MAP_ATTRIBUTE, idMapping);
            }
        }
        
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Mapping wraper ID " + wrapperId + " to persistent ID " + persistentId);
        }
        
        idMapping.put(wrapperId, persistentId);
    }
    
    protected IPortletEntityId getPersistentId(IPortletEntityId wrapperId) {
        final Map<IPortletEntityId, IPortletEntityId> idMapping = this.getPersistentIdMap();
        if (idMapping == null) {
            return null;
        }
        
        return idMapping.get(wrapperId);
    }
    
    protected void removePersistentId(IPortletEntityId wrapperId) {
        final Map<IPortletEntityId, IPortletEntityId> idMapping = this.getPersistentIdMap();
        if (idMapping == null) {
            return;
        }
        
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Removed persistent ID for wraper ID " + wrapperId);
        }
        
        idMapping.remove(wrapperId);
    }

    protected Map<IPortletEntityId, IPortletEntityId> getPersistentIdMap() {
        final HttpSession session = this.getSession();
        if (session == null) {
            return null;
        }
        
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (WebUtils.getSessionMutex(session)) {
            return (Map<IPortletEntityId, IPortletEntityId>)session.getAttribute(PORTLET_ENTITY_ID_MAP_ATTRIBUTE);
        }
    }
    
    protected void removeInterimPortletEntity(IPortletEntityId portletEntityId) {
        final InterimPortletEntityCache entityCache = getInterimPortletEntityCache();
        if (entityCache == null) {
            return;
        }
        
        entityCache.removeEntity(portletEntityId);
    }
    
    protected InterimPortletEntityImpl getInterimPortletEntity(IPortletEntityId portletEntityId) {
        final InterimPortletEntityCache entityCache = getInterimPortletEntityCache();
        if (entityCache == null) {
            return null;
        }
        
        return entityCache.getEntity(portletEntityId);
    }
    
    protected InterimPortletEntityImpl getInterimPortletEntity(String channelSubscribeId, int userId) {
        final InterimPortletEntityCache entityCache = getInterimPortletEntityCache();
        if (entityCache == null) {
            return null;
        }
        
        return entityCache.getEntity(channelSubscribeId, userId);
    }
    
    protected Set<InterimPortletEntityImpl> getInterimPortletEntities(int userId) {
        final InterimPortletEntityCache entityCache = getInterimPortletEntityCache();
        if (entityCache == null) {
            return null;
        }
        
        return entityCache.getEntities(userId);
    }
    
    protected void storeInterimPortletEntity(InterimPortletEntityImpl interimPortletEntity) {
        final HttpSession session = this.getSession();
        if (session == null) {
            return;
        }
        
        InterimPortletEntityCache entityCache;
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (WebUtils.getSessionMutex(session)) {
            entityCache = (InterimPortletEntityCache)session.getAttribute(INTERIM_PORTLET_ENTITY_MAP_ATTRIBUTE);
            if (entityCache == null) {
                entityCache = new InterimPortletEntityCache();
                session.setAttribute(INTERIM_PORTLET_ENTITY_MAP_ATTRIBUTE, entityCache);
            }
        }
        
        entityCache.storeEntity(interimPortletEntity);
    }
    
    protected InterimPortletEntityCache getInterimPortletEntityCache() {
        final HttpSession session = this.getSession();
        if (session == null) {
            return null;
        }
        
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (WebUtils.getSessionMutex(session)) {
            return (InterimPortletEntityCache)session.getAttribute(INTERIM_PORTLET_ENTITY_MAP_ATTRIBUTE);
        }
    }

    /**
     * @return The session for the current request, will return null if not in request
     */
    protected HttpSession getSession() {
        final HttpServletRequest request;
        try {
            request = this.portalRequestUtils.getCurrentPortalRequest();
        }
        catch (IllegalStateException e) {
            //No current request, just return null
            return null;
        }
        
        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("A HttpSession must already exist for the PortletEntityRegistryImpl to function");
        }
        return session;
    }
    
    private static final class InterimPortletEntityCache {
        private final ReadWriteLock cacheLock = new ReentrantReadWriteLock(true);
        
        private final Map<SubscribeKey, InterimPortletEntityImpl> entitiesBySubscribeKey = new HashMap<SubscribeKey, InterimPortletEntityImpl>();
        private final Map<IPortletEntityId, InterimPortletEntityImpl> entitiesById = new HashMap<IPortletEntityId, InterimPortletEntityImpl>();
        
        public void storeEntity(InterimPortletEntityImpl interimPortletEntity) {
            this.cacheLock.writeLock().lock();
            try {
                final SubscribeKey subscribeKey = new SubscribeKey(interimPortletEntity.getUserId(), interimPortletEntity.getChannelSubscribeId());
                this.entitiesBySubscribeKey.put(subscribeKey, interimPortletEntity);
                this.entitiesById.put(interimPortletEntity.getPortletEntityId(), interimPortletEntity);
            }
            finally {
                this.cacheLock.writeLock().unlock();
            }
        }
        
        public InterimPortletEntityImpl getEntity(String channelSubscribeId, int userId) {
            this.cacheLock.readLock().lock();
            try {
                final SubscribeKey subscribeKey = new SubscribeKey(userId, channelSubscribeId);
                return this.entitiesBySubscribeKey.get(subscribeKey);
            }
            finally {
                this.cacheLock.readLock().unlock();
            }
        }
        
        public Set<InterimPortletEntityImpl> getEntities(int userId) {
            final Set<InterimPortletEntityImpl> entities = new LinkedHashSet<InterimPortletEntityImpl>();
            
            this.cacheLock.readLock().lock();
            try {
                for (final InterimPortletEntityImpl interimPortletEntity : this.entitiesById.values()) {
                    if (userId == interimPortletEntity.getUserId()) {
                        entities.add(interimPortletEntity);
                    }
                }
            }
            finally {
                this.cacheLock.readLock().unlock();
            }
            
            return entities;
        }
        
        public InterimPortletEntityImpl getEntity(IPortletEntityId portletEntityId) {
            this.cacheLock.readLock().lock();
            try {
                return this.entitiesById.get(portletEntityId);
            }
            finally {
                this.cacheLock.readLock().unlock();
            }
        }
        
        public void removeEntity(IPortletEntityId portletEntityId) {
            this.cacheLock.writeLock().lock();
            try {
                final InterimPortletEntityImpl interimPortletEntity = this.entitiesById.remove(portletEntityId);
                if (interimPortletEntity != null) {
                    final SubscribeKey subscribeKey = new SubscribeKey(interimPortletEntity.getUserId(), interimPortletEntity.getChannelSubscribeId());
                    this.entitiesBySubscribeKey.remove(subscribeKey);
                }
            }
            finally {
                this.cacheLock.writeLock().unlock();
            }
        }
        
        private static final class SubscribeKey {
            private final int userId;
            private final String channelSubscribeId;
            
            public SubscribeKey(int userId, String channelSubscribeId) {
                this.userId = userId;
                this.channelSubscribeId = channelSubscribeId;
            }
            
            @Override
            public String toString() {
                return "[userId=" + userId + ", channelSubscribeId=" + channelSubscribeId + "]";
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((channelSubscribeId == null) ? 0 : channelSubscribeId.hashCode());
                result = prime * result + userId;
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                SubscribeKey other = (SubscribeKey) obj;
                if (channelSubscribeId == null) {
                    if (other.channelSubscribeId != null)
                        return false;
                }
                else if (!channelSubscribeId.equals(other.channelSubscribeId))
                    return false;
                if (userId != other.userId)
                    return false;
                return true;
            }
        }
    }
}
