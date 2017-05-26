/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Used with caches that will degrade performance when the maxElementsInMemory is smaller than the
 * number of objects of that type in the portal.
 *
 */
@Service("insufficientSizeCacheEventListener")
public class InsufficientSizeCacheEventListener extends CacheEventListenerAdapter {

    @Autowired private CacheHealthReporterService cacheHealthReporterService;

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        if (!element.isExpired()) {
            /*
             * We've added this listener to the configuration of this cache in
             * ehcache.xml because we believe it's worrisome for elements to
             * leave the cache EXCEPT when they exceed their timeToLiveSeconds
             * or timeToIdleSeconds.
             */
            cacheHealthReporterService.reportCacheSizeNotLargeEnough(cache, element);
        }
    }
}
