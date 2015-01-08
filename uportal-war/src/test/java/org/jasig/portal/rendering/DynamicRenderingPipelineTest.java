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

import org.jasig.portal.url.IUrlSyntaxProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for DynamicRenderingPipeline.
 */
public class DynamicRenderingPipelineTest {

    @Mock IUrlSyntaxProvider urlSyntaxProvider;

    @Mock CharacterPipelineComponent characterPipelineComponent;

    @Before
    public void beforeTests() {
        initMocks(this);
    }

    /**
     * Test that DynamicRenderingPipeline has a friendly toString() implementation.
     */
    @Test
    public void hasFriendlyToString() {

        when(urlSyntaxProvider.toString()).thenReturn("String representing urlSyntaxProvider.");
        when(characterPipelineComponent.toString()).thenReturn("String representing characterPipelineComponent.");

        final DynamicRenderingPipeline dynamicRenderingPipeline = new DynamicRenderingPipeline();
        dynamicRenderingPipeline.setUrlSyntaxProvider(urlSyntaxProvider);
        dynamicRenderingPipeline.setPipeline(characterPipelineComponent);

        final String friendlyToString = "DynamicRenderingPipeline using url syntax provider " +
                "[String representing urlSyntaxProvider.] and wrapping pipeline component " +
                "[String representing characterPipelineComponent.].";

        assertEquals(friendlyToString, dynamicRenderingPipeline.toString());
    }
}
