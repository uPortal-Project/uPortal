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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.jasig.portal.permission.target.IPermissionTarget;
import org.jasig.portal.permission.target.IPermissionTargetProvider;
import org.jasig.portal.permission.target.IPermissionTargetProviderRegistry;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/permissionAssignments")
public class PermissionsListController extends AbstractPermissionsController {

    private static final String PRINCIPAL_SEPARATOR = "\\.";

    protected final Log log = LogFactory.getLog(getClass());
    
    private IGroupListHelper groupListHelper;
    
    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }
    
    private IPermissionOwnerDao permissionOwnerDao;
    
    @Autowired(required = true)
    public void setPermissionOwnerDao(IPermissionOwnerDao permissionOwnerDao) {
        this.permissionOwnerDao = permissionOwnerDao;
    }
    
    private IPermissionTargetProviderRegistry targetProviderRegistry;
    
    @Autowired(required = true)
    public void setPermissionTargetProviderRegistry(IPermissionTargetProviderRegistry targetProviderRegistry) {
        this.targetProviderRegistry = targetProviderRegistry;
    }
    
    private IPermissionStore permissionStore;
    
    @Autowired
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getAssignments(
            @RequestParam(value = "owner", required = false) String ownerParam,
            @RequestParam(value = "principal", required = false) String principalParam,
            @RequestParam(value = "activity", required = false) String activityParam,
            @RequestParam(value = "target", required = false) String targetParam,
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        // ensure the current user is authorized to see permission owners
        // TODO: remove dependency on permission portlet subscription permission
        if (!this.isAuthorized(req)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        IPermission[] rslt = permissionStore.select(ownerParam, principalParam, 
                            activityParam, targetParam, null);
        
        return new ModelAndView("jsonView", "permissionsList", marshall(rslt));

    }

    /*
     * Private Stuff.
     */
    
    private List<Map<String,String>> marshall(IPermission[] data) {
        
        // Assertions.
        if (data == null) {
            String msg = "Argument 'data' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        List<Map<String,String>> rslt = new ArrayList<Map<String,String>>(data.length);
        for (IPermission p : data) {
            JsonEntityBean bean = getEntityBean(p.getPrincipal());

            Map<String,String> entry = new HashMap<String,String>();
            entry.put("owner", p.getOwner());
            entry.put("principalType", bean.getEntityTypeAsString());
            entry.put("principalName", bean.getName());
            entry.put("principalKey", p.getPrincipal());
            entry.put("activity", p.getActivity());
            entry.put("target", p.getTarget());
            entry.put("permissionType", p.getType());
            
            /*
             *  Attempt to find a name for this target through the permission
             *  target provider registry.  If none can be found, just use
             *  the target key. 
             */
            
            String targetName = null;
            try {
                // attempt to get the target provider for this activity
                IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(p.getOwner(), p.getActivity());
                entry.put("activityName", activity.getName());

                IPermissionOwner owner = permissionOwnerDao.getPermissionOwner(p.getOwner());
                entry.put("ownerName", owner.getName());

                String providerKey = activity.getTargetProviderKey();
                IPermissionTargetProvider provider = targetProviderRegistry.getTargetProvider(providerKey);
                
                // get the target from the provider
                IPermissionTarget target = provider.getTarget(p.getTarget());
                targetName = target.getName();
                
            } catch (RuntimeException e) {
                // likely a result of a null activity or provider
                log.trace("Failed to resolve target name", e);
            }
            
            if (targetName == null) {
                targetName = p.getTarget();
            }
            entry.put("targetName", targetName);

            rslt.add(entry);
            
        }
        
        return rslt;
        
    }
    
    protected JsonEntityBean getEntityBean(String principalString) {
        
        // split the principal string into its type and key components
        String[] parts = principalString.split(PRINCIPAL_SEPARATOR, 2);        
        String key = parts[1];
        int typeId = Integer.parseInt(parts[0]);
        
        // get the EntityEnum type for the entity id number
        @SuppressWarnings("unchecked")
        Class type = EntityTypes.getEntityType(typeId);
        String entityType = "person";
        if (IEntityGroup.class.isAssignableFrom(type)) {
            entityType = "group";
        }

        // get the JsonEntityBean for this type and key
        JsonEntityBean bean = groupListHelper.getEntity(entityType, key, false);
        return bean;
    }

}
