/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.properties.MissingPropertyException;
import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Instantiates and maintains a static singleton instance of IPortletPreferencesStore as specified by
 * the "org.jasig.portal.PortletPreferencesStoreFactory.implementation" property in 
 * portal.properties .
 * 
 * This class is final because it is not designed to be extended.  If you need a 
 * Portlet Preference Store factory that implements some other strategy for creating
 * and managing a PortletPreferencesStore instance, then you'll need to write a new class.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.4
 */
public final class PortletPreferencesStoreFactory {
    
    /**
     * The name of the portal.properties property the value of which will be the 
     * fully qualified class name of the implementation of IPortletPreferencesStore that
     * we should use.
     */
    public static final String PORTLET_PREF_STORE_PROPERTY = 
        "org.jasig.portal.PortletPreferencesStoreFactory.implementation";

    /**
     * Commons logging logger.
     */
    private static final Log LOG = LogFactory.getLog(PortletPreferencesStoreFactory.class);
    
    /**
     * Static singleton IPortletPreferencesStore instance that we're managing.
     */
    private static IPortletPreferencesStore portletPreferencesStoreImpl = null;

    /**
     * The default IPortletPreferencesStore implementation we will use.
     * Default scoped to be available to our unit test.  Not intended to be exported
     * as part of the API provided by this static factory class.
     */
    static final Class DEFAULT_PREF_STORE_CLASS = RDBMPortletPreferencesStore.class;

    /**
     * Returns the singleton instance of IPortletPreferencesStore specified in 
     * portal.properties, or the default implementation, or null.
     * 
     * That is, this static factory creates and holds a static singleton instance of 
     * IPortletPreferencesStore.  The static singleton instance it holds will be an instance
     * of the class specified in our portal.properties property if possible.  If that fails,
     * because the property was not set or wasn't set properly or the implementation
     * it specified is broken or for any other reason, we fall back on instantiating and
     * using our default implementation.  This method returns a reference to that
     * static singleton.  If we can instantiate neither the specified nor the default
     * implementation, we return null.
     * 
     * We are synchronized to address an instance of the broken double checked
     * locking idiom that previously existed in this code.  By synchronizing we 
     * ensure that every invocation of this method will either discover a fully
     * instantiated static singleton instance of our IPortletPreferencesStore 
     * or will attempt to create and store such an implementation with immediate
     * write-back to main memory such that the store will be available to others 
     * invoking this method.  This synchronization approach is likely slightly less
     * performant than would be an eager initialization approach, and this
     * difference in performance probably doesn't matter at all, being drowned out
     * by the cost of actually consulting the IPortletPreferencesStore once it
     * is obtained.
     * 
     * @return an IPortletPreferencesStore implementation, or null
     */
    public static synchronized IPortletPreferencesStore getPortletPreferencesStoreImpl() {
        
        // if we've already established our static singleton instance, return it
        if (portletPreferencesStoreImpl != null) {
            return portletPreferencesStoreImpl;
        }
        
        // try to establish our static singleton instance using portal.properties
        
        // store the class name here so we can try to use it in any error logging
        String className = "unknown";
        
        try {
            // read the desired implementation class name from PropertiesManager
            className = PropertiesManager.getProperty(PORTLET_PREF_STORE_PROPERTY);
            
            // create and store the static singleton
            portletPreferencesStoreImpl = (IPortletPreferencesStore)Class.forName(className).newInstance();
            
            LOG.info("PortletPreferencesStoreFactory instantiated static singleton of type " 
                    + className + " as configured in portal.properties.");
            
            return portletPreferencesStoreImpl;
        } catch (MissingPropertyException mpe) {
            // our property was not set.  Log this and fall through to fall back on
            // our default.  We handle this specially because this is not necessarily 
            // an error condition - our deployer
            // may just wish to accept our default.
            
            LOG.info("The property " + PORTLET_PREF_STORE_PROPERTY 
                    + " was not set.  PortletPreferencesStoreFactory will try to fall back on"
                    + " its default IPortletPreferencesStore implementation, " 
                    + DEFAULT_PREF_STORE_CLASS);
            
        } catch (Exception e) {
            // something else went wrong.  Log this and fall through to fall back on
            // our default.
            
            LOG.error("There was an error instantiating IPortletPreferrencesStore implementation "
                    + className + " as specified in the property " 
                    + PORTLET_PREF_STORE_PROPERTY 
                    + ", so PortletPreferencesStoreFactory will fall back on its default of "
                    + DEFAULT_PREF_STORE_CLASS, e);
            
        }
            
        // we failed to instantiate our configured implementation, so now we're going
        // to try our default.
        
        try {
            // instantiate our default and store it as our static singleton instance
            portletPreferencesStoreImpl = (IPortletPreferencesStore) DEFAULT_PREF_STORE_CLASS.newInstance();
            return portletPreferencesStoreImpl;
        
        } catch (Exception e) {
            // log the error and return null as our API specifies
            LOG.error("PortletPreferencesStoreFactory was unable to instantiate its "
                    + "default IPortletPreferencesStore implementation "
                    + DEFAULT_PREF_STORE_CLASS, e);
        }
        
        // if we can return neither our configured implementation nor our default,
        // we return null.
        return null;
        
    }
    
    /**
     * Resets our static singleton to null, thereby resetting our state and making the
     * next invocation of our getter method try to instantiate an IPortletPreferencesStore anew.
     * 
     * This method exists to make unit testing feasible and should not be considered
     * part of the API of this static factory class.
     */
    static void reset() {
        portletPreferencesStoreImpl = null;
    }

}
