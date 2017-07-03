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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.security.IPersonManager;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class GroupRESTControllerTest {

    @InjectMocks private GroupRESTController groupRESTController;

    // @Mock
    // ApiGroupsService apiGroupsService;

    @Mock private IPersonManager personManager;

    @Mock private HttpServletRequest req;

    @Mock private HttpServletResponse res;

    @Before
    public void setup() throws Exception {
        groupRESTController = new GroupRESTController();
        //  groupRESTController.setGroupService(apiGroupsService);
        groupRESTController.setPersonManager(personManager);
        //MockitoAnnotations.initMocks(this);
    }

    /**
     * * Need to figure out why it is not able to find GroupService class while executing the test.
     * Confirm first if api is working or not by deploying it in tomcat, making API request from
     * postman or from uPortal @Test public void testGetUsersGroup() { IPerson person = null;
     * Mockito.when(personManager.getPerson(req).getUserName()).thenReturn("john"); //
     * Mockito.when(apiGroupsService.getGroupsForMember("john")).thenReturn(Collections.emptySet());
     * ModelAndView modelAndView = groupRESTController.getUsersGroup(req,res); assertEquals("json",
     * modelAndView.getViewName()); assertEquals("groups", modelAndView.getViewName());
     * assertEquals(null,modelAndView.getModel()); }
     */
}
