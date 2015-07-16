/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.security;

import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.target.IPermissionTarget;

/**
 * Defines a pluggable strategy for evaluating the permissions associated
 * with a principal.
 * @author Dan Ellentuck
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
     * @param service org.jasig.portal.security.IAuthorizationService
     * @param principal org.jasig.portal.security.IAuthorizationPrincipal
     * @param owner The 'namespace' of the activity
     * @param activity The behavior that requires permission
     * @param target The object upon which the behavior will be invoked
     * @return TRUE if the principal has permission to perform the specified
     * activity on the specified target
     * @exception org.jasig.portal.AuthorizationException
     */
    public boolean doesPrincipalHavePermission(
            IAuthorizationService service,
            IAuthorizationPrincipal principal,
            IPermissionOwner owner,
            IPermissionActivity activity,
            IPermissionTarget target)
        throws org.jasig.portal.AuthorizationException;

}
