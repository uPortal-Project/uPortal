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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.PortletRenderResult;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;


/**
 * Default implementation of {@link IPortletCacheControlService}.
 * {@link CacheControl}s are stored in a {@link Map} stored as a {@link HttpServletRequest} attribute.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Service
public class PortletCacheControlServiceImpl implements IPortletCacheControlService, ApplicationListener<ApplicationEvent> {
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    
	private static final String SESSION_ATTRIBUTE__PORTLET_RENDER_HEADER_CACHE_KEYS_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_RENDER_HEADER_CACHE_KEYS_MAP";
	private static final String SESSION_ATTRIBUTE__PORTLET_RENDER_CACHE_KEYS_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_RENDER_CACHE_KEYS_MAP";
	private static final String SESSION_ATTRIBUTE__PORTLET_RESOURCE_CACHE_KEYS_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_RESOURCE_CACHE_KEYS_MAP";
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	//Used to coordinate mass-purge of cached data for portlets at action/event requests
	private final PublicPortletCacheKeyTracker publicPortletRenderHeaderCacheKeyTracker = new PublicPortletCacheKeyTracker();
    private final PublicPortletCacheKeyTracker publicPortletRenderCacheKeyTracker = new PublicPortletCacheKeyTracker();
	private final PublicPortletCacheKeyTracker publicPortletResourceCacheKeyTracker = new PublicPortletCacheKeyTracker();
	private final PrivatePortletCacheKeyTracker privatePortletRenderHeaderCacheKeyTracker = new PrivatePortletCacheKeyTracker(SESSION_ATTRIBUTE__PORTLET_RENDER_HEADER_CACHE_KEYS_MAP);
    private final PrivatePortletCacheKeyTracker privatePortletRenderCacheKeyTracker = new PrivatePortletCacheKeyTracker(SESSION_ATTRIBUTE__PORTLET_RENDER_CACHE_KEYS_MAP);
	private final PrivatePortletCacheKeyTracker privatePortletResourceCacheKeyTracker = new PrivatePortletCacheKeyTracker(SESSION_ATTRIBUTE__PORTLET_RESOURCE_CACHE_KEYS_MAP);
	
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IUrlSyntaxProvider urlSyntaxProvider;
	
    private Ehcache privateScopePortletRenderHeaderOutputCache;
    private Ehcache publicScopePortletRenderHeaderOutputCache;
    
    private Ehcache privateScopePortletRenderOutputCache;
    private Ehcache publicScopePortletRenderOutputCache;
    
    private Ehcache privateScopePortletResourceOutputCache;
    private Ehcache publicScopePortletResourceOutputCache;
    
    // default to 100 KB
    private int cacheSizeThreshold = 102400;
    
    
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletRenderHeaderOutputCache")
    public void setPrivateScopePortletRenderHeaderOutputCache(Ehcache privateScopePortletRenderHeaderOutputCache) {
        this.privateScopePortletRenderHeaderOutputCache = privateScopePortletRenderHeaderOutputCache;
        this.privateScopePortletRenderHeaderOutputCache.getCacheEventNotificationService()
                .registerListener(privatePortletRenderHeaderCacheKeyTracker);
    }

    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletRenderHeaderOutputCache")
    public void setPublicScopePortletRenderHeaderOutputCache(Ehcache publicScopePortletRenderHeaderOutputCache) {
        this.publicScopePortletRenderHeaderOutputCache = publicScopePortletRenderHeaderOutputCache;
        this.publicScopePortletRenderHeaderOutputCache.getCacheEventNotificationService()
                .registerListener(publicPortletRenderHeaderCacheKeyTracker);
    }
    
    
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletRenderOutputCache")
    public void setPrivateScopePortletRenderOutputCache(Ehcache privateScopePortletRenderOutputCache) {
        this.privateScopePortletRenderOutputCache = privateScopePortletRenderOutputCache;
        this.privateScopePortletRenderOutputCache.getCacheEventNotificationService()
                .registerListener(privatePortletRenderCacheKeyTracker);
    }

    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletRenderOutputCache")
    public void setPublicScopePortletRenderOutputCache(Ehcache publicScopePortletRenderOutputCache) {
        this.publicScopePortletRenderOutputCache = publicScopePortletRenderOutputCache;
        this.publicScopePortletRenderOutputCache.getCacheEventNotificationService()
                .registerListener(publicPortletRenderCacheKeyTracker);
    }

    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletResourceOutputCache")
    public void setPrivateScopePortletResourceOutputCache(Ehcache privateScopePortletResourceOutputCache) {
        this.privateScopePortletResourceOutputCache = privateScopePortletResourceOutputCache;
        this.privateScopePortletResourceOutputCache.getCacheEventNotificationService()
                .registerListener(privatePortletResourceCacheKeyTracker);
    }

    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletResourceOutputCache")
    public void setPublicScopePortletResourceOutputCache(Ehcache publicScopePortletResourceOutputCache) {
        this.publicScopePortletResourceOutputCache = publicScopePortletResourceOutputCache;
        this.publicScopePortletResourceOutputCache.getCacheEventNotificationService()
                .registerListener(publicPortletResourceCacheKeyTracker);
    }
    
	/**
	 * @param cacheSizeThreshold the cacheSizeThreshold to set in bytes
	 */
    @Value("${org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.cacheSizeThreshold:102400}")
	public void setCacheSizeThreshold(int cacheSizeThreshold) {
		this.cacheSizeThreshold = cacheSizeThreshold;
	}
    
	@Override
	public int getCacheSizeThreshold() {
		return cacheSizeThreshold;
	}
	@Autowired
	public void setPortletWindowRegistry(
			IPortletWindowRegistry portletWindowRegistry) {
		this.portletWindowRegistry = portletWindowRegistry;
	}
	@Autowired
	public void setPortletDefinitionRegistry(
			IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}
	@Autowired
	public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
	
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof HttpSessionCreatedEvent) {
            final HttpSession session = ((HttpSessionCreatedEvent) event).getSession();
            this.privatePortletRenderHeaderCacheKeyTracker.initPrivateKeyCache(session);
            this.privatePortletRenderCacheKeyTracker.initPrivateKeyCache(session);
            this.privatePortletResourceCacheKeyTracker.initPrivateKeyCache(session);
        }
        else if (event instanceof HttpSessionDestroyedEvent) {
            final HttpSession session = ((HttpSessionDestroyedEvent) event).getSession();
            this.privatePortletRenderHeaderCacheKeyTracker.destroyPrivateKeyCache(session);
            this.privatePortletRenderCacheKeyTracker.destroyPrivateKeyCache(session);
            this.privatePortletResourceCacheKeyTracker.destroyPrivateKeyCache(session);
        }
    }

    @Override
    public CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> getPortletRenderHeaderState(
            HttpServletRequest request, IPortletWindowId portletWindowId) {

        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        if (portletWindow == null) {
            logger.warn("portletWindowRegistry returned null for {}, returning default cacheControl and no cached portlet data", portletWindowId);
            return new CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
        }
        
        //Generate the public render-header cache key
        final IPortletEntity entity = portletWindow.getPortletEntity();
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();
        final Locale locale = RequestContextUtils.getLocale(request);
        final PublicPortletCacheKey publicCacheKey = PublicPortletCacheKey.createPublicPortletRenderHeaderCacheKey(definitionId, portletWindow, locale);
        
        return this.<CachedPortletData<PortletRenderResult>, PortletRenderResult> getPortletState(request,
                portletWindow,
                publicCacheKey,
                this.publicScopePortletRenderHeaderOutputCache,
                this.privateScopePortletRenderHeaderOutputCache,
                false);
    }
    
    @Override
    public CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> getPortletRenderState(
            HttpServletRequest request, IPortletWindowId portletWindowId) {

        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        if (portletWindow == null) {
            logger.warn("portletWindowRegistry returned null for {}, returning default cacheControl and no cached portlet data", portletWindowId);
            return new CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
        }
        
        //Generate the public render cache key
        final IPortletEntity entity = portletWindow.getPortletEntity();
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();
        final Locale locale = RequestContextUtils.getLocale(request);
        final PublicPortletCacheKey publicCacheKey = PublicPortletCacheKey.createPublicPortletRenderCacheKey(definitionId, portletWindow, locale);
        
        return this.<CachedPortletData<PortletRenderResult>, PortletRenderResult> getPortletState(request,
                portletWindow,
                publicCacheKey,
                this.publicScopePortletRenderOutputCache,
                this.privateScopePortletRenderOutputCache,
                false);
    }
    
    @Override
    public CacheState<CachedPortletResourceData<Long>, Long> getPortletResourceState(HttpServletRequest request,
            IPortletWindowId portletWindowId) {

        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        if (portletWindow == null) {
            logger.warn("portletWindowRegistry returned null for {}, returning default cacheControl and no cached portlet data", portletWindowId);
            return new CacheState<CachedPortletResourceData<Long>, Long>();
        }
        
        //Generate the public resource cache key
        final IPortletEntity entity = portletWindow.getPortletEntity();
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();
        final String resourceId = getResourceId(portletWindowId, request);
        final Locale locale = RequestContextUtils.getLocale(request);
        final PublicPortletCacheKey publicCacheKey = PublicPortletCacheKey.createPublicPortletResourceCacheKey(definitionId, portletWindow, resourceId, locale);
        
        return this.<CachedPortletResourceData<Long>, Long> getPortletState(request,
                portletWindow,
                publicCacheKey,
                this.publicScopePortletResourceOutputCache,
                this.privateScopePortletResourceOutputCache,
                true);
    }
    
    private <D extends CachedPortletResultHolder<T>, T extends Serializable> CacheState<D, T> getPortletState(HttpServletRequest request,
            IPortletWindow portletWindow, PublicPortletCacheKey publicCacheKey, Ehcache publicOutputCache, Ehcache privateOutputCache, boolean useHttpHeaders) {
        
        //See if there is any cached data for the portlet header request
        final CacheState<D, T> cacheState = this.<D, T> getPortletCacheState(request,
                portletWindow,
                publicCacheKey,
                publicOutputCache,
                privateOutputCache);
    
        String etagHeader = null;
        final D cachedPortletData = cacheState.getCachedPortletData();
        if (cachedPortletData != null) {
            if (useHttpHeaders) {
                //Browser headers being used, check ETag and Last Modified
                
                etagHeader = request.getHeader(IF_NONE_MATCH);
                if (etagHeader != null && etagHeader.equals(cachedPortletData.getEtag())) {
                    //ETag is valid, mark the browser data as matching
                    cacheState.setBrowserDataMatches(true);
                }
                else {
                    long ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE);
                    if (ifModifiedSince >= 0 && cachedPortletData.getTimeStored() <= ifModifiedSince) {
                        //Cached content hasn't been modified since header date, mark the browser data as matching
                        cacheState.setBrowserDataMatches(true);
                    }
                }
            }
            
            final long expirationTime = cachedPortletData.getExpirationTime();
            if (expirationTime == -1 || expirationTime > System.currentTimeMillis()) {
                //Cached data exists, see if it can be used with no additional work
                //Cached data is not expired, check if browser data should be used
                cacheState.setUseCachedData(true);
                
                //Copy browser-data-matching flag to the user-browser-data flag
                cacheState.setUseBrowserData(cacheState.isBrowserDataMatches());
            
                //No browser side data to be used, return the cached data for replay
                return cacheState;
            }
        }
        
        //Build CacheControl structure
        final CacheControl cacheControl = cacheState.getCacheControl();
        
        //Get the portlet descriptor
        final IPortletEntity entity = portletWindow.getPortletEntity();
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();
        final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(definitionId);
        
        //Set the default scope
        final String cacheScopeValue = portletDescriptor.getCacheScope();
        if (MimeResponse.PUBLIC_SCOPE.equalsIgnoreCase(cacheScopeValue)) {
            cacheControl.setPublicScope(true);
        }
        
        //Set the default expiration time
        cacheControl.setExpirationTime(portletDescriptor.getExpirationCache());
        
        // Use the request etag if it exists (implies useHttpHeaders==true)
        if (etagHeader != null) {
            cacheControl.setETag(etagHeader);
            cacheState.setBrowserSetEtag(true);
        }
        // No browser-set etag, use the cached etag value if there is cached data
        else if (cachedPortletData != null) {
            cacheControl.setETag(cachedPortletData.getEtag());
        }
        
        return cacheState;
    }
    
    /**
     * Get the cached portlet data looking in both the public and then private caches returning the first found
     * 
     * @param request The current request
     * @param portletWindow The window to get data for
     * @param publicCacheKey The public cache key
     * @param publicOutputCache The public cache
     * @param privateOutputCache The private cache
     */
    @SuppressWarnings("unchecked")
    protected <D extends CachedPortletResultHolder<T>, T extends Serializable> CacheState<D, T> getPortletCacheState(HttpServletRequest request, IPortletWindow portletWindow, 
            PublicPortletCacheKey publicCacheKey, Ehcache publicOutputCache, Ehcache privateOutputCache) {
        
        final CacheState<D, T> cacheState = new CacheState<D, T>();
        cacheState.setPublicPortletCacheKey(publicCacheKey);
        
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        //Check for publicly cached data
        D cachedPortletData = (D)this.getCachedPortletData(publicCacheKey, publicOutputCache, portletWindow);
        if (cachedPortletData != null) {
            cacheState.setCachedPortletData(cachedPortletData);
            return cacheState;
        }
        
        //Generate private cache key
        final HttpSession session = request.getSession();
        final String sessionId = session.getId();
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final PrivatePortletCacheKey privateCacheKey = new PrivatePortletCacheKey(sessionId, portletWindowId, entityId, publicCacheKey);
        cacheState.setPrivatePortletCacheKey(privateCacheKey);
        
        //Check for privately cached data
        cachedPortletData = (D)this.getCachedPortletData(privateCacheKey, privateOutputCache, portletWindow);
        if (cachedPortletData != null) {
            cacheState.setCachedPortletData(cachedPortletData);
            return cacheState;
        }

        return cacheState;
    }

    /**
     * Get the cached portlet data for the key, cache and window. If there is {@link CachedPortletData}
     * in the cache it will only be returned if {@link CachedPortletData#isExpired()} is false or
     * {@link CachedPortletData#getEtag()} is not null.
     * 
     * @param cacheKey The cache key
     * @param outputCache The cache
     * @param portletWindow The portlet window the lookup is for
     * @return The cache data for the portlet window
     */
    @SuppressWarnings("unchecked")
    protected <T extends Serializable> CachedPortletResultHolder<T> getCachedPortletData(Serializable cacheKey, Ehcache outputCache,
            IPortletWindow portletWindow) {

        final Element publicCacheElement = outputCache.get(cacheKey);
        if (publicCacheElement == null) {
            logger.debug("No cached output for key {}", cacheKey);
            return null;
        }

        final CachedPortletResultHolder<T> cachedPortletData = (CachedPortletResultHolder<T>) publicCacheElement.getValue();
        if (publicCacheElement.isExpired() && cachedPortletData.getEtag() == null) {
            logger.debug("Cached output for key {} is expired", cacheKey);
            outputCache.remove(cacheKey);
            return null;
        }

        logger.debug("Returning cached output with key {} for {}", cacheKey, portletWindow);
        return (CachedPortletResultHolder<T>) publicCacheElement.getValue();
    }    
	
    /**
     * Get the resourceId for the portlet request
     */
    private String getResourceId(IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpRequest);
        final Map<IPortletWindowId, ? extends IPortletRequestInfo> portletRequestInfoMap = portalRequestInfo.getPortletRequestInfoMap();
        final IPortletRequestInfo portletRequestInfo = portletRequestInfoMap.get(portletWindowId);
        return portletRequestInfo != null ? portletRequestInfo.getResourceId() : null;
    }
    
    @Override
    public boolean shouldOutputBeCached(CacheControl cacheControl) {
        if (cacheControl.getExpirationTime() != 0) {
            return true;
        }
        else {
            return false;
        }
    }
	
    
	@Override
    public void cachePortletRenderHeaderOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData) {
	    
        cachePortletOutput(portletWindowId,
                httpRequest,
                cacheState,
                cachedPortletData,
                this.publicScopePortletRenderHeaderOutputCache,
                this.privateScopePortletRenderHeaderOutputCache);
    }

    @Override
    public void cachePortletRenderOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData) {

        cachePortletOutput(portletWindowId,
                httpRequest,
                cacheState,
                cachedPortletData,
                this.publicScopePortletRenderOutputCache,
                this.privateScopePortletRenderOutputCache);
	}

    
	@Override
    public void cachePortletResourceOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<CachedPortletResourceData<Long>, Long> cacheState,
            CachedPortletResourceData<Long> cachedPortletResourceData) {
	    
        cachePortletOutput(portletWindowId,
                httpRequest,
                cacheState,
                cachedPortletResourceData,
                this.publicScopePortletResourceOutputCache,
                this.privateScopePortletResourceOutputCache);
	}

    private <D extends CachedPortletResultHolder<T>, T extends Serializable> void cachePortletOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CacheState<D, T> cacheState, D cachedPortletData, Ehcache publicOutputCache, Ehcache privateOutputCache) {
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        final CacheControl cacheControl = cacheState.getCacheControl();
        
        if (cacheControl.isPublicScope()) {
            final PublicPortletCacheKey publicCacheKey = cacheState.getPublicPortletCacheKey();
            this.cacheElement(publicOutputCache, publicCacheKey, cachedPortletData, cacheControl);
            logger.debug("Cached public data under key {} for {}", publicCacheKey, portletWindow);
        }
        else {
            PrivatePortletCacheKey privateCacheKey = cacheState.getPrivatePortletCacheKey();
            
            //Private key can be null if getPortletState found publicly cached data but the portlet's response is now privately scoped
            if (privateCacheKey == null) {
                final HttpSession session = httpRequest.getSession();
                final String sessionId = session.getId();
                final IPortletEntityId entityId = portletWindow.getPortletEntityId();
                final PublicPortletCacheKey publicCacheKey = cacheState.getPublicPortletCacheKey();
                privateCacheKey = new PrivatePortletCacheKey(sessionId, portletWindowId, entityId, publicCacheKey);
            }
            
            this.cacheElement(privateOutputCache, privateCacheKey, cachedPortletData, cacheControl);
            logger.debug("Cached private data under key {} for {}", privateCacheKey, portletWindow);
        }
    }
	
	/**
	 * Construct an appropriate Cache {@link Element} for the cacheKey and data.
	 * The element's ttl will be set depending on whether expiration or validation method is indicated from the CacheControl and the cache's configuration.
	 */
	protected void cacheElement(Ehcache cache, Serializable cacheKey, CachedPortletResultHolder<?> data, CacheControl cacheControl) {
		// using validation method, ignore expirationTime and defer to cache configuration
        if (cacheControl.getETag() != null) {
            final Element element = new Element(cacheKey, data);
            cache.put(element);
            return;
		}
		
		// using expiration method, -1 for CacheControl#expirationTime means "forever" (e.g. ignore and defer to cache configuration)
		final int expirationTime = cacheControl.getExpirationTime();
		if(expirationTime == -1) {
			final Element element = new Element(cacheKey, data);
			cache.put(element);
            return;
		}

		// using expiration method with a positive expiration, set that value as the element's TTL if it is lower than the configured cache TTL
		final CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
		final Element element = new Element(cacheKey, data);
        final long cacheTTL = cacheConfiguration.getTimeToLiveSeconds();
        if (expirationTime < cacheTTL) {
            element.setTimeToLive(expirationTime);
        }
		cache.put(element);
	}
	
	@Override
	public boolean purgeCachedPortletData(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        final IPortletEntity entity = portletWindow.getPortletEntity();
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();
        
        logger.debug("Purging all cached data for {}", portletWindow);
        
        boolean removed = false;

        //Remove all publicly cached render header data for the portlet
        removed = this.purgePublicCache(this.publicPortletRenderHeaderCacheKeyTracker,
                        this.publicScopePortletRenderHeaderOutputCache,
                        definitionId) || removed;

        //Remove all publicly cached render data for the portlet
        removed = this.purgePublicCache(this.publicPortletRenderCacheKeyTracker,
                        this.publicScopePortletRenderOutputCache,
                        definitionId) || removed;

        //Remove all publicly cached resource data for the portlet
        removed = this.purgePublicCache(this.publicPortletResourceCacheKeyTracker,
                        this.publicScopePortletResourceOutputCache,
                        definitionId) || removed;

        final HttpSession session = httpRequest.getSession();

        //Remove all privately cached render data
        removed = this.purgePrivateCache(this.privatePortletRenderHeaderCacheKeyTracker,
                        this.privateScopePortletRenderHeaderOutputCache,
                        session,
                        portletWindowId) || removed;

        //Remove all privately cached render data
        removed = this.purgePrivateCache(this.privatePortletRenderCacheKeyTracker,
                        this.privateScopePortletRenderOutputCache,
                        session,
                        portletWindowId) || removed;

        //Remove all privately cached render data
        removed = removed
                || this.purgePrivateCache(this.privatePortletResourceCacheKeyTracker,
                        this.privateScopePortletResourceOutputCache,
                        session,
                        portletWindowId);

        //If any keys were found remove them
        return removed;
	}
	
	protected boolean purgePublicCache(PublicPortletCacheKeyTracker tracker, Ehcache cache, IPortletDefinitionId definitionId) {
        final Set<PublicPortletCacheKey> keys = tracker.getCacheKeys(definitionId);
        final boolean removed = !keys.isEmpty();
        cache.removeAll(keys);
        
        return removed;
	}
    
    protected boolean purgePrivateCache(PrivatePortletCacheKeyTracker tracker, Ehcache cache, HttpSession session, IPortletWindowId portletWindowId) {
        final Set<PrivatePortletCacheKey> keys = tracker.getCacheKeys(session, portletWindowId);
        final boolean removed = !keys.isEmpty();
        cache.removeAll(keys);
        
        return removed;
    }
}
