/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import org.jasig.portal.security.IOpaqueCredentials;

/**
 * <p>This is a provider-specific extention to the standard opaque credentials
 * that is designed to work with the caching security context. This interface
 * adds a method that removes a bit of the opacity of the original by providing
 * means of retreiving a stored credential. See the warnings and caveats
 * associated with the CacheSecurityContext provider.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */


public interface NotSoOpaqueCredentials extends IOpaqueCredentials {

  /**
   * Returns the stored credentials as a string.
   */

  public String getCredentials();
}