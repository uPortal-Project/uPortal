/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

package  org.jasig.portal.security.provider;

import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.PortalSecurityException;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials against an LDAP directory.  It expects to be able to bind
 * to the LDAP directory as the user so that it can authenticate the
 * user.  The user's credentials are cached.</p>
 *
 * @author Russell Tokuyama (University of Hawaii)
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated As of uPortal 2.1.3, use {@link org.jasig.portal.security.provider.SimpleLdapSecurityContext} chained with {@link org.jasig.portal.security.provider.CacheSecurityContext} instead
 */
public class CacheLdapSecurityContext extends SimpleLdapSecurityContext {
  private final int CACHELDAPSECURITYAUTHTYPE = 0xFF06;
  private byte[] cachedCredentials;

  CacheLdapSecurityContext () {
    super();
  }

  /**
   * Returns the type of authentication this class provides.
   * @return authorization type
   */
  public int getAuthType () {
    /*
     * What is this for?  No one would know what to do with the
     * value returned.  Subclasses might know but our getAuthType()
     * doesn't return anything easily useful.
     */
    return this.CACHELDAPSECURITYAUTHTYPE;
  }

  /**
   * Authenticates the user.
   */
  public synchronized void authenticate () throws PortalSecurityException {
	// Save our credentials before parent's authenticate() method
	// destroys them.
	this.cachedCredentials =
		new byte[this.myOpaqueCredentials.credentialstring.length];
	System.arraycopy(this.myOpaqueCredentials.credentialstring, 0,
					 this.cachedCredentials, 0,
					 this.myOpaqueCredentials.credentialstring.length);

	super.authenticate();

	if (!this.isAuthenticated())
	  this.cachedCredentials = null;

  }

  /**
   * We need to override this method in order to return a class that implements
   * the NotSoOpaqueCredentals interface.
   */
  public IOpaqueCredentials getOpaqueCredentials () {
    if (this.isauth) {
      NotSoOpaqueCredentials oc = new CacheOpaqueCredentials();
      oc.setCredentials(this.cachedCredentials);
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

    /**
     * Gets the credentials
     * @return the credentials
     */
    public String getCredentials () {
      if (this.credentialstring != null)
        return  new String(this.credentialstring);
      else
        return  null;
    }
  }
}



