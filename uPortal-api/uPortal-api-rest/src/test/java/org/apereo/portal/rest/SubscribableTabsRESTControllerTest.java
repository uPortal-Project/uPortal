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

import static org.apereo.portal.rest.MarketplaceRESTControllerTest.USER_NAME;

import org.apereo.portal.UserInstance;
import org.apereo.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.apereo.portal.layout.dlm.ConfigurationLoader;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class SubscribableTabsRESTControllerTest {

    @InjectMocks private SubscribableTabsRESTController subscribableTabsRESTController;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private IUserFragmentSubscriptionDao userFragmentSubscriptionDao;
    @Mock private ConfigurationLoader configurationLoader;
    @Mock private MessageSource messageSource;

    private MockHttpServletRequest req;

    @Before
    public void setup() throws Exception {
        subscribableTabsRESTController = new SubscribableTabsRESTController();
        req = new MockHttpServletRequest();
        MockitoAnnotations.initMocks(this);
    }

    //@Test
    public void testGetSubscriptionList() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        IUserInstance user = new UserInstance(person, null, null);

        Mockito.when(userInstanceManager.getUserInstance(req)).thenReturn(user);
        ModelAndView modelAndView = subscribableTabsRESTController.getSubscriptionList(req);

    }
}
