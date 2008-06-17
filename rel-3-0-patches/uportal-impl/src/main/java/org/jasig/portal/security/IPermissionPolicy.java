/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

/**
 * Defines a pluggable strategy for evaluating the permissions associated
 * with a principal.
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.security.IAuthorizationService
 * @see org.jasig.portal.security.IPermission
 */
public interface IPermissionPolicy {
/**
 * Answers if the owner has authorized the principal to perform the activity 
 * on the target, based on permissions provided by the service.  Params 
 * <code>service</code>, <code>owner</code> and <code>activity</code> must 
 * be non-null.
 *
 * @return boolean
 * @param service org.jasig.portal.security.IAuthorizationService
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception org.jasig.portal.AuthorizationException
 */
public boolean doesPrincipalHavePermission
   (IAuthorizationService service,
    IAuthorizationPrincipal principal, 
    String owner, 
    String activity, 
    String target) 
throws org.jasig.portal.AuthorizationException;
}
