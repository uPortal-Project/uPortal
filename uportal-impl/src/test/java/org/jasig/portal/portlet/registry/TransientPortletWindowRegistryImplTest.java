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

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.easymock.EasyMock;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.web.util.WebUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TransientPortletWindowRegistryImplTest extends TestCase {
    public void testGetTransientWindow() throws Exception {
        final TransientPortletWindowRegistryImpl transientPortletWindowRegistry = new TransientPortletWindowRegistryImpl();
        
        final HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        final HttpSession session = EasyMock.createMock(HttpSession.class);
        final IPortletWindowId sourcePortletWindowId = EasyMock.createMock(IPortletWindowId.class);
        final IPortletWindow sourcePortletWindow = EasyMock.createMock(IPortletWindow.class);
        final IPortletEntityId portletEntityId = EasyMock.createMock(IPortletEntityId.class);
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        final IPortletDefinitionId portletDefinitionId = EasyMock.createMock(IPortletDefinitionId.class);
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        final PortletDefinition portletDescriptor = EasyMock.createMock(PortletDefinition.class);
        
        final ConcurrentHashMap<Object, Object> transientPortletWindowMap = new ConcurrentHashMap<Object, Object>();
        
        final ConcurrentHashMap<Object, Object> portletWindowMap = new ConcurrentHashMap<Object, Object>();
        portletWindowMap.put(sourcePortletWindowId, sourcePortletWindow);
        
        EasyMock.expect(sourcePortletWindowId.getStringId()).andReturn("pwid1");
        EasyMock.expect(request.getSession()).andReturn(session);
        EasyMock.expect(session.getAttribute(WebUtils.SESSION_MUTEX_ATTRIBUTE)).andReturn(session);
        EasyMock.expect(session.getAttribute(PortletWindowRegistryImpl.PORTLET_WINDOW_MAP_ATTRIBUTE)).andReturn(portletWindowMap);
        EasyMock.expect(sourcePortletWindow.getPortletEntityId()).andReturn(portletEntityId);
        EasyMock.expect(request.getAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE)).andReturn(request).times(2);
        EasyMock.expect(request.getAttribute(TransientPortletWindowRegistryImpl.TRANSIENT_PORTLET_WINDOW_MAP_ATTRIBUTE)).andReturn(transientPortletWindowMap).times(2);
        EasyMock.expect(portletEntityId.getStringId()).andReturn("peid1").times(2);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDescriptor);
        EasyMock.expect(portletEntityRegistry.getPortletEntity("peid1")).andReturn(portletEntity);                
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
                
        
        EasyMock.replay(request, session, sourcePortletWindowId, sourcePortletWindow, portletEntityId, 
                portletEntityRegistry, portletDefinitionRegistry, portletDefinition, portletDefinitionId, portletDescriptor, portletEntity);
        
        
        transientPortletWindowRegistry.setPortletEntityRegistry(portletEntityRegistry);
        transientPortletWindowRegistry.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        final IPortletWindowId portletWindowId = transientPortletWindowRegistry.createTransientPortletWindowId(request, sourcePortletWindowId);
        
        final IPortletWindow portletWindow1 = transientPortletWindowRegistry.getPortletWindow(request, portletWindowId);
        
        final IPortletWindow portletWindow2 = transientPortletWindowRegistry.getOrCreatePortletWindow(request, portletWindowId.toString(), portletEntityId);
        
        assertTrue(portletWindow1 == portletWindow2);
        assertEquals(1, transientPortletWindowMap.size());
        assertEquals(TransientPortletWindowRegistryImpl.TRANSIENT_WINDOW_ID_PREFIX + "peid1", portletWindowId.getStringId());
        assertEquals(TransientPortletWindowRegistryImpl.TRANSIENT_WINDOW_ID_PREFIX + "peid1", portletWindow2.getPortletWindowId().getStringId());
        //assertEquals("pdk1.k", portletWindow2.getContextPath());
        //assertEquals("pdk1.v", portletWindow2.getPortletName());
        assertEquals("peid1", portletWindow2.getPortletEntityId().getStringId());

        EasyMock.verify(request, session, sourcePortletWindowId, sourcePortletWindow, portletEntityId, 
                portletEntityRegistry, portletDefinitionRegistry, portletDefinition, portletDefinitionId, portletDescriptor, portletEntity);
    }
}
