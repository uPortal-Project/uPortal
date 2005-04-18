/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.whirlycott.cache.Cache;
import com.whirlycott.cache.CacheException;
import com.whirlycott.cache.CacheManager;

/**
 * Implementation of the <code>CacheFactory</code> that will return instances of a cache
 * backed by WhirlyCache.
 * 
 * <p>As WhirlyCache does not use the standard map API, these caches are wrapped by a Map.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 *
 */
public final class WhirlyCacheCacheFactory implements CacheFactory, InitializingBean, DisposableBean {
    
    /** Message to display when the cache is found and intanciated already. */
    private static final String LOG_MESSAGE_FOUND_IN_CACHE = " found in CacheFactory already, returning existing instance.";
    
    /** Message to display if the cache was not found in the map and we instanciate a new one. */
    private static final String LOG_MESSAGE_NOT_FOUND_IN_CACHE = " not found in CacheFactory, instanciating new instance.";
    
    /** Message to display if we cannot find the cache at all. */
    private static final String LOG_MESSAGE_INVALID = " is not a valid cache for this CacheFactory";
    
    /** Instance of Commons Logging for logging purposes */
    private static final Log log = LogFactory.getLog(WhirlyCacheCacheFactory.class);
    
    /** Instance of WhirlyCache manager in order to manage caches from this factory. */
    private final CacheManager cacheManager = CacheManager.getInstance();

    /** Map of caches so that we always return the same instance. */
    private final Map caches = new HashMap();

    public synchronized Map getCache(String cacheName) throws IllegalArgumentException {
        if (this.caches.containsKey(cacheName)) {
            log.debug(cacheName + LOG_MESSAGE_FOUND_IN_CACHE);
            return (Map) this.caches.get(cacheName);
        }
        
        try {
            log.debug(cacheName + LOG_MESSAGE_NOT_FOUND_IN_CACHE);
            final Map map = new WhirlyCacheMap(this.cacheManager.getCache(cacheName));
            this.caches.put(cacheName, map);
            return map;
        } catch (CacheException ce) {
            log.error(ce, ce);
            throw new IllegalArgumentException(cacheName + LOG_MESSAGE_INVALID);
        }
    }
    
    public synchronized Map getCache() throws IllegalArgumentException {
        return (Map) this.caches.get(DEFAULT);
    }

    public void afterPropertiesSet() throws Exception {
        this.caches.put(DEFAULT, new WhirlyCacheMap(this.cacheManager.getCache()));
    }

    public void destroy() throws Exception {
        log.info("Shutting down cacheManager...");
        this.cacheManager.shutdown();
    }
    
    protected static final class WhirlyCacheMap implements Map {
        private final Cache cache;
        
        protected WhirlyCacheMap(final Cache cache) {
            this.cache = cache;
        }
        public void clear() {
            this.cache.clear();
        }

        public boolean containsKey(final Object key) {
            return this.cache.retrieve(key) != null;
        }

        public boolean containsValue(final Object value) {
            throw new UnsupportedOperationException("containsValue is not supported on WhirlyCache backed maps.");
        }

        public Set entrySet() {
            throw new UnsupportedOperationException("entrySet() is not supported on WhirlyCache backed maps.");
        }

        public Object get(final Object key) {
            return this.cache.retrieve(key);
        }

        public boolean isEmpty() {
            return this.cache.size() == 0;
        }

        public Set keySet() {
            throw new UnsupportedOperationException("keySet() is not supported on WhirlyCache backed maps.");
        }

        public Object put(final Object key, final Object value) {
            this.cache.store(key, value);
            return value;
        }

        public void putAll(final Map map) {
            for (final Iterator iter = map.keySet().iterator(); iter.hasNext();) {
                final Object key = iter.next();
                final Object value = map.get(key);
                this.cache.store(key, value);
            }
        }

        public Object remove(final Object key) {
            return this.cache.remove(key);
        }

        public int size() {
            return this.cache.size();
        }

        public Collection values() {
            throw new UnsupportedOperationException("values() is not supported on WhirlyCache backed maps.");
        }
    }
}