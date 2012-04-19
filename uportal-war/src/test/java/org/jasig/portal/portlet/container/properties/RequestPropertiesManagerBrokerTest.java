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
import org.jasig.portal.utils.MultivaluedMapPopulator;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
        
        final MockRequestPropertiesManager delegateManager1 = new MockRequestPropertiesManager();
        delegateManager1.setOrder(Ordered.HIGHEST_PRECEDENCE);
        final MockRequestPropertiesManager delegateManager2 = new MockRequestPropertiesManager();
        delegateManager2.setOrder(Ordered.LOWEST_PRECEDENCE);
        
        this.requestPropertiesManagerBroker.setPropertiesManagers(ImmutableList.of(delegateManager1, delegateManager2));
        
        
        
        this.requestPropertiesManagerBroker.addResponseProperty(request, portletWindow, "prop.A", "prop.A.1");
        final Map<String, List<String>> expected1 = Collections.singletonMap("prop.A", Collections.singletonList("prop.A.1"));
        
        MultivaluedMapPopulator<String, String> multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        this.requestPropertiesManagerBroker.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected1, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager1.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected1, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager2.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(Collections.EMPTY_MAP, multivaluedMapPopulator.getMap());
        
        
        
        this.requestPropertiesManagerBroker.addResponseProperty(request, portletWindow, "prop.A", "prop.A.2");
        final Map<String, List<String>> expected2 = Collections.<String, List<String>>singletonMap("prop.A", ImmutableList.of("prop.A.1", "prop.A.2"));
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        this.requestPropertiesManagerBroker.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected2, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager1.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected2, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager2.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(Collections.EMPTY_MAP, multivaluedMapPopulator.getMap());
    }

    public void testSetProperties() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.replay(portletWindow);
        
        final MockRequestPropertiesManager delegateManager1 = new MockRequestPropertiesManager();
        delegateManager1.setOrder(Ordered.HIGHEST_PRECEDENCE);
        final MockRequestPropertiesManager delegateManager2 = new MockRequestPropertiesManager();
        delegateManager2.setOrder(Ordered.LOWEST_PRECEDENCE);
        
        this.requestPropertiesManagerBroker.setPropertiesManagers(ImmutableList.of(delegateManager1, delegateManager2));
        
        
        
        this.requestPropertiesManagerBroker.setResponseProperty(request, portletWindow, "prop.A", "prop.A.1");
        final Map<String, List<String>> expected1 = Collections.singletonMap("prop.A", Collections.singletonList("prop.A.1"));
        
        MultivaluedMapPopulator<String, String> multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        this.requestPropertiesManagerBroker.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected1, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager1.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected1, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager2.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(Collections.EMPTY_MAP, multivaluedMapPopulator.getMap());
        
        
        
        this.requestPropertiesManagerBroker.setResponseProperty(request, portletWindow, "prop.A", "prop.A.2");
        final Map<String, List<String>> expected2 = Collections.<String, List<String>>singletonMap("prop.A", ImmutableList.of("prop.A.2"));
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        this.requestPropertiesManagerBroker.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected2, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager1.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(expected2, multivaluedMapPopulator.getMap());
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        delegateManager2.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        assertEquals(Collections.EMPTY_MAP, multivaluedMapPopulator.getMap());
    }

    public void testGetProperties() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.replay(portletWindow);
        
        final MockRequestPropertiesManager delegateManager1 = new MockRequestPropertiesManager();
        delegateManager1.setOrder(Ordered.HIGHEST_PRECEDENCE);
        final MockRequestPropertiesManager delegateManager2 = new MockRequestPropertiesManager();
        delegateManager2.setOrder(Ordered.LOWEST_PRECEDENCE);
        
        this.requestPropertiesManagerBroker.setPropertiesManagers(ImmutableList.of(delegateManager1, delegateManager2));
        
        
        
        delegateManager1.setResponseProperty(request, portletWindow, "prop.A", "prop.A.1");
        delegateManager1.setResponseProperty(request, portletWindow, "prop.B", "prop.B.1");
        
        delegateManager2.setResponseProperty(request, portletWindow, "prop.C", "prop.C.1");
        
        MultivaluedMapPopulator<String, String> multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        this.requestPropertiesManagerBroker.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        
        final Map<String, List<String>> expected1 = ImmutableMap.<String, List<String>>of(
                "prop.A", ImmutableList.of("prop.A.1"),
                "prop.B", ImmutableList.of("prop.B.1"),
                "prop.C", ImmutableList.of("prop.C.1"));
        
        assertEquals(expected1, multivaluedMapPopulator.getMap());
        
        
        
        delegateManager2.setResponseProperty(request, portletWindow, "prop.A", "prop.A.2");
        
        multivaluedMapPopulator = new MultivaluedMapPopulator<String, String>();
        this.requestPropertiesManagerBroker.populateRequestProperties(request, portletWindow, multivaluedMapPopulator);
        
        final Map<String, List<String>> expected2 = ImmutableMap.<String, List<String>>of(
                "prop.A", ImmutableList.of("prop.A.1", "prop.A.2"),
                "prop.B", ImmutableList.of("prop.B.1"),
                "prop.C", ImmutableList.of("prop.C.1"));
        
        assertEquals(expected2, multivaluedMapPopulator.getMap());
    }
}
