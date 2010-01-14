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
