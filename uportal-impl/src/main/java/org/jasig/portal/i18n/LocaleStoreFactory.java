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
      String className = PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleStoreFactory.implementation", null);
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
