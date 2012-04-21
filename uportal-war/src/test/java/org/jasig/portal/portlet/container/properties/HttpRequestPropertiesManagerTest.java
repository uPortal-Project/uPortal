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

package org.jasig.portal.portlet.container.properties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.MultivaluedMapPopulator;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.collect.ImmutableMap;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HttpRequestPropertiesManagerTest extends TestCase {
    private HttpRequestPropertiesManager httpRequestPropertiesManager;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.httpRequestPropertiesManager = new HttpRequestPropertiesManager();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.httpRequestPropertiesManager = null;
    }

    public void testGetRequestProperties() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setMethod("POST");
        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request);
        
        EasyMock.replay(portletWindow, portalRequestUtils);
        
        this.httpRequestPropertiesManager.setPortalRequestUtils(portalRequestUtils);
        
        final MultivaluedMapPopulator<String, String> populator = new MultivaluedMapPopulator<String, String>();
        this.httpRequestPropertiesManager.populateRequestProperties(request, portletWindow, populator);
        
        final Map<String, List<String>> properties = populator.getMap();

        assertNotNull("properties Map should not be null", properties);
        
        final Map<String, List<String>> expected = ImmutableMap.of(
                "REMOTE_ADDR", Collections.singletonList("1.2.3.4"),
                "REQUEST_METHOD", Collections.singletonList("POST"),
                "REMOTE_HOST", Collections.singletonList("localhost"));
        
        assertEquals(expected, properties);
        
        EasyMock.verify(portletWindow, portalRequestUtils);
    }
}
