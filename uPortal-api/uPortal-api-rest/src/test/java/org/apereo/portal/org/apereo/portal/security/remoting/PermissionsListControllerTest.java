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

import junit.framework.Assert;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.security.remoting.PermissionsListController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class PermissionsListControllerTest {
    @InjectMocks private PermissionsListController permissionsListController;

    @Mock private MockHttpServletRequest req;

    @Mock private MockHttpServletResponse res;

    @Mock private IPersonManager personManager;

    @Before
    public void setup() {
        permissionsListController = new PermissionsListController();
        MockitoAnnotations.initMocks(this);
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
    }

    @Test
    public void testGetAssignments() throws Exception {
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        ModelAndView modelAndView =
                permissionsListController.getAssignments(
                        "owner", "principal", "activity", "target", req, res);

        Assert.assertNull(modelAndView);
    }
}
