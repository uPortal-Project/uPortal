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

package org.jasig.portal.rest.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
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
import org.jasig.portal.security.provider.AuthorizationImpl;
import org.jasig.portal.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * PermissionsRESTController provides a REST endpoing for permission owners
 * and activities.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
public class PermissionsRESTController {
    
    protected final Log log = LogFactory.getLog(getClass());

    private IPermissionOwnerDao permissionOwnerDao;
    
    @Autowired(required = true)
    public void setPermissionOwnerDao(IPermissionOwnerDao permissionOwnerDao) {
        this.permissionOwnerDao = permissionOwnerDao;
    }

    private IPermissionTargetProviderRegistry targetProviderRegistry;
    
    @Autowired(required = true)
    public void setPermissionTargetProviderRegistry(IPermissionTargetProviderRegistry registry) {
        this.targetProviderRegistry = registry;
    }

    private IPermissionStore permissionStore;
    
    @Autowired
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    private IGroupListHelper groupListHelper;
    
    @Autowired
    public void setGroupListHelper(IGroupListHelper groupListHelper) {
        this.groupListHelper = groupListHelper;
    }
    
    private IAuthorizationService authorizationService;

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Provide a JSON view of all known permission owners registered with uPortal.
     * 
     * @param req
     * @param response
     * @return
     * @throws Exception 
     */
    @PreAuthorize("hasPermission('string', 'ALL', new org.jasig.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping(value="/permissions/owners.json", method = RequestMethod.GET)
    public ModelAndView getOwners(
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        // get a list of all currently defined permission owners
        List<IPermissionOwner> owners = permissionOwnerDao.getAllPermissionOwners();
        
        ModelAndView mv = new ModelAndView();
        mv.addObject("owners", owners);
        mv.setViewName("json");
        
        return mv;
    }
    
    /**
     * Provide a detailed view of the specified IPermissionOwner.  This view
     * should contain a list of the owner's defined activities.
     * 
     * @param ownerParam
     * @param req
     * @param response
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasPermission('string', 'ALL', new org.jasig.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping(value="/permissions/owners/{owner}.json", method = RequestMethod.GET)
    public ModelAndView getOwners(
            @PathVariable("owner") String ownerParam,
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        IPermissionOwner owner = null;
        
        if (StringUtils.isNumeric(ownerParam)) {
            Long id = Long.valueOf(ownerParam);
            owner = permissionOwnerDao.getPermissionOwner(id);
        } else {
            owner = permissionOwnerDao.getPermissionOwner(ownerParam);
        }
        
        // if the IPermissionOwner was found, add it to the JSON model
        if (owner != null) {
            ModelAndView mv = new ModelAndView();
            mv.addObject("owner", owner);
            mv.setViewName("json");            
            return mv;
        } 
        
        // otherwise return a 404 not found error code
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
    }
    
    /**
     * Provide a list of all registered IPermissionActivities.  If an optional
     * search string is provided, the returned list will be restricted to 
     * activities matching the query.
     * 
     * @param query     optional search query
     * @param request    
     * @param response
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasPermission('string', 'ALL', new org.jasig.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping(value="/permissions/activities.json", method = RequestMethod.GET)
    public ModelAndView getActivities(
            @RequestParam(value="q", required=false) String query,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        if (StringUtils.isNotBlank(query)) {
            query = query.toLowerCase();
        }

        List<IPermissionActivity> activities = new ArrayList<IPermissionActivity>();
        Collection<IPermissionOwner> owners = permissionOwnerDao.getAllPermissionOwners();
        for (IPermissionOwner owner : owners) {
            for (IPermissionActivity activity : owner.getActivities()) {
                if (StringUtils.isBlank(query) || activity.getName().toLowerCase().contains(query)) {
                    activities.add(activity);
                }
            }
        }
        Collections.sort(activities);
        
        ModelAndView mv = new ModelAndView();
        mv.addObject("activities", activities);
        mv.setViewName("json");
        
        return mv;
    }

    /**
     * Return a list of targets defined for a particular IPermissionActivity 
     * matching the specified search query. 
     * 
     * @param activityId
     * @param query
     * @param req
     * @param response
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasPermission('string', 'ALL', new org.jasig.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping(value="/permissions/{activity}/targets.json", method = RequestMethod.GET)
    public ModelAndView getTargets(@PathVariable("activity") Long activityId,
            @RequestParam("q") String query,
            HttpServletRequest req, HttpServletResponse response)
            throws Exception {
        
        IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(activityId);
        IPermissionTargetProvider provider = targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey());
        
        SortedSet<IPermissionTarget> matchingTargets = new TreeSet<IPermissionTarget>();
        // add matching results for this identifier provider to the set
        Collection<IPermissionTarget> targets = provider.searchTargets(query);
        for (IPermissionTarget target : targets) {
            if ((StringUtils.isNotBlank(target.getName()) && target
                    .getName().toLowerCase().contains(query))
                    || target.getKey().toLowerCase().contains(query)) {
                matchingTargets.addAll(targets);
            }
        }

        ModelAndView mv = new ModelAndView();
        mv.addObject("targets", targets);
        mv.setViewName("json");
        
        return mv;
    }
    
    @PreAuthorize("hasPermission('string', 'ALL', new org.jasig.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping("/assignments/principal/{principal}.json")
    public ModelAndView getAssignmentsForPrincipal(@PathVariable("principal") String principal, @RequestParam(value="includeInherited", required = false) boolean includeInherited, HttpServletRequest request, HttpServletResponse response) {
        
        Set<UniquePermission> directAssignments = new HashSet<UniquePermission>();
        
        JsonEntityBean entity = groupListHelper.getEntityForPrincipal(principal);
        IAuthorizationPrincipal p = this.authorizationService.newPrincipal(entity.getId(), entity.getEntityType().getClazz());
        
        // first get the permissions explicitly set for this principal
        IPermission[] directPermissions = permissionStore.select(null, p.getPrincipalString(), null, null, null);
        for (IPermission permission : directPermissions) {
            directAssignments.add(new UniquePermission(permission.getOwner(), permission.getActivity(), permission.getTarget(), false));
        }
        
        Set<UniquePermission> inheritedAssignments = new HashSet<UniquePermission>();
        if (includeInherited) {
            IGroupMember member = GroupService.getGroupMember(p.getKey(), p.getType());
            for (Iterator<IEntityGroup> iter = member.getAllContainingGroups(); iter.hasNext();) {
                IEntityGroup parent = iter.next();

                IAuthorizationPrincipal parentPrincipal = this.authorizationService.newPrincipal(parent);
                IPermission[] parentPermissions = permissionStore.select(null, parentPrincipal.getPrincipalString(), null, null, null);
                for (IPermission permission : parentPermissions) {
                    inheritedAssignments.add(new UniquePermission(permission.getOwner(), permission.getActivity(), permission.getTarget(), true));
                }
            }
        }
        
        List<JsonPermission> permissions = new ArrayList<JsonPermission>();
        
        for (UniquePermission permission : directAssignments) {
            if (p.hasPermission(permission.getOwner(), permission.getActivity(), permission.getIdentifier())) {
                permissions.add(getPermissionForPrincipal(permission, entity));
            }
        }

        for (UniquePermission permission : inheritedAssignments) {
            if (p.hasPermission(permission.getOwner(), permission.getActivity(), permission.getIdentifier())) {
                permissions.add(getPermissionForPrincipal(permission, entity));
            }
        }
        Collections.sort(permissions);

        ModelAndView mv = new ModelAndView();
        mv.addObject("assignments", permissions);
        mv.setViewName("json");
        
        return mv;
    }
    
    @PreAuthorize("hasPermission('string', 'ALL', new org.jasig.portal.spring.security.evaluator.AuthorizableActivity('UP_PERMISSIONS', 'VIEW_PERMISSIONS'))")
    @RequestMapping("/assignments/target/{target}.json")
    public ModelAndView getAssignmentsOnTarget(@PathVariable("target") String target, @RequestParam(value="includeInherited", required = false) boolean includeInherited, HttpServletRequest request, HttpServletResponse response) {
        
        Set<UniquePermission> directAssignments = new HashSet<UniquePermission>();
        
        // first get the permissions explicitly set for this principal
        IPermission[] directPermissions = permissionStore.select(null, null, null, target, null);
        for (IPermission permission : directPermissions) {
            directAssignments.add(new UniquePermission(permission.getOwner(), permission.getActivity(), permission.getPrincipal(), false));
        }

        JsonEntityBean entity = groupListHelper.getEntityForPrincipal(target);
        IAuthorizationPrincipal p = this.authorizationService.newPrincipal(entity.getId(), entity.getEntityType().getClazz());
        
        Set<UniquePermission> inheritedAssignments = new HashSet<UniquePermission>();
        if (includeInherited) {
            IGroupMember member = GroupService.getGroupMember(p.getKey(), p.getType());
            for (Iterator<IEntityGroup> iter = member.getAllContainingGroups(); iter.hasNext();) {
                IEntityGroup parent = iter.next();

                IAuthorizationPrincipal parentPrincipal = this.authorizationService.newPrincipal(parent);
                IPermission[] parentPermissions = permissionStore.select(null, null, null, parentPrincipal.getKey(), null);
                for (IPermission permission : parentPermissions) {
                    inheritedAssignments.add(new UniquePermission(permission.getOwner(), permission.getActivity(), permission.getPrincipal(), true));
                }
            }
        }
        
        List<JsonPermission> permissions = new ArrayList<JsonPermission>();
        
        for (UniquePermission permission : directAssignments) {
            JsonEntityBean e = groupListHelper.getEntityForPrincipal(permission.getIdentifier());
            Class<?> clazz;
            EntityEnum entityType = EntityEnum.getEntityEnum(e.getEntityTypeAsString());
            if (entityType.isGroup()) {
                clazz = IEntityGroup.class;
            } else {
                clazz = entityType.getClazz();
            }
            IAuthorizationPrincipal principal = this.authorizationService.newPrincipal(e.getId(), clazz);
            if (principal.hasPermission(permission.getOwner(), permission.getActivity(), p.getKey())) {
                permissions.add(getPermissionOnTarget(permission, entity));
            }
        }

        for (UniquePermission permission : inheritedAssignments) {
            JsonEntityBean e = groupListHelper.getEntityForPrincipal(permission.getIdentifier());
            Class<?> clazz;
            EntityEnum entityType = EntityEnum.getEntityEnum(e.getEntityTypeAsString());
            if (entityType.isGroup()) {
                clazz = IEntityGroup.class;
            } else {
                clazz = entityType.getClazz();
            }
            IAuthorizationPrincipal principal = this.authorizationService.newPrincipal(e.getId(), clazz);
            if (principal.hasPermission(permission.getOwner(), permission.getActivity(), p.getKey())) {
                permissions.add(getPermissionOnTarget(permission, entity));
            }
        }
        Collections.sort(permissions);

        ModelAndView mv = new ModelAndView();
        mv.addObject("assignments", permissions);
        mv.setViewName("json");
        
        return mv;
    }
    
    protected JsonPermission getPermissionForPrincipal(UniquePermission permission, JsonEntityBean entity) {
        
        JsonPermission perm = new JsonPermission();
        perm.setOwnerKey(permission.getOwner());
        perm.setActivityKey(permission.getActivity());
        perm.setTargetKey(permission.getIdentifier());
        perm.setPrincipalKey(entity.getId());
        perm.setPrincipalName(entity.getName());
        perm.setInherited(permission.isInherited());
        
        try {

            IPermissionOwner owner = permissionOwnerDao.getPermissionOwner(permission.getOwner());
            if (owner != null) {
                perm.setOwnerName(owner.getName());
            }
            
            IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(permission.getOwner(), permission.getActivity());
            if (activity != null) {
                perm.setActivityName(activity.getName());
            
                IPermissionTargetProvider targetProvider = targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey());
                if (targetProvider != null) {
                    IPermissionTarget target = targetProvider.getTarget(permission.getIdentifier());
                    if (target != null) {
                        perm.setTargetName(target.getName());
                    }
                }
            }
            
        } catch (RuntimeException e) {
            log.warn("Exception while adding permission", e);
        }
        return perm;
    }
    
    protected JsonPermission getPermissionOnTarget(UniquePermission permission, JsonEntityBean entity) {
        
        JsonPermission perm = new JsonPermission();
        perm.setOwnerKey(permission.getOwner());
        perm.setActivityKey(permission.getActivity());
        perm.setTargetKey(entity.getId());
        perm.setTargetName(entity.getName());
        perm.setInherited(permission.isInherited());
        
        try {

            IPermissionOwner owner = permissionOwnerDao.getPermissionOwner(permission.getOwner());
            if (owner != null) {
                perm.setOwnerName(owner.getName());
            } else {
                perm.setOwnerName(permission.getOwner());
            }
            
            IPermissionActivity activity = permissionOwnerDao.getPermissionActivity(permission.getOwner(), permission.getActivity());
            if (activity != null) {
                perm.setActivityName(activity.getName());
            } else {
                perm.setActivityName(permission.getActivity());
            }
            
            JsonEntityBean principal = groupListHelper.getEntityForPrincipal(permission.getIdentifier());
            if (principal != null) {
                perm.setPrincipalKey(principal.getId());
                perm.setPrincipalName(principal.getName());
            }
            
        } catch (RuntimeException e) {
            log.warn("Exception while adding permission", e);
        }
        return perm;
    }
    
    protected static final class UniquePermission {
        
        private final String owner;
        private final String activity;
        private final String identifier;
        private final boolean inherited;
        
        public UniquePermission(String owner, String activity, String identifier, boolean inherited) {
            this.owner = owner;
            this.activity = activity;
            this.identifier = identifier;
            this.inherited = inherited;
        }

        public String getOwner() {
            return owner;
        }

        public String getActivity() {
            return activity;
        }

        public String getIdentifier() {
            return identifier;
        }

        public boolean isInherited() {
            return inherited;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.activity == null) ? 0 : this.activity.hashCode());
            result = prime * result + ((this.identifier == null) ? 0 : this.identifier.hashCode());
            result = prime * result + (this.inherited ? 1231 : 1237);
            result = prime * result + ((this.owner == null) ? 0 : this.owner.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UniquePermission other = (UniquePermission) obj;
            if (this.activity == null) {
                if (other.activity != null)
                    return false;
            }
            else if (!this.activity.equals(other.activity))
                return false;
            if (this.identifier == null) {
                if (other.identifier != null)
                    return false;
            }
            else if (!this.identifier.equals(other.identifier))
                return false;
            if (this.inherited != other.inherited)
                return false;
            if (this.owner == null) {
                if (other.owner != null)
                    return false;
            }
            else if (!this.owner.equals(other.owner))
                return false;
            return true;
        }
    }
}
