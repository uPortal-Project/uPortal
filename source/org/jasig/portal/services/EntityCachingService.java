/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCachingService;
import org.jasig.portal.concurrency.IEntityCachingServiceFactory;
import org.jasig.portal.properties.PropertiesManager;

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
  */

public class EntityCachingService
{
    
    private static final Log log = LogFactory.getLog(EntityCachingService.class);
    // Singleton instance of the bootstrap class:
    private static EntityCachingService instance = null;
    // The caching service:
    private IEntityCachingService cache = null;
    /** Creates new EntityLockService */
    private EntityCachingService() throws CachingException
    {
        super();
        initialize();
    }
/**
 * Adds the entity to the cache.
 * @param ent org.jasig.portal.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void add(IBasicEntity ent) throws CachingException
{
    cache.add(ent);
}
/**
 * Returns the cached entity identified by type and key.
 * @param type Class
 * @param key String
 * @return IBasicEntity entity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IBasicEntity get(Class type, String key) throws CachingException
{
    return cache.get(type, key);
}
/**
 * Returns the cached entity referred to by entityID.
 * @param entityID entity identifier
 * @return IBasicEntity entity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IBasicEntity get(EntityIdentifier entityID) throws CachingException
{
    return cache.get(entityID.getType(), entityID.getKey());
}
/**
 * @exception org.jasig.portal.concurrency.CachingException
 */
private void initialize() throws CachingException
{
    String eMsg = null;
    String factoryName =
        PropertiesManager.getProperty("org.jasig.portal.concurrency.IEntityCachingServiceFactory");

    if ( factoryName == null )
    {
        eMsg = "EntityCachingService.initialize(): No entry for org.jasig.portal.concurrency.caching.IEntityCachingServiceFactory in portal.properties.";
        log.error( eMsg);
        throw new CachingException(eMsg);
    }

    try
    {
        IEntityCachingServiceFactory cachingServiceFactory =
            (IEntityCachingServiceFactory)Class.forName(factoryName).newInstance();
        cache = cachingServiceFactory.newCachingService();
    }
    catch (Exception e)
    {
        eMsg = "EntityCachingService.initialize(): Problem creating entity caching service... " + e.getMessage();
        log.error( eMsg);
        throw new CachingException(eMsg);
    }
}
    public static synchronized EntityCachingService instance() throws CachingException {
        if ( instance==null ) {
            instance = new EntityCachingService();
        }
        return instance;
    }
/**
 * Removes the entity identified by type and key from the cache and notifies
 * peer caches.
 * @param type Class
 * @param key String
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void remove(Class type, String key) throws CachingException
{
    cache.remove(type, key);
}
/**
 * Removes the entity referred to by entityID from the cache and notifies peer
 * caches.
 * @param entityID
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void remove(EntityIdentifier entityID) throws CachingException
{
    remove(entityID.getType(), entityID.getKey());
}
/**
 * Removes the <code>IBasicEntity</code> from the cache and notifies peer
 * caches.
 * @param ent org.jasig.portal.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void remove(IBasicEntity ent) throws CachingException
{
    remove(ent.getEntityIdentifier());
}
public static synchronized EntityCachingService start() throws CachingException
{
    return instance();
}
/**
 * Updates the entity in the cache and notifies peer caches.
 * @param ent org.jasig.portal.concurrency.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void update(IBasicEntity ent) throws CachingException
{
    cache.update(ent);
}
}
