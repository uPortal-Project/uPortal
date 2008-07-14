/**
 * Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.dlm;

import java.util.Map;
import org.w3c.dom.Document;

import org.springframework.beans.factory.annotation.Required;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.cache.CacheFactory;

/**
 * Provides caching services for layouts contained in 
 * <code>DistributedLayoutManager</code> instances.  This class is an old-school 
 * singleton whose backing-map is "injected" by a spring context at startup. 
 */
public final class LayoutCachingService {

	/**
	 * The name of the cache used by <code>LayoutCachingService</code>.  A cache 
	 * of this name must be defined within uPortal's <code>'cacheFactory'</code> 
	 * bean.
	 */
	private static final String CACHE_NAME = "org.jasig.portal.layout.dlm.LAYOUT_CACHE";
	
	/**
	 * Single(ton) instance of this class.
	 */
	private static LayoutCachingService instance = null;
	
	/**
	 * "Injected" at startup by the bean container.  This object is typically 
	 * defined as the bean with id='cacheFactory' in cacheContext.xml.
	 */
	private CacheFactory cacheFactory = null;
	
    private final Log log = LogFactory.getLog(LayoutCachingService.class);
	
	/*
	 * Public API.
	 */
	    
    /**
     * Accessor method or the single(ton) instance of this class.
     */
    public static LayoutCachingService getInstance() {
		
		if (instance == null) {
			init();
		}

		return instance;
		
	}
	
    /**
     * Called by the spring IoC container at startup.
     * 
     * @param cf Ordinarily 'cacheFactory' bean defined in cacheContext.xml 
     */
	@Required
	public void setCacheFactory(CacheFactory cf) {
		
		// Assertions.
		if (cf == null) {
			String msg = "Argument 'cpf [CacheFactory]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		
		log.debug("INITIALIZING:  Setting cacheFactory.");

		this.cacheFactory = cf;
		
	}
	
	/**
	 * Provides clients of <code>LayoutCachingService</code> with access to the 
	 * layout cache.
	 * 
	 * @return A <code>Map</code> of cached layouts.
	 */
	public Map<String,Document> getLayoutCache() {
		return cacheFactory.getCache(CACHE_NAME);
	}
	
	/*
	 * Implementation.
	 */

	/**
	 * Private as per classic Singleton pattern. 
	 */
	private LayoutCachingService() {
		log.debug("INITIALIZING:  Constructing.");
	}
	
	/**
	 * Method that creates the single(ton) instance of 
	 * <code>LayoutCachingService</code>, synchronized to be sure it only 
	 * happens once.
	 */
	private static synchronized void init() {

		// Make sure we only create one instance...
		if (instance != null) {
			// Must have invoked getInstance() w/ 2 threads.
			return;
		}

		instance = new LayoutCachingService();
		
	}
	
}
