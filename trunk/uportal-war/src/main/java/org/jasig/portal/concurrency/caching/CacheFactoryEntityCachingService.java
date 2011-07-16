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

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.jasig.portal.utils.cache.CacheFactory;
import org.jasig.portal.utils.threading.MapCachingDoubleCheckedCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates {@link MapBackedEntityCache} instances that wrap {@link Map} caches retrieved from the {@link CacheFactory}
 * service. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("entityCachingService")
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
    @Autowired
    public void setCacheFactory(CacheFactory cacheFactory) {
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
