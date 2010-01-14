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

package org.jasig.portal.utils.cache.hibernate;

import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides EhCache instances to hibernate using an injected {@link CacheManager}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EhCacheProvider implements CacheProvider {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private CacheManager cacheManager;
    
    /**
     * @return the cacheManager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    /**
     * @param cacheManager the cacheManager to set
     */
    @Required
    public void setCacheManager(CacheManager cacheManager) {
        Validate.notNull(cacheManager, "cacheManager can not be null");
        this.cacheManager = cacheManager;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#buildCache(java.lang.String, java.util.Properties)
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        try {
            Ehcache cache = this.cacheManager.getEhcache(regionName);

            if (cache == null) {
                this.logger.warn("Could not find a specific ehcache configuration for cache regionNamed '" + regionName + "'. The default cache will be used.");
                this.cacheManager.addCache(regionName);
                cache = this.cacheManager.getEhcache(regionName);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Created EhCache '" + regionName + "'");
                }
            }
            
            return new net.sf.ehcache.hibernate.EhCache(cache);
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#isMinimalPutsEnabledByDefault()
     */
    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#nextTimestamp()
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#start(java.util.Properties)
     */
    public void start(Properties properties) throws CacheException {
        //Since the CacheManager is injected it is assumed its lifecycle is externally managed
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#stop()
     */
    public void stop() {
        //Since the CacheManager is injected it is assumed its lifecycle is externally managed
    }
}
