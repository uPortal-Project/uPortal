/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import org.jasig.portal.IBasicEntity;

/**
 * @author Dan Ellentuck, de3@columbia.edu
 * @version $Revision$
 *
 * A type that aggregates the <code>IPermissions</code> for an 
 * <code>IAuthorizationPrincipal</code> so that permissions can be
 * cached by the <code>EntityCachingService</code>.
 */
public interface IPermissionSet extends IBasicEntity {
    public IPermission[] getPermissions();
}
