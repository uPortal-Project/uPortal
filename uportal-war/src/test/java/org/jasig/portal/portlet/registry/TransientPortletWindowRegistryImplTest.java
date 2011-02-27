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

package org.jasig.portal.portlet.registry;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.web.PortalWebUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TransientPortletWindowRegistryImplTest extends TestCase {
    public void testGetTransientWindow() throws Exception {
        final PortletWindowRegistryImpl transientPortletWindowRegistry = new PortletWindowRegistryImpl();
        
        final HttpServletRequest request = createMock(HttpServletRequest.class);
        final HttpSession session = createMock(HttpSession.class);
        final IPortletWindowId sourcePortletWindowId = createMock(IPortletWindowId.class);
        final IPortletWindow sourcePortletWindow = createMock(IPortletWindow.class);
        final IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        final IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        final IPortletDefinitionRegistry portletDefinitionRegistry = createMock(IPortletDefinitionRegistry.class);
        final IPortletDefinition portletDefinition = createMock(IPortletDefinition.class);
        final IPortletDefinitionId portletDefinitionId = createMock(IPortletDefinitionId.class);
        final IPortletEntity portletEntity = createMock(IPortletEntity.class);
        final PortletDefinition portletDescriptor = createMock(PortletDefinition.class);
        final IPortalRequestUtils portalRequestUtils = createMock(IPortalRequestUtils.class);
        
        final ConcurrentHashMap<Object, Object> transientPortletWindowMap = new ConcurrentHashMap<Object, Object>();
        
        final ConcurrentHashMap<Object, Object> portletWindowMap = new ConcurrentHashMap<Object, Object>();
        portletWindowMap.put(sourcePortletWindowId, sourcePortletWindow);
        
        expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request).anyTimes();
        expect(sourcePortletWindowId.getStringId()).andReturn("pwid1");
        expect(request.getAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE)).andReturn(request);
        expect(request.getAttribute(PortletWindowRegistryImpl.PORTLET_WINDOW_MAP_ATTRIBUTE)).andReturn(portletWindowMap);
        expect(sourcePortletWindow.getPortletEntityId()).andReturn(portletEntityId);
        expect(request.getAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE)).andReturn(request).times(2);
        expect(request.getAttribute(PortletWindowRegistryImpl.TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE)).andReturn(transientPortletWindowMap).times(2);
        expect(portletEntityId.getStringId()).andReturn("peid1").times(2);
        expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDescriptor);
        expect(portletEntityRegistry.getPortletEntity("peid1")).andReturn(portletEntity);                
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
                
        
        replay(request, session, sourcePortletWindowId, sourcePortletWindow, portletEntityId, 
                portletEntityRegistry, portletDefinitionRegistry, portletDefinition, portletDefinitionId, 
                portletDescriptor, portletEntity, portalRequestUtils);
        
        transientPortletWindowRegistry.setPortalRequestUtils(portalRequestUtils);
        transientPortletWindowRegistry.setPortletEntityRegistry(portletEntityRegistry);
        transientPortletWindowRegistry.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        final IPortletWindowId portletWindowId = transientPortletWindowRegistry.createTransientPortletWindowId(request, sourcePortletWindowId);
        
        final IPortletWindow portletWindow1 = transientPortletWindowRegistry.getPortletWindow(request, portletWindowId);
        
        final IPortletWindow portletWindow2 = transientPortletWindowRegistry.getOrCreatePortletWindow(request, portletWindowId.toString(), portletEntityId);
        
        assertTrue(portletWindow1 == portletWindow2);
        assertEquals(1, transientPortletWindowMap.size());
        assertEquals(PortletWindowRegistryImpl.TRANSIENT_WINDOW_ID_PREFIX + "peid1", portletWindowId.getStringId());
        assertEquals(PortletWindowRegistryImpl.TRANSIENT_WINDOW_ID_PREFIX + "peid1", portletWindow2.getPortletWindowId().getStringId());
        assertEquals("peid1", portletWindow2.getPortletEntityId().getStringId());

        verify(request, session, sourcePortletWindowId, sourcePortletWindow, portletEntityId, 
                portletEntityRegistry, portletDefinitionRegistry, portletDefinition, portletDefinitionId,
                portletDescriptor, portletEntity, portalRequestUtils);
    }
}
