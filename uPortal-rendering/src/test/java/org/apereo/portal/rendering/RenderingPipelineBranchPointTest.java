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
package org.apereo.portal.rendering;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.function.Predicate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit tests for RenderingPipelineBranchPoint.
 *
 * @since 5.0
 */
public class RenderingPipelineBranchPointTest {

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private IPortalRenderingPipeline alternatePipe;

    @Mock private Predicate<HttpServletRequest> predicate;

    private RenderingPipelineBranchPoint renderingPipelineBranchPoint;

    @Before
    public void setUp() throws Exception {

        initMocks(this);

        renderingPipelineBranchPoint = new RenderingPipelineBranchPoint();
        renderingPipelineBranchPoint.setPredicate(predicate);
        renderingPipelineBranchPoint.setAlternatePipe(alternatePipe);
    }

    /**
     * Test that when the predicate is true, proceeds down true pipe, ignoring false pipe.
     *
     * @throws ServletException would be a test failure
     * @throws IOException would be a test failure
     */
    @Test
    public void rendersTruePipeWhenPredicateIsTrue() throws ServletException, IOException {

        when(predicate.test(request)).thenReturn(true);

        boolean outcome = renderingPipelineBranchPoint.renderStateIfApplicable(request, response);

        assertTrue("Expected outcome == true", outcome);
        verify(alternatePipe).renderState(request, response);
    }

    /**
     * Test that when the Predicate is false, proceeds down false pipe and ignores true pipe.
     *
     * @throws ServletException would be a test failure
     * @throws IOException would be a test failure
     */
    @Test
    public void rendersFalsePipeWhenPredicateIsFalse() throws ServletException, IOException {

        when(predicate.test(request)).thenReturn(false);

        boolean outcome = renderingPipelineBranchPoint.renderStateIfApplicable(request, response);

        assertFalse("Expected outcome == true", outcome);
        verifyNoMoreInteractions(alternatePipe);
    }

    /** Test that RenderingPipelineBranchPoint has a friendly toString() implementation. */
    @Test
    public void hasFriendlyToString() {

        when(predicate.toString()).thenReturn("String representation of the predicate.");
        when(alternatePipe.toString()).thenReturn("String representation of alternatePipe.");

        final String friendlyToString =
                "RenderingPipelineBranchPoint with predicate "
                        + "[String representation of the predicate.]"
                        + " proceeds down pipe [String representation of alternatePipe.] when the predicate is true.";

        assertEquals(friendlyToString, renderingPipelineBranchPoint.toString());
    }
}
