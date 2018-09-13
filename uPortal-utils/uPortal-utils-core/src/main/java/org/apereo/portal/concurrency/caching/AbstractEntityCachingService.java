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

import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IBasicEntity;
import org.apereo.portal.concurrency.CachingException;
import org.apereo.portal.concurrency.IEntityCache;
import org.apereo.portal.concurrency.IEntityCachingService;

/** Provides common {@link IEntityCachingService} logic */
public abstract class AbstractEntityCachingService implements IEntityCachingService {

    /**
     * Retrieves the {@link IEntityCache} to store the specified entityType in.
     *
     * @param entityType The type to retrieve the cache for
     * @return The cache for the specified type, should never be null.
     * @throws CachingException If no cache can be found/created for the specified entityType.
     * @throws IllegalArgumentException If entityType is null.
     */
    protected abstract IEntityCache getCache(Class<? extends IBasicEntity> entityType)
            throws CachingException;

    @Override
    public void add(IBasicEntity entity) throws CachingException {
        final EntityIdentifier entityIdentifier = entity.getEntityIdentifier();
        final Class<? extends IBasicEntity> entityType = entityIdentifier.getType();
        final IEntityCache entityCache = this.getCache(entityType);

        entityCache.add(entity);
    }

    @Override
    public IBasicEntity get(Class<? extends IBasicEntity> entityType, String key)
            throws CachingException {
        final IEntityCache entityCache = this.getCache(entityType);
        return entityCache.get(key);
    }

    @Override
    public void remove(Class<? extends IBasicEntity> entityType, String key)
            throws CachingException {
        final IEntityCache entityCache = this.getCache(entityType);
        entityCache.remove(key);
    }

    @Override
    public void update(IBasicEntity entity) throws CachingException {
        this.add(entity);
    }
}
