/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.jasig.portal;

import org.jasig.portal.services.LogService;

/**
 * Locates and creates an implementation of IPortletPreferencesStore as specified by
 * the "org.jasig.portal.PortletPreferencesStoreFactory.implementation" property in 
 * portal.properties
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public class PortletPreferencesStoreFactory {

    private static IPortletPreferencesStore portletPreferencesStoreImpl = null;

    private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.RDBMPortletPreferencesStore";
    private static String className;

    static {
        try {
            // Retrieve the class name of the concrete IPortletPreferencesStore implementation
            className = PropertiesManager.getProperty("org.jasig.portal.PortletPreferencesStoreFactory.implementation");
        } catch (Exception e ) {}
    
        if (className == null || className.length() == 0 )
            LogService.log(LogService.ERROR, "PortletPreferencesStoreFactory: org.jasig.portal.PortletPreferencesStoreFactory.implementation must be specified in portal.properties");
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
            LogService.log(LogService.ERROR, "PortletPreferencesStoreFactory: Could not load " + className, pe);
            
            try {
                return getPortletPreferencesImpl( DEFAULT_CLASS_NAME );
            } catch ( PortalException pe1 ) {
                LogService.log(LogService.ERROR, "PortletPreferencesStoreFactory: Could not load " + DEFAULT_CLASS_NAME, pe1);
                return null;
            }
        }
    }

    /**
     * Returns an instance of the IPortletPreferencesStore
     * 
     * @param a className <code>String</code> object specifying the class to be loaded
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
          LogService.log(LogService.ERROR, "PortletPreferencesStoreFactory: Could not instantiate " + className, e);
          throw new PortalException(e.getMessage());  
      }
    }

}
