/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency;

import org.jasig.portal.IBasicEntity;

/**
  * Defines an api for a caching service that caches and retrieves
  * <code>IBasicEntities</code>.  Cached entities of a given type are
  * stored in an <code>IEntityCache</code>.  The service manages
  * access to these caches and is respon sible for initiating any
  * cache clean up or invalidation.
  * <p>
  * The actual caching api is minimal:
  * <p>
  * <code>
  *      void add(IBasicEntity entity);<br>
  *      IBasicEntity get(Class type, String key);<br>
  *      void remove(Class type, String key);<br>
  *      void update(IBasicEntity entity);<br>
  * </code>
  * <p>
  *
  * @author Dan Ellentuck
  * @version $Revision$
  *
  * @see org.jasig.portal.IBasicEntity
  * @see org.jasig.portal.concurrency.IEntityCache
  *
*/
public interface IEntityCachingService {

/**
 * Adds the entity to the cache.
 * @param ent org.jasig.portal.concurrency.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void add(IBasicEntity ent) throws CachingException;
/**
 * Returns the cached entity identified by type and key.
 * @param type Class
 * @param key String
 * @return IBasicEntity entity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IBasicEntity get(Class type, String key) throws CachingException;
/**
 * Removes the cached entity identified by type and key from the cache
 * and notifies peer caches.
 * @param type Class
 * @param key String
 * @exception CachingException
 */
public void remove(Class type, String key) throws CachingException;
/**
 * Updates the entity in the cache and notifies peer caches.
 * @param ent org.jasig.portal.concurrency.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void update(IBasicEntity ent) throws CachingException;
}
