/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.security;

import org.jasig.portal.AuthorizationException;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @deprecated As of uPortal 2.0, replaced by {@link IPermissionManager}
 */
public abstract class PermissionManager {
  protected String m_owner = null;

  /**
   * Constructor that stores the owner of the instance of this PermissionManager
   * @param owner
   */
  public PermissionManager (String owner) {
    m_owner = owner;
  }

  /**
   * Adds or updates a Permission
   * @param newPermission
   */
  public abstract void setPermission (Permission newPermission) throws AuthorizationException;

  /**
   * Adds or updates a set up Permissions
   * @param newPermissions
   */
  public abstract void setPermissions (Permission[] newPermissions) throws AuthorizationException;

  /**
   * Retrieves a set of Permissions that meet the following criteria. Null parameters will be ignored.
   * @param principal
   * @param activity
   * @param target
   * @param type
   * @return array of Permissions
   * @exception AuthorizationException
   */
  public abstract Permission[] getPermissions (String principal, String activity, String target, String type) throws AuthorizationException;
}



