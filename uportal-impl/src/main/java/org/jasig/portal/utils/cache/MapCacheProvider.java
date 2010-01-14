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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.CacheProviderFacade;

/**
 * Hides a {@link CacheProviderFacade} behind a the {@link Map} interface. Only the following methods
 * are supported:
 * <ul>
 *  <li>{@link #clear()}</li>
 *  <li>{@link #containsKey(Object)}</li>
 *  <li>{@link #get(Object)}</li>
 *  <li>{@link #put(Serializable, Object)}</li>
 *  <li>{@link #putAll(Map)}</li>
 *  <li>{@link #remove(Object)}</li>
 * </ul>
 * 
 * The {@link #put(Serializable, Object)} and {@link #remove(Object)} methods call {@link #get(Object)} before performing
 * the requested operation to be able to return the 'old' value.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MapCacheProvider<K extends Serializable, V> implements Map<K, V> {
    private final CacheProviderFacade cacheProviderFacade;
    private final CachingModel cachingModel;
    private final FlushingModel flushModel;
    
    /**
     * @param cacheProviderFacade The facade to expose as a Map.
     * @param cachingModel The cache model to use for storing and retrieving data
     * @param flushModel The flushing model to use when clearing the cache
     */
    public MapCacheProvider(CacheProviderFacade cacheProviderFacade, CachingModel cachingModel, FlushingModel flushModel) {
        Validate.notNull(cacheProviderFacade, "cacheProviderFacade can not be null");
        Validate.notNull(cachingModel, "cachingModel can not be null");
        Validate.notNull(flushModel, "flushModel can not be null");
        
        this.cacheProviderFacade = cacheProviderFacade;
        this.cachingModel = cachingModel;
        this.flushModel = flushModel;
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.cacheProviderFacade.flushCache(this.flushModel);
    }
    
    /**
     * This does "{@link #get(Object)} != null". 
     * 
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return (V)this.cacheProviderFacade.getFromCache((Serializable)key, this.cachingModel);
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
        final V old = this.get(key);
        this.cacheProviderFacade.putInCache(key, this.cachingModel, value);
        return old;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> t) {
        for (final Map.Entry<? extends K, ? extends V> e : t.entrySet()) {
            final K key = e.getKey();
            final V value = e.getValue();
            this.cacheProviderFacade.putInCache(key, this.cachingModel, value);
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
        final V old = this.get(key);
        this.cacheProviderFacade.removeFromCache((Serializable)key, this.cachingModel);
        return old;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MapCacheProvider)) {
            return false;
        }
        MapCacheProvider<?,?> rhs = (MapCacheProvider<?,?>) object;
        return new EqualsBuilder()
            .append(this.cacheProviderFacade, rhs.cacheProviderFacade)
            .append(this.cachingModel, rhs.cachingModel)
            .append(this.flushModel, rhs.flushModel)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1916800777, 1589715735)
            .append(this.cacheProviderFacade)
            .append(this.cachingModel)
            .append(this.flushModel)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
           .append("cacheProviderFacade", this.cacheProviderFacade)
           .append("cachingModel", this.cachingModel)
           .append("flushModel", this.flushModel)
           .toString();
    }

    
    //********** Unsupported Map methods **********//
    
    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        //Don't know what the size is
        return 0;
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
        return Collections.emptySet();
    }
}
