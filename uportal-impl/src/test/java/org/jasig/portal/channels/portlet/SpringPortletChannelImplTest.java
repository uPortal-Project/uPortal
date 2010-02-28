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

package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.impl.SecurityRoleRefType;
import org.easymock.EasyMock;
import org.easymock.internal.matchers.InstanceOf;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlet.rendering.PortletRenderResult;
import org.jasig.portal.portlet.url.IPortletRequestParameterManager;
import org.jasig.portal.portlet.url.PortletUrl;
import org.jasig.portal.portlet.url.RequestType;
import org.jasig.portal.security.IPerson;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SpringPortletChannelImplTest extends TestCase {
    private SpringPortletChannelImpl springPortletChannel;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.springPortletChannel = new SpringPortletChannelImpl();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.springPortletChannel = null;
    }

    public void testInitSession() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        EasyMock.expect(person.getID()).andReturn(1);
        channelStaticData.setPerson(person);

        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        
        
        final IPortletDefinitionId portDef1 = new MockPortletDefinitionId("portDef1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portDef1);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getPortletDefinition(1)).andReturn(portletDefinition);
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getOrCreatePortletEntity(portDef1, "sub1", 1)).andReturn(portletEntity);
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        
        final IPortletRenderer portletRenderer = EasyMock.createMock(IPortletRenderer.class);
        EasyMock.expect(portletRenderer.doInit(portletEntity, null, request, response))
            .andReturn(portletWindowId);
        
        
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRenderer(portletRenderer);
        
        EasyMock.replay(portletRenderer, portletDefinition, portletDefinitionRegistry, portletWindowRegistry, portletEntity, portletEntityRegistry, person);
        
        this.springPortletChannel.initSession(channelStaticData, portalControlStructures);
        
        EasyMock.verify(portletRenderer, portletDefinition, portletDefinitionRegistry, portletWindowRegistry, portletEntity, portletEntityRegistry, person);
    }
    
    public static <T> T instanceOfMatcher(final T arg) {
        final Class<?> argClass;
        if (arg instanceof Class<?>) {
            argClass = (Class<?>)arg;
        }
        else {
            argClass = arg.getClass();
        }
        
        EasyMock.reportMatcher(new InstanceOf(argClass));
        
        return null;
    }

    public void testAction() throws Exception {
        final IPerson person = EasyMock.createMock(IPerson.class);

        
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        channelStaticData.setPerson(person);

        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getSecurityRoleRefs()).andReturn(Collections.EMPTY_LIST);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);

        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);
        
        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId);
        portletUrl.setRequestType(RequestType.RENDER);
        portletUrl.setParameters(Collections.EMPTY_MAP);
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        
        final IPortletRenderer portletRenderer = EasyMock.createMock(IPortletRenderer.class);
        portletRenderer.doAction(portletWindowId, pcsRequest, response);
        EasyMock.expectLastCall();
        
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRenderer(portletRenderer);
        
        EasyMock.replay(portletRenderer, portletDefinitionRegistry, portletEntityRegistry, portletRequestParameterManager, portletWindowRegistry, person);
        
        this.springPortletChannel.action(channelStaticData, portalControlStructures, channelRuntimeData);
        
        EasyMock.verify(portletRenderer, portletDefinitionRegistry, portletEntityRegistry, portletRequestParameterManager, portletWindowRegistry, person);
    }

    public void testGenerateCacheKey() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        

        //Test targeted portlet
        EasyMock.replay(portletWindow, person);
        
        
        final ChannelCacheKey key = this.springPortletChannel.generateKey(channelStaticData, portalControlStructures, channelRuntimeData);
        
        //Test targeted portlet
        EasyMock.verify(portletWindow, person);
        
        assertEquals(ChannelCacheKey.INSTANCE_KEY_SCOPE, key.getKeyScope());
        assertEquals("{org.jasig.portal.portlet.om.IPortletWindowId=win1, remoteUser=null}", key.getKey());
        assertTrue("Expected validity object to be of type Long", key.getKeyValidity() instanceof Long);
    }

    public void testIsCacheValidTargetedPorlet() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(portletWindowId);
        
        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        
        //Test targeted portlet
        EasyMock.replay(portletRequestParameterManager, portletWindow, portletWindowRegistry, person);
        
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, null);
        assertFalse(valid);
        
        EasyMock.verify(portletRequestParameterManager, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidNotConfigured() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(Integer.MIN_VALUE);
        EasyMock.replay(portletDD);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, null);
        assertFalse(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidNeverCache() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.expect(portletWindow.getExpirationCache()).andReturn(null);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(0);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, null);
        assertFalse(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidAlwaysCache() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.expect(portletWindow.getExpirationCache()).andReturn(null);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(-1);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, null);
        assertTrue(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidCacheWithNullValidity() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.expect(portletWindow.getExpirationCache()).andReturn(null);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(10);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, null);
        assertFalse(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidCacheStillGood() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.expect(portletWindow.getExpirationCache()).andReturn(null);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(10);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        //expirationCache is in secods, need to have a validity object that is less than 10 seconds ago
        final Long validity = System.currentTimeMillis() - 5000;
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, validity);
        assertTrue(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidCacheExpired() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.expect(portletWindow.getExpirationCache()).andReturn(null);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(10);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        //expirationCache is in seconds, need to have a validity object that is more than 10 seconds ago
        final Long validity = System.currentTimeMillis() - 10001;
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, validity);
        assertFalse(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }


    public void testIsCacheValidWindowOverride() throws Exception {
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        
        final IPerson person = EasyMock.createMock(IPerson.class);
        channelStaticData.setPerson(person);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("def1");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("ent1");
        
        
        final IPortletEntity portletEntity = EasyMock.createMock(IPortletEntity.class);
        EasyMock.expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);

        
        final IPortletWindow portletWindow = EasyMock.createMock(IPortletWindow.class);
        EasyMock.expect(portletWindow.getExpirationCache()).andReturn(30);
        

        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(pcsRequest, portletWindowId)).andReturn(portletWindow);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(pcsRequest, portletWindowId)).andReturn(portletEntity);
        
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        EasyMock.expect(portletRequestParameterManager.getTargetedPortletWindowId(pcsRequest)).andReturn(null);
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(10);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);

        
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        EasyMock.replay(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
        
        //expirationCache is in secods, need to have a validity object that is more than 10 seconds ago
        final Long validity = System.currentTimeMillis() - 10001;
        final boolean valid = this.springPortletChannel.isCacheValid(channelStaticData, portalControlStructures, channelRuntimeData, validity);
        assertTrue(valid);
        
        EasyMock.verify(portletDefinitionRegistry, portletDefinition, portletRequestParameterManager, portletEntity, portletEntityRegistry, portletWindow, portletWindowRegistry, person);
    }
    
    public void testRender() throws Exception {
        final IPerson person = EasyMock.createMock(IPerson.class);
        
        final ChannelStaticData channelStaticData = new ChannelStaticData();
        channelStaticData.setChannelPublishId("pub1");
        channelStaticData.setChannelSubscribeId("sub1");
        channelStaticData.setPerson(person);

        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PortalControlStructures portalControlStructures = new PortalControlStructures(request, response);
        final HttpServletRequest pcsRequest = portalControlStructures.getHttpServletRequest();
        
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getSecurityRoleRefs()).andReturn(Collections.EMPTY_LIST);
        EasyMock.replay(portletDD);
        
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        
        final IPortletWindowId portletWindowId = new MockPortletWindowId("win1");
        this.springPortletChannel.setPortletWidnowId(channelStaticData, portletWindowId);

        
        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        
        final PrintWriter printWriter = new PrintWriter(new NullOutputStream());
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId);
        portletUrl.setRequestType(RequestType.RENDER);
        
        final IPortletRequestParameterManager portletRequestParameterManager = EasyMock.createMock(IPortletRequestParameterManager.class);
        
        final IPortletRenderer portletRenderer = EasyMock.createMock(IPortletRenderer.class);
        EasyMock.expect(portletRenderer.doRender(portletWindowId, pcsRequest, response, printWriter))
            .andReturn(new PortletRenderResult("theTitle"));
        
        this.springPortletChannel.setPortletRequestParameterManager(portletRequestParameterManager);
        this.springPortletChannel.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.springPortletChannel.setPortletEntityRegistry(portletEntityRegistry);
        this.springPortletChannel.setPortletWindowRegistry(portletWindowRegistry);
        this.springPortletChannel.setPortletRenderer(portletRenderer);
        
        EasyMock.replay(portletRenderer, portletDefinitionRegistry, portletEntityRegistry, portletRequestParameterManager, portletWindowRegistry, person);
        
        this.springPortletChannel.render(channelStaticData, portalControlStructures, channelRuntimeData, printWriter);
        
        final String title = this.springPortletChannel.getTitle(channelStaticData, portalControlStructures, channelRuntimeData);
        assertEquals("theTitle", title);
        
        EasyMock.verify(portletRenderer, portletDefinitionRegistry, portletEntityRegistry, portletRequestParameterManager, portletWindowRegistry, person);
    }
}
