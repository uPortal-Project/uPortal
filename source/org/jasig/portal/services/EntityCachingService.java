/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.services;

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
        LogService.log(LogService.ERROR, eMsg);
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
        LogService.log(LogService.ERROR, eMsg);
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
