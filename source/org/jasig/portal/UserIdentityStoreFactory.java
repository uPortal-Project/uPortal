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



