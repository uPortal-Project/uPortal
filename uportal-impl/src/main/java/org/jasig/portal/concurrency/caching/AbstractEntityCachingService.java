/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
