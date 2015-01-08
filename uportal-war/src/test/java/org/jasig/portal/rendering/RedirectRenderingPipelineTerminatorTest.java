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
package org.jasig.portal.rendering;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for RedirectRenderingPipelineTerminator.
 * @since uPortal 4.2
 */
public class RedirectRenderingPipelineTerminatorTest {

    @Mock private HttpServletRequest mockRequest;

    @Mock private HttpServletResponse mockResponse;

    @Before
    public void beforeTests() {
        initMocks(this);
    }

    /**
     * Test that on the happy path, sends a redirect to the properly configured redirectTo path.
     * @throws ServletException never, this would be a test failure
     * @throws IOException never, this would be a test failure
     */
    @Test
    public void redirectsToConfiguredPath()
            throws ServletException, IOException {

        final RedirectRenderingPipelineTerminator terminator = new RedirectRenderingPipelineTerminator();
        terminator.setRedirectTo("/web");

        terminator.renderState(mockRequest, mockResponse);

        verify(mockResponse).sendRedirect("/web");
    }

    /**
     * Test that RedirectRenderingPipelineTerminator throws IllegalStateException if
     * invoked without the redirect target having been set.
     * @throws ServletException never, this would be a test failure.
     * @throws IOException never, this would be a test failure.
     */
    @Test(expected =  IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenRedirectToNotSet()
            throws ServletException, IOException {

        final RedirectRenderingPipelineTerminator unconfiguredTerminator = new RedirectRenderingPipelineTerminator();
        // forget to setRedirectTo

        unconfiguredTerminator.renderState(mockRequest, mockResponse);
    }

    /**
     * Test that attempts to set the redirect path to null on a RedirectRenderingPipelineTerminator
     * are rebuffed with an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionOnSettingRedirectPathToNull() {

        final RedirectRenderingPipelineTerminator badlyConfiguredTerminator = new RedirectRenderingPipelineTerminator();
        badlyConfiguredTerminator.setRedirectTo(null);

    }
}
