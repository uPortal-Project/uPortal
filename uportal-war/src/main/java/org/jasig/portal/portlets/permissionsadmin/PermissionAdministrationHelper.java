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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.permission.target.IPermissionTargetProvider;
import org.jasig.portal.permission.target.IPermissionTargetProviderRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
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
public class PermissionAdministrationHelper {

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

    private IPermissionTargetProviderRegistry targetProviderRegistry;
    
    @Autowired(required = true)
    public void setTargetProviderRegistry(
            IPermissionTargetProviderRegistry targetProviderRegistry) {
        this.targetProviderRegistry = targetProviderRegistry;
    }
    
    /**
     * Get a List of JsonEntityBeans representing the principals currently
     * assigned permissions in a PermissionDefinitionForm.
     * 
     * @param form
     * @return
     */
    public List<JsonEntityBean> getPrincipals(PermissionDefinitionForm form) {
        List<JsonEntityBean> beans = new ArrayList<JsonEntityBean>();
        for (String principal : form.getPermissions().keySet()) {
            JsonEntityBean entity = groupListHelper.getEntityForPrincipal(principal);
            beans.add(entity);
        }
        return beans;
    }
    
    /**
     * Update the permissions in an existing PermissionDefinitionForm to match
     * the list of specified principals.  For principals that were already
     * included in the form's permission map, permission settings will be 
     * preserved.  New principals will be initially assigned "INHERIT" level
     * permissions, and permissions that previously existed in the form but
     * are no longer represented in the principals collection will be removed.
     * 
     * @param form
     * @param principals
     */
    public void updatePrincipals(PermissionDefinitionForm form, Collection<JsonEntityBean> principals) {
        
        // initialize a new emtpy permissions map
        Map<String, String> newPermissions = new HashMap<String, String>();
        
        // add a permission for each member of the principals collection
        for (JsonEntityBean entity : principals) {
            IAuthorizationPrincipal principal = groupListHelper.getPrincipalForEntity(entity);
            
            // if this principal was already in the form, copy over the old
            // permission assignment
            if (form.getPermissions().containsKey(principal.getPrincipalString())) {
                newPermissions.put(principal.getPrincipalString(), form.getPermissions().get(principal.getPrincipalString()));
            }
            
            // otherwise, set the permission level to INHERIT
            else {
                newPermissions.put(principal.getPrincipalString(), "INHERIT");
            }
        }
        
        // update the form's permission map
        form.setPermissions(newPermissions);
    }
    
    /**
     * Get a new PermissionDefinitionForm for the specified owner, 
     * activity, and target.  This form will be initialized with the current
     * permission assignments for the specified arguments. 
     * 
     * @param owner
     * @param activity
     * @param targetKey
     * @return
     */
    public PermissionDefinitionForm getForm(IPermissionOwner owner, IPermissionActivity activity, String targetKey) {
        
        // Construct a new form with the specified owner and activity
        PermissionDefinitionForm form = new PermissionDefinitionForm();
        form.setOwner(owner);
        form.setActivity(activity);

        // Get the target provider for this activity and target key and
        // set the target object
        IPermissionTargetProvider provider = targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey());
        IPermissionTarget target = provider.getTarget(targetKey);
        form.setTarget(target);

        // Find permissions that match the inputs from the IPermissionStore
        IPermission[] permissions = permissionStore.select(owner.getFname(), null, activity.getFname(), target.getKey(), null);
        
        // Build the set of existing assignments
        for (IPermission p : permissions) {
            form.getPermissions().put(p.getPrincipal(), p.getType());
        }

        return form;
    }
    
    /**
     * Persist the permissions in the supplied form.  This method will overwrite
     * any existing permissions for the form's owner/activity/target combination.
     * 
     * @param form
     */
    public void savePermissions(PermissionDefinitionForm form) {
        
        String ownerFname = form.getOwner().getFname();
        String activityFname = form.getActivity().getFname();
        String targetFname = form.getTarget().getKey();
        
        List<IPermission> permissions = new ArrayList<IPermission>();
        
        /*
         * For each assignment in the form's list, construct a new IPermission
         * instance and add it to our permission list.
         */
        
        for (Map.Entry<String, String> entry : form.getPermissions().entrySet()) {
            
            Assignment.Type assignmentType = Assignment.Type.valueOf(entry.getValue());
            
            if (assignmentType != null && !Assignment.Type.INHERIT.equals(assignmentType)) {
                
                JsonEntityBean principal = groupListHelper.getEntityForPrincipal(entry.getKey());
                
                // initialize a new permission with the correct owner, activity,
                // and target
                IPermission permission = permissionStore.newInstance(ownerFname);
                permission.setActivity(activityFname);
                permission.setTarget(targetFname);

                permission.setType(entry.getValue());

                // construct an authorization principal for this JsonEntityBean
                IAuthorizationPrincipal p = groupListHelper.getPrincipalForEntity(principal);
                permission.setPrincipal(p.getPrincipalString());
                
                permissions.add(permission);
                
            }

        }
        
        
        try {
            
            // clear out any existing permissions for this activity and target
            IPermission[] existing = permissionStore.select(ownerFname, null,
                    activityFname, targetFname, null);
            permissionStore.delete(existing);
            
            // store the new permissions
            permissionStore.add(permissions.toArray(new IPermission[0]));
            
        } catch (Throwable t) {
            log.error(t);
            throw new RuntimeException(t);
        }

    }

    /**
     * Retrieve a permission map suitable for adding to a PermissionDefinitionForm
     * object from the PortletRequest.  This implementation 
     * 
     * @param form
     * @param request
     * @return
     */
    public Map<String, String> getPermissionAssignmentsFromRequest(PermissionDefinitionForm form, PortletRequest request) {
        
        Map<String, String> newPermissions = new HashMap<String, String>();
        
        @SuppressWarnings("unchecked")
        Enumeration<String> parameterNames = (Enumeration<String>) request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String value = request.getParameter(paramName);
            if (paramName.startsWith("permissions") && !Assignment.Type.INHERIT.toString().equals(value)) {
                String principal = paramName.substring(13, paramName.length()-2);
                newPermissions.put(principal, value);
            }
        }
        
        return newPermissions;
    }
    
}
