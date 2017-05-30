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
package org.hibernate.cache.ehcache;

import java.util.Properties;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.StringUtils;
import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Region factory that loads the {@link CacheManager} by name
 *
 */
public class SpringBeanEhCacheRegionFactory extends AbstractEhcacheRegionFactory {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String CACHE_MANAGER_NAME = "org.apereo.portal.cache.CacheManagerName";

    public void start(Settings settings, Properties properties) throws CacheException {
        this.settings = settings;
        try {
            String cacheManagerBeanName = null;
            if (properties != null) {
                cacheManagerBeanName =
                        StringUtils.trimToNull(properties.getProperty(CACHE_MANAGER_NAME));
            }

            if (cacheManagerBeanName == null) {
                throw new IllegalArgumentException(
                        "The '" + CACHE_MANAGER_NAME + "' property must be set");
            }

            logger.debug("Getting CacheManager bean named {}", cacheManagerBeanName);
            manager = CacheManager.getCacheManager(cacheManagerBeanName);
            mbeanRegistrationHelper.registerMBean(manager, properties);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /** {@inheritDoc} */
    public void stop() {
        //Assume spring will stop the cache manager
        manager = null;
    }
}
