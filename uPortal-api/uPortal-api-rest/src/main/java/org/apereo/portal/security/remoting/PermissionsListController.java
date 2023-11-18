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
package org.apereo.portal.security.remoting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.permission.IPermissionActivity;
import org.apereo.portal.permission.IPermissionOwner;
import org.apereo.portal.permission.dao.IPermissionOwnerDao;
import org.apereo.portal.permission.target.IPermissionTarget;
import org.apereo.portal.permission.target.IPermissionTargetProvider;
import org.apereo.portal.permission.target.IPermissionTargetProviderRegistry;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionStore;
import org.apereo.portal.spring.locator.EntityTypesLocator;
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
    public void setPermissionTargetProviderRegistry(
            IPermissionTargetProviderRegistry targetProviderRegistry) {
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
            HttpServletRequest req,
            HttpServletResponse response)
            throws Exception {

        // ensure the current user is authorized to see permission owners
        // TODO: remove dependency on permission portlet subscription permission
        if (!this.isAuthorized(req)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        IPermission[] result =
                permissionStore.select(
                        ownerParam, principalParam, activityParam, targetParam, null);

        return new ModelAndView("jsonView", "permissionsList", marshall(result));
    }

    /*
     * Private Stuff.
     */

    private List<Map<String, String>> marshall(IPermission[] data) {

        // Assertions.
        if (data == null) {
            String msg = "Argument 'data' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        List<Map<String, String>> result = new ArrayList<Map<String, String>>(data.length);
        for (IPermission p : data) {
            JsonEntityBean bean = getEntityBean(p.getPrincipal());

            Map<String, String> entry = new HashMap<String, String>();
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
                IPermissionActivity activity =
                        permissionOwnerDao.getPermissionActivity(p.getOwner(), p.getActivity());
                entry.put("activityName", activity.getName());

                IPermissionOwner owner = permissionOwnerDao.getPermissionOwner(p.getOwner());
                entry.put("ownerName", owner.getName());

                String providerKey = activity.getTargetProviderKey();
                IPermissionTargetProvider provider =
                        targetProviderRegistry.getTargetProvider(providerKey);

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

            result.add(entry);
        }

        return result;
    }

    protected JsonEntityBean getEntityBean(String principalString) {

        // split the principal string into its type and key components
        String[] parts = principalString.split(PRINCIPAL_SEPARATOR, 2);
        String key = parts[1];
        int typeId = Integer.parseInt(parts[0]);

        // get the EntityEnum type for the entity id number
        Class type = EntityTypesLocator.getEntityTypes().getEntityTypeFromID(typeId);
        String entityType = "person";
        if (IEntityGroup.class.isAssignableFrom(type)) {
            entityType = "group";
        }

        // get the JsonEntityBean for this type and key
        JsonEntityBean bean = groupListHelper.getEntity(entityType, key, false);
        return bean;
    }
}
