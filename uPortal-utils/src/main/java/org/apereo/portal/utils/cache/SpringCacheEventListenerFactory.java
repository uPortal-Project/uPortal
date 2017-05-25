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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Properties;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import net.sf.ehcache.event.CacheEventListenerFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Returns references to Spring configured {@link CacheEventListener}s */
@Service
public class SpringCacheEventListenerFactory extends CacheEventListenerFactory
        implements DisposableBean {
    public static final String BEAN_NAME = "beanName";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SpringCacheEventListenerFactory.class);
    private static final CacheEventListener NOOP_CACHE_EVENT_LISTENER =
            new CacheEventListenerAdapter();
    private static volatile Map<String, CacheEventListener> cacheEventListeners;

    @Autowired
    public void setCacheEventListeners(Map<String, CacheEventListener> cacheEventListeners) {
        SpringCacheEventListenerFactory.cacheEventListeners =
                ImmutableMap.copyOf(cacheEventListeners);
    }

    @Override
    public void destroy() throws Exception {
        cacheEventListeners = null;
    }

    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        final String beanName = StringUtils.trimToNull(properties.getProperty(BEAN_NAME));
        if (beanName == null) {
            throw new IllegalArgumentException("The " + BEAN_NAME + " property must be set");
        }
        return new LazyCacheEventListener(beanName);
    }

    private static class LazyCacheEventListener implements CacheEventListener {
        private final String beanName;
        private CacheEventListener delegate;

        public LazyCacheEventListener(String beanName) {
            this.beanName = beanName;
        }

        /** Always resolves to the same delegate object, no need for thread-sync checks */
        private CacheEventListener getDelegate() {
            CacheEventListener d = this.delegate;
            if (d == null) {
                //Need a local reference here to avoid locking around cacheEventListeners reference changes
                final Map<String, CacheEventListener> cel = cacheEventListeners;
                if (cel == null) {
                    //either pre-init or post-destroy, return noop logger
                    return NOOP_CACHE_EVENT_LISTENER;
                }

                final CacheEventListener cacheEventListener = cel.get(beanName);
                if (cacheEventListener == null) {
                    //If no listener is found just use the noop listener
                    LOGGER.warn(
                            "No CacheEventListener bean found for name '"
                                    + beanName
                                    + "', using NOOP CacheEventListener instead.");
                    d = NOOP_CACHE_EVENT_LISTENER;
                } else {
                    d = cacheEventListener;
                }

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
