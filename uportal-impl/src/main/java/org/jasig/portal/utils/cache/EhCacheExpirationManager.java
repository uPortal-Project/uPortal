/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
