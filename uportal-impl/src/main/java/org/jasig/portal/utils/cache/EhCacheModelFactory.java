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
