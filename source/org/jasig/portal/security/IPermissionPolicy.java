package org.jasig.portal.security;

/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


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
