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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntityDescriptor;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PortletEntityCacheTest {

    private PortletEntityCache portletEntityCache;

    @Mock private IPortletEntityId portletEntityId;

    @Mock private IPortletDefinitionId portletDefinitionId;

    @Mock private IPortletEntityDescriptor portletEntityDescriptor;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        portletEntityCache = new PortletEntityCache();
        when(portletEntityId.getStringId()).thenReturn("110_u18l1n1203_60");
        when(portletDefinitionId.getLongId()).thenReturn(1010L);
        when(portletEntityDescriptor.getLayoutNodeId()).thenReturn("u18l1n1203");
        when(portletEntityDescriptor.getPortletDefinitionId()).thenReturn(portletDefinitionId);
        when(portletEntityDescriptor.getPortletEntityId()).thenReturn(portletEntityId);
        when(portletEntityDescriptor.getUserId()).thenReturn(60);
    }

    @Test
    public void storeIfAbsentEntityTest() {
        portletEntityCache.storeEntity(portletEntityDescriptor);
        IPortletEntityDescriptor _result = portletEntityCache.getEntity("u18l1n1203", 60);
        assertNotNull(_result);
        assertEquals(_result.getPortletDefinitionId(), portletDefinitionId);
        assertEquals(_result.getPortletEntityId(), portletEntityId);
        portletEntityCache.removeEntity(portletEntityId);
    }

    @Test
    public void removeEntityTest() {
        portletEntityCache.storeEntity(portletEntityDescriptor);
        IPortletEntityDescriptor _result = portletEntityCache.getEntity("u18l1n1203", 60);
        assertNotNull(_result);
        portletEntityCache.removeEntity(portletEntityId);
        IPortletEntityDescriptor _result1 = portletEntityCache.getEntity("u18l1n1203", 60);
        assertNull(_result1);
    }
}
