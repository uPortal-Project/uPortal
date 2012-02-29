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

package org.jasig.portal.security.remoting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.permissionsadmin.Assignment;
import org.jasig.portal.portlets.permissionsadmin.IPermissionAdministrationHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.jasig.portal.security.provider.PermissionImpl;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Drew Wills
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
public class PermissionAssignmentMapController extends AbstractPermissionsController {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private IGroupListHelper groupListHelper;
    
    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }

    private IPermissionAdministrationHelper permissionAdministrationHelper;
    
    @Autowired(required = true)
    public void setPermissionAdministrationHelper(IPermissionAdministrationHelper permissionAdministrationHelper) {
        this.permissionAdministrationHelper = permissionAdministrationHelper;
    }
    
    private IPersonManager personManager;
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private IPermissionStore permissionStore;
    
    @Autowired
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }
    
    private IAuthorizationService authorizationService;
    
    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @RequestMapping(value="/updatePermission", method = RequestMethod.GET)
    public ModelAndView updatePermission(
            @RequestParam("principal") String principal,
            @RequestParam("assignment") String assignment,
            @RequestParam("principals[]") String[] principals,
            @RequestParam("owner") String owner,
            @RequestParam("activity") String activity,
            @RequestParam("target") String target, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // ensure the current user is authorized to update and view permissions
        final IPerson currentUser = personManager.getPerson((HttpServletRequest) request);
        if (!permissionAdministrationHelper.canEditPermission(currentUser, target) || 
                !permissionAdministrationHelper.canViewPermission(currentUser, target)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        JsonEntityBean bean = groupListHelper.getEntityForPrincipal(principal);

        if (bean != null) {
            
            IAuthorizationPrincipal p = groupListHelper.getPrincipalForEntity(bean);
            
            IPermission[] directPermissions = permissionStore.select(owner, p.getPrincipalString(), activity, target, null);
            this.authorizationService.removePermissions(directPermissions);
            
            assignment = assignment.toUpperCase();
            if (assignment.equals(Assignment.Type.GRANT.toString()) || assignment.equals(Assignment.Type.DENY.toString())) {
                IPermission permission = new PermissionImpl(owner);
                permission.setActivity(activity);
                permission.setPrincipal(bean.getPrincipalString());
                permission.setTarget(target);
                permission.setType(assignment);
                this.authorizationService.addPermissions(new IPermission[]{ permission });
            }
            
        } else {
            log.warn("Unable to resolve the following principal (will " +
                    "be omitted from the list of assignments):  " + 
                    principal);
        }

        
        return getOwners(principals, owner, activity, target, request, response);
    }

    @RequestMapping(value="/permissionAssignmentMap", method = RequestMethod.GET)
    public ModelAndView getOwners(@RequestParam("principals[]") String[] principals,
            @RequestParam("owner") String owner, @RequestParam("activity") String activity,
            @RequestParam("target") String target,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to view permissions
        final IPerson currentUser = personManager.getPerson((HttpServletRequest) request);
        if (!permissionAdministrationHelper.canViewPermission(currentUser, target)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        // Build the set of existing assignments
        List<Assignment> flatAssignmentsList = new ArrayList<Assignment>();
        for (String principal : principals) {
            
            JsonEntityBean bean = groupListHelper.getEntityForPrincipal(principal);

            if (bean != null) {
                
                IAuthorizationPrincipal p = groupListHelper.getPrincipalForEntity(bean);
                
                // first get the permissions explicitly set for this principal
                Assignment.Type type = getAssignmentType(p, owner, activity, target);
                flatAssignmentsList.add(new Assignment(principal, bean, type));

            } else {
                log.warn("Unable to resolve the following principal (will " +
                        "be omitted from the list of assignments):  " + 
                        principal);
            }
            
        }
        
        List<Assignment> assignments = new ArrayList<Assignment>();
        for (Assignment a : flatAssignmentsList) {
            placeInHierarchy(a, assignments, owner, activity, target);
        }
        
        Map<String,Object> model = Collections.<String,Object>singletonMap("assignments", assignments); 
        return new ModelAndView("jsonView", model);
    }
    
    private void placeInHierarchy(Assignment a, List<Assignment> hierarchy, String owner, String activity, String target) {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a' [Assignment] cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (hierarchy == null) {
            String msg = "Argument 'hierarchy' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        // Don't add another node if the specified Assignment 
        // is already in the hierarchy somewhere...
        for (Assignment root : hierarchy) {
            Assignment duplicate = root.findDecendentOrSelfIfExists(a.getPrincipal());
            if (duplicate != null) {
                return;
            }
        }
        
        // To proceed, we need to know about the containing 
        // groups (if any) for this principal...
        IGroupMember member = null;
        EntityEnum entityEnum = a.getPrincipal().getEntityType();
        if (entityEnum.isGroup()) {
            member = GroupService.findGroup(a.getPrincipal().getId());
        } else {
            member = GroupService.getGroupMember(a.getPrincipal().getId(), entityEnum.getClazz());
        }

        AuthorizationService authService = AuthorizationService.instance();
        Iterator<?> it = GroupService.getCompositeGroupService().findContainingGroups(member);
        if (it.hasNext()) {
            // This member must be nested within its parent(s)...
            while (it.hasNext()) {
                IEntityGroup group = (IEntityGroup) it.next();

                EntityEnum beanType = EntityEnum.getEntityEnum(group.getEntityType(), true);

                JsonEntityBean bean = new JsonEntityBean(group, beanType);
                Assignment parent = null;
                for (Assignment root : hierarchy) {
                    parent = root.findDecendentOrSelfIfExists(bean);
                    if (parent != null) {
                        // We found one...
                        parent.addChild(a);
                        break;
                    }
                }
                if (parent == null) {
                    // We weren't able to integrate this node into the existing 
                    // hierarchy;  we have to dig deeper, until we either (1) 
                    // find a match, or (2) reach a root;  type is INHERIT, 
                    // unless (by chance) there's something specified in an 
                    // entry on grantOrDenyMap.
                    IAuthorizationPrincipal principal = authService.newPrincipal(group);
                    Assignment.Type assignmentType = getAssignmentType(principal, owner, activity, target);
                    parent = new Assignment(principal.getPrincipalString(), bean, assignmentType);
                    parent.addChild(a);
                    placeInHierarchy(parent, hierarchy, owner, activity, target);
                }
            }
        } else {
            // This member is a root...
            hierarchy.add(a);
        }

    }

    private Assignment.Type getAssignmentType(IAuthorizationPrincipal principal, String owner, String activity, String target) {
        IPermission[] directPermissions = permissionStore.select(owner, principal.getPrincipalString(), activity, target, null);
        Assignment.Type type;
        if (directPermissions.length > 0) {
            type = directPermissions[0].getType().equals(IPermission.PERMISSION_TYPE_GRANT) ? Assignment.Type.GRANT : Assignment.Type.DENY;
        } else  if (principal.hasPermission(owner, activity, target)) {
            type = Assignment.Type.INHERIT_GRANT;
        } else {
            type = Assignment.Type.INHERIT_DENY;
        }
        return type;
    }
    
}
