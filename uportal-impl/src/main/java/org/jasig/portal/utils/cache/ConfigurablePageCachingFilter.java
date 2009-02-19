package org.jasig.portal.utils.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.web.filter.SimplePageCachingFilter;

/**
 * ConfigurablePageCachingFilter provides a subclass of ehCache's 
 * SimplePageCachingFilter that allows configuration of the cache name
 * and cache manager.
 * 
 * @author Jen Bourey
 */
public class ConfigurablePageCachingFilter extends SimplePageCachingFilter {

	private static final String DEFAULT_CACHE_NAME = 
		"org.jasig.portal.utils.cache.ConfigurablePageCachingFilter.PAGE_CACHE";
	
	private final CacheManager cacheManager;
	private final String cacheName;
	
	/**
	 * 
	 * @param cacheManager
	 */
	public ConfigurablePageCachingFilter(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		this.cacheName = DEFAULT_CACHE_NAME;
		super.doInit(this.filterConfig);
	}
	
	/**
	 * 
	 * @param cacheManager
	 * @param cacheName
	 */
	public ConfigurablePageCachingFilter(CacheManager cacheManager, String cacheName) {
		this.cacheManager = cacheManager;
		this.cacheName = cacheName;
		super.doInit(this.filterConfig);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @Override
	 */
	protected CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @Override
	 */
	public String getCacheName() {
		return cacheName;
	}
	
}
