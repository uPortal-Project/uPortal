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
package org.apereo.portal.utils.cache;

import java.io.Serializable;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Factory bean that gets a Map<K extends Serializable, V> cache wrapper from the configured {@link
 * CacheFactory} for the specified cache name. If no name is specified the default cache is used.
 *
 */
public class MapCacheFactoryBean extends AbstractFactoryBean<Map> {
    private CacheFactory cacheFactory;
    private String cacheName;

    /** @return the cacheFactory */
    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }
    /** @param cacheFactory the cacheFactory to set */
    @Autowired
    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    /** @return the cacheName */
    public String getCacheName() {
        return cacheName;
    }
    /** @param cacheName the cacheName to set */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
     */
    @Override
    protected Map<Serializable, Object> createInstance() throws Exception {
        final Map<Serializable, Object> cache;
        if (this.cacheName != null) {
            cache = this.cacheFactory.getCache(this.cacheName);
        } else {
            cache = this.cacheFactory.getCache();
        }

        return cache;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
     */
    @Override
    public Class<Map> getObjectType() {
        return Map.class;
    }
}
