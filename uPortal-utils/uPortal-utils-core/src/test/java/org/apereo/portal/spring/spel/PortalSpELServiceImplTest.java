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
package org.apereo.portal.spring.spel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.WebRequest;

/** */
public class PortalSpELServiceImplTest {

    PortalSpELServiceImpl provider;
    @Mock WebRequest request;
    @Mock HttpServletRequest portalRequest;
    @Mock IPortalRequestUtils portalRequestUtils;
    @Mock IUserInstanceManager userInstanceManager;
    @Mock IUserInstance userInstance;
    @Mock IPerson person;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new PortalSpELServiceImpl();

        when(request.getContextPath()).thenReturn("/uPortal");
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(portalRequest);
        when(userInstanceManager.getUserInstance(portalRequest)).thenReturn(userInstance);
        when(userInstance.getPerson()).thenReturn(person);

        provider.setPortalRequestUtils(portalRequestUtils);
        provider.setUserInstanceManager(userInstanceManager);
    }

    @Test
    public void testParser() {
        String replaced = provider.parseString("${1+1}", request);
        assertTrue(("2").equals(replaced));

        replaced = provider.parseString("${1+1}/${3+4}", request);
        assertTrue("2/7".equals(replaced));

        replaced = provider.parseString("${1+1}/${3+4}/path", request);
        assertTrue("2/7/path".equals(replaced));
    }
}
