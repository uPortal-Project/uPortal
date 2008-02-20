/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCachingService;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.context.ApplicationContext;

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

    // The caching service:
    private final IEntityCachingService cache;

    /** Creates new EntityLockService */
    private EntityCachingService() throws CachingException {
        super();

        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        this.cache = (IEntityCachingService) applicationContext.getBean("entityCachingService", IEntityCachingService.class);
    }

    /**
     * Adds the entity to the cache.
     * @param ent org.jasig.portal.IBasicEntity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void add(IBasicEntity ent) throws CachingException {
        this.cache.add(ent);
    }

    /**
     * Returns the cached entity identified by type and key.
     * @param type Class
     * @param key String
     * @return IBasicEntity entity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public IBasicEntity get(Class<? extends IBasicEntity> type, String key) throws CachingException {
        return this.cache.get(type, key);
    }

    /**
     * Returns the cached entity referred to by entityID.
     * @param entityID entity identifier
     * @return IBasicEntity entity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public IBasicEntity get(EntityIdentifier entityID) throws CachingException {
        return this.cache.get(entityID.getType(), entityID.getKey());
    }

    /**
     * Removes the entity identified by type and key from the cache and notifies
     * peer caches.
     * @param type Class
     * @param key String
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void remove(Class<? extends IBasicEntity> type, String key) throws CachingException {
        this.cache.remove(type, key);
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
        this.cache.update(ent);
    }
}
