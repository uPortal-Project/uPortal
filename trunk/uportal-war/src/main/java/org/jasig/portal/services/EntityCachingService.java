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

package org.jasig.portal.services;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCachingService;
import org.jasig.portal.spring.locator.EntityCachingServiceLocator;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;

/**
  * This class presents a facade for the IEntityCachingService implementation
  * that lets clients cache and retrieve <code>IBasicEntities</code>.  It
  * hides such details as the physical structure of the cache and whether
  * it is running in a multi- or single-JVM environment.
  * <p>
  * An <code>IBasicEntity</code> can answer its type and key.  (See
  * org.jasig.portal.groups.EntityTypes).
  * <p>
  * Caching consists of asking the service to add, retrieve, update and
  * remove elements from the cache, e.g.,
  * <p>
  * <code>
  *       // Retrieve the entity from its store:<br>
  *       Class type = getEntityClass();<br>
  *       String key = getEntityKey();<br>
  *       IBasicEntity ent = findEntity(key); <br>
  *        ... <br>
  *       // Cache the entity:<br>
  *       EntityCachingService.add(ent);<br>
  *        ... <br>
  *       // Retrieve the entity from the cache:<br>
  *       IEntity aCopy = EntityCachingService.get(type, key);<br>
  *        ...<br>
  *       // Update the entity and then:<br>
  *       EntityCachingService..update(aCopy);    // notifies peer caches.<br>
  *        ...<br>
  *       // Or delete the entity and:<br>
  *       EntityCachingService.remove(type, key); // notifies peer caches.<br>
  * </code>
  * <p>
  * @author  Dan Ellentuck
  * @version $Revision$
  * @deprecated Where possible the Spring managed {@link IEntityCachingService} bean should be injected instead of using this lookup class.
  */
@Deprecated
public class EntityCachingService implements IEntityCachingService {
    // Singleton instance of the bootstrap class:
    private static final SingletonDoubleCheckedCreator<EntityCachingService> instanceHolder = new SingletonDoubleCheckedCreator<EntityCachingService>() {
        @Override
        protected EntityCachingService createSingleton(Object... args) {
            return new EntityCachingService();
        }
    };

    /**
     * @deprecated Use {@link #getEntityCachingService()} instead.
     */
    @Deprecated
    public static EntityCachingService instance() throws CachingException {
        return instanceHolder.get();
    }
    
    public static IEntityCachingService getEntityCachingService() {
        return instanceHolder.get();
    }


    /** Creates new EntityLockService */
    private EntityCachingService() throws CachingException {
        super();
    }

    /**
     * Adds the entity to the cache.
     * @param ent org.jasig.portal.IBasicEntity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void add(IBasicEntity ent) throws CachingException {
        EntityCachingServiceLocator.getEntityCachingService().add(ent);
    }

    /**
     * Returns the cached entity identified by type and key.
     * @param type Class
     * @param key String
     * @return IBasicEntity entity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public IBasicEntity get(Class<? extends IBasicEntity> type, String key) throws CachingException {
        return EntityCachingServiceLocator.getEntityCachingService().get(type, key);
    }

    /**
     * Returns the cached entity referred to by entityID.
     * @param entityID entity identifier
     * @return IBasicEntity entity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public IBasicEntity get(EntityIdentifier entityID) throws CachingException {
        return EntityCachingServiceLocator.getEntityCachingService().get(entityID.getType(), entityID.getKey());
    }

    /**
     * Removes the entity identified by type and key from the cache and notifies
     * peer caches.
     * @param type Class
     * @param key String
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void remove(Class<? extends IBasicEntity> type, String key) throws CachingException {
        EntityCachingServiceLocator.getEntityCachingService().remove(type, key);
    }

    /**
     * Removes the entity referred to by entityID from the cache and notifies peer
     * caches.
     * @param entityID
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void remove(EntityIdentifier entityID) throws CachingException {
        this.remove(entityID.getType(), entityID.getKey());
    }

    /**
     * Removes the <code>IBasicEntity</code> from the cache and notifies peer
     * caches.
     * @param ent org.jasig.portal.IBasicEntity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void remove(IBasicEntity ent) throws CachingException {
        this.remove(ent.getEntityIdentifier());
    }

    /**
     * Updates the entity in the cache and notifies peer caches.
     * @param ent org.jasig.portal.concurrency.IBasicEntity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void update(IBasicEntity ent) throws CachingException {
        EntityCachingServiceLocator.getEntityCachingService().update(ent);
    }
}
