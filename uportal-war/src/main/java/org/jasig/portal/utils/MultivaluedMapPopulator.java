package org.jasig.portal.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Populates a Map with a List for the value. Each call to {@link #put(Object, Object)} or {@link #putAll(Map)}
 * adds values to the list
 * 
 * @author Eric Dalquist
 * @version $Revision$
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
        return (Map<K, List<V>>)map;
    }
}
