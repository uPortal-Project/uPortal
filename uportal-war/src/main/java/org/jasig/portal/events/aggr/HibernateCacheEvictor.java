package org.jasig.portal.events.aggr;

import java.io.Serializable;

import org.hibernate.Cache;

/**
 * Utility to evict data from the underlying hibernate caches
 * 
 * @author Eric Dalquist
 */
public interface HibernateCacheEvictor {
    /**
     * @see Cache#evictEntity(Class, Serializable)
     */
    void evictEntity(Class<?> entityClass, Serializable identifier);
}
