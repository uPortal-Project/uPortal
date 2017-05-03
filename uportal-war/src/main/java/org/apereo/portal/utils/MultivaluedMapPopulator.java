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
package org.apereo.portal.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Populates a Map with a List for the value. Each call to {@link #put(Object, Object)} or {@link
 * #putAll(Map)} adds values to the list
 *
 * @param <K>
 * @param <V>
 */
public class MultivaluedMapPopulator<K, V> implements Populator<K, V> {
    private final Map<? super K, List<V>> map;

    public MultivaluedMapPopulator() {
        this.map = new LinkedHashMap<K, List<V>>();
    }

    public MultivaluedMapPopulator(Map<? super K, List<V>> map) {
        this.map = map;
    }

    @Override
    public Populator<K, V> put(K k, V v) {
        List<V> values = this.map.get(k);
        if (values == null) {
            values = new LinkedList<V>();
            this.map.put(k, values);
        }
        values.add(v);

        return this;
    }

    @Override
    public Populator<K, V> putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public Map<K, List<V>> getMap() {
        return (Map<K, List<V>>) map;
    }
}
