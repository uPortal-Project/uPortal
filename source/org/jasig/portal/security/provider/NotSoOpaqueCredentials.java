package org.jasig.portal.security.provider;

import org.jasig.portal.security.OpaqueCredentials;

/**
 * <p>This is a provider-specific extention to the standard opaque credentials
 * that is designed to work with the caching security context. This interface
 * adds a method that removes a bit of the opacity of the original by providing
 * means of retreiving a stored credential. See the warnings and caveats
 * associated with the CacheSecurityContext provider.</p>
 *
 * <p>Copyright (c) Yale University 2000</p>
 *
 * <p>(Note that Yale University intends to relinquish copyright claims to
 * this code once an appropriate JASIG copyright arrangement can be
 * established -ADN)</p>
 *
 * @author Andrew Newman
 * @version $Revision$
 */


public interface NotSoOpaqueCredentials extends OpaqueCredentials {

  /**
   * Returns the stored credentials as a string.
   */

  public String getCredentials();
}