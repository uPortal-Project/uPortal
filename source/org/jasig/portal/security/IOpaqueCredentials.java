/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import java.io.Serializable;

/**
 * <p>Our OpaqueCredentials interface stores password or passphrase information
 * for authentication. The same structure stores information that can
 * potentially be used post-authentication to keep Kerberos TGT's or other
 * useful security context authenticators.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public interface IOpaqueCredentials extends Serializable {

  /**
   * Set the credentials value.
   *
   * @param credentials A string of bytes that represents an authenticator.
   */
  public void setCredentials(byte[] credentials);

  /**
   * Set the credentials value.
   *
   * @param credentials A Java String that contains an authenticator such as
   * a passphrase that is typically represented by a locale-specific text
   * string.
   */
  public void setCredentials(String credentials);
}