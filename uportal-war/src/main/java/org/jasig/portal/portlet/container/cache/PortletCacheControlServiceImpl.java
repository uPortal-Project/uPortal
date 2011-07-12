/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

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
	//private static final String PRIVATE_SCOPE = "private";
	//private static final String PUBLIC_SCOPE = "public";
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletEntityRegistry portletEntityRegistry;
	private IPortletDefinitionRegistry portletDefinitionRegistry;
	
	// key=sessionId+windowId+entityId+definitionId; value=CachedPortletData
    private Cache privateScopePortletOutputCache;
    // key=definitionId+renderParams+publicRenderParams; value=CachedPortletData
    private Cache publicScopePortletDataCache;
    // default to 100 KB
    private int cacheSizeThreshold = 102400;
    /**
	 * @param portletDataCache the portletDataCache to set
	 */
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletOutputCache")
	public void setPrivateScopePortletOutputCache(Cache privateScopePortletOutputCache) {
		this.privateScopePortletOutputCache = privateScopePortletOutputCache;
	}
	/**
	 * @param publicScopePortletDataCache the publicScopePortletDataCache to set
	 */
    @Autowired
    @Qualifier("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletOutputCache")
	public void setPublicScopePortletDataCache(Cache publicScopePortletDataCache) {
		this.publicScopePortletDataCache = publicScopePortletDataCache;
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
	public CacheControl getPortletCacheControl(IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
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
	        CachedPortletData cachedData = getCachedPortletData(portletWindowId, httpRequest);
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
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#getCachedPortletData(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public CachedPortletData getCachedPortletData(
			IPortletWindowId portletWindowId, HttpServletRequest httpRequest) {
		
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
		
		Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters());
		Element publicCacheElement = this.publicScopePortletDataCache.get(publicCacheKey);
		if(publicCacheElement != null) {
			if(publicCacheElement.isExpired()) {
				this.publicScopePortletDataCache.remove(publicCacheKey);
				return null;
			} else {
				return (CachedPortletData) publicCacheElement.getValue();
			}
		} else {
			// public cache contained no content, check private
			final String sessionId = httpRequest.getSession().getId();
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(sessionId, portletWindowId, entityId, definitionId);
			Element privateCacheElement = this.privateScopePortletOutputCache.get(privateCacheKey);
			if(privateCacheElement != null) {
				if(privateCacheElement.isExpired()) {
					this.privateScopePortletOutputCache.remove(privateCacheKey);
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
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#shouldOutputBeCached(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean shouldOutputBeCached(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest) {
		CacheControl control = getPortletCacheControl(portletWindowId, httpRequest);
		if(control.getExpirationTime() != 0) {
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
		// TODO 
		//newData.setHeaders(Collections.emptyMap());
		//newData.setProperties(Collections.emptyMap());
		newData.setStringData(content);
		newData.setEtag(cacheControl.getETag());
		
		if(cacheControl.isPublicScope()) {
			Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters());
			Element publicCacheElement;
			if(expirationTime < 0) {
				// negative cacheControl expiration time means "cache forever"
				// use default cache timeToLive
				publicCacheElement = new Element(publicCacheKey, newData);
			} else {
				// null constructor arguments mean "use default from cache's configuration"
				publicCacheElement = new Element(publicCacheKey, newData, null, null, expirationTime);
			}
			
			
			this.publicScopePortletDataCache.put(publicCacheElement);		
		} else {
			final String sessionId = httpRequest.getSession().getId();
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(sessionId, portletWindowId, entityId, definitionId);
			Element privateCacheElement;
			if(expirationTime < 0) {
				// negative cacheControl expiration time means "cache forever"
				// use default cache timeToLive
				privateCacheElement = new Element(privateCacheKey, newData);
			} else {
				// null constructor arguments mean "use default from cache's configuration"
				privateCacheElement = new Element(privateCacheKey, newData, null, null, expirationTime);
			}
			
			this.privateScopePortletOutputCache.put(privateCacheElement);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.cache.IPortletCacheControlService#cachePortletData(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, byte[])
	 */
	@Override
	public void cachePortletResourceOutput(IPortletWindowId portletWindowId,
			HttpServletRequest httpRequest, byte[] content, String contentType, CacheControl cacheControl) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId);
        
        final IPortletEntityId entityId = portletWindow.getPortletEntityId();
        final IPortletEntity entity = this.portletEntityRegistry.getPortletEntity(httpRequest, entityId);
        final IPortletDefinitionId definitionId = entity.getPortletDefinitionId();	
		
		CachedPortletData newData = new CachedPortletData();
		newData.setByteData(content);
		newData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		newData.setTimeStored(new Date());
		newData.setContentType(contentType);
		//newData.setHeaders(Collections.emptyMap());
		//newData.setProperties(Collections.emptyMap());
		
		if(cacheControl.isPublicScope()) {
			Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters());
			Element publicCacheElement = new Element(publicCacheKey, newData, false, 0, cacheControl.getExpirationTime());
			this.publicScopePortletDataCache.put(publicCacheElement);		
		} else {
			final String sessionId = httpRequest.getSession().getId();
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(sessionId, portletWindowId, entityId, definitionId);
			Element privateCacheElement = new Element(privateCacheKey, newData, false, 0, cacheControl.getExpirationTime());
			this.privateScopePortletOutputCache.put(privateCacheElement);
		}
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
			Serializable publicCacheKey = generatePublicScopePortletDataCacheKey(definitionId, portletWindow.getRenderParameters(), portletWindow.getPublicRenderParameters());
			return this.publicScopePortletDataCache.remove(publicCacheKey);
			
				
		} else {
			final String sessionId = httpRequest.getSession().getId();
			Serializable privateCacheKey = generatePrivateScopePortletDataCacheKey(sessionId, portletWindowId, entityId, definitionId);
			return this.privateScopePortletOutputCache.remove(privateCacheKey);
		}
	}
	/**
     * Generate a cache key for the publicScopePortletDataCache.
     *
     * definitionId + renderParams + publicRenderParams + cacheControl#eTag
     * 
     * Internally uses {@link ArrayList} as it implements {@link Serializable} and an appropriate equals/hashCode.
     * 
     * @param portletDefinitionId
     * @param renderParameters
     * @param publicRenderParameters
     * @return
     */
    protected Serializable generatePublicScopePortletDataCacheKey(IPortletDefinitionId portletDefinitionId, Map<String,String[]> renderParameters, Map<String,String[]> publicRenderParameters) {
    	ArrayList<Object> key = new ArrayList<Object>();
    	key.add(portletDefinitionId);
    	key.add(renderParameters);
    	key.add(publicRenderParameters);
    	return key;
    }
    /**
     * Generate a cache key for the privateScopePortletDataCache.
     * 
     * sessionId + windowId + entityId + definitionId
     * 
     * Internally uses {@link ArrayList} as it implements {@link Serializable} and an appropriate equals/hashCode.
     * 
     * @param sessionId
     * @param windowId
     * @param entityId
     * @param definitionId
     * @return
     */
    protected Serializable generatePrivateScopePortletDataCacheKey(String sessionId, IPortletWindowId windowId, IPortletEntityId entityId, IPortletDefinitionId definitionId) {
    	ArrayList<Object> key = new ArrayList<Object>();
    	key.add(sessionId);
    	key.add(windowId);
    	key.add(entityId);
    	key.add(definitionId);
    	return key;
    }
	
}
