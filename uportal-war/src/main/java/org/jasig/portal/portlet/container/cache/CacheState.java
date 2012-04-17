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
package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;

import javax.portlet.CacheControl;

/**
 * Represents the cache state for a portlet
 */
public final class CacheState<D extends CachedPortletResultHolder<T>, T extends Serializable> {
    private CacheControl cacheControl;
    private D cachedPortletData;
    private boolean useCachedData = false;
    private boolean useBrowserData = false;
    private PublicPortletCacheKey publicPortletCacheKey;
    private PrivatePortletCacheKey privatePortletCacheKey;
    
    
    public CacheControl getCacheControl() {
        return cacheControl;
    }
    public D getCachedPortletData() {
        return cachedPortletData;
    }
    public boolean isUseCachedData() {
        return useCachedData;
    }
    public boolean isUseBrowserData() {
        return useBrowserData;
    }
    public PublicPortletCacheKey getPublicPortletCacheKey() {
        return publicPortletCacheKey;
    }
    public PrivatePortletCacheKey getPrivatePortletCacheKey() {
        return privatePortletCacheKey;
    }
    
    
    void setCacheControl(CacheControl cacheControl) {
        this.cacheControl = cacheControl;
    }
    void setCachedPortletData(D cachedPortletData) {
        this.cachedPortletData = cachedPortletData;
    }
    void setUseCachedData(boolean useCachedData) {
        this.useCachedData = useCachedData;
    }
    void setUseBrowserData(boolean useBrowserData) {
        this.useBrowserData = useBrowserData;
    }
    void setPublicPortletCacheKey(PublicPortletCacheKey publicPortletCacheKey) {
        this.publicPortletCacheKey = publicPortletCacheKey;
    }
    void setPrivatePortletCacheKey(PrivatePortletCacheKey privatePortletCacheKey) {
        this.privatePortletCacheKey = privatePortletCacheKey;
    }
}