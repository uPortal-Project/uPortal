/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

/**
 * <p>The factory class for the simple security context. Just returns a new
 * instance of the TruestSecurityContext.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

public class SimpleSecurityContextFactory implements ISecurityContextFactory {

  public ISecurityContext getSecurityContext() {
    return new SimpleSecurityContext();
  }
}