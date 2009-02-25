/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.cache;

import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.ehcache.EhCacheCachingModel;
import org.springmodules.cache.provider.ehcache.EhCacheFlushingModel;

/**
 * Creates {@link EhCacheCachingModel} and {@link EhCacheFlushingModel} using the specified cacheName
 * to name the model.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EhCacheModelFactory implements ICacheModelFactory {

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.ICacheModelFactory#getCachingModel(java.lang.String)
     */
    public CachingModel getCachingModel(String cacheName) {
        return new EhCacheCachingModel(cacheName);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.ICacheModelFactory#getFlushingModel(java.lang.String)
     */
    public FlushingModel getFlushingModel(String cacheName) {
        return new EhCacheFlushingModel(cacheName);
    }

}
