/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Produces an implementation of IUserIdentityStore
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class UserIdentityStoreFactory {
    
    private static final Log log = LogFactory.getLog(UserIdentityStoreFactory.class);
    
  private static IUserIdentityStore UserIdentityStoreImpl = null;

  static {
    // Retrieve the class name of the concrete IUserIdentityStore implementation
    String className = PropertiesManager.getProperty("org.jasig.portal.UserIdentityStoreFactory.implementation");
    // Fail if this is not found
    if (className == null)
      log.error( "UserIdentityStoreFactory: org.jasig.portal.UserIdentityStoreFactory.implementation must be specified in portal.properties");
    try {
      // Create an instance of the IUserIdentityStore as specified in portal.properties
      UserIdentityStoreImpl = (IUserIdentityStore)Class.forName(className).newInstance();
    } catch (Exception e) {
      log.error( "UserIdentityStoreFactory: Could not instantiate " + className, e);
    }
  }

  /**
   * Returns an instance of the IUserIdentityStore specified in portal.properties
   * @return an IUserIdentityStore implementation
   */
  public static IUserIdentityStore getUserIdentityStoreImpl() {
    return UserIdentityStoreImpl;
  }
}



