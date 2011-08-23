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

package org.jasig.portal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleTabUrlNodeSyntaxHelperTest {
    @InjectMocks private SingleTabUrlNodeSyntaxHelper urlNodeSyntaxHelper = new SingleTabUrlNodeSyntaxHelper();
    @Mock IUserInstanceManager userInstanceManager;
    @Mock IUserInstance userInstance;
    @Mock IPortletEntityRegistry portletEntityRegistry;
    @Mock IPortletWindowRegistry portletWindowRegistry;
    @Mock IPortletDefinition portletDefinition;
    @Mock IPortletEntity portletEntity;
    @Mock IPortletWindow portletWindow;
    
    @Test
    public void getPortletForFolderNameFanmeIdTest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String folder = "fname.id";
        
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("eid");
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("wid");
        
        when(this.userInstanceManager.getUserInstance(request)).thenReturn(this.userInstance);
        when(this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, "id")).thenReturn(portletEntity);
        when(this.portletEntity.getPortletDefinition()).thenReturn(portletDefinition);
        when(this.portletDefinition.getFName()).thenReturn("fname");
        when(this.portletEntity.getPortletEntityId()).thenReturn(portletEntityId);
        when(this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntityId)).thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        
        final IPortletWindowId parsedPortletWindowId = this.urlNodeSyntaxHelper.getPortletForFolderName(request, null, folder);
        assertNotNull(parsedPortletWindowId);
        assertEquals(portletWindowId, parsedPortletWindowId);
    }
    
    @Test
    public void getPortletForFolderNameFnameTest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String folder = "fname";
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("wid");
        
        when(this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(request, folder)).thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        
        final IPortletWindowId parsedPortletWindowId = this.urlNodeSyntaxHelper.getPortletForFolderName(request, null, folder);
        assertNotNull(parsedPortletWindowId);
        assertEquals(portletWindowId, parsedPortletWindowId);
    }
}
