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

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.Cache;

public final class CacheStatistics implements CacheStatisticsMBean {
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong loadSuccessCount = new AtomicLong();
    private final AtomicLong loadExceptionCount = new AtomicLong();
    private final AtomicLong totalHitTime = new AtomicLong();
    private final AtomicLong totalLoadTime = new AtomicLong();
    private final AtomicLong totalExceptionTime = new AtomicLong();
    
    public final void recordHit(long time) {
        hitCount.incrementAndGet();
        totalHitTime.addAndGet(time);
    }
    public final void recordMissAndLoad(long time) {
        missCount.incrementAndGet();
        loadSuccessCount.incrementAndGet();
        totalLoadTime.addAndGet(time);
    }
    public final void recordMissAndException(long time) {
        missCount.incrementAndGet();
        loadExceptionCount.incrementAndGet();
        totalExceptionTime.addAndGet(time);
    }
    
    /**
     * Returns the number of times {@link Cache} lookup methods have returned either a cached or
     * uncached value. This is defined as {@code hitCount + missCount}.
     */
    @Override
    public long getRequestCount() {
        return hitCount.get() + missCount.get();
    }

    /**
     * Returns the number of times {@link Cache} lookup methods have returned a cached value.
     */
    @Override
    public long getHitCount() {
        return hitCount.get();
    }

    /**
     * Returns the ratio of cache requests which were hits. This is defined as
     * {@code hitCount / requestCount}, or {@code 1.0} when {@code requestCount == 0}.
     * Note that {@code hitRate + missRate =~ 1.0}.
     */
    @Override
    public double getHitRate() {
        long requestCount = getRequestCount();
        return (requestCount == 0) ? 1.0 : (double) hitCount.get() / requestCount;
    }

    /**
     * Returns the number of times {@link Cache} lookup methods have returned an uncached (newly
     * loaded) value, or null. Multiple concurrent calls to {@link Cache} lookup methods on an absent
     * value can result in multiple misses, all returning the results of a single cache load
     * operation.
     */
    @Override
    public long getMissCount() {
        return missCount.get();
    }

    /**
     * Returns the ratio of cache requests which were misses. This is defined as
     * {@code missCount / requestCount}, or {@code 0.0} when {@code requestCount == 0}.
     * Note that {@code hitRate + missRate =~ 1.0}. Cache misses include all requests which
     * weren't cache hits, including requests which resulted in either successful or failed loading
     * attempts, and requests which waited for other threads to finish loading. It is thus the case
     * that {@code missCount &gt;= loadSuccessCount + loadExceptionCount}. Multiple
     * concurrent misses for the same key will result in a single load operation.
     */
    @Override
    public double getMissRate() {
        long requestCount = getRequestCount();
        return (requestCount == 0) ? 0.0 : (double) missCount.get() / requestCount;
    }

    /**
     * Returns the total number of times that {@link Cache} lookup methods attempted to load new
     * values. This includes both successful load operations, as well as those that threw
     * exceptions. This is defined as {@code loadSuccessCount + loadExceptionCount}.
     */
    @Override
    public long getLoadCount() {
        return loadSuccessCount.get() + loadExceptionCount.get();
    }

    /**
     * Returns the number of times {@link Cache} lookup methods have successfully loaded a new value.
     * This is always incremented in conjunction with {@link #missCount}, though {@code missCount}
     * is also incremented when an exception is encountered during cache loading (see
     * {@link #loadExceptionCount}). Multiple concurrent misses for the same key will result in a
     * single load operation.
     */
    @Override
    public long getLoadSuccessCount() {
        return loadSuccessCount.get();
    }

    /**
     * Returns the number of times {@link Cache} lookup methods threw an exception while loading a
     * new value. This is always incremented in conjunction with {@code missCount}, though
     * {@code missCount} is also incremented when cache loading completes successfully (see
     * {@link #loadSuccessCount}). Multiple concurrent misses for the same key will result in a
     * single load operation.
     */
    @Override
    public long getLoadExceptionCount() {
        return loadExceptionCount.get();
    }

    /**
     * Returns the ratio of cache loading attempts which threw exceptions. This is defined as
     * {@code loadExceptionCount / (loadSuccessCount + loadExceptionCount)}, or
     * {@code 0.0} when {@code loadSuccessCount + loadExceptionCount == 0}.
     */
    @Override
    public double getLoadExceptionRate() {
        long totalLoadCount = loadSuccessCount.get() + loadExceptionCount.get();
        return (totalLoadCount == 0) ? 0.0 : (double) loadExceptionCount.get() / totalLoadCount;
    }

    /**
     * Returns the total number of nanoseconds the cache has spent loading new values. This can be
     * used to calculate the miss penalty. This value is increased every time
     * {@code loadSuccessCount} is incremented.
     */
    @Override
    public long getTotalLoadTime() {
        return totalLoadTime.get();
    }

    /**
     * Returns the average time spent loading new values. This is defined as
     * {@code totalLoadTime / loadSuccessCount}.
     */
    @Override
    public double getAverageLoadPenalty() {
        final long loadCount = loadSuccessCount.get();
        return (loadCount == 0) ? 0.0 : (double) totalLoadTime.get() / loadCount;
    }

    /**
     * Returns the total number of nanoseconds the cache has spent loading values from cache. This can be
     * used to calculate the miss penalty. This value is increased every time
     * {@code hitCount} is incremented.
     */
    @Override
    public long getTotalHitTime() {
        return totalHitTime.get();
    }

    /**
     * Returns the average time spent loading values from cache. This is defined as
     * {@code totalHitTime / hitCount}.
     */
    @Override
    public double getAverageHitPenalty() {
        final long hits = hitCount.get();
        return (hits == 0) ? 0.0 : (double) totalHitTime.get() / hits;
    }

    /**
     * Returns the total number of nanoseconds the cache has spent loading new values when an exception is thrown. This can be
     * used to calculate the miss penalty. This value is increased every time
     * {@code totalExceptionTime} is incremented.
     */
    @Override
    public long getTotalExceptionTime() {
        return totalExceptionTime.get();
    }

    /**
     * Returns the average time spent loading new values. This is defined as
     * {@code totalExceptionTime / loadExceptionCount}.
     */
    @Override
    public double getAverageExceptionPenalty() {
        long exceptions = loadExceptionCount.get();
        return (exceptions == 0) ? 0.0 : (double) totalExceptionTime.get() / exceptions;
    }
}