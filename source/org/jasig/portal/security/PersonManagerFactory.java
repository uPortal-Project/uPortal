/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.security;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 */
public class PersonManagerFactory {
    
    private static final Log log = LogFactory.getLog(PersonManagerFactory.class);
    
  private static IPersonManager m_personManager = null;
  static {
    // Retrieve the class name of the concrete IPersonManager implementation
    String personManagerClass = PropertiesManager.getProperty("org.jasig.portal.security.PersonManagerFactory.implementation", null);
    // Fail if this is not found
    if (personManagerClass == null) {
      log.error( "PersonManagerFactory: org.jasig.portal.security.PersonManagerFactory.implementation must be specified in portal.properties");
    }
    try {
      // Create an instance of the IPersonManager as specified in portal.properties
      m_personManager = (IPersonManager)Class.forName(personManagerClass).newInstance();
    } catch (Exception e) {
      log.error( "PersonManagerFactory: Could not instantiate " + personManagerClass, e);
    }
  }

  /**
   * Returns an instance of the IPersonManager specified in portal.properties
   * @return instance of the IPersonManager
   */
  public static IPersonManager getPersonManagerInstance () {
    return  (m_personManager);
  }
}



