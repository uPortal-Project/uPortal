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
