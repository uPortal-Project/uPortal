/*
 * Copyright 2001, 2002 The JA-SIG Collaborative. All rights reserved. See
 * license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextListener;
import org.springframework.web.context.WebApplicationContext;

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

    /**
     * Method to retrieve the cache factory
     * @return the cache factory.
     */
    public static final CacheFactory getCacheFactory() {
        try {
            final WebApplicationContext webAppCtx = PortalApplicationContextListener.getRequiredWebApplicationContext();
            final CacheFactory cacheFactory = (CacheFactory)webAppCtx.getBean(CACHE_FACTORY_BEAN, CacheFactory.class);
            return cacheFactory;
        }
        catch (Exception e) {
            log.warn("Failed to load a CacheFactory interface from the Spring WebApplicationContext for bean name '" + CACHE_FACTORY_BEAN + "'. " + WhirlyCacheCacheFactory.class + " will be used.", e);
            return new WhirlyCacheCacheFactory();
        }
    }
}
