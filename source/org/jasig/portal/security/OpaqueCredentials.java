package org.jasig.portal.security;

/**
 * <p>Our OpaqueCredentials interface stores password or passphrase information
 * for authentication. The same structure stores information that can
 * potentially be used post-authentication to keep Kerberos TGT's or other
 * useful security context authenticators.</p>
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

public interface OpaqueCredentials {

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