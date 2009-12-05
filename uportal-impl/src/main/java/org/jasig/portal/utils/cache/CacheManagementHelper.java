/**
 * 
 */
package org.jasig.portal.utils.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;



/**
 * This class exposes some limited functions around the provided
 * {@link CacheManager}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class CacheManagementHelper {

	protected final Log logger = LogFactory.getLog(this.getClass());
	
	private CacheManager cacheManager;

	/**
	 * @param cacheManager the cacheManager to set
	 */
	@Required
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	/**
	 * 
	 * @see CacheManager#getCacheNames()
	 * @return a {@link List} of cache names
	 */
	public List<String> getCacheNames() {
		List<String> result = new ArrayList<String>();
		result.addAll(Arrays.asList(this.cacheManager.getCacheNames()));
		Collections.sort(result);
		return result;
	}
	
	/**
	 * 
	 * @see Status#STATUS_ALIVE
	 * @see Cache#getStatus()
	 * @see Cache#getStatistics()
	 * @param cacheName
	 * @return the {@link Statistics} for the specified cache; returns null of cache is not alive or doesn't exist
	 */
	public Statistics getCacheStatistics(String cacheName) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if(null != cache && Status.STATUS_ALIVE.equals(cache.getStatus())) {
			Statistics result = cache.getStatistics();
			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * Call {@link Cache#removeAll()} on the specified cache, if it
	 * exists and is alive.
	 * 
	 * @see Status#STATUS_ALIVE
	 * @see Cache#getStatus()
	 * @param cacheName
	 */
	public void clearCache(String cacheName) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if(null != cache && Status.STATUS_ALIVE.equals(cache.getStatus())) {
			cache.removeAll();
			logger.warn("finished removeAll for cache: " + cacheName);
		}
	}
	
	/**
	 * Call {@link #clearCache(String)} on ALL caches.
	 */
	public void clearAllCaches() {
		logger.warn("beginning request to clear all caches");
		for(String cacheName: getCacheNames()) {
			clearCache(cacheName);
		}
		logger.warn("completed request to clear all caches");
	}
}
