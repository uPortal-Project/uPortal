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



