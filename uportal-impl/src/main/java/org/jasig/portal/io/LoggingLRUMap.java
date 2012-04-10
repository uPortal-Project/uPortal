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

package org.jasig.portal.io;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.CacheHelper;
import org.danann.cernunnos.CacheHelper.CacheEvictionListener;

import com.google.common.base.Function;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

/**
 * @author Eric Dalquist
 * @version $Revision: 1.2 $
 */
public class LoggingLRUMap<K, V> implements ConcurrentMap<K, V>, CacheHelper.EvictionAwareCache<K, V> {
    private static final Object NULL_PLACEHOLDER = new Object();
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Set<CacheEvictionListener<K, V>> listeners = Collections.newSetFromMap(new MapMaker().weakKeys().<CacheEvictionListener<K, V>, Boolean>makeMap());
    private final ConcurrentMap<K, V> writeMap;
    private final Map<K, V> readMap;
    private final V nullPlacholder;
    
    public LoggingLRUMap(int maximumSize) {
        writeMap = new MapMaker().maximumSize(maximumSize).concurrencyLevel(20).evictionListener(new LoggingMapEvictionListener<K, V>()).makeMap();
        readMap = Maps.transformValues(this.writeMap, new Function<V, V>() {
            @Override
            public V apply(V input) {
                if (input == nullPlacholder) {
                    return null;
                }
                return input;
            }
        });
        
        nullPlacholder = (V)NULL_PLACEHOLDER;
    }
    
    public LoggingLRUMap(Map<K, V> base, int maximumSize) {
        this(maximumSize);
        this.putAll(base);
    }
    

    @Override
    public V putIfAbsent(K key, V value) {
        if (value == null) {
            value = nullPlacholder;
        }
        
        final V oldValue = this.writeMap.putIfAbsent(key, value);
        if (oldValue == nullPlacholder) {
            return null;
        }
        return oldValue;
    }

    @Override
    public V replace(K key, V value) {
        if (value == null) {
            value = nullPlacholder;
        }
        
        final V replacedValue = this.writeMap.replace(key, value);
        if (replacedValue == nullPlacholder) {
            return null;
        }
        return replacedValue;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null) {
            oldValue = nullPlacholder;
        }
        if (newValue == null) {
            newValue = nullPlacholder;
        }
        
        return this.writeMap.replace(key, oldValue, newValue);
    }

    @Override
    public V put(K key, V value) {
        if (value == null) {
            value = nullPlacholder;
        }
        return this.writeMap.put(key, value);
    }
    
    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            value = nullPlacholder;
        }
        
        return this.writeMap.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (value == null) {
            value = nullPlacholder;
        }
        
        return this.writeMap.remove(key, value);
    }
    

    @Override
    public int size() {
        return this.writeMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.writeMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.writeMap.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return this.readMap.get(key);
    }

    @Override
    public V remove(Object key) {
        return this.readMap.remove(key);
    }

    @Override
    public void clear() {
        this.writeMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.writeMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.readMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.readMap.entrySet();
    }

    @Override
    public void registerCacheEvictionListener(CacheEvictionListener<K, V> listener) {
        listeners.add(listener);
    }

    private class LoggingMapEvictionListener<K, V> implements MapEvictionListener<K, V> {
        @Override
        public void onEviction(K key, V value) {
            if (logger.isWarnEnabled()) {
                logger.warn("Removing LRU entry with key='" + key + "' and value='" + value + "'");
            }
            
            for (final CacheEvictionListener listener : listeners) {
                try {
                    listener.onEviction(key, value);
                }
                catch (RuntimeException e) {
                    logger.warn(listener.getClass() + " threw an exception while being notified of element eviction", e);
                }
            }
        }
    }
}
