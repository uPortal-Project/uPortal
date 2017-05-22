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
package org.apereo.portal.utils.cache;

import java.io.Serializable;
import java.util.Map;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.ObjectExistsException;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.utils.threading.MapCachingDoubleCheckedCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CacheFactory impl that provides Map instances that wrap a configured {@link CacheProviderFacade}.
 * This uses the {@link MapCacheProvider} to perform the wrapping. Refer to that class for which
 * operations on the {@link Map} interface are supported.
 *
 */
@Service("cacheFactory")
public class CacheProviderFactory implements CacheFactory {
    protected final Log logger = LogFactory.getLog(this.getClass());

    // Stores caches that are created with a soft reference to the cache Map to avoid re-creating wrapper objects without need
    private final MapCacheCreator mapCacheCreator = new MapCacheCreator();

    private CacheManager cacheManager;

    /** @param cacheManager the cacheManager to set */
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.cache.CacheFactory#getCache()
     */
    public <K extends Serializable, V> Map<K, V> getCache() {
        return this.getCache(DEFAULT);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.cache.CacheFactory#getCache(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <K extends Serializable, V> Map<K, V> getCache(String cacheName)
            throws IllegalArgumentException {
        return (Map<K, V>) this.mapCacheCreator.get(cacheName);
    }

    private class MapCacheCreator extends MapCachingDoubleCheckedCreator<String, Map<?, ?>> {
        @SuppressWarnings("unchecked")
        public MapCacheCreator() {
            super(new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT));
        }

        /* (non-Javadoc)
         * @see org.apereo.portal.utils.threading.MapCachingDoubleCheckedCreator#getKey(java.lang.Object[])
         */
        @Override
        protected String getKey(Object... args) {
            return (String) args[0];
        }

        /* (non-Javadoc)
         * @see org.apereo.portal.utils.threading.MapCachingDoubleCheckedCreator#createInternal(java.lang.Object, java.lang.Object[])
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Map<?, ?> createInternal(String cacheName, Object... args) {
            final Ehcache cache;
            if (cacheManager.cacheExists(cacheName)) {
                cache = cacheManager.getCache(cacheName);

                if (logger.isDebugEnabled()) {
                    logger.debug("Using existing EhCache for '" + cacheName + "'");
                }
            } else {
                try {
                    cacheManager.addCache(cacheName);
                } catch (ObjectExistsException oee) {
                    //Ignore, some other thread created the cache while we were trying to do the same thing
                }
                cache = cacheManager.getCache(cacheName);

                if (logger.isWarnEnabled()) {
                    logger.warn("Created new default EhCache for '" + cacheName + "'");
                }
            }

            return new MapCacheProvider(cache);
        }
    }
}
