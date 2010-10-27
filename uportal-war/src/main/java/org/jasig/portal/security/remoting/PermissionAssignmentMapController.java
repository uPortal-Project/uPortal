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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.portlets.permissionsadmin.Assignment;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Drew Wills
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("/permissionAssignmentMap")
public class PermissionAssignmentMapController extends AbstractPermissionsController {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    private IGroupListHelper groupListHelper;
    
    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getOwners(
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to see permission owners
        // TODO: remove dependency on permission portlet subscription permission
        if (!this.isAuthorized(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        // get the serialized JSON permissions map from the request
        String json = request.getParameter("permissions");
        
        // de-serialize the string into a map
        JSONObject jsonObject = JSONObject.fromObject( json );  
        @SuppressWarnings("unchecked")
        Map<String,String> permissions = (Map<String,String>) JSONObject.toBean( jsonObject, HashMap.class );  
        
        // Build the set of existing assignments
        List<Assignment> flatAssignmentsList = new ArrayList<Assignment>();
        for (String principal : permissions.keySet()) {
            
            JsonEntityBean bean = groupListHelper.getEntityForPrincipal(principal);

            if (bean != null) {
                Assignment.Type y = Assignment.Type.valueOf(permissions.get(principal).toUpperCase());
                flatAssignmentsList.add(new Assignment(principal, bean, y));
            } else {
                log.warn("Unable to resolve the following principal (will " +
                        "be omitted from the list of assignments):  " + 
                        principal);
            }
            
        }
        
        Map<JsonEntityBean,Assignment.Type> grantOrDenyMap = new HashMap<JsonEntityBean,Assignment.Type>();
        for (Assignment a : flatAssignmentsList) {
            grantOrDenyMap.put(a.getPrincipal(), a.getType());
        }
        
        List<Assignment> assignments = new ArrayList<Assignment>();
        for (Assignment a : flatAssignmentsList) {
            placeInHierarchy(a, assignments, grantOrDenyMap);
        }
        
        Map<String,Object> model = Collections.<String,Object>singletonMap("assignments", assignments); 
        return new ModelAndView("jsonView", model);
    }
    
    private void placeInHierarchy(Assignment a, List<Assignment> hierarchy, Map<JsonEntityBean,Assignment.Type> grantOrDenyMap) {

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
                // We don't add, but *do* override INHERIT with anything else in 
                // this circumstance;  we don't add nodes to the selection 
                // basket for the sake of setting INHERIT permissions. 
                if (duplicate.getType().equals(Assignment.Type.INHERIT)) {
                    duplicate.setType(a.getType());
                }
                return;
            }
        }
        
        // To proceed, we need to know about the containing 
        // groups (if any) for this principal...
        IGroupMember member = null;
        EntityEnum entityEnum = EntityEnum.getEntityEnum(a.getPrincipal().getEntityType());
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

                String beanType = EntityEnum.getEntityEnum(group.getEntityType(), true).toString();

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
                    Assignment.Type assignmentType = grantOrDenyMap.containsKey(bean) 
                                                        ? grantOrDenyMap.get(bean) 
                                                        : Assignment.Type.INHERIT;  // default...
                    IAuthorizationPrincipal principal = authService.newPrincipal(group);
                    parent = new Assignment(principal.getPrincipalString(), bean, assignmentType);
                    parent.addChild(a);
                    placeInHierarchy(parent, hierarchy, grantOrDenyMap);
                }
            }
        } else {
            // This member is a root...
            hierarchy.add(a);
        }

    }

}
