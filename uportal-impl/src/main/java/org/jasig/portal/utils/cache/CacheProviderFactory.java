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
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.CacheProviderFacade;

/**
 * CacheFactory impl that provides Map instances that wrap a configured {@link CacheProviderFacade}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheProviderFactory implements CacheFactory {
    private CacheProviderFacade cacheProviderFacade;
    private ICacheModelFactory cacheModelFactory;
    
    /**
     * @return the cacheProviderFacade
     */
    public CacheProviderFacade getCacheProviderFacade() {
        return cacheProviderFacade;
    }
    /**
     * @param cacheProviderFacade the cacheProviderFacade to set
     */
    @Required
    public void setCacheProviderFacade(CacheProviderFacade cacheProviderFacade) {
        Validate.notNull(cacheProviderFacade);
        this.cacheProviderFacade = cacheProviderFacade;
    }

    /**
     * @return the cacheModelFactory
     */
    public ICacheModelFactory getCacheModelFactory() {
        return cacheModelFactory;
    }
    /**
     * @param cacheModelFactory the cacheModelFactory to set
     */
    @Required
    public void setCacheModelFactory(ICacheModelFactory cacheModelFactory) {
        Validate.notNull(cacheModelFactory);
        this.cacheModelFactory = cacheModelFactory;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.CacheFactory#getCache()
     */
    public <K extends Serializable, V> Map<K, V> getCache() {
        return this.getCache(DEFAULT);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.CacheFactory#getCache(java.lang.String)
     */
    public <K extends Serializable, V> Map<K, V> getCache(String cacheName) throws IllegalArgumentException {
        final FlushingModel flushingModel = this.cacheModelFactory.getFlushingModel(cacheName);
        final CachingModel cachingModel = this.cacheModelFactory.getCachingModel(cacheName);
        
        return new MapCacheProvider<K, V>(this.cacheProviderFacade, cachingModel, flushingModel);
    }
}
