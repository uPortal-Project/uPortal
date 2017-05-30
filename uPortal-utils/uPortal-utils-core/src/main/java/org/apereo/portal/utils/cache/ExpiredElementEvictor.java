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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evicts expired elements from a {@link CacheManager}
 *
 */
public class ExpiredElementEvictor {
    private final ReentrantLock evictLock = new ReentrantLock();

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /** Evicts expired elements from the {@link CacheManager} */
    public void evictExpiredElements() {
        long evictedTotal = 0;
        final long startTime;
        final String[] cacheNames;

        if (!evictLock.tryLock()) {
            //Lock is already held, skip eviction
            return;
        }
        try {
            startTime = System.nanoTime();
            cacheNames = this.cacheManager.getCacheNames();

            for (String cacheName : cacheNames) {
                final Ehcache cache = this.cacheManager.getEhcache(cacheName);
                if (null != cache) {
                    final long preEvictSize = cache.getMemoryStoreSize();
                    final long evictStart = System.nanoTime();
                    cache.evictExpiredElements();
                    if (logger.isDebugEnabled()) {
                        final long evicted = preEvictSize - cache.getMemoryStoreSize();
                        evictedTotal += evicted;
                        logger.debug(
                                "Evicted "
                                        + evicted
                                        + " elements from cache '"
                                        + cacheName
                                        + "' in "
                                        + TimeUnit.NANOSECONDS.toMillis(
                                                System.nanoTime() - evictStart)
                                        + " ms");
                    }
                } else if (logger.isDebugEnabled()) {
                    logger.debug("No cache found with name " + cacheName);
                }
            }
        } finally {
            this.evictLock.unlock();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Evicted "
                            + evictedTotal
                            + " elements from "
                            + cacheNames.length
                            + " caches in "
                            + this.cacheManager.getName()
                            + " in "
                            + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
                            + " ms");
        }
    }
}
