/**
 * 
 */
package org.jasig.portal.utils.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.springframework.beans.factory.annotation.Required;

/**
 * This class exposes some limited functions around the provided
 * {@link CacheManager}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class CacheManagementHelper {

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
	 * Call {@link Cache#flush} on the specified cache, if it
	 * exists and is alive.
	 * 
	 * @see Status#STATUS_ALIVE
	 * @see Cache#getStatus()
	 * @param cacheName
	 */
	public void flushCache(String cacheName) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if(null != cache && Status.STATUS_ALIVE.equals(cache.getStatus())) {
			cache.flush();
		}
	}
}
