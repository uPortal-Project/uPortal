package org.jasig.portal.security.provider;

/**
 * <p>The factory class for the simple security context. Just returns a new
 * instance of the TruestSecurityContext.</p>
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

import org.jasig.portal.security.*;

public class SimpleSecurityContextFactory implements SecurityContextFactory {

  public SecurityContext getSecurityContext() {
    return new SimpleSecurityContext();
  }
}