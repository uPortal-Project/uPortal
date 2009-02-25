/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
