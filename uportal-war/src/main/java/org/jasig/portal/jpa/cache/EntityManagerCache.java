package org.jasig.portal.jpa.cache;

import java.io.Serializable;

/**
 * Used to cache data associated with a specific EntityManager. The EntityManager specified by the Persistent Unit Name
 * must be active in the current Thread.
 * 
 * @author Eric Dalquist
 */
public interface EntityManagerCache {

    /**
     * Cache a value associated with the persistent unit.
     * 
     * @param persistenceUnitName The name of the JPA persistent unit to associate the cached data with 
     * @param key The cache key
     * @param value The cache value
     */
    void put(String persistenceUnitName, Serializable key, Object value);

    /**
     * @param persistenceUnitName The name of the JPA persistent unit to associate the cached data with
     * @param key The cache key
     * @return The cached value
     */
    <T> T get(String persistenceUnitName, Serializable key);
}
