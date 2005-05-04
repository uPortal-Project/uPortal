/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.layout.UserLayoutStoreFactory;
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
    private static String className;

    static {
        try {
            // Retrieve the class name of the concrete IPortletPreferencesStore implementation
            className = PropertiesManager.getProperty("org.jasig.portal.PortletPreferencesStoreFactory.implementation");
        } catch (Exception e ) {}
    
        if (className == null || className.length() == 0 )
            log.error( "PortletPreferencesStoreFactory: org.jasig.portal.PortletPreferencesStoreFactory.implementation must be specified in portal.properties");
    }

    /**
     * Returns an instance of the IPortletPreferencesStore specified in portal.properties
     * 
     * @return an IPortletPreferencesStore implementation
     */
    public static IPortletPreferencesStore getPortletPreferencesStoreImpl() {
        try {
            return getPortletPreferencesImpl( className );
        } catch ( PortalException pe ) {
            log.error( "PortletPreferencesStoreFactory: Could not load " + className, pe);
            
            try {
                return getPortletPreferencesImpl( DEFAULT_CLASS_NAME );
            } catch ( PortalException pe1 ) {
                log.error( "PortletPreferencesStoreFactory: Could not load " + DEFAULT_CLASS_NAME, pe1);
                return null;
            }
        }
    }

    /**
     * Returns an instance of the IPortletPreferencesStore
     * 
     * @param className <code>String</code> object specifying the class to be loaded
     * @return an IPortletPreferencesStore implementation
     */
    protected static IPortletPreferencesStore getPortletPreferencesImpl( String className ) throws PortalException {
      try {
          if (portletPreferencesStoreImpl == null) {
              synchronized (UserLayoutStoreFactory.class) {
                  if (portletPreferencesStoreImpl == null) {
                      portletPreferencesStoreImpl = (IPortletPreferencesStore)Class.forName(className).newInstance();
                  }
              }
          }
          return portletPreferencesStoreImpl;
      } catch (Exception e) {
          log.error( "PortletPreferencesStoreFactory: Could not instantiate " + className, e);
          throw new PortalException(e);
      }
    }

}
