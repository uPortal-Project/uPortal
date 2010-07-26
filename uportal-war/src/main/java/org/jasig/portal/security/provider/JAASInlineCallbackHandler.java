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

package org.jasig.portal.security.provider;


import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * <p>Used by JAAS security provider
 * checks userid and credentials using JAAS.
 *
 * @author Nathan Jacobs
 *
 */
 public class JAASInlineCallbackHandler implements CallbackHandler {

  private String username = null;
  private char [] password = null;

  public JAASInlineCallbackHandler(String username, char [] password) {

    // make a defensive copy
    this.password = (char []) password.clone();

    if( "".equals(username) ) {
      throw new IllegalArgumentException("Username must be non-empty");
    }

    this.username = username;

  }


  public void handle(Callback[] callbacks)
  throws IOException, UnsupportedCallbackException {

    for (int i = 0; i < callbacks.length; i++) {
      if (callbacks[i] instanceof NameCallback) {
        NameCallback nc = (NameCallback)callbacks[i];
        nc.setName(username);
      } else if (callbacks[i] instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback)callbacks[i];
        pc.setPassword(password);
      } else {
        throw new UnsupportedCallbackException (callbacks[i], "Unrecognized Callback");
      }
    }
  }

}
