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
package org.apereo.portal.rest.layout;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.layout.IStylesheetUserPreferencesService;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.rendering.IPortalRenderingPipeline;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LayoutJsonV43RenderingControllerTest {

    @InjectMocks LayoutJsonV43RenderingController controller;
    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse res;
    @Mock private IPortalRenderingPipeline portalRenderingPipeline;
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private IStylesheetUserPreferencesService stylesheetUserPrefService;

    @Before
    public void setup() {
        controller = new LayoutJsonV43RenderingController();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRenderRequest() throws IOException, ServletException {
        controller.renderRequest(req, res);
        Mockito.verify(portletWindowRegistry).disablePersistentWindowStates(req);
        Mockito.verify(portalRenderingPipeline).renderState(req, res);
    }
}
