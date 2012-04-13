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
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.lang.StringUtils;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.container.CacheControlImpl;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.utils.web.PortalWebUtils;
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

	protected static final String REQUEST_ATTRIBUTE__PORTLET_CACHE_CONTROL_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_CACHE_CONTROL_MAP";
	protected static final String SESSION_ATTRIBUTE__PORTLET_RENDER_CACHE_KEYS_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_RENDER_CACHE_KEYS_MAP";
	protected static final String SESSION_ATTRIBUTE__PORTLET_RESOURCE_CACHE_KEYS_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_RESOURCE_CACHE_KEYS_MAP";
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
	private final PublicPortletCacheKeyTracker publicPortletRenderCacheKeyTracker = new PublicPortletCacheKeyTracker();
	private final PublicPortletCacheKeyTracker publicPortletResourceCacheKeyTracker = new PublicPortletCacheKeyTracker();
	private final PrivatePortletCacheKeyTracker privatePortletRenderCacheKeyTracker = new PrivatePortletCacheKeyTracker(SESSION_ATTRIBUTE__PORTLET_RENDER_CACHE_KEYS_MAP);
	private final PrivatePortletCacheKeyTracker privatePortletResourceCacheKeyTracker = new PrivatePortletCacheKeyTracker(SESSION_ATTRIBUTE__PORTLET_RESOURCE_CACHE_KEYS_MAP);
	
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletEntityRegistry portletEntityRegistry;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	private IUrlSyntaxProvider urlSyntaxProvider;
	
	// key=sessionId+windowId+entityId+definitionId+renderParameters+locale; value=CachedPortletData
    private Ehcache privateScopePortletRenderOutputCache;
    // key=definitionId+renderParams+publicRenderParam+locale; value=CachedPortletData
    private Ehcache publicScopePortletRenderOutputCache;
    
    // key=sessionId+windowId+entityId+definitionId+renderParameters+locale; value=CachedPortletData
    private Ehcache privateScopePortletResourceOutputCache;
    // key=definitionId+renderParams+publicRenderParams+locale; value=CachedPortletData
    private Ehcache publicScopePortletResourceOutputCache;
    
    // default to 100 KB
    private int cacheSizeThreshold = 102400;
    
    
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
	public void setPortletEntityRegistry(
			IPortletEntityRegistry portletEntityRegistry) {
		this.portletEntityRegistry = portletEntityRegistry;
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
            this.privatePortletRenderCacheKeyTracker.initPrivateKeyCache(session);
            this.privatePortletResourceCacheKeyTracker.initPrivateKeyCache(session);
        }
        else if (event instanceof HttpSessionDestroyedEvent) {
            final HttpSession session = ((HttpSessionDestroyedEvent) event).getSession();
            this.privatePortletRenderCacheKeyTracker.destroyPrivateKeyCache(session);
            this.privatePortletResourceCacheKeyTracker.destroyPrivateKeyCache(session);
        }
    }

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.services.IPortletCacheService#getPortletCacheControl(org.jasig.portal.portlet.om.IPortletWindowId)
	 */
	@Override
	public CacheControl getPortletRenderCacheControl(IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
		Map<IPortletWindowId, CacheControl> map = PortalWebUtils.getMapRequestAttribute(httpRequest, REQUEST_ATTRIBUTE__PORTLET_CACHE_CONTROL_MAP);
		CacheControl cacheControl = map.get(portletWindowId);
		if(cacheControl == null) {
			cacheControl = new CacheControlImpl();
			final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
	        if(portletWindow == null) {
	        	logger.warn("portletWindowRegistry returned null for {}, returning default cacheControl", portletWindowId);
	        	return cacheControl;
	        }
	        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
	        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
	        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
			
	        PortletDefinition portletDefinition = this.portletDefinitionRegistry.getParentPortletDescriptor(definitionId);
	        final String cacheScopeValue = portletDefinition.getCacheScope();
	        if(MimeResponse.PUBLIC_SCOPE.equalsIgnoreCase(cacheScopeValue)) {
	        	cacheControl.setPublicScope(true);
	        }
	        cacheControl.setExpirationTime(portletDefinition.getExpirationCache());
	        
	        // check for CachedPortletData to see if there is an etag to set
	        CachedPortletData cachedData = getCachedPortletRenderOutput(portletWindowId, httpRequest);
	        if(cachedData != null) {
	        	cacheControl.setETag(cachedData.getEtag());
	        }
			map.put(portletWindowId, cacheControl);
		}
		return cacheControl;
	}

	@Override
	public CacheControl getPortletResourceCacheControl(
			IPortletWindowId portletWindowId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		Map<IPortletWindowId, CacheControl> map = PortalWebUtils.getMapRequestAttribute(httpRequest, REQUEST_ATTRIBUTE__PORTLET_CACHE_CONTROL_MAP);
		CacheControl cacheControl = map.get(portletWindowId);
		if(cacheControl == null) {
			cacheControl = new CacheControlImpl(httpResponse);
			final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
	        if(portletWindow == null) {
	            logger.warn("portletWindowRegistry returned null for {}, returning default cacheControl", portletWindowId);
	        	return cacheControl;
	        }
	        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
	        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
	        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
			
	        PortletDefinition portletDefinition = this.portletDefinitionRegistry.getParentPortletDescriptor(definitionId);
	        final String cacheScopeValue = portletDefinition.getCacheScope();
	        if(MimeResponse.PUBLIC_SCOPE.equalsIgnoreCase(cacheScopeValue)) {
	        	cacheControl.setPublicScope(true);
	        }
	        cacheControl.setExpirationTime(portletDefinition.getExpirationCache());
	        
	        // check for CachedPortletData to see if there is an etag to set
	        CachedPortletData cachedData = getCachedPortletResourceOutput(portletWindowId, httpRequest);
	        if(cachedData != null) {
	        	cacheControl.setETag(cachedData.getEtag());
	        }
			map.put(portletWindowId, cacheControl);
		}
		return cacheControl;
	}
	/*
	 * 
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#getCachedPortletOutput(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public CachedPortletData getCachedPortletRenderOutput(
			IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
		
        final Locale locale = RequestContextUtils.getLocale(httpRequest);
        final PublicPortletCacheKey publicCacheKey = new PublicPortletCacheKey(definitionId, portletWindow, locale);
		Element publicCacheElement = this.publicScopePortletRenderOutputCache.get(publicCacheKey);
		if(publicCacheElement != null) {
		    CachedPortletData cachedPortletData = (CachedPortletData) publicCacheElement.getValue();
		    if(publicCacheElement.isExpired() && StringUtils.isBlank(cachedPortletData.getEtag())) {
				this.publicScopePortletRenderOutputCache.remove(publicCacheKey);
				logger.debug("Publicly cached render output for key {} is expired", publicCacheKey);
				return null;
			} else {
			    logger.debug("Returning publicly cached render output with key {} for {}", publicCacheKey, portletWindow);
				return (CachedPortletData) publicCacheElement.getValue();
			}
		} else {
			// public cache contained no content, check private
		    final PrivatePortletCacheKey privateCacheKey = new PrivatePortletCacheKey(httpRequest.getSession().getId(), portletWindowId, entityId, publicCacheKey);
			Element privateCacheElement = this.privateScopePortletRenderOutputCache.get(privateCacheKey);
			if(privateCacheElement != null) {
	            CachedPortletData cachedPortletData = (CachedPortletData) privateCacheElement.getValue();
	            if(privateCacheElement.isExpired() && StringUtils.isBlank(cachedPortletData.getEtag())) {
					this.privateScopePortletRenderOutputCache.remove(privateCacheKey);
					logger.debug("Privately cached render output for key {} is expired", privateCacheKey);
					return null;
				} else {
				    logger.debug("Returning privately cached render output with key {} for {}", privateCacheKey, portletWindow);
					return (CachedPortletData) privateCacheElement.getValue();
				}
			}
		}
		
		logger.debug("No cached render output exists for portlet window {}", portletWindow);
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#getCachedPortletResourceOutput(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public CachedPortletData getCachedPortletResourceOutput(
			IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
        
        final String resourceId = getResourceId(portletWindowId, httpRequest);
		
        final Locale locale = RequestContextUtils.getLocale(httpRequest);
        final PublicPortletCacheKey publicCacheKey = new PublicPortletCacheKey(definitionId, portletWindow, resourceId, locale);
		Element publicCacheElement = this.publicScopePortletResourceOutputCache.get(publicCacheKey);	
		if(publicCacheElement != null) {
			CachedPortletData cachedPortletData = (CachedPortletData) publicCacheElement.getValue();
			// only remove from cache if not using validation method
			if(publicCacheElement.isExpired() && StringUtils.isBlank(cachedPortletData.getEtag())) {
				this.publicScopePortletResourceOutputCache.remove(publicCacheKey);
                logger.debug("Publicly cached resource output with for key {} is expired", publicCacheKey);
				return null;
			}
			logger.debug("Returning publicly cached resource output with key {} for {}", publicCacheKey, portletWindow);
			return cachedPortletData;
		} else {
			// public cache contained no content, check private
		    final PrivatePortletCacheKey privateCacheKey = new PrivatePortletCacheKey(httpRequest.getSession().getId(), portletWindowId, entityId, publicCacheKey);
			Element privateCacheElement = this.privateScopePortletResourceOutputCache.get(privateCacheKey);
			if(privateCacheElement != null) {
				CachedPortletData cachedPortletData = (CachedPortletData) privateCacheElement.getValue();
				if(privateCacheElement.isExpired() && StringUtils.isBlank(cachedPortletData.getEtag())) {
					this.privateScopePortletResourceOutputCache.remove(privateCacheKey);
                    logger.debug("Privately cached resource output with for key {} is expired", privateCacheKey);
					return null;
				} 	
				logger.debug("Returning privately cached resource output with key {} for {}", privateCacheKey, portletWindow);
				return cachedPortletData;
			}
		}
		
		logger.debug("No cached resource output exists for portlet window {}", portletWindow);
		return null;
	}
	
    /**
     * Get the resourceId for the portlet request
     */
    protected String getResourceId(IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpRequest);
        final Map<IPortletWindowId, ? extends IPortletRequestInfo> portletRequestInfoMap = portalRequestInfo.getPortletRequestInfoMap();
        final IPortletRequestInfo portletRequestInfo = portletRequestInfoMap.get(portletWindowId);
        return portletRequestInfo != null ? portletRequestInfo.getResourceId() : null;
    }

    @Override
	public boolean shouldOutputBeCached(CacheControl cacheControl) {
		if(cacheControl.getExpirationTime() != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void cachePortletRenderOutput(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest, String content,
			CacheControl cacheControl) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
		
        final int expirationTime = cacheControl.getExpirationTime();
		CachedPortletData newData = new CachedPortletData();
		newData.setExpirationTimeSeconds(expirationTime);
		newData.setTimeStored(new Date());
		newData.setStringData(content);
		newData.setEtag(cacheControl.getETag());
		
		final Locale locale = RequestContextUtils.getLocale(httpRequest);
        final PublicPortletCacheKey publicCacheKey = new PublicPortletCacheKey(definitionId, portletWindow, locale);
		if(cacheControl.isPublicScope()) {
			newData.setCacheConfigurationMaxTTL((int)publicScopePortletRenderOutputCache.getCacheConfiguration().getTimeToLiveSeconds());
			Element publicCacheElement = constructCacheElement(publicCacheKey, newData, publicScopePortletRenderOutputCache.getCacheConfiguration(), cacheControl);
			this.publicScopePortletRenderOutputCache.put(publicCacheElement);
			
			logger.debug("Cached public render data under key {} for {}", publicCacheKey, portletWindow);
		} else {
		    final HttpSession session = httpRequest.getSession();
			newData.setCacheConfigurationMaxTTL((int)privateScopePortletRenderOutputCache.getCacheConfiguration().getTimeToLiveSeconds());
            final PrivatePortletCacheKey privateCacheKey = new PrivatePortletCacheKey(session.getId(), portletWindowId, entityId, publicCacheKey);
			Element privateCacheElement = constructCacheElement(privateCacheKey, newData, privateScopePortletRenderOutputCache.getCacheConfiguration(), cacheControl);
			this.privateScopePortletRenderOutputCache.put(privateCacheElement);
			
			logger.debug("Cached private render data under key {} for {}", privateCacheKey, portletWindow);
		}
	}

	@Override
    public void cachePortletResourceOutput(IPortletWindowId portletWindowId, HttpServletRequest httpRequest,
            CachedPortletData cachedPortletData, CacheControl cacheControl) {
	    
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
		
        final int expirationTime = cacheControl.getExpirationTime();
        cachedPortletData.setEtag(cacheControl.getETag());
        cachedPortletData.setExpirationTimeSeconds(expirationTime);
        cachedPortletData.setTimeStored(new Date());
        
        final String resourceId = getResourceId(portletWindowId, httpRequest);
        
        //TODO look at cacheability of the resource req/res
        
        final Locale locale = RequestContextUtils.getLocale(httpRequest);
        final PublicPortletCacheKey publicCacheKey = new PublicPortletCacheKey(definitionId, portletWindow, resourceId, locale);
		
		if(cacheControl.isPublicScope()) {
		    cachedPortletData.setCacheConfigurationMaxTTL((int)publicScopePortletResourceOutputCache.getCacheConfiguration().getTimeToLiveSeconds());
			Element publicCacheElement = constructCacheElement(publicCacheKey, cachedPortletData, publicScopePortletResourceOutputCache.getCacheConfiguration(), cacheControl);
			this.publicScopePortletResourceOutputCache.put(publicCacheElement);
			
			logger.debug("Cached public resource data under key {} for {}", publicCacheKey, portletWindow);
		} else {
		    final HttpSession session = httpRequest.getSession();
		    cachedPortletData.setCacheConfigurationMaxTTL((int)privateScopePortletResourceOutputCache.getCacheConfiguration().getTimeToLiveSeconds());
            final PrivatePortletCacheKey privateCacheKey = new PrivatePortletCacheKey(session.getId(), portletWindowId, entityId, publicCacheKey);
			Element privateCacheElement = constructCacheElement(privateCacheKey, cachedPortletData, privateScopePortletResourceOutputCache.getCacheConfiguration(), cacheControl);
			this.privateScopePortletResourceOutputCache.put(privateCacheElement);
			
			logger.debug("Cached private resource data under key {} for {}", privateCacheKey, portletWindow);
		}
	}
	
	/**
	 * Construct an appropriate Cache {@link Element} for the cacheKey and data.
	 * The element's ttl will be set depending on whether expiration or validation method is indicated from the CacheControl and the cache's configuration.
	 * 
	 * @param cacheKey
	 * @param data
	 * @param cacheConfig
	 * @param cacheControl
	 * @return
	 */
	protected Element constructCacheElement(Serializable cacheKey, CachedPortletData data, CacheConfiguration cacheConfig, CacheControl cacheControl) {
		// if validation method is being triggered, ignore expirationTime and defer to cache configuration
		if(StringUtils.isNotBlank(cacheControl.getETag())) {
			return new Element(cacheKey, data);
		}
		
		Integer cacheControlTTL = cacheControl.getExpirationTime();
		if(cacheControlTTL < 0) {
			// using expiration method, negative value for CacheControl#expirationTime means "forever" (e.g. ignore and defer to cache configuration)
			return new Element(cacheKey, data);
		}
		Long cacheConfigTTL = cacheConfig.getTimeToLiveSeconds();
		Long min = Math.min(cacheConfigTTL, cacheControlTTL.longValue());
		
		return new Element(cacheKey, data, null, null, min.intValue());
	}
	
	@Override
	public boolean purgeCachedPortletData(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();
        
        logger.debug("Purging all cached data for {}", portletWindow);
        
        boolean removed = false;

        //Remove all publicly cached render data for the portlet
        final Set<PublicPortletCacheKey> publicRenderKeys = this.publicPortletRenderCacheKeyTracker.getCacheKeys(definitionId);
        removed = removed || !publicRenderKeys.isEmpty();
        this.publicScopePortletRenderOutputCache.removeAll(publicRenderKeys);
        
        //Remove all publicly cached resource data for the portlet
        final Set<PublicPortletCacheKey> publicResourceKeys = this.publicPortletResourceCacheKeyTracker.getCacheKeys(definitionId);
        removed = removed || !publicResourceKeys.isEmpty();
        this.publicScopePortletResourceOutputCache.removeAll(publicResourceKeys);
        
        final HttpSession session = httpRequest.getSession();
        
        //Remove all privately cached render data
        final Set<PrivatePortletCacheKey> privateRenderKeys = this.privatePortletRenderCacheKeyTracker.getCacheKeys(session, portletWindowId);
        removed = removed || !privateRenderKeys.isEmpty();
        this.privateScopePortletRenderOutputCache.removeAll(privateRenderKeys);
        
        //Remove all privately cached render data
        final Set<PrivatePortletCacheKey> privateResourceKeys = this.privatePortletResourceCacheKeyTracker.getCacheKeys(session, portletWindowId);
        removed = removed || !privateResourceKeys.isEmpty();
        this.privateScopePortletResourceOutputCache.removeAll(privateResourceKeys);

        //If any keys were found remove them
        return removed;
	}
}
