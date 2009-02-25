/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider;

/**
 * <p>The factory class for the remote security context. Just returns a new
 * instance of the RemoteUserSecurityContext.</p>
 *
 * @author Pete Boysen, pboysen@iastate.edu
 * @version $Revision$
 */

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

public class RemoteUserSecurityContextFactory implements ISecurityContextFactory {

  public ISecurityContext getSecurityContext() {
    return new RemoteUserSecurityContext();
  }
}