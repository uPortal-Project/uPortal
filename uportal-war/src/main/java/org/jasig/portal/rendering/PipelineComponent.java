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
 * @author Eric Dalquist
 * @version $Revision$
 * @param <R> The event reader implementation
 */
public interface PipelineComponent<R, E> {
    
    /**
     * Get the cache key for the request
     */
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Get the event reader and corresponding cache key for the request
     */
    public PipelineEventReader<R, E> getEventReader(HttpServletRequest request, HttpServletResponse response);
}
