/*
 * Copyright 2001, 2002 The JA-SIG Collaborative. All rights reserved. See
 * license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import org.jasig.portal.spring.PortalApplicationContextFacade;

/**
 * 
 * Convenience locator class for locating the CacheFactory without each class
 * needing the cache factory to do the lookup in the application context.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.5
 * 
 * @see CacheFactory
 *
 */
public class CacheFactoryLocator {
	
	/** String name of the CacheFactory we wish to retrieve. */
    private static final String CACHE_FACTORY_BEAN = "cacheFactory";
	
	/** Single instance of the cache factory that uPortal will use. */
	private final static CacheFactory cacheFactory = ((CacheFactory) PortalApplicationContextFacade.getPortalApplicationContext().getBean(CACHE_FACTORY_BEAN));

	/**
	 * Method to retrieve the cache factory
	 * @return the cache factory.
	 */
	public static final CacheFactory getCacheFactory() {
		return cacheFactory;
	}
}
