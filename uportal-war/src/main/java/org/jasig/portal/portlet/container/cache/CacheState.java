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
public class CacheState<D extends CachedPortletResultHolder<T>, T extends Serializable> {
    private final CacheControl cacheControl = new CacheControlImpl();
    private D cachedPortletData;
    private boolean useCachedData = false;
    private boolean useBrowserData = false;
    private boolean browserDataMatches = false;
    private boolean browserSetEtag = false;
    private PublicPortletCacheKey publicPortletCacheKey;
    private PrivatePortletCacheKey privatePortletCacheKey;
    
    protected CacheState() {
    }
    
    /**
     * @return The {@link CacheControl} object to use for the portlet, never null
     */
    public CacheControl getCacheControl() {
        return cacheControl;
    }
    /**
     * @return The cached portlet data, if it was found
     */
    public D getCachedPortletData() {
        return cachedPortletData;
    }
    /**
     * @return true If the cached portlet data should be replayed. If true {@link #getCachedPortletData()} will not be false
     */
    public boolean isUseCachedData() {
        return useCachedData;
    }
    /**
     * @return true If the browser has the data cached and a 304 should be sent
     */
    public boolean isUseBrowserData() {
        return useBrowserData;
    }
    /**
     * @return true If the browser's If-None-Match or If-Modified-Since header values mean the browser's cache matches the portal's cache
     */
    public boolean isBrowserDataMatches() {
        return browserDataMatches;
    }
    /**
     * @return true If the browser set an ETag. If true {@link CacheControl#getETag()} will return the browser set ETag
     */
    public boolean isBrowserSetEtag() {
        return browserSetEtag;
    }
    /**
     * @return The public cache key for the portlet, will not be null
     */
    public PublicPortletCacheKey getPublicPortletCacheKey() {
        return publicPortletCacheKey;
    }
    /**
     * @return The private cahe key for the portlet, may be null if publicly scoped cache data was found
     */
    public PrivatePortletCacheKey getPrivatePortletCacheKey() {
        return privatePortletCacheKey;
    }

    
    protected void setBrowserSetEtag(boolean browserSetEtag) {
        this.browserSetEtag = browserSetEtag;
    }
    protected void setCachedPortletData(D cachedPortletData) {
        this.cachedPortletData = cachedPortletData;
    }
    protected void setUseCachedData(boolean useCachedData) {
        this.useCachedData = useCachedData;
    }
    protected void setUseBrowserData(boolean useBrowserData) {
        this.useBrowserData = useBrowserData;
    }
    protected void setBrowserDataMatches(boolean browserDataMatches) {
        this.browserDataMatches = browserDataMatches;
    }
    protected void setPublicPortletCacheKey(PublicPortletCacheKey publicPortletCacheKey) {
        this.publicPortletCacheKey = publicPortletCacheKey;
    }
    protected void setPrivatePortletCacheKey(PrivatePortletCacheKey privatePortletCacheKey) {
        this.privatePortletCacheKey = privatePortletCacheKey;
    }
}