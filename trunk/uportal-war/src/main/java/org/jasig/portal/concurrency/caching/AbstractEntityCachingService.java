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

package org.jasig.portal.concurrency.caching;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.jasig.portal.concurrency.IEntityCachingService;

/**
 * Provides common {@link IEntityCachingService} logic
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractEntityCachingService implements IEntityCachingService {
    
    /**
     * Retrieves the {@link IEntityCache} to store the specified entityType in.
     * 
     * @param entityType The type to retrieve the cache for
     * @return The cache for the specified type, should never be null.
     * @throws CachingException If no cache can be found/created for the specified entityType.
     * @throws IllegalArgumentException If entityType is null.
     */
    protected abstract IEntityCache getCache(Class<? extends IBasicEntity> entityType) throws CachingException; 
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCachingService#add(org.jasig.portal.IBasicEntity)
     */
    public void add(IBasicEntity entity) throws CachingException {
        final EntityIdentifier entityIdentifier = entity.getEntityIdentifier();
        final Class<? extends IBasicEntity> entityType = entityIdentifier.getType();
        final IEntityCache entityCache = this.getCache(entityType);
        
        entityCache.add(entity);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCachingService#get(java.lang.Class, java.lang.String)
     */
    public IBasicEntity get(Class<? extends IBasicEntity> entityType, String key) throws CachingException {
        final IEntityCache entityCache = this.getCache(entityType);
        return entityCache.get(key);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCachingService#remove(java.lang.Class, java.lang.String)
     */
    public void remove(Class<? extends IBasicEntity> entityType, String key) throws CachingException {
        final IEntityCache entityCache = this.getCache(entityType);
        entityCache.remove(key);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCachingService#update(org.jasig.portal.IBasicEntity)
     */
    public void update(IBasicEntity entity) throws CachingException {
        this.add(entity);
    }
}
