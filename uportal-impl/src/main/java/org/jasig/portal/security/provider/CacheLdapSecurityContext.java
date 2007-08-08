/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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



