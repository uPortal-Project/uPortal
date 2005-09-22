/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Produces an implementation of IUserLayoutStore
 * @author Ken Weiner, kweiner@unicon.net
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class UserLayoutStoreFactory {

    private static final Log log = LogFactory.getLog(UserLayoutStoreFactory.class);
    
  private static IUserLayoutStore userLayoutStoreImpl;

  private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.layout.AggregatedUserLayoutStore";

  static {
      // preserve previous implementation behavior
      String className = null;
      
      try {
          // Retrieve the class name of the concrete IUserLayoutStore implementation
          className = PropertiesManager.getProperty("org.jasig.portal.UserLayoutStoreFactory.implementation");
      } catch (Exception e) {
          log.error( "UserLayoutStoreFactory: org.jasig.portal.UserLayoutStoreFactory.implementation must be specified in portal.properties");
      }
      
      // retrieve the configured (or default) instance
      try {
          userLayoutStoreImpl = (IUserLayoutStore) Class.forName(className).newInstance();
          
      } catch (Exception e ) {
          log.error( "UserLayoutStoreFactory: Could not instantiate " + className);
          
          // if unable to load AND it was not the default class name, try the default
          if (!DEFAULT_CLASS_NAME.equals(className)) {
              try {
                  userLayoutStoreImpl = (IUserLayoutStore) Class.forName(DEFAULT_CLASS_NAME).newInstance();
              } catch (Exception e2) {
                  log.error( "UserLayoutStoreFactory: Could not instantiate " + DEFAULT_CLASS_NAME);
              }
          }
      }
      
      // in the end, userLayoutStoreImpl may still be null
 }

  /**
   * Returns an instance of the IUserLayoutStore specified in portal.properties
   * @return an IUserLayoutStore implementation
   */
  public static IUserLayoutStore getUserLayoutStoreImpl() {
      return (userLayoutStoreImpl);
  }

}
