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
  * Defines the api for a cache that caches <code>IBasicEntities</code>
  * of a single type.
  *
  * @author Dan Ellentuck
  * @version $Revision$
  */
public interface IEntityCache {
    
    /**
     * @param entity - the entity to be cached.
     */
    public void add(IBasicEntity entity) throws CachingException;
    
    /**
     * Purge stale entries from the cache.
     * @deprecated This is the responsibility of the cache or cache manager code
     */
    @Deprecated
    public void cleanupCache() throws CachingException;
    
    /**
     * Remove all entries from the cache.
     */
    public void clearCache() throws CachingException;
    
    /**
     * @param key the key of the entity.
     * @return org.jasig.portal.concurrency.IBasicEntity
     */
    public IBasicEntity get(String key);
    
    /**
     * @see org.jasig.portal.EntityTypes for known types.
     * @return java.lang.Class
     */
    public Class<? extends IBasicEntity> getEntityType();
    
    /**
     * @param entityKey - the key of the entity to be un-cached.
     */
    public void remove(String entityKey) throws CachingException;
    
    /**
     * Answers the number of entries in the cache. May return -1 if the cache does not make this
     * information avaialble
     */
    public int size();
    
    /**
     * @param entity - the entity to be updated in the cache.
     */
    public void update(IBasicEntity entity) throws CachingException;

}
