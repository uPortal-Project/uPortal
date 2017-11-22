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
package org.apereo.portal.org.apereo.portal.security.remoting;

import java.util.List;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlets.permissionsadmin.Assignment;
import org.apereo.portal.portlets.permissionsadmin.IPermissionAdministrationHelper;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermissionStore;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.security.remoting.PermissionAssignmentMapController;
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

public class PermissionAssignmentMapControllerTest {

    public static final String ENTITY_TYPE = "entityType";
    public static final String ENTITY_ID_101 = "entityId-101";

    @Mock private IGroupListHelper groupListHelper;

    @Mock private IPermissionAdministrationHelper permissionAdministrationHelper;

    @Mock private IPersonManager personManager;

    @Mock private IPermissionStore permissionStore;

    @Mock private IAuthorizationService authorizationService;

    @InjectMocks private PermissionAssignmentMapController permissionAssignmentMapController;

    @Mock private MockHttpServletRequest req;

    @Mock private MockHttpServletResponse res;

    @Before
    public void setup() {
        permissionAssignmentMapController = new PermissionAssignmentMapController();
        MockitoAnnotations.initMocks(this);
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
    }

    @Test
    public void testUpdatePermissionCannotEditPermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canEditPermission(person, "target"))
                .thenReturn(false);
        ModelAndView modelAndView =
                permissionAssignmentMapController.updatePermission(
                        "principal", "assignment1", str, "owner", "activity1", "target", req, res);

        Assert.assertNull(modelAndView);
        Assert.assertEquals(401L, res.getStatus());
    }

    @Test
    public void testUpdatePermissionCannotViewPermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canViewPermission(person, "target"))
                .thenReturn(false);
        ModelAndView modelAndView =
                permissionAssignmentMapController.updatePermission(
                        "principal", "assignment1", str, "owner", "activity1", "target", req, res);

        Assert.assertNull(modelAndView);
        Assert.assertEquals(401L, res.getStatus());
    }

    private JsonEntityBean buildJsonEntityBean() {
        JsonEntityBean bean = new JsonEntityBean();
        bean.setEntityType(ENTITY_TYPE);
        bean.setId(ENTITY_ID_101);
        bean.setName("test");
        bean.setDescription("Testing bean");

        return bean;
    }

    @Test
    public void testUpdatePermissionNull() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canViewPermission(person, "target"))
                .thenReturn(true);
        Mockito.when(groupListHelper.getEntityForPrincipal("principal")).thenReturn(null);
        ModelAndView modelAndView =
                permissionAssignmentMapController.updatePermission(
                        "principal", "assignment1", str, "owner", "activity1", "target", req, res);

        Assert.assertNull(modelAndView);
    }

    @Test
    public void testDeletePermissionCanEditPermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canEditPermission(person, "target"))
                .thenReturn(false);
        permissionAssignmentMapController.deletePermission(
                "principal", "owner", "activity1", "target", req, res);

        Assert.assertEquals(401L, res.getStatus());
    }

    @Test
    public void testDeletePermissionCanViewPermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canViewPermission(person, "target"))
                .thenReturn(false);
        permissionAssignmentMapController.deletePermission(
                "principal", "owner", "activity1", "target", req, res);

        Assert.assertEquals(401L, res.getStatus());
    }

    @Test
    public void testDeletePermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canViewPermission(person, "target"))
                .thenReturn(true);
        Mockito.when(permissionAdministrationHelper.canEditPermission(person, "target"))
                .thenReturn(true);
        Mockito.when(groupListHelper.getEntityForPrincipal("principal")).thenReturn(null);
        permissionAssignmentMapController.deletePermission(
                "principal", "owner", "activity1", "target", req, res);

        Assert.assertEquals(200L, res.getStatus());
    }

    @Test
    public void testGetOwnersCanNotViewPermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canViewPermission(person, "target"))
                .thenReturn(false);
        ModelAndView modelAndView =
                permissionAssignmentMapController.getOwners(
                        str, "owner", "activity1", "target", req, res);

        Assert.assertEquals(401L, res.getStatus());
    }

    @Test
    public void testGetOwnersCanNotEditPermission() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canEditPermission(person, "target"))
                .thenReturn(false);
        ModelAndView modelAndView =
                permissionAssignmentMapController.getOwners(
                        str, "owner", "activity1", "target", req, res);

        Assert.assertEquals(401L, res.getStatus());
    }

    @Test
    public void testGetOwners() throws Exception {
        String[] str = new String[] {"principal1", "principal2"};
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(permissionAdministrationHelper.canViewPermission(person, "target"))
                .thenReturn(true);
        Mockito.when(permissionAdministrationHelper.canEditPermission(person, "target"))
                .thenReturn(true);
        Mockito.when(groupListHelper.getEntityForPrincipal("principal")).thenReturn(null);
        ModelAndView modelAndView =
                permissionAssignmentMapController.getOwners(
                        str, "owner", "activity1", "target", req, res);
        List<Assignment> model = (List<Assignment>) modelAndView.getModel().get("assignments");

        Assert.assertTrue(model.isEmpty());
    }
}
