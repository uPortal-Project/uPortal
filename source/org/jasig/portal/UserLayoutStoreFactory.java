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
    
  private static IUserLayoutStore userLayoutStoreImpl = null;

  private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.layout.AggregatedUserLayoutStore";
  private static String className;

  static {
    try {
      // Retrieve the class name of the concrete IUserLayoutStore implementation
      className = PropertiesManager.getProperty("org.jasig.portal.UserLayoutStoreFactory.implementation");
    } catch (Exception e ) {}
  
    if (className == null || className.length() == 0 )
      log.error( "UserLayoutStoreFactory: org.jasig.portal.UserLayoutStoreFactory.implementation must be specified in portal.properties");
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
      throw new PortalException(e.getMessage());
    }
  }

}



