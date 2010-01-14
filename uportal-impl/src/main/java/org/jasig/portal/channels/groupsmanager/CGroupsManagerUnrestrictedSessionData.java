/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;


/**
 * Session data that is a subset of CGroupsManagerSessionData.
 * 
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CGroupsManagerUnrestrictedSessionData
      implements GroupsManagerConstants {
   public Document model;
   public IPerson user;
   public boolean isAdminUser;
   public IGroupsManagerPermissions gmPermissions;
   public IAuthorizationPrincipal authPrincipal;

   /** 
    * Creates new CGroupsManagerUnrestrictedSessionData
    */
   public CGroupsManagerUnrestrictedSessionData () {}

   /** 
    * Creates new CGroupsManagerUnrestrictedSessionData
    * @param model Document
    * @param user IPerson
    * @param isAdminUser boolean
    * @param gmPermissions IGroupsManagerPermissions
    * @param authPrincipal IAuthorizationPrincipal
    */
   public CGroupsManagerUnrestrictedSessionData (Document model, IPerson user, boolean isAdminUser,
         IGroupsManagerPermissions gmPermissions, IAuthorizationPrincipal authPrincipal) {
      this.model = model;
      this.user = user;
      this.isAdminUser = isAdminUser;
      this.gmPermissions = gmPermissions;
      this.authPrincipal = authPrincipal;
   }
   
   public String toString() {
        StringBuffer sb =  new StringBuffer();
        sb.append("[");
        sb.append(CGroupsManagerUnrestrictedSessionData.class.getName());
        if (this.user != null){
            sb.append(" user=");
            sb.append(this.user);
        }
        sb.append(" isAdminUser=");
        sb.append(this.isAdminUser);
        if (this.gmPermissions != null){
            sb.append(" gmPermissions=");
            sb.append(this.gmPermissions);
        }
        if (this.authPrincipal != null){
            sb.append(" authPrincipal=");
            sb.append(this.authPrincipal);
        }
        if (this.model != null){
            sb.append(" model=");
            sb.append(this.model);
        }
        sb.append("]");
        return sb.toString();
      }
}

