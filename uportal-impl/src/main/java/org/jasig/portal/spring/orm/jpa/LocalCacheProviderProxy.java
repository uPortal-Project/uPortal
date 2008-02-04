/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring.orm.jpa;

import java.util.Properties;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;

/**
 * Copy of the Spring LocalCacheProviderProxy that works with JPA when the {@link HibernateJpaVendorAdapter} is used
 * and the {@link HibernateJpaVendorAdapter#setCacheProvider(CacheProvider)} property is set.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LocalCacheProviderProxy implements CacheProvider {
    private final CacheProvider cacheProvider;


    public LocalCacheProviderProxy() {
        final CacheProvider cp = HibernateJpaVendorAdapter.getConfigTimeCacheProvider();
        
        // absolutely needs thread-bound CacheProvider to initialize
        if (cp == null) {
            throw new IllegalStateException("No Hibernate CacheProvider found - 'cacheProvider' property must be set on HibernateJpaVendorAdapter");
        }

        this.cacheProvider = cp;
    }


    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        return this.cacheProvider.buildCache(regionName, properties);
    }

    public long nextTimestamp() {
        return this.cacheProvider.nextTimestamp();
    }

    public void start(Properties properties) throws CacheException {
        this.cacheProvider.start(properties);
    }

    public void stop() {
        this.cacheProvider.stop();
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return this.cacheProvider.isMinimalPutsEnabledByDefault();
    }

}
