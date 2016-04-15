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
package org.jasig.portal.api.permissions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.api.Principal;
import org.jasig.portal.api.PrincipalImpl;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.permission.target.IPermissionTargetProvider;
import org.jasig.portal.permission.target.IPermissionTargetProviderRegistry;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiPermissionsService implements PermissionsService {

	private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private IAuthorizationService authorizationService;

    @Autowired
    private IPermissionOwnerDao permissionOwnerDao;
    
    @Autowired
    private IPermissionStore permissionStore;

    @Autowired
    private IPermissionTargetProviderRegistry targetProviderRegistry;

    @Override
	public Set<Assignment> getAssignmentsForPerson(String username, boolean includeInherited) {

    	Set<Assignment> rslt = new HashSet<Assignment>();

        IAuthorizationPrincipal authP = this.authorizationService.newPrincipal(
        		username, 
        		EntityEnum.PERSON.getClazz());
        
        // first get the permissions explicitly set for this principal
        IPermission[] directPermissions = permissionStore.select(null, authP.getPrincipalString(), null, null, null);
        for (IPermission permission : directPermissions) {
            if (authP.hasPermission(permission.getOwner(), permission.getActivity(), permission.getTarget())) {
            	Assignment a = createAssignment(permission, authP, false);
            	if (a != null) {
                    rslt.add(a);
            	}
            }
        }
        
        if (includeInherited) {
            IGroupMember member = GroupService.getGroupMember(authP.getKey(), authP.getType());
            for (@SuppressWarnings("unchecked") Iterator<IEntityGroup> iter = member.getAncestorGroups(); iter.hasNext();) {
                IEntityGroup parent = iter.next();

                IAuthorizationPrincipal parentPrincipal = this.authorizationService.newPrincipal(parent);
                IPermission[] parentPermissions = permissionStore.select(null, parentPrincipal.getPrincipalString(), null, null, null);
                for (IPermission permission : parentPermissions) {
                    if (authP.hasPermission(permission.getOwner(), permission.getActivity(), permission.getTarget())) {
                    	Assignment a = createAssignment(permission, authP, true);
                    	if (a != null) {
                            rslt.add(a);
                    	}
                    }
                }
            }
        }
        
        return rslt;

	}
    
    /*
     * Implementation
     */

	private Assignment createAssignment(IPermission permission, IAuthorizationPrincipal authP, boolean inherited) {
		
		Assignment rslt = null;
        
		try {
			
	        // Owner
	        IPermissionOwner owner = permissionOwnerDao.getPermissionOwner(permission.getOwner());
	        Owner ownerImpl = new OwnerImpl(permission.getOwner(), owner.getName());
	        
	        // Activity
	        IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(permission.getOwner(), permission.getActivity());
	        Activity activityImpl = new ActivityImpl(permission.getActivity(), activity.getName());
	        
	        // Principal
	        Principal principalImpl = new PrincipalImpl(authP.getKey(), authP.getPrincipalString());
	        
	        // Target
	        Target targetImpl = null;  // default
	        IPermissionTargetProvider targetProvider = targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey());
	        IPermissionTarget target = targetProvider.getTarget(permission.getTarget());
	        if (target != null) {
	        	targetImpl = new TargetImpl(permission.getTarget(), target.getName());
	        }

	        rslt = new AssignmentImpl(ownerImpl, activityImpl, principalImpl, targetImpl, inherited);

		} catch (Exception e) {
            log.warn("Exception while adding permission", e);
		}
		
		return rslt;

	}

}
