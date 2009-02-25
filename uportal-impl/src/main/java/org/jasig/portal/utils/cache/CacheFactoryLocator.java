/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.cache;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

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
 * @deprecated Code should be injected with the 'cacheFactory' wherever possible 
 */
public class CacheFactoryLocator {

    /** String name of the CacheFactory we wish to retrieve. */
    private static final String CACHE_FACTORY_BEAN = "cacheFactory";

    /**
     * Method to retrieve the cache factory
     * @return the cache factory.
     */
    public static final CacheFactory getCacheFactory() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final CacheFactory cacheFactory = (CacheFactory) applicationContext.getBean(CACHE_FACTORY_BEAN, CacheFactory.class);
        return cacheFactory;
    }
}
