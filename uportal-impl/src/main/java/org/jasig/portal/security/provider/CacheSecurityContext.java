/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>This is an implementation of a SecurityContext that performs absolutely
 * NO validation of the Principal but merely caches the claimed password.
 * We implement this to provide the illusion of single-signon but it comes
 * with significant risk. A channel is able to retrieve the originally
 * validated password of passphrase to perform just-in-time validation but the
 * means of validation is now COMPLETELY in the hands of the channel. If the
 * channel utilizes a weak authenticity-checking mechanism and the password is
 * the same as the one that portal users regard as secure, then unbeknownst to
 * the user, their "secure" password is being placed in jeopardy. PLEASE use
 * this SecurityContext implementation sparingly and with your eyes open!</p>
 *
 * CacheSecurityContext can be chained together with another context such that 
 * both are required.  This allows an authentication provider such as 
 * SimpleLdapSecurityContext to be used to verify the password and 
 * CacheSecurityContext to allow channels access to the password. Example of 
 * security.properties settings to accomplish this:
 * 
 * root=org.jasig.portal.security.provider.SimpleSecurityContextFactory
 * root.cache=org.jasig.portal.security.provider.CacheSecurityContextFactory
 * principalToken.root=userName
 * credentialToken.root=password
 * 
 * To ensure that both contexts are exercized the portal property
 * org.jasig.portal.security.provider.ChainingSecurityContext.stopWhenAuthenticated
 * must be set to false (by default it is set to true).

 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 *
 */
class CacheSecurityContext extends ChainingSecurityContext
    implements ISecurityContext {
    
    private static final Log log = LogFactory.getLog(CacheSecurityContext.class);
    
  private final int CACHESECURITYAUTHTYPE = 0xFF03;
  private byte[] cachedcredentials;

  CacheSecurityContext() {
    super();
  }


  public int getAuthType() {
    return  this.CACHESECURITYAUTHTYPE;
  }

  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = false;
    if (this.myPrincipal.UID != null && this.myOpaqueCredentials.credentialstring != null) {
      String first_name = null, last_name = null;
      try {
        String acct[] = AccountStoreFactory.getAccountStoreImpl().getUserAccountInformation(this.myPrincipal.UID);
        if (acct[0] != null) {
          first_name = acct[1];
          last_name = acct[2];
          this.myPrincipal.FullName = first_name + " " + last_name;
          if (log.isInfoEnabled())
              log.info( "User " + this.myPrincipal.UID + " is authenticated");
          // Save our credentials so that the parent's authenticate()
          // method doesn't blow them away.
          this.cachedcredentials = new byte[this.myOpaqueCredentials.credentialstring.length];
          System.arraycopy(this.myOpaqueCredentials.credentialstring, 0, this.cachedcredentials, 0, this.myOpaqueCredentials.credentialstring.length);
          this.isauth = true;
        }
        else
            if (log.isInfoEnabled())
                log.info( "No such user: " + this.myPrincipal.UID);
      } catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException("SQL Database Error");
        log.error( "SQL database error", e);
        throw  (ep);
      }
    }
    else
      log.error( "Principal or OpaqueCredentials not initialized prior to authenticate");
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }

  /**
   * We need to override this method in order to return a class that implements
   * the NotSoOpaqueCredentals interface.
   */
  public IOpaqueCredentials getOpaqueCredentials() {
    if (this.isauth) {
      NotSoOpaqueCredentials oc = new CacheOpaqueCredentials();
      oc.setCredentials(this.cachedcredentials);
      return  oc;
    }
    else
      return  null;
  }

  /**
   * This is a new implementation of an OpaqueCredentials class that
   * implements the less-opaque NotSoOpaqueCredentials.
   */
  private class CacheOpaqueCredentials extends ChainingSecurityContext.ChainingOpaqueCredentials
      implements NotSoOpaqueCredentials {

    public String getCredentials() {
      if (this.credentialstring != null)
        return  new String(this.credentialstring);
      else
        return  null;
    }
  }
}



