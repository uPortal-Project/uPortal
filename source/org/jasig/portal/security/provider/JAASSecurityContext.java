/* Copyright 2001, 2003, 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.io.Serializable;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IAdditionalDescriptor;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials using JAAS.
 *
 * @author Nathan Jacobs
 * @version $Revision$
 *
 */

class JAASSecurityContext extends ChainingSecurityContext implements ISecurityContext, Serializable {

    private static final Log log = LogFactory.getLog(JAASSecurityContext.class);
    
  private final int JAASSECURITYAUTHTYPE = 0xFF05;
  private IAdditionalDescriptor additionalDescriptor;

  JAASSecurityContext() {
    super();
  }

  public int getAuthType() {
    return this.JAASSECURITYAUTHTYPE;
  }

  public IAdditionalDescriptor getAdditionalDescriptor() {
    return additionalDescriptor;
  }

  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = false;

    if (this.myPrincipal.UID != null && this.myOpaqueCredentials.credentialstring != null) {

      try {
          boolean isAuthenticated = false;

          // JAAS Stuff

          LoginContext lc = null;

          lc = new LoginContext("uPortal",
                   new JAASInlineCallbackHandler(
                               this.myPrincipal.UID,
                               (new String(this.myOpaqueCredentials.credentialstring)).toCharArray())); // could not come up w/ a better way to do this

          lc.login();
          additionalDescriptor = new JAASSubject(lc.getSubject());

          // the above will throw an exception if authentication does not succeed

          log.info( "User " + this.myPrincipal.UID + " is authenticated");
          this.isauth = true;

      } catch (LoginException e) {
        log.info( "User " + this.myPrincipal.UID + ": invalid password");
        log.debug("LoginException: " + e.getMessage());
      }
    } else {
      log.error( "Principal or OpaqueCredentials not initialized prior to authenticate");
    }

    // authenticate all subcontexts.
    super.authenticate();

    return;
  }
}
