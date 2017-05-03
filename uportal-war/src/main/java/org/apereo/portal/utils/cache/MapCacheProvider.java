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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.Validate;

/**
 * Hides a {@link CacheProviderFacade} behind a the {@link Map} interface. Only the following
 * methods are supported:
 *
 * <ul>
 *   <li>{@link #clear()}
 *   <li>{@link #containsKey(Object)}
 *   <li>{@link #get(Object)}
 *   <li>{@link #put(Serializable, Object)}
 *   <li>{@link #putAll(Map)}
 *   <li>{@link #remove(Object)}
 * </ul>
 *
 * The {@link #put(Serializable, Object)} and {@link #remove(Object)} methods call {@link
 * #get(Object)} before performing the requested operation to be able to return the 'old' value.
 *
 */
public class MapCacheProvider<K extends Serializable, V> implements Map<K, V> {
    private final Ehcache cache;

    /**
     * @param cacheProviderFacade The facade to expose as a Map.
     * @param cachingModel The cache model to use for storing and retrieving data
     * @param flushModel The flushing model to use when clearing the cache
     */
    public MapCacheProvider(Ehcache cache) {
        Validate.notNull(cache, "cache can not be null");

        this.cache = cache;
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.cache.removeAll();
    }

    /**
     * This does "{@link #get(Object)} != null".
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.cache.isKeyInCache(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        final Element element = this.cache.get((Serializable) key);
        if (element == null) {
            return null;
        }

        return (V) element.getObjectValue();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
        final V old = this.get(key);
        this.cache.put(new Element(key, value));
        return old;
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> t) {
        for (final Map.Entry<? extends K, ? extends V> e : t.entrySet()) {
            final K key = e.getKey();
            final V value = e.getValue();

            this.cache.put(new Element(key, value));
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
        final V old = this.get(key);
        this.cache.remove((Serializable) key);
        return old;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cache == null) ? 0 : cache.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MapCacheProvider<?, ?>)) {
            return false;
        }
        MapCacheProvider<?, ?> other = (MapCacheProvider<?, ?>) obj;
        if (cache == null) {
            if (other.cache != null) {
                return false;
            }
        } else if (!cache.equals(other.cache)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MapCacheProvider [" + cache + "]";
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return this.cache.isValueInCache(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.cache.getSize() > 0;
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return this.cache.getSize();
    }

    //********** Unsupported Map methods **********//

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
        return Collections.emptySet();
    }
}
