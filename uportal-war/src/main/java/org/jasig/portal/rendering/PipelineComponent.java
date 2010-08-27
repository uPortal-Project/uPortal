/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.utils.cache.CacheKey;


/**
 * Base interface for rendering pipeline components. A component can return an event reader and a corresponding cache 
 * key or just the cache key.
 * 
 * Implementations must be thread safe.
 * 
 * Important, due to other threads or background processes the {@link CacheKey} for a request and response can change between
 * a call to {@link #getCacheKey(HttpServletRequest, HttpServletResponse) and a subsequent call to
 * {@link PipelineComponent#getEventReader(HttpServletRequest, HttpServletResponse)}. If the returned event reader is
 * cached the {@link CacheKey} from the {@link CacheableEventReader} must be used.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <R> The event reader implementation
 * @param <E> The type of event the event reader returns
 */
public interface PipelineComponent<R, E> {
    
    /**
     * Get the cache key for the request
     */
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Get the event reader and corresponding cache key for the request
     */
    public CacheableEventReader<R, E> getEventReader(HttpServletRequest request, HttpServletResponse response);
}
