/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Function;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.portlet.dao.IPortletEntityDao;
import org.apereo.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.ConcurrentMapUtils;
import org.apereo.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.WebUtils;

/**
 * Provides access to IPortletEntity objects and convenience methods for creating and converting
 * them and related objects.
 *
 * <p>The portlet adaptor channel will be responsible for listenting to unsubscribe events and
 * cleaning up entity objects
 */
@Service
public class PortletEntityRegistryImpl implements IPortletEntityRegistry, InitializingBean {

    private static final String PORTLET_ENTITY_DATA_ATTRIBUTE =
            PortletEntityRegistryImpl.class.getName() + ".PORTLET_ENTITY_DATA";
    private static final String PORTLET_ENTITY_ATTRIBUTE =
            PortletEntityRegistryImpl.class.getName() + ".PORTLET_ENTITY.thread-";
    private static final String PORTLET_ENTITY_LOCK_MAP_ATTRIBUTE =
            PortletEntityRegistryImpl.class.getName() + ".PORTLET_ENTITY_LOCK_MAP_ATTRIBUTE";
    private static final String PORTLET_DEFINITION_LOOKUP_MAP_ATTRIBUTE =
            PortletEntityRegistryImpl.class.getName() + ".PORTLET_DEFINITION_LOOKUP_MAP_ATTRIBUTE";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IUserInstanceManager userInstanceManager;
    private IPortletEntityDao portletEntityDao;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    private Ehcache entityIdParseCache;
    private ObjectMapper mapper;

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }

    @Autowired
    public void setEntityIdParseCache(
            @Qualifier("org.apereo.portal.portlet.dao.jpa.PortletEntityImpl.idParseCache")
                    Ehcache entityIdParseCache) {
        this.entityIdParseCache = entityIdParseCache;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortletEntityDao(@Qualifier("transient") IPortletEntityDao portletEntityDao) {
        this.portletEntityDao = portletEntityDao;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(PortletEntityData.class, new PortletEntityDataSerializer());
        module.addDeserializer(PortletEntityData.class, new PortletEntityDataDeserializer());
        mapper.registerModule(module);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(org.apereo.portal.portlet.om.IPortletEntityId)
     */
    @Override
    public IPortletEntity getPortletEntity(
            HttpServletRequest request, IPortletEntityId portletEntityId) {
        Validate.notNull(portletEntityId, "portletEntityId can not be null");

        // Sync on the request map to make sure duplicate IPortletEntitys aren't ever created
        final PortletEntityCache<IPortletEntity> portletEntityCache =
                this.getPortletEntityMap(request);

        return this.getPortletEntity(request, portletEntityCache, portletEntityId, null, -1);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.registry.IPortletEntityRegistry#getPortletEntity(java.lang.String)
     */
    @Override
    public IPortletEntity getPortletEntity(
            HttpServletRequest request, String portletEntityIdString) {
        final IPortletEntityId portletEntityId =
                this.parseConsistentPortletEntityId(request, portletEntityIdString);
        return this.getPortletEntity(request, portletEntityId);
    }

    @Override
    public IPortletEntity getOrCreateDefaultPortletEntity(
            HttpServletRequest request, IPortletDefinitionId portletDefinitionId) {
        Validate.notNull(request, "HttpServletRequest cannot be null");
        Validate.notNull(portletDefinitionId, "portletDefinitionId cannot be null");

        final IPortletDefinition portletDefinition =
                this.getPortletDefinition(request, portletDefinitionId);
        if (portletDefinition == null) {
            throw new IllegalArgumentException(
                    "No portlet definition found for id '" + portletDefinitionId + "'.");
        }

        // Determine the appropriate portlet window ID for the definition
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        // Determine the subscribe ID
        final String portletFName = portletDefinition.getFName();
        final String layoutNodeId = userLayoutManager.getSubscribeId(portletFName);
        if (layoutNodeId == null) {
            throw new IllegalArgumentException(
                    "No layout node ID found for fname '" + portletFName + "'.");
        }

        this.logger.trace(
                "Found layout node {} for portlet definition {}", layoutNodeId, portletFName);

        final IPerson person = userInstance.getPerson();
        final int personId = person.getID();
        return this.getOrCreatePortletEntity(request, portletDefinitionId, layoutNodeId, personId);
    }

    @Override
    public IPortletEntity getOrCreatePortletEntity(
            HttpServletRequest request, IUserInstance userInstance, String layoutNodeId) {
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        // Find the channel and portlet definitions
        final IUserLayoutChannelDescription channelNode =
                (IUserLayoutChannelDescription) userLayoutManager.getNode(layoutNodeId);
        if (channelNode == null) {
            this.logger.warn(
                    "No layout node exists for id "
                            + layoutNodeId
                            + ", no portlet entity will be returned.");
            return null;
        }

        final String channelPublishId = channelNode.getChannelPublishId();

        final IPortletDefinition portletDefinition =
                this.getPortletDefinition(request, userInstance, channelPublishId);

        if (portletDefinition != null) {
            final IPerson person = userInstance.getPerson();
            return this.getOrCreatePortletEntity(
                    request,
                    portletDefinition.getPortletDefinitionId(),
                    layoutNodeId,
                    person.getID());
        }

        // No permission to see the portlet
        return null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.registry.IPortletEntityRegistry#getOrCreatePortletEntity(org.apereo.portal.portlet.om.IPortletDefinitionId, java.lang.String, int)
     */
    @Override
    public IPortletEntity getOrCreatePortletEntity(
            HttpServletRequest request,
            IPortletDefinitionId portletDefinitionId,
            String layoutNodeId,
            int userId) {
        final PortletEntityCache<IPortletEntity> portletEntityCache = getPortletEntityMap(request);

        // Try just getting an existing entity first
        IPortletEntity portletEntity =
                this.getPortletEntity(request, portletEntityCache, null, layoutNodeId, userId);

        // Found an existing entity!
        if (portletEntity != null) {
            // Verify the definition IDs match, this is a MUST in the case where the subscribed
            // portlet changes
            final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
            if (portletDefinitionId.equals(portletDefinition.getPortletDefinitionId())) {
                return portletEntity;
            }

            // Remove the entity if the definition IDs don't match
            this.logger.warn(
                    "Found portlet entity '{}' is not the correct entity for portlet definition id: {}. The entity will be deleted and a new one created.",
                    portletEntity,
                    portletDefinitionId);
            this.deletePortletEntity(request, portletEntity, false);
        }

        // Create the entity data object and store it in the session map (if not already there)
        final PortletEntityCache<PortletEntityData> portletEntityDataMap =
                this.getPortletEntityDataMap(request);

        final IPortletEntityId portletEntityId =
                this.createConsistentPortletEntityId(portletDefinitionId, layoutNodeId, userId);
        PortletEntityData portletEntityData =
                new PortletEntityData(portletEntityId, portletDefinitionId, layoutNodeId, userId);
        // TODO store data
        storeEntityDataMapToSession(request, portletEntityData);
        portletEntityData = portletEntityDataMap.storeIfAbsentEntity(portletEntityData);

        portletEntity = wrapPortletEntityData(portletEntityData);

        // Stick the wrapper in the request map
        portletEntity = portletEntityCache.storeIfAbsentEntity(portletEntity);

        return portletEntity;
    }

    @Override
    public IPortletEntity getOrCreatePortletEntityByFname(
            HttpServletRequest request, IUserInstance userInstance, String fname) {
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final String subscribeId = userLayoutManager.getSubscribeId(fname);
        return this.getOrCreatePortletEntity(request, userInstance, subscribeId);
    }

    @Override
    public IPortletEntity getOrCreatePortletEntityByFname(
            HttpServletRequest request,
            IUserInstance userInstance,
            String fname,
            String preferredChannelSubscribeId) {
        try {
            final IPortletEntity portletEntity =
                    this.getOrCreatePortletEntity(
                            request, userInstance, preferredChannelSubscribeId);

            if (portletEntity != null) {
                // Verify the fname matches before returning the entity
                final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
                if (fname.equals(portletDefinition.getFName())) {
                    return portletEntity;
                }
            }
        } catch (PortalException pe) {
            // Ignored, can be the case if no layout node exists for the specified subscribe ID
        }

        // Either the layout node didn't exist or the entity for the node doesn't match the
        // requested fname
        return this.getOrCreatePortletEntityByFname(request, userInstance, fname);
    }

    @Override
    public IPortletEntity getOrCreateDelegatePortletEntity(
            HttpServletRequest request,
            IPortletWindowId parentPortletWindowId,
            IPortletDefinitionId delegatePortletDefinitionId) {
        // Create a special synthetic layout node ID for the delegate entity
        final String layoutNodeId =
                PortletWindowIdStringUtils.convertToDelegateLayoutNodeId(
                        parentPortletWindowId.toString());

        // Grab the current user
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPerson person = userInstance.getPerson();
        final int userId = person.getID();

        // Use the general API, the only thing special is the layout node id
        return getOrCreatePortletEntity(request, delegatePortletDefinitionId, layoutNodeId, userId);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.registry.IPortletEntityRegistry#storePortletEntity(org.apereo.portal.portlet.om.IPortletEntity)
     */
    @Override
    public void storePortletEntity(HttpServletRequest request, final IPortletEntity portletEntity) {
        Validate.notNull(portletEntity, "portletEntity can not be null");

        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPerson person = userInstance.getPerson();
        if (person.isGuest()) {
            // Never persist things for the guest user, just rely on in-memory storage
            return;
        }

        final IPortletEntityId wrapperPortletEntityId = portletEntity.getPortletEntityId();
        final Lock portletEntityLock = this.getPortletEntityLock(request, wrapperPortletEntityId);
        portletEntityLock.lock();
        try {
            final boolean shouldBePersisted = this.shouldBePersisted(portletEntity);

            if (portletEntity instanceof PersistentPortletEntityWrapper) {
                // Unwrap the persistent entity
                final IPortletEntity persistentEntity =
                        ((PersistentPortletEntityWrapper) portletEntity).getPersistentEntity();

                // Already persistent entity that still has prefs
                if (shouldBePersisted) {
                    try {
                        this.portletEntityDao.updatePortletEntity(persistentEntity);
                    } catch (HibernateOptimisticLockingFailureException e) {
                        // Check if this exception is from the entity being deleted from under us.
                        final boolean exists =
                                this.portletEntityDao.portletEntityExists(
                                        persistentEntity.getPortletEntityId());
                        if (!exists) {
                            this.logger.warn(
                                    "The persistent portlet has already been deleted: "
                                            + persistentEntity
                                            + ". The passed entity should be persistent so a new persistent entity will be created");
                            this.deletePortletEntity(request, portletEntity, true);
                            this.createPersistentEntity(persistentEntity, wrapperPortletEntityId);
                        } else {
                            throw e;
                        }
                    }
                }
                // Already persistent entity that should not be, DELETE!
                else {
                    // Capture identifiers needed to recreate the entity as session persistent
                    final IPortletDefinitionId portletDefinitionId =
                            portletEntity.getPortletDefinitionId();
                    final String layoutNodeId = portletEntity.getLayoutNodeId();
                    final int userId = portletEntity.getUserId();

                    // Delete the persistent entity
                    this.deletePortletEntity(request, portletEntity, false);

                    // Create a new entity and stick it in the cache
                    this.getOrCreatePortletEntity(
                            request, portletDefinitionId, layoutNodeId, userId);
                }
            } else if (portletEntity instanceof SessionPortletEntityImpl) {
                // There are preferences on the interim entity, create an store it
                if (shouldBePersisted) {
                    // Remove the session scoped entity from the request and session caches
                    this.deletePortletEntity(request, portletEntity, false);

                    final IPortletEntity persistentEntity =
                            createPersistentEntity(portletEntity, wrapperPortletEntityId);

                    if (this.logger.isTraceEnabled()) {
                        this.logger.trace(
                                "Session scoped entity "
                                        + wrapperPortletEntityId
                                        + " should now be persistent. Deleted it from session cache and created persistent portlet entity "
                                        + persistentEntity.getPortletEntityId());
                    }
                }
                // Session scoped entity that is still session scoped,
                else {
                    // Look for a persistent entity and delete it
                    final String channelSubscribeId = portletEntity.getLayoutNodeId();
                    final int userId = portletEntity.getUserId();
                    IPortletEntity existingPersistentEntity =
                            this.portletEntityDao.getPortletEntity(channelSubscribeId, userId);
                    if (existingPersistentEntity != null) {
                        final IPortletEntityId consistentPortletEntityId =
                                this.createConsistentPortletEntityId(existingPersistentEntity);
                        existingPersistentEntity =
                                new PersistentPortletEntityWrapper(
                                        existingPersistentEntity, consistentPortletEntityId);

                        this.logger.warn(
                                "A persistent portlet entity already exists: "
                                        + existingPersistentEntity
                                        + ". The passed entity has no preferences so the persistent version will be deleted");
                        this.deletePortletEntity(request, existingPersistentEntity, false);

                        // Add to request cache
                        final PortletEntityCache<IPortletEntity> portletEntityMap =
                                this.getPortletEntityMap(request);
                        portletEntityMap.storeIfAbsentEntity(portletEntity);

                        // Add to session cache
                        final PortletEntityCache<PortletEntityData> portletEntityDataMap =
                                this.getPortletEntityDataMap(request);
                        // TODO store data
                        storeEntityDataMapToSession(
                                request,
                                ((SessionPortletEntityImpl) portletEntity).getPortletEntityData());
                        portletEntityDataMap.storeIfAbsentEntity(
                                ((SessionPortletEntityImpl) portletEntity).getPortletEntityData());
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid portlet entity implementation passed: "
                                + portletEntity.getClass());
            }
        } finally {
            portletEntityLock.unlock();
        }
    }

    @Override
    public Lock getPortletEntityLock(HttpServletRequest request, IPortletEntityId portletEntityId) {
        final ConcurrentMap<IPortletEntityId, Lock> lockMap = getPortletEntityLockMap(request);

        // See if the lock already exist, return if it does
        Lock lock = lockMap.get(portletEntityId);
        if (lock != null) {
            return lock;
        }

        // Create a new lock and do a putIfAbsent to avoid synchronizing but still get a single lock
        // instance for the session.
        lock = createPortletEntityLock();
        return ConcurrentMapUtils.putIfAbsent(lockMap, portletEntityId, lock);
    }

    protected IPortletDefinition getPortletDefinition(
            HttpServletRequest request, String portletDefinitionIdStr) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        return this.getPortletDefinition(request, userInstance, portletDefinitionIdStr);
    }

    protected IPortletDefinition getPortletDefinition(
            HttpServletRequest request, IPortletDefinitionId portletDefinitionId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        return this.getPortletDefinition(userInstance, portletDefinitionId);
    }

    protected IPortletDefinition getPortletDefinition(
            HttpServletRequest request, IUserInstance userInstance, String portletDefinitionIdStr) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        final ConcurrentMap<String, IPortletDefinition> portletDefinitions =
                PortalWebUtils.getMapRequestAttribute(
                        request, PORTLET_DEFINITION_LOOKUP_MAP_ATTRIBUTE);

        IPortletDefinition portletDefinition = portletDefinitions.get(portletDefinitionIdStr);
        if (portletDefinition == NO_PERMISSION_PORTLET_DEFINITION) {
            return null;
        }
        if (portletDefinition != null) {
            return portletDefinition;
        }

        portletDefinition =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionIdStr);
        portletDefinition =
                checkPortletDefinitionRenderPermissions(userInstance, portletDefinition);
        if (portletDefinition == null) {
            portletDefinitions.put(portletDefinitionIdStr, NO_PERMISSION_PORTLET_DEFINITION);
        } else {
            portletDefinitions.put(portletDefinitionIdStr, portletDefinition);
        }

        return portletDefinition;
    }

    protected IPortletDefinition getPortletDefinition(
            IUserInstance userInstance, IPortletDefinitionId portletDefinitionId) {
        final IPortletDefinition portletDefinition =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
        return checkPortletDefinitionRenderPermissions(userInstance, portletDefinition);
    }

    private IPortletDefinition checkPortletDefinitionRenderPermissions(
            IUserInstance userInstance, final IPortletDefinition portletDefinition) {
        if (portletDefinition == null) {
            return null;
        }

        final IPerson person = userInstance.getPerson();
        final EntityIdentifier ei = person.getEntityIdentifier();
        final IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        if (ap.canRender(portletDefinition.getPortletDefinitionId().getStringId())) {
            return portletDefinition;
        }

        return null;
    }

    protected IPortletEntity createPersistentEntity(
            final IPortletEntity portletEntity, final IPortletEntityId wrapperPortletEntityId) {
        final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
        final String layoutNodeId = portletEntity.getLayoutNodeId();
        final int userId = portletEntity.getUserId();

        IPortletEntity persistentEntity =
                this.portletEntityDao.getPortletEntity(layoutNodeId, userId);
        if (persistentEntity != null) {
            this.logger.warn(
                    "A persistent portlet entity already exists: "
                            + persistentEntity
                            + ". The data from the passed in entity will be copied to the persistent entity: "
                            + portletEntity);
        } else {
            persistentEntity =
                    this.portletEntityDao.createPortletEntity(
                            portletDefinitionId, layoutNodeId, userId);
        }

        // Copy over preferences to avoid modifying any part of the interim entity by reference
        final List<IPortletPreference> existingPreferences = portletEntity.getPortletPreferences();
        final List<IPortletPreference> persistentPreferences =
                persistentEntity.getPortletPreferences();

        // Only do the copy if the List objects are not the same instance
        if (persistentPreferences != existingPreferences) {
            persistentPreferences.clear();
            for (final IPortletPreference preference : existingPreferences) {
                persistentPreferences.add(new PortletPreferenceImpl(preference));
            }
        }

        // Copy over WindowStates
        final Map<Long, WindowState> windowStates = portletEntity.getWindowStates();
        for (Map.Entry<Long, WindowState> windowStateEntry : windowStates.entrySet()) {
            final Long stylesheetDescriptorId = windowStateEntry.getKey();
            final IStylesheetDescriptor stylesheetDescriptor =
                    stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetDescriptorId);
            final WindowState windowState = windowStateEntry.getValue();
            persistentEntity.setWindowState(stylesheetDescriptor, windowState);
        }

        this.portletEntityDao.updatePortletEntity(persistentEntity);

        return persistentEntity;
    }

    /** Delete a portlet entity, removes it from the request, session and persistent stores */
    protected void deletePortletEntity(
            HttpServletRequest request, IPortletEntity portletEntity, boolean cacheOnly) {
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();

        // Remove from request cache
        final PortletEntityCache<IPortletEntity> portletEntityMap =
                this.getPortletEntityMap(request);
        portletEntityMap.removeEntity(portletEntityId);

        // Remove from session cache
        final PortletEntityCache<PortletEntityData> portletEntityDataMap =
                this.getPortletEntityDataMap(request);
        // TODO store data
        portletEntityDataMap.removeEntity(portletEntityId);
        removePortletEntityDataFromSesssion(request, portletEntityId);
        if (!cacheOnly && portletEntity instanceof PersistentPortletEntityWrapper) {
            final IPortletEntity persistentEntity =
                    ((PersistentPortletEntityWrapper) portletEntity).getPersistentEntity();

            try {
                this.portletEntityDao.deletePortletEntity(persistentEntity);
            } catch (HibernateOptimisticLockingFailureException e) {
                this.logger.warn(
                        "This persistent portlet has already been deleted: "
                                + persistentEntity
                                + ", trying to find and delete by layout node and user.");
                final IPortletEntity existingPersistentEntity =
                        this.portletEntityDao.getPortletEntity(
                                persistentEntity.getLayoutNodeId(), persistentEntity.getUserId());
                if (existingPersistentEntity != null) {
                    this.portletEntityDao.deletePortletEntity(existingPersistentEntity);
                }
            }
        }
    }

    /** Lookup the portlet entity by layoutNodeId and userId */
    protected IPortletEntity getPortletEntity(
            HttpServletRequest request,
            PortletEntityCache<IPortletEntity> portletEntityCache,
            IPortletEntityId portletEntityId,
            String layoutNodeId,
            int userId) {

        IPortletEntity portletEntity;

        // First look in the request map
        if (portletEntityId != null) {
            portletEntity = portletEntityCache.getEntity(portletEntityId);
        } else {
            portletEntity = portletEntityCache.getEntity(layoutNodeId, userId);
        }

        if (portletEntity != null) {
            logger.trace(
                    "Found IPortletEntity {} in request cache", portletEntity.getPortletEntityId());
            return portletEntity;
        }

        // Didn't find it, next look in the session map
        final PortletEntityCache<PortletEntityData> portletEntityDataMap =
                this.getPortletEntityDataMap(request);
        final PortletEntityData portletEntityData;
        // TODO store data
        if (portletEntityId != null) {
            portletEntityData = portletEntityDataMap.getEntity(portletEntityId);
        } else {
            portletEntityData = portletEntityDataMap.getEntity(layoutNodeId, userId);
        }
        storeEntityDataMapToSession(request, portletEntityData);
        if (portletEntityData != null) {

            // Stick the entity wrapper in the request map (if it wasn't already added by another
            // thread)
            portletEntity =
                    portletEntityCache.storeIfAbsentEntity(
                            portletEntityData.getPortletEntityId(),
                            new Function<IPortletEntityId, IPortletEntity>() {
                                @Override
                                public IPortletEntity apply(IPortletEntityId input) {
                                    // Found a session stored entity, wrap it to make it a real
                                    // IPortletEntity
                                    logger.trace(
                                            "Found PortletEntityData {} in session cache, caching wrapper in the request",
                                            portletEntityData.getPortletEntityId());

                                    return wrapPortletEntityData(portletEntityData);
                                }
                            });

            return portletEntity;
        }

        // Still didn't find it, look in the persistent store
        if (portletEntityId != null) {
            if (portletEntityId instanceof PortletEntityIdImpl) {
                final PortletEntityIdImpl consistentPortletEntityId =
                        (PortletEntityIdImpl) portletEntityId;
                final String localLayoutNodeId = consistentPortletEntityId.getLayoutNodeId();
                final int localUserId = consistentPortletEntityId.getUserId();

                portletEntity =
                        this.portletEntityDao.getPortletEntity(localLayoutNodeId, localUserId);
            } else {
                portletEntity = this.portletEntityDao.getPortletEntity(portletEntityId);
            }
        } else {
            portletEntity = this.portletEntityDao.getPortletEntity(layoutNodeId, userId);
        }

        // Found a persistent entity, wrap it to make the id consistent between the persistent and
        // session stored entities
        if (portletEntity != null) {
            final IPortletEntityId consistentPortletEntityId =
                    this.createConsistentPortletEntityId(portletEntity);

            final IPortletEntity anonPortletEntity = portletEntity;

            // Stick the entity wrapper in the request map (if it wasn't already added by another
            // thread)
            portletEntity =
                    portletEntityCache.storeIfAbsentEntity(
                            consistentPortletEntityId,
                            new Function<IPortletEntityId, IPortletEntity>() {
                                @Override
                                public IPortletEntity apply(IPortletEntityId input) {
                                    logger.trace(
                                            "Found persistent IPortletEntity {}, mapped id to {}, caching the wrapper in the request",
                                            anonPortletEntity.getPortletEntityId(),
                                            consistentPortletEntityId);
                                    return new PersistentPortletEntityWrapper(
                                            anonPortletEntity, consistentPortletEntityId);
                                }
                            });

            return portletEntity;
        }

        // Didn't find an entity, just return null
        return null;
    }

    protected IPortletEntityId createConsistentPortletEntityId(IPortletEntity portletEntity) {
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final String layoutNodeId = portletEntity.getLayoutNodeId();
        final int userId = portletEntity.getUserId();
        return this.createConsistentPortletEntityId(portletDefinitionId, layoutNodeId, userId);
    }

    protected IPortletEntityId createConsistentPortletEntityId(
            IPortletDefinitionId portletDefinitionId, String layoutNodeId, int userId) {
        return new PortletEntityIdImpl(portletDefinitionId, layoutNodeId, userId);
    }

    protected IPortletEntityId parseConsistentPortletEntityId(
            HttpServletRequest request, String consistentEntityIdString) {
        Validate.notNull(consistentEntityIdString, "consistentEntityIdString can not be null");

        // Check in the cache first
        final Element element = this.entityIdParseCache.get(consistentEntityIdString);
        if (element != null) {
            final Object value = element.getObjectValue();
            if (value != null) {
                return (IPortletEntityId) value;
            }
        }

        if (!PortletEntityIdStringUtils.hasCorrectNumberOfParts(consistentEntityIdString)) {
            throw new IllegalArgumentException(
                    "consistentEntityIdString does not have 3 parts and is invalid: "
                            + consistentEntityIdString);
        }

        // Verify the portlet definition id
        final String portletDefinitionIdString =
                PortletEntityIdStringUtils.parsePortletDefinitionId(consistentEntityIdString);
        final IPortletDefinition portletDefinition =
                this.getPortletDefinition(request, portletDefinitionIdString);
        if (portletDefinition == null) {
            throw new IllegalArgumentException(
                    "No parent IPortletDefinition found for "
                            + portletDefinitionIdString
                            + " from entity id string: "
                            + consistentEntityIdString);
        }
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();

        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        // Verify non-delegate layout node id exists and is for a portlet
        final String layoutNodeId =
                PortletEntityIdStringUtils.parseLayoutNodeId(consistentEntityIdString);
        if (!PortletEntityIdStringUtils.isDelegateLayoutNode(layoutNodeId)) {
            final IUserLayoutNodeDescription node = userLayoutManager.getNode(layoutNodeId);
            if (node == null || node.getType() != LayoutNodeType.PORTLET) {
                throw new IllegalArgumentException(
                        "No portlet layout node found for "
                                + layoutNodeId
                                + " from entity id string: "
                                + consistentEntityIdString);
            }

            // TODO is this doable for delegation?
            // Verify the portlet definition matches
            final IUserLayoutChannelDescription portletNode = (IUserLayoutChannelDescription) node;
            final String channelPublishId = portletNode.getChannelPublishId();
            if (!portletDefinitionId.getStringId().equals(channelPublishId)) {
                throw new IllegalArgumentException(
                        "The portlet layout node found for "
                                + layoutNodeId
                                + " does not match the IPortletDefinitionId "
                                + portletDefinitionId
                                + " specified in entity id string: "
                                + consistentEntityIdString);
            }
        }

        // TODO when there is a JPA backed user dao actually verify this mapping
        // User just conver to an int
        final int userId;
        final String userIdAsString =
                PortletEntityIdStringUtils.parseUserIdAsString(consistentEntityIdString);
        try {
            userId = Integer.parseInt(userIdAsString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "The user id "
                            + userIdAsString
                            + " is not a valid integer from entity id string: "
                            + consistentEntityIdString,
                    e);
        }

        final IPortletEntityId portletEntityId =
                createConsistentPortletEntityId(portletDefinitionId, layoutNodeId, userId);

        // Cache the resolution
        this.entityIdParseCache.put(new Element(consistentEntityIdString, portletEntityId));

        return portletEntityId;
    }

    protected Lock createPortletEntityLock() {
        return new ReentrantLock(true);
    }

    protected ConcurrentMap<IPortletEntityId, Lock> getPortletEntityLockMap(
            HttpServletRequest request) {
        request = portalRequestUtils.getOriginalPortalRequest(request);
        final HttpSession session = request.getSession();
        return PortalWebUtils.getMapSessionAttribute(session, PORTLET_ENTITY_LOCK_MAP_ATTRIBUTE);
    }

    protected PortletEntityCache<IPortletEntity> getPortletEntityMap(HttpServletRequest request) {
        request = portalRequestUtils.getOriginalPortletOrPortalRequest(request);

        // create the thread specific cache name
        final String entityMapAttribute = PORTLET_ENTITY_ATTRIBUTE + Thread.currentThread().getId();

        // No need to do this in a request attribute mutex since the map is scoped to a specific
        // thread
        @SuppressWarnings("unchecked")
        PortletEntityCache<IPortletEntity> cache =
                (PortletEntityCache<IPortletEntity>) request.getAttribute(entityMapAttribute);
        if (cache == null) {
            cache = new PortletEntityCache<IPortletEntity>(false);
            request.setAttribute(PORTLET_ENTITY_ATTRIBUTE, cache);
        }

        return cache;
    }

    private PortletEntityData convertPortletEntityDataStrToPortletEntityData(
            String portletEntityDataStr) {
        if (StringUtils.isBlank(portletEntityDataStr)) {
            return null;
        }

        try {
            TypeReference<PortletEntityData> typeRef = new TypeReference<PortletEntityData>() {};
            PortletEntityData portletEntityData = mapper.readValue(portletEntityDataStr, typeRef);
            return portletEntityData;
        } catch (JsonMappingException e1) {
            logger.error("JsonMappingException [" + e1.getMessage() + "]", e1);
        } catch (JsonProcessingException e1) {
            logger.error("JsonProcessingException [" + e1.getMessage() + "]", e1);
        }
        return null;
    }

    // This method must be called any time PortletEntityData is removed from the cache.
    // This way, when the cache is needed, the removed entity won't be added back into
    // the cache.
    private void removePortletEntityDataFromSesssion(
            HttpServletRequest request, IPortletEntityId portletEntityId) {
        if (portletEntityId == null) {
            logger.debug("portletEntityId is null, returning");
            return;
        }
        String portletEntityIdStr = portletEntityId.getStringId();
        String key = PORTLET_ENTITY_DATA_ATTRIBUTE + ":" + portletEntityIdStr;
        HttpSession session = request.getSession();
        session.removeAttribute(key);
    }

    // This method must be called any time PortletEntityData is stored to the cache.
    // This way, when the cache is needed, it can rebuild the PortletWindowData objects
    // from the session
    private void storeEntityDataMapToSession(
            HttpServletRequest request, PortletEntityData portletEntityData) {
        if (portletEntityData == null) {
            logger.warn("portletEntityData is null, returning without saving to session");
            return;
        }
        final IPortletEntityId portletEntityId = portletEntityData.getPortletEntityId();
        String portletEntityIdStr = portletEntityId.getStringId();
        try {
            String key = PORTLET_ENTITY_DATA_ATTRIBUTE + ":" + portletEntityIdStr;
            String portletEntityDataStr = mapper.writeValueAsString(portletEntityData);
            HttpSession session = request.getSession();
            session.setAttribute(key, portletEntityDataStr);
        } catch (JsonProcessingException e) {
            logger.error("unable to convert portletEntityData to string [" + e.getMessage(), e);
        }
    }

    protected PortletEntityCache<PortletEntityData> getPortletEntityDataMap(
            HttpServletRequest request) {
        request = portalRequestUtils.getOriginalPortalRequest(request);
        final HttpSession session = request.getSession();
        boolean enableRedisson = true;
        final Object mutex = WebUtils.getSessionMutex(session);
        synchronized (mutex) {
            if (!enableRedisson) {
                @SuppressWarnings("unchecked")
                PortletEntityCache<PortletEntityData> cache =
                        (PortletEntityCache<PortletEntityData>)
                                session.getAttribute(PORTLET_ENTITY_DATA_ATTRIBUTE);
                if (cache == null) {
                    cache = new PortletEntityCache<PortletEntityData>();
                    session.setAttribute(PORTLET_ENTITY_DATA_ATTRIBUTE, cache);
                }
                return cache;
            } else {
                PortletEntityCache<PortletEntityData> cache =
                        new PortletEntityCache<PortletEntityData>();
                Enumeration<String> portletEntityStrings = session.getAttributeNames();
                while (portletEntityStrings.hasMoreElements()) {
                    String name = portletEntityStrings.nextElement();
                    if (name.startsWith(PORTLET_ENTITY_DATA_ATTRIBUTE + ":")) {
                        String portletEntityDataStr = (String) session.getAttribute(name);
                        PortletEntityData windowData =
                                convertPortletEntityDataStrToPortletEntityData(
                                        portletEntityDataStr);
                        if (windowData != null) {
                            cache.storeEntity(windowData);
                        }
                    }
                }
                return cache;
            }
        }
    }

    protected IPortletEntity wrapPortletEntityData(final PortletEntityData portletEntityData) {
        final IPortletDefinitionId portletDefinitionId = portletEntityData.getPortletDefinitionId();
        final IPortletDefinition portletDefinition =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
        return new SessionPortletEntityImpl(portletDefinition, portletEntityData);
    }

    @Override
    public boolean shouldBePersisted(IPortletEntity portletEntity) {
        // Delegate entities should NEVER be persisted
        final String layoutNodeId = portletEntity.getLayoutNodeId();
        if (PortletEntityIdStringUtils.isDelegateLayoutNode(layoutNodeId)) {
            return false;
        }

        // Only non delegate entities with preferences or a non-null window state should be
        // persisted
        final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
        return !CollectionUtils.isEmpty(preferences) || !portletEntity.getWindowStates().isEmpty();
    }

    private static final IPortletDefinition NO_PERMISSION_PORTLET_DEFINITION =
            new IPortletDefinition() {
                @Override
                public String getDataTitle() {
                    return null;
                }

                @Override
                public String getDataId() {
                    return null;
                }

                @Override
                public String getDataDescription() {
                    return null;
                }

                @Override
                public void setType(IPortletType channelType) {}

                @Override
                public void setTitle(String title) {}

                @Override
                public void setTimeout(int timeout) {}

                @Override
                public void setResourceTimeout(Integer resourceTimeout) {}

                @Override
                public void setRenderTimeout(Integer renderTimeout) {}

                @Override
                public boolean setPortletPreferences(List<IPortletPreference> portletPreferences) {
                    return false;
                }

                @Override
                public void setParameters(Set<IPortletDefinitionParameter> parameters) {}

                @Override
                public void setName(String name) {}

                @Override
                public void setFName(String fname) {}

                @Override
                public void setEventTimeout(Integer eventTimeout) {}

                @Override
                public void setDescription(String descr) {}

                @Override
                public void setActionTimeout(Integer actionTimeout) {}

                @Override
                public void removeParameter(String name) {}

                @Override
                public void removeParameter(IPortletDefinitionParameter parameter) {}

                @Override
                public IPortletType getType() {
                    return null;
                }

                @Override
                public String getTitle(String locale) {
                    return null;
                }

                @Override
                public String getAlternativeMaximizedLink() {
                    return null;
                }

                @Override
                public String getAlternativeMaximizedLinkTarget() {
                    return null;
                }

                @Override
                public String getTitle() {
                    return null;
                }

                @Override
                public int getTimeout() {
                    return 0;
                }

                @Override
                public Integer getResourceTimeout() {
                    return null;
                }

                @Override
                public Integer getRenderTimeout() {
                    return null;
                }

                @Override
                public List<IPortletPreference> getPortletPreferences() {
                    return null;
                }

                @Override
                public IPortletDescriptorKey getPortletDescriptorKey() {
                    return null;
                }

                @Override
                public IPortletDefinitionId getPortletDefinitionId() {
                    return null;
                }

                @Override
                public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
                    return null;
                }

                @Override
                public Set<IPortletDefinitionParameter> getParameters() {
                    return null;
                }

                @Override
                public IPortletDefinitionParameter getParameter(String key) {
                    return null;
                }

                @Override
                public String getName(String locale) {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public PortletLifecycleState getLifecycleState() {
                    return null;
                }

                @Override
                public void updateLifecycleState(
                        PortletLifecycleState lifecycleState, IPerson user) {}

                @Override
                public void updateLifecycleState(
                        PortletLifecycleState lifecycleState, IPerson user, Date timestamp) {}

                @Override
                public List<IPortletLifecycleEntry> getLifecycle() {
                    return null;
                }

                @Override
                public void clearLifecycle() {}

                @Override
                public String getFName() {
                    return null;
                }

                @Override
                public Integer getEventTimeout() {
                    return null;
                }

                @Override
                public EntityIdentifier getEntityIdentifier() {
                    return null;
                }

                @Override
                public String getDescription(String locale) {
                    return null;
                }

                @Override
                public String getDescription() {
                    return null;
                }

                @Override
                public Integer getActionTimeout() {
                    return null;
                }

                @Override
                public void addParameter(String name, String value) {}

                @Override
                public void addParameter(IPortletDefinitionParameter parameter) {}

                @Override
                public void addLocalizedTitle(String locale, String chanTitle) {}

                @Override
                public void addLocalizedName(String locale, String chanName) {}

                @Override
                public void addLocalizedDescription(String locale, String chanDesc) {}

                @Override
                public Double getRating() {
                    return null;
                }

                @Override
                public void setRating(Double rating) {}

                @Override
                public Long getUsersRated() {
                    return null;
                }

                @Override
                public void setUsersRated(Long usersRated) {}

                @Override
                public String getTarget() {
                    return null;
                }
            };
}
