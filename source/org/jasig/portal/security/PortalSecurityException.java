/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

/**
 * <p>An exception handler that will provide portal security-specific
 * information during an exception condition.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

import org.jasig.portal.PortalException;

public class PortalSecurityException extends PortalException {

  public PortalSecurityException() {
    super();
  }

  public PortalSecurityException(String s) {
    super(s);
  }
}
