/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.concurrency.caching;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.Validate;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.jasig.portal.utils.cache.CacheFactory;
import org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator;
import org.springframework.beans.factory.annotation.Required;

/**
 * Creates {@link MapBackedEntityCache} instances that wrap {@link Map} caches retrieved from the {@link CacheFactory}
 * service. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheFactoryEntityCachingService extends AbstractEntityCachingService {
    private final EntityCacheCreator entityCacheCreator = new EntityCacheCreator();
    private CacheFactory cacheFactory;
    
    /**
     * @return the cacheFactory
     */
    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }
    /**
     * @param cacheFactory the cacheFactory to set
     */
    @Required
    public void setCacheFactory(CacheFactory cacheFactory) {
        Validate.notNull(cacheFactory, "cacheFactory can not be null");
        this.cacheFactory = cacheFactory;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.concurrency.caching.AbstractEntityCachingService#getCache(java.lang.Class)
     */
    @Override
    protected IEntityCache getCache(Class<? extends IBasicEntity> entityType) throws CachingException {
        return this.entityCacheCreator.get(entityType);
    }

    private class EntityCacheCreator extends MapCachingDoubleCheckedCreator<String, IEntityCache> {
        public EntityCacheCreator() {
            super(new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT));
        }
        
        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator#getKey(java.lang.Object[])
         */
        @SuppressWarnings("unchecked")
        @Override
        protected String getKey(Object... args) {
            final Class<? extends IBasicEntity> entityType = (Class<? extends IBasicEntity>) args[0];
            return entityType.getName();
        }
        
        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator#createInternal(java.lang.Object, java.lang.Object[])
         */
        @SuppressWarnings("unchecked")
        @Override
        protected IEntityCache createInternal(String typeName, Object... args) {
            final Map<Serializable, IBasicEntity> cacheMap = CacheFactoryEntityCachingService.this.cacheFactory.getCache(typeName);
            final Class<? extends IBasicEntity> entityType = (Class<? extends IBasicEntity>) args[0];
            return new MapBackedEntityCache(cacheMap, entityType);
        }
    }
}
