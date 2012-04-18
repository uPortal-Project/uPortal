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
package org.jasig.portal.portlet.container.cache;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Used to track keys by portlet definition id for a cache, at any point clients can call
 * {@link PublicPortletCacheKeyTracker#getCacheKeys(IPortletDefinitionId)} to get all of the keys in the cache
 * for that portlet definition id
 */
final class PublicPortletCacheKeyTracker extends CacheEventListenerAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final LoadingCache<IPortletDefinitionId, Set<PublicPortletCacheKey>> publicPortletCacheKeys = CacheBuilder
            .newBuilder().build(new CacheLoader<IPortletDefinitionId, Set<PublicPortletCacheKey>>() {
                @Override
                public Set<PublicPortletCacheKey> load(IPortletDefinitionId key) throws Exception {
                    final ConcurrentHashMap<PublicPortletCacheKey, Boolean> keyMap = new ConcurrentHashMap<PublicPortletCacheKey, Boolean>();
                    return Collections.newSetFromMap(keyMap);
                }
            });

    public Set<PublicPortletCacheKey> getCacheKeys(IPortletDefinitionId portletDefinitionId) {
        final Set<PublicPortletCacheKey> keySet = publicPortletCacheKeys.getIfPresent(portletDefinitionId);
        if (keySet == null) {
            return Collections.emptySet();
        }

        return keySet;
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        final PublicPortletCacheKey key = (PublicPortletCacheKey) element.getKey();
        publicPortletCacheKeys.getUnchecked(key.getPortletDefinitionId()).add(key);
        logger.debug("Added cache key {} to tracker for {}", key, cache.getName());
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        removeEntry(cache, element);
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        removeEntry(cache, element);
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        removeEntry(cache, element);
    }

    protected void removeEntry(Ehcache cache, Element element) {
        final PublicPortletCacheKey key = (PublicPortletCacheKey) element.getKey();
        final Set<PublicPortletCacheKey> keySet = publicPortletCacheKeys.getIfPresent(key.getPortletDefinitionId());
        if (keySet != null) {
            logger.debug("Removed cache key {} from tracker for {}", key, cache.getName());
            keySet.remove(key);
        }
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        this.publicPortletCacheKeys.invalidateAll();
        
        logger.debug("Removed all cache keys from tracker for {}", cache.getName());
    }
}