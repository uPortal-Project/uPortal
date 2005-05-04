/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import org.jasig.portal.PortalException;
import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Produces an implementation of IUserLayoutStore.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.
 * It has been moved to its present package to express that it is part of the
 * user layout infrastructure.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class UserLayoutStoreFactory {

    private static final Log log = LogFactory.getLog(UserLayoutStoreFactory.class);
    
  private static IUserLayoutStore userLayoutStoreImpl = null;

  private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.layout.alm.AggregatedUserLayoutStore";
  private static String className;

  static {
    try {
      // Retrieve the class name of the concrete IUserLayoutStore implementation
      className = PropertiesManager.getProperty("org.jasig.portal.layout.UserLayoutStoreFactory.implementation");
    } catch (Exception e ) {}
  
    if (className == null || className.length() == 0 )
      log.error( "UserLayoutStoreFactory: org.jasig.portal.layout.UserLayoutStoreFactory.implementation must be specified in portal.properties");
  }

  /**
   * Returns an instance of the IUserLayoutStore specified in portal.properties
   * @return an IUserLayoutStore implementation
   */
  public static IUserLayoutStore getUserLayoutStoreImpl() {
    try {
      return getUserLayoutStoreImpl( className );
    } catch ( PortalException pe ) {
      log.error( "UserLayoutStoreFactory: Could not load " + className, pe);
      try {
        return getUserLayoutStoreImpl( DEFAULT_CLASS_NAME );
      } catch ( PortalException pe1 ) {
        log.error( "UserLayoutStoreFactory: Could not load " + DEFAULT_CLASS_NAME, pe1);
        return null;
      }
    }
  }

  /**
   * Returns an instance of the IUserLayoutStore
   * @param className <code>String</code> object specifying the class to be loaded
   * @return an IUserLayoutStore implementation
   */
  protected static IUserLayoutStore getUserLayoutStoreImpl( String className ) throws PortalException {
    try {
        if (userLayoutStoreImpl == null) {
            synchronized (UserLayoutStoreFactory.class) {
                if (userLayoutStoreImpl == null) {
                    userLayoutStoreImpl = (IUserLayoutStore) Class.forName(className).newInstance();
                }
            }
        }
        return userLayoutStoreImpl;
    } catch (Exception e) {
      log.error( "UserLayoutStoreFactory: Could not instantiate " + className, e);
      throw new PortalException(e);
    }
  }

}



