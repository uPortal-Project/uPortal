/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>This is an implementation of a SecurityContext that merely checks to see
 * if the user exists in the UP_USERS database table but otherwise presumes
 * to be pre-authenticated by the context from which it is called. The typical
 * system where this might be used is a portal whose main page is protected by
 * HTTP authentication (BASIC or otherwise).</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
class TrustSecurityContext extends ChainingSecurityContext
    implements ISecurityContext {
    
    private static final Log log = LogFactory.getLog(TrustSecurityContext.class);
    
  private final int TRUSTSECURITYAUTHTYPE = 0xFF01;


  TrustSecurityContext() {
    super();
  }


  public int getAuthType() {
    return  this.TRUSTSECURITYAUTHTYPE;
  }


  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = true;
    if (this.myPrincipal.UID != null) {
      try {
        String first_name, last_name;
        String acct[] = AccountStoreFactory.getAccountStoreImpl().getUserAccountInformation(this.myPrincipal.UID);
        if (acct[0] != null) {
          first_name = acct[1];
          last_name = acct[2];
          this.myPrincipal.FullName = first_name + " " + last_name;
          log.info( "User " + this.myPrincipal.UID + " is authenticated");
          this.isauth = true;
        }
        else {
            log.info( "No such user: " + this.myPrincipal.UID);
        }
      } catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException("SQL Database Error");
        log.error(e,e);
        throw  (ep);
      }
    }
    else {
        log.error( "Principal not initialized prior to authenticate");
    }
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }
}



