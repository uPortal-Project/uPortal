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
import java.util.Set;

import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.security.IPerson;

/**
 * IPermissionAdministrationHelper is designed to offer access to common
 * permissions administration operations.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public interface IPermissionAdministrationHelper {

    public static final String PERMISSIONS_OWNER = "UP_PERMISSIONS";
    public static final String EDIT_PERMISSION = "EDIT_PERMISSIONS";
    public static final String VIEW_PERMISSION = "VIEW_PERMISSIONS";
    public static final String ALL_PERMISSIONS_TARGET = "ALL";

    public boolean canEditOwner(IPerson currentUser, String owner);

    public boolean canViewOwner(IPerson currentUser, String owner);

    public boolean canEditActivity(IPerson currentUser, String activity);

    public boolean canViewActivity(IPerson currentUser, String activity);

    public boolean canEditPermission(IPerson currentUser, String target);

    public boolean canViewPermission(IPerson currentUser, String target);

    public Set<String> getPrincipalsForEntities(Collection<JsonEntityBean> entities);

    public Set<JsonEntityBean> getEntitiesForPrincipals(Collection<String> principals);

    public Set<String> getCurrentPrincipals(IPermissionOwner owner,
            IPermissionActivity activity, String targetKey);

}