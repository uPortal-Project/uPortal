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

package org.jasig.portal.utils.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EhCacheExpirationManager implements Runnable {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private CacheManager cacheManager;
    
    /**
     * @return the cacheManager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    /**
     * @param cacheManager the cacheManager to set
     */
    @Required
    public void setCacheManager(CacheManager cacheManager) {
        Validate.notNull(cacheManager);
        this.cacheManager = cacheManager;
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        final long startEvictions = System.currentTimeMillis();
        long evictions = 0;
        
        final String[] cacheNames = this.cacheManager.getCacheNames();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Found " + cacheNames.length + " caches to clean.");
        }
        
        for (final String cacheName : cacheNames) {
            final Ehcache cache = this.cacheManager.getEhcache(cacheName);
            
            final long preEvictSize = cache.getMemoryStoreSize();
            final long startEviction = System.currentTimeMillis();
            cache.evictExpiredElements();
            
            if (this.logger.isDebugEnabled()) {
                final long evicted = preEvictSize - cache.getMemoryStoreSize();
                evictions += evicted;
                this.logger.debug("Evicted " + evicted + " expired elements from cache '" + cacheName + "' in " + (System.currentTimeMillis() - startEviction) + "ms");
            }
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Scanned " + cacheNames.length + " caches and evicted " + evictions + " elements in " + (System.currentTimeMillis() - startEvictions) + "ms.");
        }
    }
}
