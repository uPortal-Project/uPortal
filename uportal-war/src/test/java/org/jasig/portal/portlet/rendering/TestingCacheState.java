package org.jasig.portal.portlet.rendering;

import java.io.Serializable;

import org.jasig.portal.portlet.container.cache.CacheState;
import org.jasig.portal.portlet.container.cache.CachedPortletResultHolder;
import org.jasig.portal.portlet.container.cache.PrivatePortletCacheKey;
import org.jasig.portal.portlet.container.cache.PublicPortletCacheKey;

public class TestingCacheState<D extends CachedPortletResultHolder<T>, T extends Serializable> extends CacheState<D, T> {

    @Override
    public void setCachedPortletData(D cachedPortletData) {
        super.setCachedPortletData(cachedPortletData);
    }

    @Override
    public void setUseCachedData(boolean useCachedData) {
        super.setUseCachedData(useCachedData);
    }

    @Override
    public void setUseBrowserData(boolean useBrowserData) {
        super.setUseBrowserData(useBrowserData);
    }

    @Override
    public void setBrowserDataMatches(boolean browserDataMatches) {
        super.setBrowserDataMatches(browserDataMatches);
    }

    @Override
    public void setPublicPortletCacheKey(PublicPortletCacheKey publicPortletCacheKey) {
        super.setPublicPortletCacheKey(publicPortletCacheKey);
    }

    @Override
    public void setPrivatePortletCacheKey(PrivatePortletCacheKey privatePortletCacheKey) {
        super.setPrivatePortletCacheKey(privatePortletCacheKey);
    }
}
