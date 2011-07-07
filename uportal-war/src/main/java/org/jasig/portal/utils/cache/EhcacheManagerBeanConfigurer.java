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

package org.jasig.portal.utils.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Registers all of the Ehcache's from the {@link CacheManager} as bean's in the 
 * application context.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EhcacheManagerBeanConfigurer implements BeanFactoryPostProcessor {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final String[] cacheNames = this.cacheManager.getCacheNames();
        for (final String cacheName : cacheNames) {
            final Ehcache ehcache = this.cacheManager.getEhcache(cacheName);
            this.logger.debug("Registering Ehcache '" + cacheName + "' with bean factory");
            beanFactory.registerSingleton(cacheName, ehcache);
        }
        this.logger.debug("Registered " + cacheNames.length + " Ehcaches with bean factory");
    }

}
