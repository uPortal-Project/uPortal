/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Factory bean that gets a Map<K extends Serializable, V> cache wrapper from the configured {@link CacheFactory}
 * for the specified cache name. If no name is specified the default cache is used. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MapCacheFactoryBean extends AbstractFactoryBean {
    private CacheFactory cacheFactory;
    private String cacheName;
    
    /**
     * @return the cacheFactory
     */
    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }
    /**
     * @param cacheFactory the cacheFactory to set
     */
    @Required
    public void setCacheFactory(CacheFactory cacheFactory) {
        Validate.notNull(cacheFactory, "cacheFactory can not be null");
        this.cacheFactory = cacheFactory;
    }

    /**
     * @return the cacheName
     */
    public String getCacheName() {
        return cacheName;
    }
    /**
     * @param cacheName the cacheName to set
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
     */
    @Override
    protected Object createInstance() throws Exception {
        final Map<Serializable, Object> cache;
        if (this.cacheName != null) {
            cache = this.cacheFactory.getCache(this.cacheName);
        }
        else {
            cache = this.cacheFactory.getCache();
        }

        return cache;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return Map.class;
    }
}
