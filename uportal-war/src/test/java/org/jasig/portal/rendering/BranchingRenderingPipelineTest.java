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

import com.google.common.base.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for BranchingRedneringPipeline.
 *
 * @since uPortal 4.2
 */
public class BranchingRenderingPipelineTest {

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private IPortalRenderingPipeline truePipe;

    @Mock private IPortalRenderingPipeline falsePipe;

    @Mock private Predicate predicate;

    private BranchingRenderingPipeline branchingRenderingPipeline;

    @Before
    public void setUp() throws Exception {

        initMocks(this);

        branchingRenderingPipeline = new BranchingRenderingPipeline();
        branchingRenderingPipeline.setTruePipe(truePipe);
        branchingRenderingPipeline.setFalsePipe(falsePipe);
        branchingRenderingPipeline.setPredicate(predicate);

    }

    /**
     * Test that when the predicate is true, proceeds down true pipe, ignoring false pipe.
     *
     * @throws ServletException would be a test failure
     * @throws IOException would be a test failure
     */
    @Test
    public void rendersTruePipeWhenPredicateIsTrue()
            throws ServletException, IOException {

        when(predicate.apply(request)).thenReturn(true);

        branchingRenderingPipeline.renderState(request, response);

        verify(truePipe).renderState(request, response);

        verifyZeroInteractions(falsePipe);
    }

    /**
     * Test that when the Predicate is false, proceeds down false pipe and ignores true pipe.
     *
     * @throws ServletException would be a test failure
     * @throws IOException would be a test failure
     */
    @Test
    public void rendersFalsePipeWhenPredicateIsFalse()
            throws ServletException, IOException {

        when(predicate.apply(request)).thenReturn(false);

        branchingRenderingPipeline.renderState(request, response);

        verify(falsePipe).renderState(request, response);

        verifyZeroInteractions(truePipe);
    }

    /**
     * Test that BranchingRenderingPipeline has a friendly toString() implementation.
     */
    @Test
    public void hasFriendlyToString() {

        when(predicate.toString()).thenReturn("String representation of the predicate.");
        when(truePipe.toString()).thenReturn("String representation of truePipe.");
        when(falsePipe.toString()).thenReturn("String representation of falsePipe.");

        final String friendlyToString = "BranchingRenderingPipeline which considering predicate " +
                "[String representation of the predicate.]" +
                " proceeds down pipe [String representation of truePipe.] when the predicate is true" +
                " and proceeds down pipe [String representation of falsePipe.] when the predicate is false.";

        assertEquals(friendlyToString, branchingRenderingPipeline.toString());
    }


}
