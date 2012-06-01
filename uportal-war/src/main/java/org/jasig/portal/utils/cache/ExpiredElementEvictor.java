package org.jasig.portal.utils.cache;

import java.util.concurrent.TimeUnit;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evicts expired elements from a {@link CacheManager}
 * 
 * @author Eric Dalquist
 */
public class ExpiredElementEvictor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Evicts expired elements from the {@link CacheManager}
     */
    public void evictExpiredElements() {
        final long startTime = System.nanoTime();

        final String[] cacheNames = this.cacheManager.getCacheNames();

        long evictedTotal = 0;
        for (String cacheName : cacheNames) {
            final Ehcache cache = this.cacheManager.getEhcache(cacheName);
            if (null != cache) {
                final long preEvictSize = cache.getMemoryStoreSize();
                final long evictStart = System.nanoTime();
                cache.evictExpiredElements();
                if (logger.isDebugEnabled()) {
                    final long evicted = preEvictSize - cache.getMemoryStoreSize();
                    evictedTotal += evicted;
                    logger.debug("Evicted " + evicted + " elements from cache '" + cacheName + "' in "
                            + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - evictStart) + " ms");
                }
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("No cache found with name " + cacheName);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Evicted " + evictedTotal + " elements from " + cacheNames.length + " caches in "
                    + this.cacheManager.getName() + " in "
                    + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
        }
    }
}
