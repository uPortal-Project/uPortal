/*
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
package org.apereo.portal.portlet.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletWindowDescriptor;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PortletWindowCacheTest {

    private PortletWindowCache portletWindowCache;

    @Mock private IPortletEntityId portletEntityId;

    @Mock private IPortletWindowId portletWindowId;

    @Mock private IPortletWindowDescriptor portletWindowDescriptor;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        portletWindowCache = new PortletWindowCache();
        when(portletEntityId.getStringId()).thenReturn("110_u18l1n1203_60");
        when(portletWindowId.getStringId()).thenReturn("210_u18l1n1203_80");
        when(portletWindowDescriptor.getPortletEntityId()).thenReturn(portletEntityId);
        when(portletWindowDescriptor.getPortletWindowId()).thenReturn(portletWindowId);
    }

    @Test
    public void storeIfAbsentWindowTest() {
        portletWindowCache.storeIfAbsentWindow(portletWindowDescriptor);
        Set windows = portletWindowCache.getWindows(portletEntityId);
        assertNotNull(windows);
        assertEquals(windows.size(), 1);
        IPortletWindowDescriptor _descriptor_ =
                (IPortletWindowDescriptor) windows.iterator().next();
        assertEquals(_descriptor_.getPortletEntityId(), portletEntityId);
        assertEquals(_descriptor_.getPortletWindowId(), portletWindowId);
        IPortletWindowDescriptor _portletWindowDescriptor_ =
                portletWindowCache.getWindow(portletWindowId);
        assertEquals(_portletWindowDescriptor_.getPortletEntityId(), portletEntityId);
    }
}
