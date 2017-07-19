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
package org.apereo.portal.json.rendering;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apereo.portal.events.IPortletExecutionEventFactory;
import org.apereo.portal.rendering.CharacterPipelineComponent;
import org.apereo.portal.url.IUrlSyntaxProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JsonLayoutRenderingPipelineTest {

    @InjectMocks JsonLayoutRenderingPipeline jsonLayoutRenderingPipeline;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;
    private CharacterPipelineComponent pipeline;
    @Mock private IUrlSyntaxProvider urlSyntaxProvider;
    @Mock private IPortletExecutionEventFactory portalEventFactory;

    @Before
    public void setup() {
        jsonLayoutRenderingPipeline = new JsonLayoutRenderingPipeline();
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRenderStateNull() throws ServletException, IOException {
        pipeline = mock(CharacterPipelineComponent.class);
        jsonLayoutRenderingPipeline.setPipeline(pipeline);
        when(pipeline.getEventReader(req, res)).thenReturn(null);
        jsonLayoutRenderingPipeline.renderState(req, res);
        verify(urlSyntaxProvider).getPortalRequestInfo(req);
    }
}
