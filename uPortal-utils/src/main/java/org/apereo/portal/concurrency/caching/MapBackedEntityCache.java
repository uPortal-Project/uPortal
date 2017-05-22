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
package org.apereo.portal.concurrency.caching;

import java.io.Serializable;
import java.util.Map;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IBasicEntity;
import org.apereo.portal.concurrency.CachingException;
import org.apereo.portal.concurrency.IEntityCache;

/**
 * Implements the uPortal caching API to wrap a Map based cache
 *
 */
public class MapBackedEntityCache implements IEntityCache {
    private final Map<Serializable, IBasicEntity> cache;
    private final Class<? extends IBasicEntity> entityType;

    public MapBackedEntityCache(
            Map<Serializable, IBasicEntity> cache, Class<? extends IBasicEntity> entityType) {
        this.cache = cache;
        this.entityType = entityType;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#add(org.apereo.portal.IBasicEntity)
     */
    public void add(IBasicEntity entity) throws CachingException {
        final EntityIdentifier entityIdentifier = entity.getEntityIdentifier();
        final Class<? extends IBasicEntity> addType = entityIdentifier.getType();

        if (!this.entityType.isAssignableFrom(addType)) {
            throw new CachingException(
                    "Problem adding "
                            + entity
                            + ": entity type '"
                            + addType
                            + "' is incompatible with cache type '"
                            + this.entityType
                            + "'.");
        }

        this.cache.put(entityIdentifier.getKey(), entity);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#clearCache()
     */
    public void clearCache() throws CachingException {
        this.cache.clear();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#get(java.lang.String)
     */
    public IBasicEntity get(String key) {
        return this.cache.get(key);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#getEntityType()
     */
    public Class<? extends IBasicEntity> getEntityType() {
        return this.entityType;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#remove(java.lang.String)
     */
    public void remove(String entityKey) throws CachingException {
        this.cache.remove(entityKey);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#update(org.apereo.portal.IBasicEntity)
     */
    public void update(IBasicEntity entity) throws CachingException {
        this.add(entity);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#size()
     */
    public int size() {
        return this.cache.size();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.concurrency.IEntityCache#cleanupCache()
     */
    @Deprecated
    public void cleanupCache() throws CachingException {
        //NOOP
    }
}
