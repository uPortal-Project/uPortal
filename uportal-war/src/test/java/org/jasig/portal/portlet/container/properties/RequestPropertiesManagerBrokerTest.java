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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.ParameterMap;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestPropertiesManagerBrokerTest extends TestCase {
    private RequestPropertiesManagerBroker requestPropertiesManagerBroker;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.requestPropertiesManagerBroker = new RequestPropertiesManagerBroker();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.requestPropertiesManagerBroker = null;
    }

    public void testAddProperties() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.replay(portletWindow);
        
        final IRequestPropertiesManager delegateManager1 = new MockRequestPropertiesManager();
        final IRequestPropertiesManager delegateManager2 = new MockRequestPropertiesManager();
        
        this.requestPropertiesManagerBroker.setPropertiesManagers(Arrays.asList(new IRequestPropertiesManager[] { delegateManager1, delegateManager2 }));
        
        
        
        this.requestPropertiesManagerBroker.addResponseProperty(request, portletWindow, "prop.A", "prop.A.1");
        final Map<String, String[]> expected1 = Collections.singletonMap("prop.A", new String[] { "prop.A.1" });
        
        Map<String, String[]> properties = this.requestPropertiesManagerBroker.getRequestProperties(request, portletWindow);
        validateProperties(expected1, properties);
        
        Map<String, String[]> properties1 = delegateManager1.getRequestProperties(request, portletWindow);
        validateProperties(expected1, properties1);
        
        Map<String, String[]> properties2 = delegateManager2.getRequestProperties(request, portletWindow);
        validateProperties(expected1, properties2);
        
        
        
        this.requestPropertiesManagerBroker.addResponseProperty(request, portletWindow, "prop.A", "prop.A.2");
        final Map<String, String[]> expected2 = Collections.singletonMap("prop.A", new String[] { "prop.A.1", "prop.A.2" });
        
        properties = this.requestPropertiesManagerBroker.getRequestProperties(request, portletWindow);
        validateProperties(expected2, properties);
        
        properties1 = delegateManager1.getRequestProperties(request, portletWindow);
        validateProperties(expected2, properties1);
        
        properties2 = delegateManager2.getRequestProperties(request, portletWindow);
        validateProperties(expected2, properties2);
    }

    public void testSetProperties() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.replay(portletWindow);
        
        final IRequestPropertiesManager delegateManager1 = new MockRequestPropertiesManager();
        final IRequestPropertiesManager delegateManager2 = new MockRequestPropertiesManager();
        
        this.requestPropertiesManagerBroker.setPropertiesManagers(Arrays.asList(new IRequestPropertiesManager[] { delegateManager1, delegateManager2 }));
        
        
        
        this.requestPropertiesManagerBroker.setResponseProperty(request, portletWindow, "prop.A", "prop.A.1");
        final Map<String, String[]> expected1 = Collections.singletonMap("prop.A", new String[] { "prop.A.1" });
        
        Map<String, String[]> properties = this.requestPropertiesManagerBroker.getRequestProperties(request, portletWindow);
        validateProperties(expected1, properties);
        
        Map<String, String[]> properties1 = delegateManager1.getRequestProperties(request, portletWindow);
        validateProperties(expected1, properties1);
        
        Map<String, String[]> properties2 = delegateManager2.getRequestProperties(request, portletWindow);
        validateProperties(expected1, properties2);
        
        
        
        this.requestPropertiesManagerBroker.setResponseProperty(request, portletWindow, "prop.A", "prop.A.2");
        final Map<String, String[]> expected2 = Collections.singletonMap("prop.A", new String[] { "prop.A.2" });
        
        properties = this.requestPropertiesManagerBroker.getRequestProperties(request, portletWindow);
        validateProperties(expected2, properties);
        
        properties1 = delegateManager1.getRequestProperties(request, portletWindow);
        validateProperties(expected2, properties1);
        
        properties2 = delegateManager2.getRequestProperties(request, portletWindow);
        validateProperties(expected2, properties2);
    }

    public void testGetProperties() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.replay(portletWindow);
        
        final IRequestPropertiesManager delegateManager1 = new MockRequestPropertiesManager();
        final IRequestPropertiesManager delegateManager2 = new MockRequestPropertiesManager();
        
        this.requestPropertiesManagerBroker.setPropertiesManagers(Arrays.asList(new IRequestPropertiesManager[] { delegateManager1, delegateManager2 }));
        
        
        
        delegateManager1.setResponseProperty(request, portletWindow, "prop.A", "prop.A.1");
        delegateManager1.setResponseProperty(request, portletWindow, "prop.B", "prop.B.1");
        
        delegateManager2.setResponseProperty(request, portletWindow, "prop.C", "prop.C.1");
        
        Map<String, String[]> properties = this.requestPropertiesManagerBroker.getRequestProperties(request, portletWindow);
        
        final Map<String, String[]> expected1 = new ParameterMap();
        expected1.put("prop.A", new String[] { "prop.A.1" });
        expected1.put("prop.B", new String[] { "prop.B.1" });
        expected1.put("prop.C", new String[] { "prop.C.1" });
        validateProperties(expected1, properties);
        
        
        
        delegateManager2.setResponseProperty(request, portletWindow, "prop.A", "prop.A.2");
        
        properties = this.requestPropertiesManagerBroker.getRequestProperties(request, portletWindow);
        
        final Map<String, String[]> expected2 = new ParameterMap();
        expected2.put("prop.A", new String[] { "prop.A.2" });
        expected2.put("prop.B", new String[] { "prop.B.1" });
        expected2.put("prop.C", new String[] { "prop.C.1" });
        validateProperties(expected2, properties);
    }

    /**
     * @param properties
     */
    private void validateProperties(Map<String, String[]> exptected, Map<String, String[]> actual) {
        if (exptected != actual && (actual == null || !exptected.equals(actual))) {
            assertNotNull("actual Map should not be null", actual);
            assertEquals("actual Map is the wrong size", exptected.size(), actual.size());
            
            for (final Map.Entry<String, String[]> expectedEntries : exptected.entrySet()) {
                final String key = expectedEntries.getKey();
                
                assertEquals(Arrays.asList(expectedEntries.getValue()), Arrays.asList(actual.get(key)));
            }
        }
    }
}
