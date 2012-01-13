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
package org.jasig.portal.spring.security.evaluator;

import java.io.Serializable;

import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupadmin.GroupAdministrationHelper;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.lookup.IPersonLookupHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * PortalPermissionEvaluator provides support for Spring EL hasPermission
 * expressions in method security annotations.  This current implementation is
 * an early attempt at connecting the uPortal permissions framework to Spring
 * Security and will need future adjustment and expansion.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class PortalPermissionEvaluator implements PermissionEvaluator {

    private IGroupListHelper groupListHelper;
    
    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }
    
    private AuthorizationService authorizationService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authorizationService == null) {
            authorizationService = AuthorizationService.instance();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        final IAuthorizationPrincipal principal = authorizationService.newPrincipal(
                userDetails.getUsername(), IPerson.class);

        String targetId = null;
        
        // if the target object is a string, assume it already represents a 
        // valid uPortal permission target
        if (targetDomainObject instanceof String) {
            targetId = (String) targetDomainObject;
        }
        
        // if the target is a JsonEntityBean, use the principal associated with
        // the entity
        else if (targetDomainObject instanceof JsonEntityBean) {
            targetId = ((JsonEntityBean) targetDomainObject).getPrincipalString();
        }

        // if the permission is already an AuthorizableActivity, go ahead and 
        // use it
        AuthorizableActivity activity = null;
        if (permission instanceof AuthorizableActivity) {
            activity = (AuthorizableActivity) permission;
        } 

        // if the permission is a string, allow our local method to try and
        // translate it into a permission relevant to the provided target
        else if (permission instanceof String) {
            String activityName = (String) permission;
            activity = getViewActivity(activityName, (JsonEntityBean) targetDomainObject);
        }
        

        else {
            throw new RuntimeException("Unable to determine permission target id for type " + targetDomainObject.getClass());
        }
        
        if (activity != null) {
            final boolean hasPermission = principal.hasPermission(activity.getOwnerFname(), activity.getActivityFname(), targetId);
            return hasPermission;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
            String targetType, Object permission) {
        if (authorizationService == null) {
            authorizationService = AuthorizationService.instance();
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        final IAuthorizationPrincipal principal = authorizationService.newPrincipal(
                userDetails.getUsername(), IPerson.class);

        // if the permission is already an AuthorizableActivity, go ahead and 
        // use it
        AuthorizableActivity activity = null;
        if (permission instanceof AuthorizableActivity) {
            activity = (AuthorizableActivity) permission;
        } 

        // if the permission is a string, allow our local method to try and
        // translate it into a permission relevant to the provided target
        else if (permission instanceof String && targetId instanceof String) {
            String activityName = (String) permission;
            activity = getViewActivity(activityName, (String) targetId);
        }
        
        if (activity != null) {
            final boolean hasPermission = principal.hasPermission(activity.getOwnerFname(), activity.getActivityFname(), targetId.toString());
            return hasPermission;
        } else {
            return false;
        }
    }

    protected AuthorizableActivity getViewActivity(final String activityKey, final JsonEntityBean entity) {
        if (entity != null && activityKey.equals("VIEW")) {
            final EntityEnum type = entity.getEntityType();
            if (type.isGroup()) {
                return new AuthorizableActivity(GroupAdministrationHelper.GROUPS_OWNER,
                            GroupAdministrationHelper.VIEW_PERMISSION);
            } else if (type.equals(EntityEnum.PERSON)) {
                return new AuthorizableActivity(IPersonLookupHelper.USERS_OWNER,
                        IPersonLookupHelper.VIEW_USER_PERMISSION);
            } else if (type.equals(EntityEnum.PORTLET)) {
                return new AuthorizableActivity(IPermission.PORTAL_SUBSCRIBE, IPermission.PORTLET_SUBSCRIBER_ACTIVITY);
            }
        }
        return null;
    }
    
    protected AuthorizableActivity getViewActivity(final String activityKey, final String target) {
        final JsonEntityBean entity = groupListHelper.getEntityForPrincipal(target);
        return getViewActivity(activityKey, entity);
    }
    
}
