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
/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import javax.portlet.CacheControl;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.rendering.PortletRenderResult;

/**
 * Interface defining mechanism for retrieving {@link CacheControl}s.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public interface IPortletCacheControlService {

	/**
	 * 
	 * @return the maximum size of cached portlet output, in bytes
	 */
	int getCacheSizeThreshold();

    CacheState<Long> getPortletResourceState(HttpServletRequest request, IPortletWindowId portletWindowId);

    CacheState<PortletRenderResult> getPortletRenderState(HttpServletRequest request, IPortletWindowId portletWindowId);

    CacheState<PortletRenderResult> getPortletRenderHeaderState(HttpServletRequest request, IPortletWindowId portletWindowId);
	
	/**
	 * This method checks the {@link CacheControl} to determine if the output should be captured
	 * for caching.
	 * 
	 * @param portletWindowId
	 * @param httpRequest
	 * @return true if the {@link CacheControl} indicates the output should be cached
	 */
	boolean shouldOutputBeCached(CacheControl cacheControl);
	
	/**
	 * Store the output of a render header request in the cache for the portlet and request. This method internally will determine
	 * if the content needs to be stored in a public or private scoped cache.
	 * 
     * @param portletWindowId
     * @param httpRequest
     * @param cacheState
     * @param cachedPortletData
     */
    void cachePortletRenderHeaderOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData);
    
    /**
     * Store the output of a render request in the cache for the portlet and request. This method internally will determine
     * if the content needs to be stored in a public or private scoped cache.
     * 
     * @param portletWindowId
     * @param httpRequest
     * @param cacheState
     * @param cachedPortletData
     */
    void cachePortletRenderOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData);
    
    /**
     * Store the output of a resource request in the cache for the portlet and request. This method internally will determine
     * if the content needs to be stored in a public or private scoped cache.
     * 
     * @param portletWindowId
     * @param httpRequest
     * @param cacheState
     * @param cachedPortletData
     */
    void cachePortletResourceOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<Long> cacheState, CachedPortletData<Long> cachedPortletData);
	
	/**
	 * Purge any {@link CachedPortletRenderData} for the portlet.
	 * Generally triggered on any Action or Event Request.
	 * 
	 * @param portletWindowId
	 * @param httpRequest
	 * @param cacheControl
	 * @return true if the element was found in the cache and removed, false if otherwise
	 */
	boolean purgeCachedPortletData(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest);
}
