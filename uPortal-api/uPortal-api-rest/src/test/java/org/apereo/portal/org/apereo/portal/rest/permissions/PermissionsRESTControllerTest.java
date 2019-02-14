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
package org.apereo.portal.org.apereo.portal.rest.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.permission.IPermissionActivity;
import org.apereo.portal.permission.IPermissionOwner;
import org.apereo.portal.permission.dao.IPermissionOwnerDao;
import org.apereo.portal.permission.dao.jpa.JpaPermissionOwnerDao;
import org.apereo.portal.permission.target.IPermissionTarget;
import org.apereo.portal.permission.target.IPermissionTargetProvider;
import org.apereo.portal.permission.target.IPermissionTargetProviderRegistry;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.rest.permissions.PermissionsRESTController;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class PermissionsRESTControllerTest {

    @InjectMocks private PermissionsRESTController permissionsRESTController;

    @Mock private IPermissionOwnerDao permissionOwnerDao;
    @Mock private IPermissionTargetProviderRegistry targetProviderRegistry;
    @Mock private IGroupListHelper groupListHelper;
    @Mock private IAuthorizationService authorizationService;
    @Mock private IPermissionStore permissionStore;

    private MockHttpServletRequest req;

    private MockHttpServletResponse res;

    @Before
    public void setup() {
        permissionsRESTController = new PermissionsRESTController();
        permissionOwnerDao = Mockito.mock(JpaPermissionOwnerDao.class);
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetOwnersNull() {
        Mockito.when(permissionOwnerDao.getAllPermissionOwners()).thenReturn(null);
        ModelAndView modelAndView = permissionsRESTController.getOwners();

        Assert.assertNull(modelAndView.getModel().get("owners"));
    }

    @Test
    public void testGetOwnersEmpty() {
        Mockito.when(permissionOwnerDao.getAllPermissionOwners())
                .thenReturn(Collections.emptyList());
        ModelAndView modelAndView = permissionsRESTController.getOwners();
        List<IPermissionOwner> owners =
                (List<IPermissionOwner>) modelAndView.getModel().get("owners");

        Assert.assertTrue(owners.isEmpty());
    }

    @Test
    public void testGetOwnersByOwnerParamString() {
        String ownerParam = "test";
        IPermissionOwner owner = Mockito.mock(IPermissionOwner.class);
        owner.setFname("john");
        owner.setDescription("testing description");
        owner.setName("john doe");

        Mockito.when(permissionOwnerDao.getPermissionOwner(ownerParam)).thenReturn(owner);
        ModelAndView modelAndView = permissionsRESTController.getOwners(ownerParam, res);
        IPermissionOwner returnOwner = (IPermissionOwner) modelAndView.getModel().get("owner");

        Assert.assertEquals(owner.getId(), returnOwner.getId());
        Assert.assertEquals(200, res.getStatus());
    }

    @Test
    public void testGetOwnersByOwnerParamNumeric() {
        String ownerParam = "123";

        IPermissionOwner owner = Mockito.mock(IPermissionOwner.class);
        owner.setFname("john");
        owner.setDescription("testing description");
        owner.setName("john doe");

        Mockito.when(permissionOwnerDao.getPermissionOwner(Long.valueOf(ownerParam)))
                .thenReturn(owner);

        ModelAndView modelAndView = permissionsRESTController.getOwners(ownerParam, res);

        IPermissionOwner returnOwner = (IPermissionOwner) modelAndView.getModel().get("owner");

        Assert.assertEquals(owner.getId(), returnOwner.getId());
        Assert.assertEquals(200, res.getStatus());
    }

    @Test
    public void testGetActivitiesEmpty() {
        String query = "activity1";

        Mockito.when(permissionOwnerDao.getAllPermissionOwners())
                .thenReturn(Collections.EMPTY_LIST);
        ModelAndView modelAndView = permissionsRESTController.getActivities(query);
        List<IPermissionActivity> activities =
                (List<IPermissionActivity>) modelAndView.getModel().get("activities");

        Assert.assertTrue(activities.isEmpty());
        Assert.assertEquals(200, res.getStatus());
    }

    @Test
    public void testGetActivities() {
        String query = "activity1";
        IPermissionOwner owner = Mockito.mock(IPermissionOwner.class);
        owner.setFname("john");
        owner.setDescription("testing description");
        owner.setName("john doe");
        IPermissionActivity activity = Mockito.mock(IPermissionActivity.class);
        activity.setDescription("Course Activity");
        activity.setFname("john");
        activity.setName("activity1");
        owner.getActivities().add(activity);
        List<IPermissionOwner> owners = new ArrayList<>();
        owners.add(owner);

        Mockito.when(permissionOwnerDao.getAllPermissionOwners()).thenReturn(owners);
        ModelAndView modelAndView = permissionsRESTController.getActivities(query);
        List<IPermissionActivity> activities =
                (List<IPermissionActivity>) modelAndView.getModel().get("activities");

        Assert.assertEquals(200, res.getStatus());
    }

    @Test
    public void testGetTargetsEmpty() {
        Long activityId = 2L;
        String query = "activity1";

        IPermissionActivity activity = Mockito.mock(IPermissionActivity.class);
        activity.setDescription("Course Activity");
        activity.setFname("john");
        activity.setName("activity1");

        IPermissionTargetProvider provider = Mockito.mock(IPermissionTargetProvider.class);
        activity.setTargetProviderKey("providerKey");

        Mockito.when(permissionOwnerDao.getPermissionActivity(activityId)).thenReturn(activity);
        Mockito.when(targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey()))
                .thenReturn(provider);

        ModelAndView modelAndView = permissionsRESTController.getTargets(activityId, query);
        Collection<IPermissionTarget> targets =
                (Collection<IPermissionTarget>) modelAndView.getModel().get("targets");

        Assert.assertEquals(200, res.getStatus());

        Assert.assertTrue(targets.isEmpty());
    }

    @Test
    public void testGetTargetsNullActivity() {
        Long activityId = 2L;
        String query = "activity1";

        IPermissionActivity activity = Mockito.mock(IPermissionActivity.class);
        activity.setDescription("Course Activity");
        activity.setFname("john");
        activity.setName("activity1");

        IPermissionTargetProvider provider = Mockito.mock(IPermissionTargetProvider.class);
        activity.setTargetProviderKey("providerKey");

        Mockito.when(permissionOwnerDao.getPermissionActivity(activityId)).thenReturn(null);
        Mockito.when(targetProviderRegistry.getTargetProvider(activity.getTargetProviderKey()))
                .thenReturn(null);

        ModelAndView modelAndView = permissionsRESTController.getTargets(activityId, query);
        Collection<IPermissionTarget> targets =
                (Collection<IPermissionTarget>) modelAndView.getModel().get("targets");
        Assert.assertTrue(targets.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testGetgetAssignmentsForPrincipalNullPrincipal() {
        String principal = "principal";
        boolean includeInherited = true;
        JsonEntityBean entity = buildJsonPersonEntityBean();
        Mockito.when(groupListHelper.getEntityForPrincipal(principal)).thenReturn(entity);
        Mockito.when(
                        this.authorizationService.newPrincipal(
                                entity.getId(), entity.getEntityType().getClazz()))
                .thenReturn(null);
        ModelAndView modelAndView =
                permissionsRESTController.getAssignmentsForPrincipal(principal, includeInherited);
        Collection<IPermissionTarget> targets =
                (Collection<IPermissionTarget>) modelAndView.getModel().get("assignments");
    }

    private JsonEntityBean buildJsonPersonEntityBean() {
        JsonEntityBean personBean = new JsonEntityBean();
        personBean.setEntityType(EntityEnum.GROUP.toString());
        personBean.setId("person101");
        personBean.setName("test");
        personBean.setDescription("Testing person bean");

        return personBean;
    }

    @Test
    public void testGetgetAssignmentsForTargetNull() {
        String target = "target";
        boolean includeInherited = false;
        JsonEntityBean entity = buildJsonPersonEntityBean();

        Mockito.when(permissionStore.select(null, null, null, target, null))
                .thenReturn(new IPermission[0]);
        Mockito.when(groupListHelper.getEntityForPrincipal(target)).thenReturn(null);
        Mockito.when(
                        this.authorizationService.newPrincipal(
                                entity.getId(), entity.getEntityType().getClazz()))
                .thenReturn(null);

        ModelAndView modelAndView =
                permissionsRESTController.getAssignmentsOnTarget(target, includeInherited);

        Collection<IPermissionTarget> assignments =
                (Collection<IPermissionTarget>) modelAndView.getModel().get("assignments");
    }
}
