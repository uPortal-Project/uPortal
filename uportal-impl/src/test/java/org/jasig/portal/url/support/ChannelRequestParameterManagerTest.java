/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.url.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChannelRequestParameterManagerTest extends TestCase {
    public void testNoParameters() throws Exception {
        final ChannelRequestParameterManager parameterManager = new ChannelRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        parameterManager.setNoChannelParameters(request);
        
        try {
            parameterManager.setChannelParameters(request, "id", new HashMap<String, Object[]>());
            fail("An IllegalStateException should have been thrown for calling setChannelParameters after setNoChannelParameters");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        final Set<String> targetedChannelIds = parameterManager.getTargetedChannelIds(request);
        assertEquals(Collections.emptySet(), targetedChannelIds);
        
        final Map<String, Object[]> channelParameters = parameterManager.getChannelParameters(request, "id");
        assertNull("channelParameters should be null", channelParameters);
    }
    
    public void testParameters() throws Exception {
        final ChannelRequestParameterManager parameterManager = new ChannelRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
        parameters.put("p1", new Object[] { "v1.1" });
        
        parameterManager.setChannelParameters(request, "id", parameters);
        
        try {
            parameterManager.setNoChannelParameters(request);
            fail("An IllegalStateException should have been thrown for calling setNoChannelParameters after setChannelParameters");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        final Set<String> targetedChannelIds = parameterManager.getTargetedChannelIds(request);
        assertEquals(Collections.singleton("id"), targetedChannelIds);
        
        final Map<String, Object[]> channelParameters = parameterManager.getChannelParameters(request, "id");
        assertEquals(parameters, channelParameters);
    }

    public void testNoParsing() throws Exception {
        final ChannelRequestParameterManager parameterManager = new ChannelRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            parameterManager.getTargetedChannelIds(request);
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getTargetedChannelIds before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        try {
            parameterManager.getChannelParameters(request, "id");
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getChannelParameters before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
    }
}
