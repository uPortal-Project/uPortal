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
package org.apereo.portal.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.api.groups.ApiGroupsService;
import org.apereo.portal.api.groups.Entity;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class GroupRESTControllerTest {

    @InjectMocks private GroupRESTController groupRESTController;

    @Mock private ApiGroupsService apiGroupsService;

    @Mock private IPersonManager personManager;

    @Mock private HttpServletRequest req;

    @Before
    public void setup() {
        groupRESTController = new GroupRESTController();
        apiGroupsService = Mockito.mock(ApiGroupsService.class);
        req = new MockHttpServletRequest();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUsersGroupEmpty() {
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(apiGroupsService.getGroupsForMember("john"))
                .thenReturn(Collections.emptySet());
        ModelAndView modelAndView = groupRESTController.getUsersGroup(req);
        Set<Entity> groups = (Set<Entity>) modelAndView.getModel().get("groups");

        Assert.assertEquals("json", modelAndView.getViewName());
        Assert.assertTrue(groups.isEmpty());
    }

    @Test
    public void testGetUsersGroupNULL() {
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Set<Entity> groups = new HashSet<>();
        groups.add(null);
        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(apiGroupsService.getGroupsForMember("jdoe")).thenReturn(groups);

        ModelAndView modelAndView = groupRESTController.getUsersGroup(req);
        Set<Entity> returnGroups = (Set<Entity>) modelAndView.getModel().get("groups");

        Assert.assertFalse(returnGroups.isEmpty());
    }

    @Test
    public void testGetUsersGroupPersonNULL() {
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Set<Entity> groups = new HashSet<>();
        groups.add(null);
        Mockito.when(personManager.getPerson(req)).thenReturn(null);
        Mockito.when(apiGroupsService.getGroupsForMember("john")).thenReturn(groups);

        ModelAndView modelAndView = groupRESTController.getUsersGroup(req);
        Set<Entity> returnGroups = (Set<Entity>) modelAndView.getModel().get("groups");
        Assert.assertTrue(returnGroups.isEmpty());
    }
}
