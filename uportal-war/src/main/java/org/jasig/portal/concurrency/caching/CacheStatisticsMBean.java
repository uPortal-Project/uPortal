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

import com.google.common.cache.Cache;

public interface CacheStatisticsMBean {

    /**
     * Returns the number of times {@link Cache} lookup methods have returned either a cached or
     * uncached value. This is defined as {@code hitCount + missCount}.
     */
    long getRequestCount();

    /**
     * Returns the number of times {@link Cache} lookup methods have returned a cached value.
     */
    long getHitCount();

    /**
     * Returns the ratio of cache requests which were hits. This is defined as
     * {@code hitCount / requestCount}, or {@code 1.0} when {@code requestCount == 0}.
     * Note that {@code hitRate + missRate =~ 1.0}.
     */
    double getHitRate();

    /**
     * Returns the number of times {@link Cache} lookup methods have returned an uncached (newly
     * loaded) value, or null. Multiple concurrent calls to {@link Cache} lookup methods on an absent
     * value can result in multiple misses, all returning the results of a single cache load
     * operation.
     */
    long getMissCount();

    /**
     * Returns the ratio of cache requests which were misses. This is defined as
     * {@code missCount / requestCount}, or {@code 0.0} when {@code requestCount == 0}.
     * Note that {@code hitRate + missRate =~ 1.0}. Cache misses include all requests which
     * weren't cache hits, including requests which resulted in either successful or failed loading
     * attempts, and requests which waited for other threads to finish loading. It is thus the case
     * that {@code missCount &gt;= loadSuccessCount + loadExceptionCount}. Multiple
     * concurrent misses for the same key will result in a single load operation.
     */
    double getMissRate();

    /**
     * Returns the total number of times that {@link Cache} lookup methods attempted to load new
     * values. This includes both successful load operations, as well as those that threw
     * exceptions. This is defined as {@code loadSuccessCount + loadExceptionCount}.
     */
    long getLoadCount();

    /**
     * Returns the number of times {@link Cache} lookup methods have successfully loaded a new value.
     * This is always incremented in conjunction with {@link #missCount}, though {@code missCount}
     * is also incremented when an exception is encountered during cache loading (see
     * {@link #loadExceptionCount}). Multiple concurrent misses for the same key will result in a
     * single load operation.
     */
    long getLoadSuccessCount();

    /**
     * Returns the number of times {@link Cache} lookup methods threw an exception while loading a
     * new value. This is always incremented in conjunction with {@code missCount}, though
     * {@code missCount} is also incremented when cache loading completes successfully (see
     * {@link #loadSuccessCount}). Multiple concurrent misses for the same key will result in a
     * single load operation.
     */
    long getLoadExceptionCount();

    /**
     * Returns the ratio of cache loading attempts which threw exceptions. This is defined as
     * {@code loadExceptionCount / (loadSuccessCount + loadExceptionCount)}, or
     * {@code 0.0} when {@code loadSuccessCount + loadExceptionCount == 0}.
     */
    double getLoadExceptionRate();

    /**
     * Returns the total number of nanoseconds the cache has spent loading new values. This can be
     * used to calculate the miss penalty. This value is increased every time
     * {@code loadSuccessCount} is incremented.
     */
    long getTotalLoadTime();

    /**
     * Returns the average time spent loading new values. This is defined as
     * {@code totalLoadTime / loadSuccessCount}.
     */
    double getAverageLoadPenalty();

    /**
     * Returns the total number of nanoseconds the cache has spent loading values from cache. This can be
     * used to calculate the miss penalty. This value is increased every time
     * {@code hitCount} is incremented.
     */
    long getTotalHitTime();

    /**
     * Returns the average time spent loading values from cache. This is defined as
     * {@code totalHitTime / hitCount}.
     */
    double getAverageHitPenalty();

    /**
     * Returns the total number of nanoseconds the cache has spent loading new values when an exception is thrown. This can be
     * used to calculate the miss penalty. This value is increased every time
     * {@code totalExceptionTime} is incremented.
     */
    long getTotalExceptionTime();

    /**
     * Returns the average time spent loading new values. This is defined as
     * {@code totalExceptionTime / loadExceptionCount}.
     */
    double getAverageExceptionPenalty();

}