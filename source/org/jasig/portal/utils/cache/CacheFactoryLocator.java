/*
 * Copyright 2001, 2002 The JA-SIG Collaborative. All rights reserved. See
 * license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.cache.WhirlyCacheCacheFactory;
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

    /** Instance of Commons Logging for logging purposes */
    private static final Log log = LogFactory.getLog(CacheFactoryLocator.class);

    /** String name of the CacheFactory we wish to retrieve. */
    private static final String CACHE_FACTORY_BEAN = "cacheFactory";

    /** Single instance of the cache factory that uPortal will use. */
    private static CacheFactory cacheFactory;

    static {
        // This form of initialization is necessary b/c  we can't bootstrap the entire bean
        // container to perform tasks based on Ant scripts (e.g. publish channels or portlets).
        try {
            cacheFactory = ((CacheFactory) PortalApplicationContextFacade.getPortalApplicationContext().getBean(CACHE_FACTORY_BEAN));
        }
        catch (Throwable t) {
            log.warn("The 'cacheFactory' bean is unavailable", t);
            cacheFactory = new WhirlyCacheCacheFactory();   // default...
        }
    }

    /**
     * Method to retrieve the cache factory
     * @return the cache factory.
     */
    public static final CacheFactory getCacheFactory() {
        return cacheFactory;
    }
}
