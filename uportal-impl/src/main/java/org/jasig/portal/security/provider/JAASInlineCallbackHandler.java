/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
