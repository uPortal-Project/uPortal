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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.MapMaker;

/**
 * Used to track keys by session id and portlet window id for a cache.
 * Clients need to call {@link PrivatePortletCacheKeyTracker#initPrivateKeyCache(HttpSession)} on a session
 * before keys will be tracked for that session. At any point clients can call
 * {@link PrivatePortletCacheKeyTracker#getCacheKeys(HttpSession, IPortletWindowId)}
 * to get all of the keys in the cache for that portlet window id
 */
final class PrivatePortletCacheKeyTracker extends CacheEventListenerAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Map from {@link HttpSession#getId()} to LoadingCache of private portlet cache keys.
     * Weak values are used so that when the {@link HttpSession} that holds the LoadingCache
     * is invalidated the reference is freed
     */
    private final Map<String, LoadingCache<IPortletWindowId, Set<PrivatePortletCacheKey>>> privatePortletCacheKeysBySession = new MapMaker()
            .weakValues().makeMap();
    private final String sessionKey;
    
    
    public PrivatePortletCacheKeyTracker(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void initPrivateKeyCache(HttpSession session) {
        //Sync to make sure only one instance of the LoadingCache is created per session
        synchronized (WebUtils.getSessionMutex(session)) {
            if (session.getAttribute(sessionKey) == null) {
                final LoadingCache<IPortletWindowId, Set<PrivatePortletCacheKey>> privateCacheKeysMap = CacheBuilder
                        .newBuilder().build(new CacheLoader<IPortletWindowId, Set<PrivatePortletCacheKey>>() {
                            @Override
                            public Set<PrivatePortletCacheKey> load(IPortletWindowId key) throws Exception {
                                final ConcurrentHashMap<PrivatePortletCacheKey, Boolean> keyMap = new ConcurrentHashMap<PrivatePortletCacheKey, Boolean>();
                                return Collections.newSetFromMap(keyMap);
                            }
                        });

                final String sessionId = session.getId();
                
                session.setAttribute(sessionKey, privateCacheKeysMap);
                privatePortletCacheKeysBySession.put(sessionId, privateCacheKeysMap);
                logger.debug("Initiated key tracking for session {} and tracker {}", sessionId, this.sessionKey);
            }
        }
    }
    
    public void destroyPrivateKeyCache(HttpSession session) {
        final String sessionId = session.getId();
        privatePortletCacheKeysBySession.remove(sessionId);
        logger.debug("Removed all cache keys for session {} to tracker {}", sessionId, this.sessionKey);
    }

    public Set<PrivatePortletCacheKey> getCacheKeys(HttpSession session, IPortletWindowId portletWindowId) {
        final LoadingCache<IPortletWindowId, Set<PrivatePortletCacheKey>> privatePortletCacheKeys = privatePortletCacheKeysBySession.get(session.getId());
        if (privatePortletCacheKeys == null) {
            return Collections.emptySet();
        }
        
        final Set<PrivatePortletCacheKey> keySet = privatePortletCacheKeys.getIfPresent(portletWindowId);
        if (keySet == null) {
            return Collections.emptySet();
        }

        return keySet;
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        final PrivatePortletCacheKey key = (PrivatePortletCacheKey) element.getKey();
        
        final LoadingCache<IPortletWindowId, Set<PrivatePortletCacheKey>> privatePortletCacheKeys = privatePortletCacheKeysBySession.get(key.getSessionId());
        if (privatePortletCacheKeys != null) {
            privatePortletCacheKeys.getUnchecked(key.getPortletWindowId()).add(key);
            logger.debug("Added cache key {} to tracker {}", key, this.sessionKey);
        }
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        removeEntry(element);
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        removeEntry(element);
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        removeEntry(element);
    }

    protected void removeEntry(Element element) {
        final PrivatePortletCacheKey key = (PrivatePortletCacheKey) element.getKey();
        
        final LoadingCache<IPortletWindowId, Set<PrivatePortletCacheKey>> privatePortletCacheKeys = privatePortletCacheKeysBySession.get(key.getSessionId());
        if (privatePortletCacheKeys != null) {
            final Set<PrivatePortletCacheKey> keySet = privatePortletCacheKeys.getIfPresent(key.getPortletWindowId());
            if (keySet != null) {
                logger.debug("Removed cache key {} from tracker {}", key, this.sessionKey);
                keySet.remove(key);
            }
        }
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        for (final LoadingCache<IPortletWindowId, Set<PrivatePortletCacheKey>> privatePortletCacheKeys : this.privatePortletCacheKeysBySession.values()) {
            privatePortletCacheKeys.invalidateAll();
        }
        this.privatePortletCacheKeysBySession.clear();
        
        logger.debug("Removed all cache keys tracker {}", this.sessionKey);
    }
}