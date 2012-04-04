package org.jasig.portal.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Populator that targets a Map 
 * 
 * @author Eric Dalquist
 * @param <K>
 * @param <V>
 */
public class MapPopulator<K, V> implements Populator<K, V> {
    private final Map<? super K, ? super V> map;
    
    public MapPopulator() {
        this.map = new LinkedHashMap<K, V>();
    }

    public MapPopulator(Map<? super K, ? super V> map) {
        this.map = map;
    }

    @Override
    public Populator<K, V> put(K k, V v) {
        this.map.put(k, v);
        return this;
    }
    
    @Override
    public Populator<K, V> putAll(Map<? extends K, ? extends V> m) {
        this.map.putAll(m);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Map<K, V> getMap() {
        return (Map<K, V>) map;
    }
}