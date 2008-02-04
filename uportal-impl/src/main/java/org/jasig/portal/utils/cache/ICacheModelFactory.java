/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;

/**
 * Provides a way for the {@link CacheProviderFactory} to generate the correct models for a named
 * {@link org.springmodules.cache.provider.CacheProviderFacade} wrapper.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ICacheModelFactory {
    /**
     * @param cacheName The name of the cache this model will be for
     * @return The appropriate model to use for the named cache.
     */
    public CachingModel getCachingModel(String cacheName);
    
    /**
     * @param cacheName The name of the cache this model will be for
     * @return The appropriate model to use for the named cache.
     */
    public FlushingModel getFlushingModel(String cacheName);
}
