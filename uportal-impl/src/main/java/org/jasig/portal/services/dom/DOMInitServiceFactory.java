/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.services.dom;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Bolton, nbolton@unicon.net
 * @version $Revision$
 */
public class DOMInitServiceFactory {

    private static final Log log = LogFactory.getLog(DOMInitServiceFactory.class);
    
  private static IDOMInitService service = null;

  private static String className = null;

  static {
    try {
      // Retrieve the class name of the concrete IDOMInitService implementation
      className = PropertiesManager.getProperty("org.jasig.portal.services.dom.DOMInitServiceFactory.implementation");
    } catch (Exception e ) {}
  }

  /**
   * Returns an instance of the IDOMInitService specified in portal.properties.
   * If the property doesn't exist or is empty, null is returned.
   * @return an IDOMInitService implementation
   */
  public static IDOMInitService getService() {
    try {
      if (className == null || "".equals(className)) return null;

      if (service == null) {
        service = (IDOMInitService)Class.forName(className).newInstance();
      }
      return service;
    } catch (Exception e) {
      log.error( "DOMInitServiceFactory: Could not instantiate " + className, e);
    }
    return service;
  }
}
