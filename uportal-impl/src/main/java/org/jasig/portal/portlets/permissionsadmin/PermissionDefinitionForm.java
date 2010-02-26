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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.provider.RDBMPermissionImpl;
import org.jasig.portal.security.provider.RDBMPermissionImpl.PrincipalType;
import org.jasig.portal.services.GroupService;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.core.collection.ParameterMap;

public class PermissionDefinitionForm implements Serializable {
    
    // Static Members.
    private static final String ENTITY_OTHER = "other";
    private static final long serialVersionUID = 1L;

    // Instance Members.
    private String owner;
    private List<Assignment> assignments = Collections.emptyList();
    private String activity;
    private JsonEntityBean target;
    private static final Log log = LogFactory.getLog(RDBMPermissionImpl.class);
    
    /*
     * Public API.
     */
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {

        if (owner == null || owner.trim().length() == 0) {
            // This is a no-op;  let's keep null if that's what we have.
            return;
        }

        this.owner = owner;

    }
    
    public List<Assignment> getAssignments() {
        return new ArrayList<Assignment>(assignments);
    }

    public List<JsonEntityBean> getPrincipals() {
        Map<JsonEntityBean,Assignment.Type> rslt = new HashMap<JsonEntityBean,Assignment.Type>();
        for (Assignment a : assignments) {
            aggregatePrincipalMap(a, rslt);
        }
        return new ArrayList<JsonEntityBean>(rslt.keySet());
    }

    public void setPrincipals(List<JsonEntityBean> principals) {
        assignments = updateAssignments(assignments, principals);
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {

        if (activity == null || activity.trim().length() == 0) {
            // This is a no-op;  let's keep null if that's what we have.
            return;
        }

        this.activity = activity;

    }

    public String getTarget() {
        return target != null ? target.getName() : null;
    }

    public void setTargetAsStringIfDifferent(String target) {
        
        if (target == null || target.trim().length() == 0) {
            // This is a no-op;  let's keep null if that's what we have.
            return;
        }
        
        if (this.target != null && target.equals(this.target.getName())) {
            // The UI is merely re-setting the previous choice;  we need to keep 
            // what we have b/c our existing version may be 'better' due to 
            // selection of group/channel/category in a sub-flow.
            return;
        }
        
        // This is a legitimate, string-based target selection.
        this.target = new JsonEntityBean();
        this.target.setEntityType(ENTITY_OTHER);
        this.target.setName(target);
        this.target.setId(target);

    }
    
    public void setTarget(List<JsonEntityBean> target) {

        // Assertions.
        if (target == null) {
            String msg = "Argument 'target' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        this.target = target.size() > 0 ? target.get(0) : null;

    }
    
    public void setTypes(ParameterMap requestParameters) {
        
        // Assertions.
        if (requestParameters == null) {
            String msg = "Argument 'requestParameters' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        for (Assignment a : assignments) {
            updateTypes(a, requestParameters);
        }
        
    }

    public boolean validateEditPermission(MessageContext msgs) {
        
        /*
         * All fields must be completed.
         */

        // owner
        if (owner == null) {
            msgs.addMessage(new MessageBuilder().error().source("owner")
                            .defaultText("Owner is required").build());
        }
        
        // principals
        if (assignments.isEmpty()) {
            msgs.addMessage(new MessageBuilder().error().source("principal")
                .defaultText("Specify one or more principals").build());
        }
        
        // activity
        if (activity == null) {
            msgs.addMessage(new MessageBuilder().error().source("activity")
                            .defaultText("Activity is required").build());
        }
        
        // target
        if (target == null) {
            msgs.addMessage(new MessageBuilder().error().source("target")
                            .defaultText("Target is required").build());
        }
        
        return msgs.getAllMessages().length == 0;
        
    }
    
    public boolean save(IPermissionStore store, MessageContext msgs) {

        List<IPermission> list = new ArrayList<IPermission>();
        for (Assignment a : assignments) {
            aggregatePermissions(a, list, store);
        }
        
        boolean rslt = true;  // default
        try {
            store.add(list.toArray(new IPermission[0]));
        } catch (Throwable t) {
            log.error(t);
            msgs.addMessage(new MessageBuilder().error().source(null)
                    .defaultText("PermissionStore was unable to save changes").build());
            rslt = false;
        }
        
        return rslt;

    }

    /*
     * Private Stuff.
     */
    
    private void updateTypes(Assignment a, ParameterMap requestParameters) {

        JsonEntityBean principal = a.getPrincipal();
        String val = requestParameters.get(principal.getId() + "_type");
        if (val != null) {
            Assignment.Type type = Assignment.Type.valueOf(val);
            a.setType(type);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("No type parameter specified for the following principal:  name=" 
                                + principal.getName() + ", Id=" + principal.getId());
            }
        }
        
        for (Assignment child : a.getChildren()) {
            updateTypes(child, requestParameters);
        }

    }

    private List<Assignment> updateAssignments(List<Assignment> currentList, List<JsonEntityBean> selections) {

        // Assertions.
        if (currentList == null) {
            String msg = "Argument 'currentList' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (selections == null) {
            String msg = "Argument 'selections' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        final List<Assignment> rslt = new ArrayList<Assignment>();
        
        final Map<JsonEntityBean,Assignment.Type> grantOrDenyMap = new HashMap<JsonEntityBean,Assignment.Type>();
        for (Assignment root : currentList) {
            aggregatePrincipalMap(root, grantOrDenyMap);
        }
        
        // We always rebuild assignments from scratch;  we only take 
        // Assignment.Type from the existing data structure.
        for (JsonEntityBean bean : selections) {
            
            // default Type for newly selected principal is GRANT
            // But that will be overridden by an existing, non-INHERIT entry
            Assignment.Type y = grantOrDenyMap.containsKey(bean) 
                                        ? grantOrDenyMap.get(bean) 
                                        : Assignment.Type.GRANT;
            
            Assignment a = new Assignment(bean, y);
            placeInHierarchy(a, rslt, grantOrDenyMap);

        }
        
        return rslt;

    }
    
    private void aggregatePrincipalMap(Assignment a, Map<JsonEntityBean,Assignment.Type> grantOrDenyMap) {
        
        // Assertions.
        if (a == null) {
            String msg = "Argument 'a' [Assignment] cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (grantOrDenyMap == null) {
            String msg = "Argument 'grantOrDenyMap' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        if (!Assignment.Type.INHERIT.equals(a.getType())) {
            grantOrDenyMap.put(a.getPrincipal(), a.getType());
        }
        
        for (Assignment child : a.getChildren()) {
            aggregatePrincipalMap(child, grantOrDenyMap);
        }

    }
    
    private void aggregatePermissions(Assignment a, List<IPermission> list, IPermissionStore store) {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a' [Assignment] cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (list == null) {
            String msg = "Argument 'list' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (store == null) {
            String msg = "Argument 'store' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        // We don't persist INHERIT records (it's the default)
        Assignment.Type type = a.getType();
        if (!Assignment.Type.INHERIT.equals(type)) {
            JsonEntityBean principal = a.getPrincipal();
            IPermission permission = store.newInstance(owner);
            permission.setPrincipal(PrincipalType.byEntityTypeName(principal.getEntityType()).toInt() 
                                        + RDBMPermissionImpl.PRINCIPAL_SEPARATOR 
                                        + principal.getId());
            permission.setType(type.name());
            permission.setActivity(activity);
            permission.setTarget(target.getId());
            list.add(permission);
        }
        
        for (Assignment child : a.getChildren()) {
            aggregatePermissions(child, list, store);
        }

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
                    parent = new Assignment(bean, assignmentType);
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
