/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
