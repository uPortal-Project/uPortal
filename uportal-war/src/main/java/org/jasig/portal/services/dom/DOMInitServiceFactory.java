/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
