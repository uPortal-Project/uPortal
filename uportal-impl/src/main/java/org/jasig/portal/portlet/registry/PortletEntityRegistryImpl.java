/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId) {
        Validate.notNull(portletDefinitionId, "portletDefinitionId can not be null");
        Validate.notNull(channelSubscribeId, "channelSubscribeId can not be null");
        
        final InterimPortletEntityImpl interimPortletEntity = new InterimPortletEntityImpl(portletDefinitionId, channelSubscribeId, userId);
        this.storeInterimPortletEntity(interimPortletEntity);
        return interimPortletEntity;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityId)) {
            final InterimPortletEntityImpl interimPortletEntity = this.getInterimPortletEntity(portletEntityId);
            if (interimPortletEntity != null) {
                return interimPortletEntity;
            }
            
            return new InterimPortletEntityImpl(portletEntityId);
        }
        
        return this.portletEntityDao.getPortletEntity(portletEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String)
     */
    public IPortletEntity getPortletEntity(String portletEntityIdString) {
        Validate.notNull(portletEntityIdString, "portletEntityId can not be null");
        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityIdString)) {
            final IPortletEntityId portletEntityId = new InterimPortletEntityImpl.InterimPortletEntityIdImpl(portletEntityIdString);
            final InterimPortletEntityImpl interimPortletEntity = this.getInterimPortletEntity(portletEntityId);
            if (interimPortletEntity != null) {
                return interimPortletEntity;
            }
            
            return new InterimPortletEntityImpl(portletEntityId);
        }
        
        final long portletEntityIdLong;
        try {
            portletEntityIdLong = Long.parseLong(portletEntityIdString);
        } catch (NumberFormatException nfe) {
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
        final InterimPortletEntityImpl interimPortletEntity = this.getInterimPortletEntity(channelSubscribeId, userId);
        if (interimPortletEntity != null) {
            return interimPortletEntity;
        }
        
        return this.portletEntityDao.getPortletEntity(channelSubscribeId, userId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getPortletEntitiesForUser(int)
     */
    public Set<IPortletEntity> getPortletEntitiesForUser(int userId) {
        return this.portletEntityDao.getPortletEntitiesForUser(userId);
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
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
        
        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityId)) {
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
            }
        }
        else {
            //Already persistent entity that still has prefs 
            if (preferences.size() > 0) {
                this.portletEntityDao.updatePortletEntity(portletEntity);
            }
            //Already persistent entity with no preferences, DELETE!
            else {
                this.portletEntityDao.deletePortletEntity(portletEntity);
                
                //Setup an interim entity in its place
                final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
                final String channelSubscribeId = portletEntity.getChannelSubscribeId();
                final int userId = portletEntity.getUserId();
                this.createPortletEntity(portletDefinitionId, channelSubscribeId, userId);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#deletePortletEntity(org.jasig.portal.portlet.om.IPortletEntity)
     */
    public void deletePortletEntity(IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityId)) {
            this.removeInterimPortletEntity(portletEntityId);
        }
        else {
            this.portletEntityDao.deletePortletEntity(portletEntity);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletEntityRegistry#getParentPortletDefinition(org.jasig.portal.portlet.om.IPortletEntityId)
     */
    public IPortletDefinition getParentPortletDefinition(IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");
        
        final IPortletDefinitionId portletDefinitionId;
        if (InterimPortletEntityImpl.isInterimPortletEntityId(portletEntityId)) {
            IPortletEntity portletEntity = new InterimPortletEntityImpl(portletEntityId);
            portletDefinitionId = portletEntity.getPortletDefinitionId();
        } else {
            final IPortletEntity portletEntity = this.getPortletEntity(portletEntityId);
            portletDefinitionId = portletEntity.getPortletDefinitionId();
        }

        return this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
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
    
    protected void storeInterimPortletEntity(InterimPortletEntityImpl interimPortletEntity) {
        final HttpSession session = this.getSession();
        
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
        
        //Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (WebUtils.getSessionMutex(session)) {
            return (InterimPortletEntityCache)session.getAttribute(INTERIM_PORTLET_ENTITY_MAP_ATTRIBUTE);
        }
    }

    /**
     * Gets the session for the request.
     * 
     * @return The session for the current request, will not return null.
     */
    protected HttpSession getSession() {
        final HttpServletRequest request = this.portalRequestUtils.getCurrentPortalRequest();
        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("A HttpSession must already exist for the PortletEntityRegistryImpl to function");
        }
        return session;
    }
    

    private static final class PortletEntityIdImpl extends AbstractObjectId implements IPortletEntityId {
        private static final long serialVersionUID = 1L;
    
        public PortletEntityIdImpl(long portletEntityId) {
            super(Long.toString(portletEntityId));
        }
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
