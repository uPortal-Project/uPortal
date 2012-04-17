package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;

import javax.portlet.CacheControl;

/**
 * Represents the cache state for a portlet
 */
public final class CacheState<T extends Serializable> {
    private CacheControl cacheControl;
    private CachedPortletData<T> cachedPortletData;
    private boolean useCachedData;
    private boolean useBrowserData;
    private PublicPortletCacheKey publicPortletCacheKey;
    private PrivatePortletCacheKey privatePortletCacheKey;
    
    
    public CacheControl getCacheControl() {
        return cacheControl;
    }
    public CachedPortletData<T> getCachedPortletData() {
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
    void setCachedPortletData(CachedPortletData<T> cachedPortletData) {
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