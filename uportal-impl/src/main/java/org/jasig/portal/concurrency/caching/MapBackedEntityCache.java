/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.concurrency.caching;

import java.io.Serializable;
import java.util.Map;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;

/**
 * Implements the uPortal caching API to wrap a Map based cache
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MapBackedEntityCache implements IEntityCache {
    private final Map<Serializable, IBasicEntity> cache;
    private final Class<? extends IBasicEntity> entityType;
    
    public MapBackedEntityCache(Map<Serializable, IBasicEntity> cache, Class<? extends IBasicEntity> entityType) {
        this.cache = cache;
        this.entityType = entityType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#add(org.jasig.portal.IBasicEntity)
     */
    public void add(IBasicEntity entity) throws CachingException {
        final EntityIdentifier entityIdentifier = entity.getEntityIdentifier();
        final Class<? extends IBasicEntity> addType = entityIdentifier.getType();
        
        if (!this.entityType.isAssignableFrom(addType)) {
            throw new CachingException("Problem adding " + entity + ": entity type '" + addType + "' is incompatible with cache type '" + this.entityType + "'.");
        }

        this.cache.put(entityIdentifier.getKey(), entity);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#clearCache()
     */
    public void clearCache() throws CachingException {
        this.cache.clear();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#get(java.lang.String)
     */
    public IBasicEntity get(String key) {
        return this.cache.get(key);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#getEntityType()
     */
    public Class<? extends IBasicEntity> getEntityType() {
        return this.entityType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#remove(java.lang.String)
     */
    public void remove(String entityKey) throws CachingException {
        this.cache.remove(entityKey);

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#update(org.jasig.portal.IBasicEntity)
     */
    public void update(IBasicEntity entity) throws CachingException {
        this.add(entity);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#size()
     */
    public int size() {
        return this.cache.size();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.IEntityCache#cleanupCache()
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public void cleanupCache() throws CachingException {
        //NOOP
    }
}
