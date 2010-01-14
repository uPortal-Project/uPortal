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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cfg.Environment;

/**
 * Extension of the Spring {@link org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter} class that can provide a
 * CacheProvider implementation to the hibernate session factory.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HibernateJpaVendorAdapter extends org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter {
    private static final ThreadLocal<CacheProvider> configTimeCacheProviderHolder = new ThreadLocal<CacheProvider>();
    
    /**
     * Return the CacheProvider for the currently configured Hibernate SessionFactory,
     * to be used by LocalCacheProviderProxy.
     * <p>This instance will be set before initialization of the corresponding
     * SessionFactory, and reset immediately afterwards. It is thus only available
     * during configuration.
     * @see #setCacheProvider
     */
    public static CacheProvider getConfigTimeCacheProvider() {
        return configTimeCacheProviderHolder.get();
    }
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private CacheProvider cacheProvider = null;
    
    /**
     * @return the cacheProvider
     */
    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }
    /**
     * @param cacheProvider the cacheProvider to set
     */
    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /* (non-Javadoc)
     * @see org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter#getJpaPropertyMap()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getJpaPropertyMap() {
        final Map<String, String> jpaPropertyMap = super.getJpaPropertyMap();
        
        if (this.cacheProvider != null) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Setting CacheProvider '" + this.cacheProvider + "' on ThreadLocal");
            }
            
            configTimeCacheProviderHolder.set(this.cacheProvider);
            jpaPropertyMap.put(Environment.CACHE_PROVIDER, LocalCacheProviderProxy.class.getName());
        }
        
        return jpaPropertyMap;
    }
}
