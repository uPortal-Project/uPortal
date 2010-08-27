/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import org.jasig.portal.utils.cache.CacheKey;

/**
 * Response for a rendering pipeline component that includes the event reader for data and the
 * corresponding cache key
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <R> The event reader implementation
 * @param <E> The type of event the event reader returns
 */
public interface CacheableEventReader<R, E> extends Iterable<E> {
    public R getEventReader();
    
    public CacheKey getCacheKey();
}
