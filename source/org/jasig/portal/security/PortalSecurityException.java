package org.jasig.portal.security;

/**
 * <p>An exception handler that will provide portal security-specific
 * information during an exception condition.</p>
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

public class PortalSecurityException extends Exception {

  public PortalSecurityException() {
    super();
  }

  public PortalSecurityException(String s) {
    super(s);
  }
}