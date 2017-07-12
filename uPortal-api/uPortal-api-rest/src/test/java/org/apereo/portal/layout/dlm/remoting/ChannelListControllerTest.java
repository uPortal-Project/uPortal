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
package org.apereo.portal.layout.dlm.remoting;

import org.apereo.portal.i18n.ILocaleStore;
import org.apereo.portal.portlet.marketplace.IMarketplaceService;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.spring.spel.IPortalSpELService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class ChannelListControllerTest {

    @InjectMocks ChannelListController channelListController;
    @Mock private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Mock private IPortletCategoryRegistry portletCategoryRegistry;
    @Mock private IPersonManager personManager;
    @Mock private IPortalSpELService spELService;
    @Mock private ILocaleStore localeStore;
    @Mock private MessageSource messageSource;
    @Mock private IAuthorizationService authorizationService;

    @Autowired private IMarketplaceService marketplaceService;

    private MockHttpServletRequest req;
    private MockHttpServletResponse res;

    @Test
    public void setup() {
        channelListController = new ChannelListController();
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testListChannels() {

        ModelAndView modelAndView = channelListController.listChannels(null, req, null);
    }
}
