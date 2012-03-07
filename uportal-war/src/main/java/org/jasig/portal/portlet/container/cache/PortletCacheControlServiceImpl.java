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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class PortletCacheControlServiceImpl implements IPortletCacheControlService {

	protected static final String REQUEST_ATTRIBUTE__PORTLET_CACHE_CONTROL_MAP = PortletCacheControlServiceImpl.class.getName() + ".PORTLET_CACHE_CONTROL_MAP";
	private final Log log = LogFactory.getLog(this.getClass());
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletEntityRegistry portletEntityRegistry;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	
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
    /**
	 * @param privateScopePortletRenderOutputCache the privateScopePortletRenderOutputCache to set
	 */
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletRenderOutputCache")
	public void setPrivateScopePortletRenderOutputCache(Ehcache privateScopePortletRenderOutputCache) {
		this.privateScopePortletRenderOutputCache = privateScopePortletRenderOutputCache;
	}
	/**
	 * @param publicScopePortletRenderOutputCache the publicScopePortletRenderOutputCache to set
	 */
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletRenderOutputCache")
	public void setPublicScopePortletRenderOutputCache(Ehcache publicScopePortletRenderOutputCache) {
		this.publicScopePortletRenderOutputCache = publicScopePortletRenderOutputCache;
	}
    /**
     * @param privateScopePortletResourceOutputCache
     */
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletResourceOutputCache")
    public void setPrivateScopePortletResourceOutputCache(
    		Ehcache privateScopePortletResourceOutputCache) {
		this.privateScopePortletResourceOutputCache = privateScopePortletResourceOutputCache;
	}
    /**
     * @param publicScopePortletResourceOutputCache
     */
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletResourceOutputCache")
	public void setPublicScopePortletResourceOutputCache(
			Ehcache publicScopePortletResourceOutputCache) {
		this.publicScopePortletResourceOutputCache = publicScopePortletResourceOutputCache;
	}
	/**
	 * @param cacheSizeThreshold the cacheSizeThreshold to set
	 */
	public void setCacheSizeThreshold(int cacheSizeThreshold) {
		this.cacheSizeThreshold = cacheSizeThreshold;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#getCacheSizeThreshold()
	 */
	@Override
	public int getCacheSizeThreshold() {
		return cacheSizeThreshold;
	}
	/**
	 * @param portletWindowRegistry
	 */
	@Autowired
	public void setPortletWindowRegistry(
			IPortletWindowRegistry portletWindowRegistry) {
		this.portletWindowRegistry = portletWindowRegistry;
	}
	/**
	 * @param portletEntityRegistry
	 */
	@Autowired
	public void setPortletEntityRegistry(
			IPortletEntityRegistry portletEntityRegistry) {
		this.portletEntityRegistry = portletEntityRegistry;
	}
	/**
	 * @param portletDefinitionRegistry
	 */
	@Autowired
	public void setPortletDefinitionRegistry(
			IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
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
	        	log.warn("portletWindowRegistry returned null portletWindow for " + portletWindowId + ", returning default cacheControl");
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
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#getPortletResourceCacheControl(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public CacheControl getPortletResourceCacheControl(
			IPortletWindowId portletWindowId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		Map<IPortletWindowId, CacheControl> map = PortalWebUtils.getMapRequestAttribute(httpRequest, REQUEST_ATTRIBUTE__PORTLET_CACHE_CONTROL_MAP);
		CacheControl cacheControl = map.get(portletWindowId);
		if(cacheControl == null) {
			cacheControl = new CacheControlImpl(httpResponse);
			final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
	        if(portletWindow == null) {
	        	log.warn("portletWindowRegistry returned null portletWindow for " + portletWindowId + ", returning default cacheControl");
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
		
		Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters(), RequestContextUtils.getLocale(httpRequest));
		Element publicCacheElement = this.publicScopePortletRenderOutputCache.get(publicCacheKey);
		if(publicCacheElement != null) {
			if(publicCacheElement.isExpired()) {
				this.publicScopePortletRenderOutputCache.remove(publicCacheKey);
				return null;
			} else {
				return (CachedPortletData) publicCacheElement.getValue();
			}
		} else {
			// public cache contained no content, check private
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(httpRequest, portletWindowId, entityId, definitionId, portletWindow.getRenderParameters());
			Element privateCacheElement = this.privateScopePortletRenderOutputCache.get(privateCacheKey);
			if(privateCacheElement != null) {
				if(privateCacheElement.isExpired()) {
					this.privateScopePortletRenderOutputCache.remove(privateCacheKey);
					return null;
				} else {
					return (CachedPortletData) privateCacheElement.getValue();
				}
			}
		}
		
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
		
		Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters(), RequestContextUtils.getLocale(httpRequest));
		Element publicCacheElement = this.publicScopePortletResourceOutputCache.get(publicCacheKey);	
		if(publicCacheElement != null) {
			CachedPortletData cachedPortletData = (CachedPortletData) publicCacheElement.getValue();
			// only remove from cache if not using validation method
			if(publicCacheElement.isExpired() && StringUtils.isBlank(cachedPortletData.getEtag())) {
				this.publicScopePortletResourceOutputCache.remove(publicCacheKey);
				return null;
			}
			return cachedPortletData;
		} else {
			// public cache contained no content, check private
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(httpRequest, portletWindowId, entityId, definitionId, portletWindow.getRenderParameters());
			Element privateCacheElement = this.privateScopePortletResourceOutputCache.get(privateCacheKey);
			if(privateCacheElement != null) {
				CachedPortletData cachedPortletData = (CachedPortletData) privateCacheElement.getValue();
				if(privateCacheElement.isExpired() && StringUtils.isBlank(cachedPortletData.getEtag())) {
					this.privateScopePortletResourceOutputCache.remove(privateCacheKey);
					return null;
				} 	
				return cachedPortletData;
			}
		}
		
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#shouldOutputBeCached(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean shouldOutputBeCached(CacheControl cacheControl) {
		if(cacheControl.getExpirationTime() != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#cachePortletRenderOutput(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, java.lang.String, javax.portlet.CacheControl)
	 */
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
		
		if(cacheControl.isPublicScope()) {
			newData.setCacheConfigurationMaxTTL(new Long(publicScopePortletRenderOutputCache.getCacheConfiguration().getTimeToLiveSeconds()).intValue());
			Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters(), RequestContextUtils.getLocale(httpRequest));
			Element publicCacheElement = constructCacheElement(publicCacheKey, newData, publicScopePortletRenderOutputCache.getCacheConfiguration(), cacheControl);
			this.publicScopePortletRenderOutputCache.put(publicCacheElement);		
		} else {
			newData.setCacheConfigurationMaxTTL(new Long(privateScopePortletRenderOutputCache.getCacheConfiguration().getTimeToLiveSeconds()).intValue());
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(httpRequest, portletWindowId, entityId, definitionId, portletWindow.getRenderParameters());
			Element privateCacheElement = constructCacheElement(privateCacheKey, newData, privateScopePortletRenderOutputCache.getCacheConfiguration(), cacheControl);
			this.privateScopePortletRenderOutputCache.put(privateCacheElement);
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
		
		if(cacheControl.isPublicScope()) {
		    cachedPortletData.setCacheConfigurationMaxTTL(new Long(publicScopePortletResourceOutputCache.getCacheConfiguration().getTimeToLiveSeconds()).intValue());
			Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters(), RequestContextUtils.getLocale(httpRequest));
			Element publicCacheElement = constructCacheElement(publicCacheKey, cachedPortletData, publicScopePortletResourceOutputCache.getCacheConfiguration(), cacheControl);
			this.publicScopePortletResourceOutputCache.put(publicCacheElement);		
		} else {
		    cachedPortletData.setCacheConfigurationMaxTTL(new Long(privateScopePortletResourceOutputCache.getCacheConfiguration().getTimeToLiveSeconds()).intValue());
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(httpRequest, portletWindowId, entityId, definitionId, portletWindow.getRenderParameters());
			Element privateCacheElement = constructCacheElement(privateCacheKey, cachedPortletData, privateScopePortletResourceOutputCache.getCacheConfiguration(), cacheControl);
			this.privateScopePortletResourceOutputCache.put(privateCacheElement);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#purgeCachedPortletData(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean purgeCachedPortletData(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest, CacheControl cacheControl) {
		
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
		if(cacheControl.isPublicScope()) {
			Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters(), RequestContextUtils.getLocale(httpRequest));
			boolean renderPurged = this.publicScopePortletRenderOutputCache.remove(publicCacheKey);
			return this.publicScopePortletResourceOutputCache.remove(publicCacheKey) || renderPurged;
		} else {
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(httpRequest, portletWindowId, entityId, definitionId, portletWindow.getRenderParameters());
			boolean renderPurged = this.privateScopePortletRenderOutputCache.remove(privateCacheKey);
			return this.privateScopePortletResourceOutputCache.remove(privateCacheKey) || renderPurged;
		}
	}
	/**
     * Generate a cache key for the public scope cache.
     *
     * definitionId + renderParams + publicRenderParams
     * 
     * Internally uses {@link ArrayList} as it implements {@link Serializable} and an appropriate equals/hashCode.
     * 
     * @param portletDefinitionId
     * @param renderParameters
     * @param publicRenderParameters
     * @return
     */
    protected Serializable generatePublicScopePortletDataCacheKey(IPortletDefinitionId portletDefinitionId, Map<String,String[]> renderParameters, Map<String,String[]> publicRenderParameters, Locale locale) {
    	ArrayList<Object> key = new ArrayList<Object>();
    	key.add(portletDefinitionId);
    	key.add(renderParameters);
    	key.add(publicRenderParameters);
        key.add(locale);
    	return key;
    }
    /**
     * Generate a cache key for the private scope Cache.
     * 
     * sessionId + windowId + entityId + definitionId + renderParameters
     * 
     * Internally uses {@link ArrayList} as it implements {@link Serializable} and an appropriate equals/hashCode.
     * 
     * @param request
     * @param windowId
     * @param entityId
     * @param definitionId
     * @param renderParameters
     * @return
     */
    protected Serializable generatePrivateScopePortletDataCacheKey(HttpServletRequest request, IPortletWindowId windowId, IPortletEntityId entityId, IPortletDefinitionId definitionId, Map<String,String[]> renderParameters) {
    	ArrayList<Object> key = new ArrayList<Object>();
    	final String sessionId = request.getSession().getId();
    	key.add(sessionId);
    	key.add(windowId);
    	key.add(entityId);
    	key.add(definitionId);
    	key.add(renderParameters);
    	final Locale locale =  RequestContextUtils.getLocale(request);
    	key.add(locale);
    	return key;
    }
	
}
