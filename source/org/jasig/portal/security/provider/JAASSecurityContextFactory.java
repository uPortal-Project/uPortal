/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

/**
 * <p>The factory class for the simple security context. Just returns a new
 * instance of the JAASSecurityContext.</p>
 *
 * @author Nathan Jacobs
 */

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

public class JAASSecurityContextFactory implements ISecurityContextFactory {
  public ISecurityContext getSecurityContext() {
    return new JAASSecurityContext();
  }
}
