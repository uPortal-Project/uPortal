/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlets.permissionsadmin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.permission.IPermissionActivity;
import org.apereo.portal.permission.IPermissionOwner;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionStore;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * PermissionAdministrationHelper contains convenience methods for the permission editing Spring
 * Webflows.
 *
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

    @Override
    public boolean canEditOwner(IPerson currentUser, String owner) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(
                IPermission.PORTAL_PERMISSIONS,
                IPermission.EDIT_PERMISSIONS_ACTIVITY,
                IPermission.ALL_TARGET));
    }

    @Override
    public boolean canViewOwner(IPerson currentUser, String owner) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(
                IPermission.PORTAL_PERMISSIONS,
                IPermission.VIEW_PERMISSIONS_ACTIVITY,
                IPermission.ALL_TARGET));
    }

    @Override
    public boolean canEditActivity(IPerson currentUser, String activity) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(
                IPermission.PORTAL_PERMISSIONS,
                IPermission.EDIT_PERMISSIONS_ACTIVITY,
                IPermission.ALL_TARGET));
    }

    @Override
    public boolean canViewActivity(IPerson currentUser, String activity) {
        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(
                IPermission.PORTAL_PERMISSIONS,
                IPermission.VIEW_PERMISSIONS_ACTIVITY,
                IPermission.ALL_TARGET));
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.permissionsadmin.IPermissionAdministrationHelper#canEditPermission(org.apereo.portal.security.IPerson, java.lang.String)
     */
    @Override
    public boolean canEditPermission(IPerson currentUser, String target) {

        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(
                IPermission.PORTAL_PERMISSIONS,
                IPermission.EDIT_PERMISSIONS_ACTIVITY,
                IPermission.ALL_TARGET));
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlets.permissionsadmin.IPermissionAdministrationHelper#canViewPermission(org.apereo.portal.security.IPerson, java.lang.String)
     */
    @Override
    public boolean canViewPermission(IPerson currentUser, String target) {

        EntityIdentifier ei = currentUser.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return (ap.hasPermission(
                IPermission.PORTAL_PERMISSIONS,
                IPermission.VIEW_PERMISSIONS_ACTIVITY,
                IPermission.ALL_TARGET));
    }

    @Override
    public Set<JsonEntityBean> getEntitiesForPrincipals(Collection<String> principals) {

        Set<JsonEntityBean> entities = new HashSet<JsonEntityBean>();

        // add a permission for each member of the principals collection
        for (String principal : principals) {
            JsonEntityBean entity = groupListHelper.getEntityForPrincipal(principal);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public Set<String> getPrincipalsForEntities(Collection<JsonEntityBean> entities) {

        Set<String> principals = new HashSet<String>();

        // add a permission for each member of the principals collection
        for (JsonEntityBean entity : entities) {
            principals.add(entity.getPrincipalString());
        }

        return principals;
    }

    @Override
    public Set<String> getCurrentPrincipals(
            IPermissionOwner owner, IPermissionActivity activity, String targetKey) {

        // Find permissions that match the inputs from the IPermissionStore
        IPermission[] permissions =
                permissionStore.select(
                        owner.getFname(), null, activity.getFname(), targetKey, null);

        // Build the set of existing assignments
        Set<String> principals = new HashSet<String>();
        for (IPermission p : permissions) {
            principals.add(p.getPrincipal());
        }

        return principals;
    }
}
