/**
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
package org.jasig.portal.json.rendering;

import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.rendering.IPortalRenderingPipeline;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * JUnit test class for {@link LayoutJsonV1RenderingController}.
 */
public class LayoutJsonV1RenderingControllerTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private IPortalRenderingPipeline portalRenderingPipeline;
    @Mock private IPortletWindowRegistry portletWindowRegistry;

    private LayoutJsonV1RenderingController controller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.controller = new LayoutJsonV1RenderingController();
        this.controller.setPortalRenderingPipeline(this.portalRenderingPipeline);
        this.controller.setPortletWindowRegistry(this.portletWindowRegistry);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void renderRequestMethodShouldSetStructureAttributeStylesheetName() throws Exception {
        // when
        this.controller.v1RenderRequest(this.request, this.response);
        // then
        this.verifyStructureAttributeStylesheetNameWasSet(this.request);
    }

    @Test
    public void renderRequestMethodShouldSetStructureTransformerStylesheetName() throws Exception {
        // when
        this.controller.v1RenderRequest(this.request, this.response);
        // then
        this.verifyStructureTransformerStylesheetNameWasSet(this.request);
    }

    private void verifyStructureAttributeStylesheetNameWasSet(final HttpServletRequest request) {
        verify(this.request).setAttribute(JsonStructureAttributeSource.STYLESHEET_NAME_REQUEST_ATTRIBUTE, "DLMMobileColumns");
    }

    private void verifyStructureTransformerStylesheetNameWasSet(final HttpServletRequest request) {
        verify(this.request).setAttribute(JsonStructureTransformerSource.STYLESHEET_NAME_REQUEST_ATTRIBUTE, "DLMMobileColumns");
    }

}
