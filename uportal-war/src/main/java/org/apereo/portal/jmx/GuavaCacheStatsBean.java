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
package org.apereo.portal.jmx;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import org.apereo.portal.character.stream.events.CharacterDataEventImpl;

/**
 * Base bean to expose a {@link CacheStats} object
 *
 */
public abstract class GuavaCacheStatsBean {
    private volatile Cache<?, ?> cache = null;
    private volatile CacheStats cacheStats = null;
    private volatile long size;
    private volatile long nextLoad;
    private volatile long loadIterval = 100;

    protected abstract Cache<?, ?> getCache();

    private CacheStats getCachedCacheStats() {
        if (cache == null || nextLoad <= System.currentTimeMillis()) {
            cache = getCache();
            cacheStats = cache.stats();
            size = cache.size();
            nextLoad = System.currentTimeMillis() + loadIterval;
        }

        return cacheStats;
    }

    public final long getLoadIterval() {
        return loadIterval;
    }

    /**
     * Number of milliseconds between calls to {@link CharacterDataEventImpl#getEventCacheStats()}
     */
    public final void setLoadIterval(long loadIterval) {
        this.loadIterval = loadIterval;
    }

    public void cleanUp() {
        getCachedCacheStats();
        cache.cleanUp();
    }

    public final long getSize() {
        getCachedCacheStats();
        return size;
    }

    public final long getRequestCount() {
        return getCachedCacheStats().requestCount();
    }

    public final long getHitCount() {
        return getCachedCacheStats().hitCount();
    }

    public final double getHitRate() {
        return getCachedCacheStats().hitRate();
    }

    public final long getMissCount() {
        return getCachedCacheStats().missCount();
    }

    public final double getMissRate() {
        return getCachedCacheStats().missRate();
    }

    public final long getLoadCount() {
        return getCachedCacheStats().loadCount();
    }

    public final long getLoadSuccessCount() {
        return getCachedCacheStats().loadSuccessCount();
    }

    public final long getLoadExceptionCount() {
        return getCachedCacheStats().loadExceptionCount();
    }

    public final double getLoadExceptionRate() {
        return getCachedCacheStats().loadExceptionRate();
    }

    public final long getTotalLoadTime() {
        return getCachedCacheStats().totalLoadTime();
    }

    public final double getAverageLoadPenalty() {
        return getCachedCacheStats().averageLoadPenalty();
    }

    public final long getEvictionCount() {
        return getCachedCacheStats().evictionCount();
    }

    @Override
    public String toString() {
        return getCachedCacheStats().toString();
    }
}
