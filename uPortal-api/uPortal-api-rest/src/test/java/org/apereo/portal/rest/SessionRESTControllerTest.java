/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.rest;

import java.util.Map;
import org.apereo.portal.events.PortalEventFactoryImpl;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class SessionRESTControllerTest {

    public static final String USER_NAME = "jdoe";

    @InjectMocks private SessionRESTController sessionRESTController;

    @Mock private IPersonManager personManager;

    @Mock private PortalEventFactoryImpl portalEventFactory;

    private MockHttpServletRequest req;

    private MockHttpServletResponse res;

    @Before
    public void setup() {
        sessionRESTController = new SessionRESTController();
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsAuthenticated404() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(portalEventFactory.getPortalEventSessionId(req, person)).thenReturn("id");
        req.setSession(null);

        ModelAndView modelAndView = sessionRESTController.isAuthenticated(req, res);
        Map<String, Object> attributes =
                (Map<String, Object>) modelAndView.getModel().get("person");

        Assert.assertEquals(404, res.getStatus());
        Assert.assertNull(modelAndView.getModel().get("person"));
    }

    @Test
    public void testIsAuthenticated() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(portalEventFactory.getPortalEventSessionId(req, person)).thenReturn("id");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        req.setSession(session);

        ModelAndView modelAndView = sessionRESTController.isAuthenticated(req, res);
        Map<String, Object> attributes =
                (Map<String, Object>) modelAndView.getModel().get("person");
        Assert.assertEquals(USER_NAME, attributes.get("userName"));
    }
}
