/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.i18n;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Produces an implementation of ILocaleStore
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LocaleStoreFactory {

    private static final Log log = LogFactory.getLog(LocaleStoreFactory.class);
    
    private static ILocaleStore localeStoreImpl = null;

    static {
      // Retrieve the class name of the concrete ILocaleStore implementation
      String className = PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleStoreFactory.implementation");
      // Fail if this is not found
      if (className == null)
        log.error( "LocaleStoreFactory: org.jasig.portal.i18n.LocaleStoreFactory.implementation must be specified in portal.properties");
      try {
        // Create an instance of the ILocaleStore as specified in portal.properties
        localeStoreImpl = (ILocaleStore)Class.forName(className).newInstance();
      } catch (Exception e) {
        log.error( "LocaleStoreFactory: Could not instantiate " + className, e);
      }
    }

    /**
     * Returns an instance of the ILocaleStore specified in portal.properties
     * @return an ILocaleStore implementation
     */
    public static ILocaleStore getLocaleStoreImpl() {
      return localeStoreImpl;
    }

}
