/* Copyright 2004-2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.MissingPropertyException;
import org.jasig.portal.properties.PropertiesManager;

/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class PooledDataSourceFactoryFactory {

    /**
     * The name of the portal.properties property the value of which will be the name of the
     * IPooledDataSourceFactory implementation that we will use.
     */
    public static final String POOLED_DATA_SOURCE_FACTORY_PROPERTY = "org.jasig.portal.PooledDataSourceFactory.implementation";
    
    /**
     * A Commons Logging log instance.
     */
    private static final Log LOG = LogFactory.getLog(PooledDataSourceFactoryFactory.class);

    /**
     * Our default IPooledDataSourceFactory implementation upon which we will fall back if
     * our property is not set or we cannot instantiate the implementation specified by
     * our property.
     * 
     * Default scoped to be accessible to our testcase.
     */
    static final Class DEFAULT_POOLED_DATASOURCE_FACTORY = DBCPDataSourceFactory.class;

    /**
     * Our static singleton instance that we're managing.
     */
    private static IPooledDataSourceFactory pooledDataSourceFactoryImpl = null;

    /**
     * Get a reference to our static singleton instance of IPooledDataSourceFactory
     * as specified in our portal.properties property, or our default implementation, or null.
     * 
     * That is, this method returns our static singleton instance of IPooledDataSourceFactory.
     * That instance will be an instance of the implementation named in our portal.properties property
     * if we are able to instantiate that, or an instance of our default implementation if we were
     * unable to instantiate the configured implementation, or null if we can instantiate neither.
     * 
     * This method is synchronized to avoid using the much-feared Double Checked Locking
     * idiom.  By synchronizing we force the change we make when we first (lazily) initialize 
     * our pooledDataSourceFactoryImpl to be written back to main memory and thereby
     * be available to other threads when they succeed in obtaining the lock and entering
     * this method.
     * 
     * @return the configured or default IPooledDataSourceFactory, or null if neither can be instantiated.
     */
    public static synchronized IPooledDataSourceFactory getPooledDataSourceFactory() {
        
        // if we already have instantiated our static singleton instance, return it
        if (pooledDataSourceFactoryImpl != null) {
            return pooledDataSourceFactoryImpl;
        }
        
        // initialize the class name so we can try to use it in our error logging
        // on failure
        String className = "unknown";
        try {
            className = PropertiesManager.getProperty(POOLED_DATA_SOURCE_FACTORY_PROPERTY);
            pooledDataSourceFactoryImpl = (IPooledDataSourceFactory)Class.forName(className).newInstance();
            return pooledDataSourceFactoryImpl;
        } catch (MissingPropertyException mpe) {
            // we recover from our property not having been set by falling back on our default.
            // This is not necessarily an error condition.  The deployer may simply have chosen
            // not to set our property and expects us to fall back on our default.
            LOG.info("The portal.properties property " + POOLED_DATA_SOURCE_FACTORY_PROPERTY 
                    + " was not set, so PooledDataSourceFactoryFactory will fall back on its default " 
                    + "IPooledDataSourceFactory implementation, which is "
                    + DEFAULT_POOLED_DATASOURCE_FACTORY);
        } catch (Exception e) {
            // we also recover from any other problem that happened in instantiating the configured
            // IPooledDataSourceFactory by falling back on our default.  However, this is an error
            // condition and logged as such.
            
            LOG.error("Unable to instantiate the configured IPooledDataSourceFactory :" 
                    + className + ", so falling back on default of " 
                    + DEFAULT_POOLED_DATASOURCE_FACTORY, e);
            
        }
        
        try {
            pooledDataSourceFactoryImpl = (IPooledDataSourceFactory) DEFAULT_POOLED_DATASOURCE_FACTORY.newInstance();
        }
        catch (Exception e2) {
            LOG.error("Could not instantiate our default IPooledDataSourceFactory " + DEFAULT_POOLED_DATASOURCE_FACTORY, e2);
        }
        
        // the field will either be our default if we succeeded in instantiating our default
        // or will be null if we failed.
        return pooledDataSourceFactoryImpl;
        
    }

    /**
     * This private constructor prevents instantiation of this static 
     * factory class.
     */
    private PooledDataSourceFactoryFactory() { 
        // do nothing
    }
    
    /**
     * Clears this static factory's static singleton instance of 
     * IPooledDataSourceFactory.  This method exists to support the unit test
     * for this class and should not be considered part of the API exported
     * by this class.
     */
    static void reset() {
        pooledDataSourceFactoryImpl = null;
    }
}