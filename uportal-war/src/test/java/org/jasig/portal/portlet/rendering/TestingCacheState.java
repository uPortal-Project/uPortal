/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

    @Override
    public void setBrowserSetEtag(boolean browserSetEtag) {
        super.setBrowserSetEtag(browserSetEtag);
    }
    
}
