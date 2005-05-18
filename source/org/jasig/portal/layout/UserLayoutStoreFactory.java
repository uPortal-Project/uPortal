/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import org.jasig.portal.layout.alm.AggregatedUserLayoutStore;
import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Produces and manages a static singleton instance of IUserLayoutStore.
 * 
 * The {@link IUserLayoutStore} implementation that we use is that named by the 
 * portal.properties property "org.jasig.portal.layout.UserLayoutStoreFactory.implementation".
 * In the case where that property is not set or the IUserLayoutStore it names cannot
 * be instantiated, we fall back on the {@link AggregatedUserLayoutStore} as the default.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.
 * It has been moved to its present package to express that it is part of the
 * user layout infrastructure.
 * 
 * This class is final because it is not designed to be subclassed.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$ $Date$ 
 */
public final class UserLayoutStoreFactory {

    /**
     * The name of the portal.properties property the value of which is the name
     * of the IUserLayoutStore implementation we should use.
     */
    public static final String LAYOUT_STORE_IMPL_PROPERTY = 
        "org.jasig.portal.layout.UserLayoutStoreFactory.implementation";
    
    /**
     * Commons Logging log instance.
     */
    private static final Log LOG = 
        LogFactory.getLog(UserLayoutStoreFactory.class);
    
    
    /**
     * Static singleton instance of userLayoutStoreImpl.
     */
    private static IUserLayoutStore userLayoutStoreImpl = null;

    /**
     * The default IUserLayoutStore implementation we will fall back upon 
     * in the case where our property is not set or we cannot instantiate the 
     * IUserLayoutStore named by our property.
     * 
     * This field is default scoped so that our testcase can access it.  It is not intended
     * to be part of the public API of this factory.
     */
    private static final Class DEFAULT_LAYOUT_STORE = AggregatedUserLayoutStore.class;

  /**
   * Returns the singleton IUserLayoutStore instance, which will be that specified in portal.properties, 
   * an instance of the default IUserLayoutStore, or null, in that order.
   * 
   * That is, we return a static singleton IUserLayoutStore instance when possible,
   * prefering to return an instance of the IUserLayoutStore implementation named
   * in our portal.properties property.  If that property is not set or that implementation
   * cannot be successfully instantiated, we will return an instance of the default
   * IUserLayoutStore implementation.  If we cannot instantiate that default IUserLayoutStore
   * we will return null.
   * 
   * This method is synchronized to ensure a consistent return value, resolving a 
   * double checked locking problem.  By synchronizing, we ensure that when we
   * write to the userLayoutStoreImpl static field, that write will be available
   * to other threads when they obtain the lock and enter this method.
   * 
   * @return the configured or default IUserLayoutStore implementation, or null
   */
  public static synchronized IUserLayoutStore getUserLayoutStoreImpl() {

        // if we already have a singleton instance, return it.
        if (userLayoutStoreImpl != null) {
            return userLayoutStoreImpl;
        }

        // if we can instantiate one from portal.properties, let's do it

        // initialize the class name before the try so we can log the broken
        // class name if available.
        String className = "unknown";
        try {
            className = PropertiesManager.getProperty(LAYOUT_STORE_IMPL_PROPERTY);
            userLayoutStoreImpl = (IUserLayoutStore) Class.forName(className).newInstance();
            // note that we stored the static singleton instance
            LOG.info("Instantiated and stored singleton IUserLayoutStore of type " + className);
            return userLayoutStoreImpl;
            
        } catch (Exception e) {
            // if anything went wrong, log the problem and fall back on our
            // default layout store
            LOG.error("Unable to instantiate IUserLayoutStore implementation ["
                    + className + "], attempting to fall back on default of " + DEFAULT_LAYOUT_STORE.getName(), e);

            try {
                userLayoutStoreImpl = (IUserLayoutStore) DEFAULT_LAYOUT_STORE
                        .newInstance();
                
            } catch (Exception e2) {
                LOG.error("Error insantiating default layout store ["
                        + DEFAULT_LAYOUT_STORE + "]", e2);
            }
            // this will either return the default implementation if we succeeded in
            // instantiating it, or null if we did not succeed.
            return userLayoutStoreImpl;
        }
    }
  
}



