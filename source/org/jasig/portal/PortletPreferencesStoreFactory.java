/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Locates and creates an implementation of IPortletPreferencesStore as specified by
 * the "org.jasig.portal.PortletPreferencesStoreFactory.implementation" property in 
 * portal.properties
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ 
 */
public class PortletPreferencesStoreFactory {

    private static final Log log = LogFactory.getLog(PortletPreferencesStoreFactory.class);
    
    private static IPortletPreferencesStore portletPreferencesStoreImpl = null;

    private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.RDBMPortletPreferencesStore";

    static {
        // preserve previous implementation behavior
        String className = null;
        
        try {
            // Retrieve the class name of the concrete IPortletPreferencesStore implementation
            className = PropertiesManager.getProperty("org.jasig.portal.PortletPreferencesStoreFactory.implementation");
        } catch (Exception e) {
            log.error( "PortletPreferencesStoreFactory: org.jasig.portal.PortletPreferencesStoreFactory.implementation must be specified in portal.properties");
        }
        
        // retrieve the configured (or default) instance
        try {
        	portletPreferencesStoreImpl = (IPortletPreferencesStore)Class.forName(className).newInstance();
            
        } catch (Exception e ) {
        	log.error( "PortletPreferencesStoreFactory: Could not instantiate " + className);
            
            
            // if unable to load AND it was not the default class name, try the default
            if (!DEFAULT_CLASS_NAME.equals(className)) {
                try {
                	portletPreferencesStoreImpl = (IPortletPreferencesStore)Class.forName(DEFAULT_CLASS_NAME).newInstance();
                } catch (Exception e2) {
                    log.error( "PortletPreferencesStoreFactory: Could not instantiate " + DEFAULT_CLASS_NAME);
                }
            }
        }
        
        // in the end, portletPreferencesStoreImpl may still be null
   }

    /**
     * Returns an instance of the IPortletPreferencesStore specified in portal.properties
     * 
     * @return an IPortletPreferencesStore implementation
     */
    public static IPortletPreferencesStore getPortletPreferencesStoreImpl() {
        return (portletPreferencesStoreImpl);
    }

}
