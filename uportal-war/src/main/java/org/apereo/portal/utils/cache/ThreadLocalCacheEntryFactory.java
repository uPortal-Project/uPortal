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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Base CacheEntryFactory that uses a {@link ThreadLocal} to pass along additional data to the
 * {@link #createEntry(Object, Object)} method.
 *
 */
public abstract class ThreadLocalCacheEntryFactory<A> implements CacheEntryFactory {
    public final ThreadLocal<A> threadData = new ThreadLocal<A>();

    /* (non-Javadoc)
     * @see net.sf.ehcache.constructs.blocking.CacheEntryFactory#createEntry(java.lang.Object)
     */
    @Override
    public final Object createEntry(Object key) throws Exception {
        final A data = threadData.get();
        return this.createEntry(key, data);
    }

    /** Wraps an {@link Ehcache#get(Object)} call with setting/clearing the {@link ThreadLocal} */
    public Element getWithData(Ehcache cache, Object key, A data) {
        this.threadData.set(data);
        try {
            return cache.get(key);
        } finally {
            this.threadData.remove();
        }
    }

    /** @see CacheEntryFactory#createEntry(Object) */
    protected abstract Object createEntry(Object key, A threadData) throws Exception;
}
