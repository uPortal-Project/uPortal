/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
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


package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;


/**
 * Session data that is a subset of CGroupsManagerSessionData.
 * @author Don Fracapane
 * @version $Revision$
 */
public class CGroupsManagerUnrestrictedSessionData
      implements GroupsManagerConstants {
   public Document model;
   public IPerson user;
   public boolean isAdminUser;
   public IGroupsManagerPermissions gmPermissions;
   public IAuthorizationPrincipal authPrincipal;

   /** Creates new CGroupsManagerUnrestrictedSessionData
    */
   public CGroupsManagerUnrestrictedSessionData () {}

   /** Creates new CGroupsManagerUnrestrictedSessionData
    * @param model Document
    * @param user IPerson
    * @param isAdminUser boolean
    * @param gmPermissions IGroupsManagerPermissions
    * @param authPrincipal IAuthorizationPrincipal
    * @return an <code>CGroupsManagerUnrestrictedSessionData</code> object
    */
   public CGroupsManagerUnrestrictedSessionData (Document model, IPerson user, boolean isAdminUser,
         IGroupsManagerPermissions gmPermissions, IAuthorizationPrincipal authPrincipal) {
      this.model = model;
      this.user = user;
      this.isAdminUser = isAdminUser;
      this.gmPermissions = gmPermissions;
      this.authPrincipal = authPrincipal;
   }
}

