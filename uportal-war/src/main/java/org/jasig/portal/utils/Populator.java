package org.jasig.portal.utils;

import java.util.Map;

/**
 * Basic interface used for populating a Map-like data structure 
 * 
 * @author Eric Dalquist
 * @param <K>
 * @param <V>
 */
public interface Populator<K, V> {
    Populator<K, V> put(K k, V v);
    
    Populator<K, V> putAll(Map<? extends K, ? extends V> m);
}