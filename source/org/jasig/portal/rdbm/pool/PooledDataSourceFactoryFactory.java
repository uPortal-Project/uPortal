/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.properties.PropertiesManager;

/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public final class PooledDataSourceFactoryFactory {

    private static final Log log = LogFactory.getLog(PooledDataSourceFactoryFactory.class);

    private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.RDBMPortletPreferencesStore";

    private static IPooledDataSourceFactory pooledDataSourceFactoryImpl = null;
    private static String className = null;

    static {
        try {
            // Retrieve the class name of the concrete IPortletPreferencesStore implementation
            className = PropertiesManager.getProperty("org.jasig.portal.PooledDataSourceFactory.implementation");
        }
        catch (Exception e) { }

        if (className == null || className.length() == 0)
            log.error("org.jasig.portal.PooledDataSourceFactory.implementation must be specified in portal.properties");
    }

    public static final IPooledDataSourceFactory getPooledDataSourceFactory() {
        try {
            return getPooledDataSourceFactory(className);
        }
        catch (PortalException pe) {
            log.error(" Could not load " + className, pe);

            try {
                return getPooledDataSourceFactory(DEFAULT_CLASS_NAME);
            }
            catch (PortalException pe1) {
                log.error("Could not load " + DEFAULT_CLASS_NAME, pe1);
                return null;
            }
        }
    }

    protected static IPooledDataSourceFactory getPooledDataSourceFactory(String className)
            throws PortalException {
        try {
            if (pooledDataSourceFactoryImpl == null) {
                synchronized (UserLayoutStoreFactory.class) {
                    if (pooledDataSourceFactoryImpl == null) {
                        pooledDataSourceFactoryImpl = (IPooledDataSourceFactory)Class.forName(className).newInstance();
                    }
                }
            }
            
            return pooledDataSourceFactoryImpl;
        }
        catch (Exception e) {
            log.error("Could not instantiate " + className, e);
            throw new PortalException(e.getMessage());
        }
    }

    private PooledDataSourceFactoryFactory() { }
}