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

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Creates {@link TagTrackingCacheEventListener} instances
 */
public class SpringCacheEventListenerFactory extends CacheEventListenerFactory {
    public static final String BEAN_NAME = "beanName";
    
    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        final String beanName = properties.getProperty(BEAN_NAME);
        return new LazyCacheEventListener(beanName);
    }
    
    private static class LazyCacheEventListener implements CacheEventListener {
        private final String beanName;
        private CacheEventListener delegate;
        
        public LazyCacheEventListener(String beanName) {
            this.beanName = beanName;
        }
        
        /**
         * Always resolves to the same delegate object, no need for thread-sync checks
         */
        private CacheEventListener getDelegate() {
            CacheEventListener d = this.delegate;
            if (d == null) {
                final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
                d = applicationContext.getBean(beanName, CacheEventListener.class);
                this.delegate = d;
            }
            return d;
        }

        @Override
        public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
            this.getDelegate().notifyElementRemoved(cache, element);
        }

        @Override
        public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
            this.getDelegate().notifyElementPut(cache, element);
        }

        @Override
        public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
            this.getDelegate().notifyElementUpdated(cache, element);
        }

        @Override
        public void notifyElementExpired(Ehcache cache, Element element) {
            this.getDelegate().notifyElementExpired(cache, element);
        }

        @Override
        public void notifyElementEvicted(Ehcache cache, Element element) {
            this.getDelegate().notifyElementEvicted(cache, element);
        }

        @Override
        public void notifyRemoveAll(Ehcache cache) {
            this.getDelegate().notifyRemoveAll(cache);
        }

        @Override
        public void dispose() {
            this.getDelegate().dispose();
            this.delegate = null;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new LazyCacheEventListener(beanName);
        }
    }
}
