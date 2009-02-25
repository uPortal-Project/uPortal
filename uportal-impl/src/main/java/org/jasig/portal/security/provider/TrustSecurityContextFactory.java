/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider;

/**
 * <p>The factory class for the trust security context. Just returns a new
 * instance of the TruestSecurityContext.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

public class TrustSecurityContextFactory implements ISecurityContextFactory {

  public ISecurityContext getSecurityContext() {
    return new TrustSecurityContext();
  }
}