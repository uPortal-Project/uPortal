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
    public IBasicEntity get(Class<? extends IBasicEntity> type, String key) throws CachingException;

    /**
     * Removes the cached entity identified by type and key from the cache
     * and notifies peer caches.
     * @param type Class
     * @param key String
     * @exception CachingException
     */
    public void remove(Class<? extends IBasicEntity> type, String key) throws CachingException;

    /**
     * Updates the entity in the cache and notifies peer caches.
     * @param ent org.jasig.portal.concurrency.IBasicEntity
     * @exception org.jasig.portal.concurrency.CachingException
     */
    public void update(IBasicEntity ent) throws CachingException;
}
