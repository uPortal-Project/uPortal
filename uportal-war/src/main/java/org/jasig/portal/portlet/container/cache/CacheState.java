package org.jasig.portal.portlet.container.cache;

import javax.portlet.CacheControl;

/**
 * Represents the cache state for a portlet
 */
public final class CacheState {
    private final CacheControl cacheControl;
    private final CachedPortletData cachedPortletData;
    private final boolean useCachedData;
    private final boolean useBrowserData;
    
    public CacheState(CacheControl cacheControl, CachedPortletData cachedPortletData, boolean useCachedData,
            boolean useBrowserData) {
        this.cacheControl = cacheControl;
        this.cachedPortletData = cachedPortletData;
        this.useCachedData = useCachedData;
        this.useBrowserData = useBrowserData;
    }

    public CacheControl getCacheControl() {
        return cacheControl;
    }

    public CachedPortletData getCachedPortletData() {
        return cachedPortletData;
    }

    public boolean isUseCachedData() {
        return useCachedData;
    }

    public boolean isUseBrowserData() {
        return useBrowserData;
    }
}