package org.jasig.portal.security.provider;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import java.io.*;
import java.lang.IllegalArgumentException;
import java.util.Arrays;

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
