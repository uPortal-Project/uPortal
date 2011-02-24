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

package org.jasig.portal.portlets.permissionsadmin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * PermissionAdministrationHelper contains convenience methods for the 
 * permission editing Spring Webflows.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Service
public class PermissionAdministrationHelper implements IPermissionAdministrationHelper {

    protected final Log log = LogFactory.getLog(getClass());

    private IGroupListHelper groupListHelper;
    
    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }
    
    private IPermissionStore permissionStore;
    
    @Autowired(required = true)
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    public boolean canEditOwner(IPerson currentUser, String owner) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(PERMISSIONS_OWNER, EDIT_PERMISSION, ALL_PERMISSIONS_TARGET));
    }

    public boolean canViewOwner(IPerson currentUser, String owner) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(PERMISSIONS_OWNER, VIEW_PERMISSION, ALL_PERMISSIONS_TARGET));
    }

    public boolean canEditActivity(IPerson currentUser, String activity) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(PERMISSIONS_OWNER, EDIT_PERMISSION, ALL_PERMISSIONS_TARGET));
    }

    public boolean canViewActivity(IPerson currentUser, String activity) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(PERMISSIONS_OWNER, VIEW_PERMISSION, ALL_PERMISSIONS_TARGET));
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.permissionsadmin.IPermissionAdministrationHelper#canEditPermission(org.jasig.portal.security.IPerson, java.lang.String)
     */
    public boolean canEditPermission(IPerson currentUser, String target) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(PERMISSIONS_OWNER, EDIT_PERMISSION, ALL_PERMISSIONS_TARGET));
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.permissionsadmin.IPermissionAdministrationHelper#canViewPermission(org.jasig.portal.security.IPerson, java.lang.String)
     */
    public boolean canViewPermission(IPerson currentUser, String target) {
        
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(PERMISSIONS_OWNER, VIEW_PERMISSION, ALL_PERMISSIONS_TARGET));
    }

    public Set<JsonEntityBean> getEntitiesForPrincipals(Collection<String> principals) {
        
        Set<JsonEntityBean> entities = new HashSet<JsonEntityBean>();
        
        // add a permission for each member of the principals collection
        for (String principal : principals) {
            JsonEntityBean entity = groupListHelper.getEntityForPrincipal(principal);
            entities.add(entity);
        }

        return entities;
    }
    
    public Set<String> getPrincipalsForEntities(Collection<JsonEntityBean> entities) {
        
        Set<String> principals = new HashSet<String>();
        
        // add a permission for each member of the principals collection
        for (JsonEntityBean entity : entities) {
            principals.add(entity.getPrincipalString());
        }

        return principals;
    }
    
    public Set<String> getCurrentPrincipals(IPermissionOwner owner, IPermissionActivity activity, String targetKey) {

        // Find permissions that match the inputs from the IPermissionStore
        IPermission[] permissions = permissionStore.select(owner.getFname(), null, activity.getFname(), targetKey, null);
        
        // Build the set of existing assignments
        Set<String> principals = new HashSet<String>();
        for (IPermission p : permissions) {
            principals.add(p.getPrincipal());
        }

        return principals;
    }

}
