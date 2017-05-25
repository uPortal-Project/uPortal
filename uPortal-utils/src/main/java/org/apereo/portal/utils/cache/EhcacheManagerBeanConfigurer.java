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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Registers all of the Ehcache's from the {@link CacheManager} as bean's in the application
 * context.
 *
 */
public class EhcacheManagerBeanConfigurer implements BeanFactoryPostProcessor {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private CacheManager cacheManager;
    private boolean skipDuplicates = false;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setSkipDuplicates(boolean skipDuplicates) {
        this.skipDuplicates = skipDuplicates;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        final String[] cacheNames = this.cacheManager.getCacheNames();
        for (final String cacheName : cacheNames) {
            final Ehcache ehcache = this.cacheManager.getEhcache(cacheName);
            this.logger.debug("Registering Ehcache '" + cacheName + "' with bean factory");
            if (beanFactory.containsBean(cacheName)) {
                if (skipDuplicates) {
                    continue;
                }

                throw new BeanCreationException(
                        "Duplicate Ehcache "
                                + cacheName
                                + " from CacheManager "
                                + cacheManager.getName());
            }

            try {
                beanFactory.registerSingleton(cacheName, ehcache);
            } catch (Exception e) {
                throw new BeanCreationException(
                        "Failed to register Ehcache "
                                + cacheName
                                + " from CacheManager "
                                + cacheManager.getName(),
                        e);
            }
        }
        this.logger.debug("Registered " + cacheNames.length + " Ehcaches with bean factory");
    }
}
