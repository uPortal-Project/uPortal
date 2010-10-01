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

package org.jasig.portal.portlet.url;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.channels.portlet.PortletHttpServletRequestWrapper;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntity;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindow;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.delegation.IPortletDelegationManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlSyntaxProviderImplTest extends TestCase {
    
    public void testEncodeAndAppend() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        StringBuilder url = new StringBuilder();
        portletUrlSyntaxProvider.encodeAndAppend(url, "UTF-8", "name");
        assertEquals("name=", url.toString());
        
        url = new StringBuilder();
        portletUrlSyntaxProvider.encodeAndAppend(url, "UTF-8", "name", "value1");
        assertEquals("name=value1", url.toString());
        
        url = new StringBuilder();
        portletUrlSyntaxProvider.encodeAndAppend(url, "UTF-8", "name", "value1", "value2");
        assertEquals("name=value1&name=value2", url.toString());
        
        portletUrlSyntaxProvider.encodeAndAppend(url.append("&"), "UTF-8", "name2", "value21", "value22");
        assertEquals("name=value1&name=value2&name2=value21&name2=value22", url.toString());
    }
    
    public void testGeneratePortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request).times(4);
        
        replay(portalRequestUtils);
        portletUrlSyntaxProvider.setPortalRequestUtils(portalRequestUtils);
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("entityId1");
        final IPortletWindowId portletWindowId = new MockPortletWindowId("windowId1");
        final IPortletWindow portletWindow = new MockPortletWindow(portletWindowId, portletEntityId, "portletApp", "portletName");
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        channelRuntimeData.setBaseActionURL("base/action.url");
        
        request.setAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA, channelRuntimeData);
        
        MockPortletEntity portletEntity = new MockPortletEntity();
        portletEntity.setPortletEntityId(portletEntityId);
        portletEntity.setChannelSubscribeId(portletEntityId.getStringId());
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId))
            .andReturn(portletEntity)
            .anyTimes();
        
        final MockPortletWindowId transientPortletWindowId = new MockPortletWindowId("tp.windowId1");
        expect(portletWindowRegistry.createTransientPortletWindowId(request, portletWindowId))
            .andReturn(transientPortletWindowId)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId))
            .andReturn(portletWindowId)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId))
            .andReturn(portletWindow)
            .anyTimes();
        
        replay(portletWindowRegistry);

        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId);
        
    	String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
       	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type_windowId1=RENDER#entityId1", urlString);
 
        final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>(); 
        parameters.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters.put("key3", Arrays.asList(new String[] { "" }) );
        
        portletUrl.setPortletMode(PortletMode.EDIT);
        portletUrl.setWindowState(WindowState.MINIMIZED);
        portletUrl.setParameters(parameters);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
    	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type_windowId1=RENDER&pltc_state_windowId1=minimized&uP_root=root&uP_tcattr=minimized&minimized_channelId=entityId1&minimized_entityId1_value=true&uP_save=all&pltc_mode_windowId1=edit&pltp_windowId1_key1=value1.1&pltp_windowId1_key1=value1.2&pltp_windowId1_key2=value2.1&pltp_windowId1_key3=#entityId1", urlString);

        portletUrl.setRequestType(RequestType.ACTION);
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
    	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type_windowId1=ACTION&pltc_state_windowId1=maximized&uP_root=entityId1&pltc_mode_windowId1=edit&pltp_windowId1_key1=value1.1&pltp_windowId1_key1=value1.2&pltp_windowId1_key2=value2.1&pltp_windowId1_key3=", urlString);
        portletUrl.setWindowState(new WindowState("EXCLUSIVE"));
        portletUrl.setRequestType(RequestType.RENDER);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
    	assertEquals("/uPortal/worker/download/worker.download.uP?pltc_target=tp.windowId1&pltc_type_tp.windowId1=RENDER&pltc_state_tp.windowId1=exclusive&pltc_mode_tp.windowId1=edit&pltp_tp.windowId1_key1=value1.1&pltp_tp.windowId1_key1=value1.2&pltp_tp.windowId1_key2=value2.1&pltp_tp.windowId1_key3=", urlString);
        verify(portalRequestUtils);
    }
    

    
    public void testGenerateSingleDelegateDefaultPortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request);
        
        portletUrlSyntaxProvider.setPortalRequestUtils(portalRequestUtils);
        
        final IPortletEntityId portletEntityId1 = new MockPortletEntityId("eId1");
        final IPortletWindowId portletWindowId1 = new MockPortletWindowId("wId1");
        final IPortletWindow portletWindow1 = new MockPortletWindow(portletWindowId1, portletEntityId1, "portletAppA", "portletNameA");
       
        final IPortletEntityId portletEntityId2 = new MockPortletEntityId("eId2");
        final IPortletWindowId portletWindowId2 = new MockPortletWindowId("wId2");
        final MockPortletWindow portletWindow2 = new MockPortletWindow(portletWindowId2, portletEntityId2, "portletAppB", "portletNameB");

        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        channelRuntimeData.setBaseActionURL("base/action.url");
        
        request.setAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA, channelRuntimeData);
        
        final MockPortletEntity portletEntity1 = new MockPortletEntity();
        portletEntity1.setPortletEntityId(portletEntityId1);
        portletEntity1.setChannelSubscribeId(portletEntityId1.getStringId());
        
        final MockPortletEntity portletEntity2 = new MockPortletEntity();
        portletEntity2.setPortletEntityId(portletEntityId2);
        portletEntity2.setChannelSubscribeId(portletEntityId2.getStringId());
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId1))
            .andReturn(portletEntity1)
            .anyTimes();
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId2))
            .andReturn(portletEntity2)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId2))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId1))
            .andReturn(false)
            .anyTimes();
        
        expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId1))
            .andReturn(portletWindowId1)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId1))
            .andReturn(portletWindow1)
            .anyTimes();

        final IPortletDelegationManager portletDelegationManager = createMock(IPortletDelegationManager.class);
        
        
        expect(portletDelegationManager.getParentPortletUrl(request, portletWindowId1))
            .andReturn(null);
        
        
        replay(portletDelegationManager, portalRequestUtils, portletWindowRegistry);

        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        portletUrlSyntaxProvider.setPortletDelegationManager(portletDelegationManager);
        

        //Setup portlet window 1
        portletWindow1.setWindowState(WindowState.MAXIMIZED);
        portletWindow1.setPortletMode(PortletMode.VIEW);
        final Map<String, List<String>> parameters1 = new LinkedHashMap<String, List<String>>();
        parameters1.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters1.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters1.put("key3", Arrays.asList(new String[] { "" }) );
        portletWindow1.setRequestParameters(parameters1);
        
        //Setup delegation (wId1 delegates to wId2)
        portletWindow2.setDelegationParent(portletWindowId1);
        
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId1);

        final Map<String, List<String>> parameters2 = new LinkedHashMap<String, List<String>>();
        parameters2.put("keyA", Arrays.asList(new String[] { "valueA.A", "valueA.B" }) );
        parameters2.put("keyB", Arrays.asList(new String[] { "valueB.A" }) );
        portletUrl.setParameters(parameters2);
        
        portletUrl.setPortletMode(IPortletAdaptor.CONFIG);
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        
        String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow2, portletUrl);
        
        verify(portletDelegationManager, portalRequestUtils, portletWindowRegistry);
        
        assertEquals(
                "/uPortal/base/action.url" +
        		"?pltc_target=wId1" +
        		"&pltc_delegate_wId1=wId2" +
        		"&pltc_type_wId1=RENDER" +
        		"&pltc_mode_wId1=view" +
        		"&pltp_wId1_key1=value1.1" +
        		"&pltp_wId1_key1=value1.2" +
        		"&pltp_wId1_key2=value2.1" +
        		"&pltp_wId1_key3=" +
                "&pltc_type_wId2=RENDER" +
        		"&pltc_state_wId2=maximized" +
        		"&pltc_mode_wId2=config" +
        		"&pltp_wId2_keyA=valueA.A" +
                "&pltp_wId2_keyA=valueA.B" +
                "&pltp_wId2_keyB=valueB.A", 
        		urlString);
    }
    
    public void testGenerateSingleDelegatePortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request);
        
        portletUrlSyntaxProvider.setPortalRequestUtils(portalRequestUtils);
        
        final IPortletEntityId portletEntityId1 = new MockPortletEntityId("eId1");
        final IPortletWindowId portletWindowId1 = new MockPortletWindowId("wId1");
        final IPortletWindow portletWindow1 = new MockPortletWindow(portletWindowId1, portletEntityId1, "portletAppA", "portletNameA");
       
        final IPortletEntityId portletEntityId2 = new MockPortletEntityId("eId2");
        final IPortletWindowId portletWindowId2 = new MockPortletWindowId("wId2");
        final MockPortletWindow portletWindow2 = new MockPortletWindow(portletWindowId2, portletEntityId2, "portletAppB", "portletNameB");

        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        channelRuntimeData.setBaseActionURL("base/action.url");
        
        request.setAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA, channelRuntimeData);
        
        final MockPortletEntity portletEntity1 = new MockPortletEntity();
        portletEntity1.setPortletEntityId(portletEntityId1);
        portletEntity1.setChannelSubscribeId(portletEntityId1.getStringId());
        
        final MockPortletEntity portletEntity2 = new MockPortletEntity();
        portletEntity2.setPortletEntityId(portletEntityId2);
        portletEntity2.setChannelSubscribeId(portletEntityId2.getStringId());
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId1))
            .andReturn(portletEntity1)
            .anyTimes();
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId2))
            .andReturn(portletEntity2)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId2))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId1))
            .andReturn(false)
            .anyTimes();
        
        expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId1))
            .andReturn(portletWindowId1)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId1))
            .andReturn(portletWindow1)
            .anyTimes();

        final IPortletDelegationManager portletDelegationManager = createMock(IPortletDelegationManager.class);
        
        
        final PortletUrl portletUrl1 = new PortletUrl(portletWindowId1);
        final Map<String, List<String>> urlParameters1 = new LinkedHashMap<String, List<String>>();
        urlParameters1.put("newKey1", Arrays.asList(new String[] { "newValue1.1", "newValue1.2" }) );
        urlParameters1.put("newKey2", Arrays.asList(new String[] { "newValue2.1" }) );
        urlParameters1.put("newKey3", Arrays.asList(new String[] { "" }) );
        portletUrl1.setParameters(urlParameters1);
        
        expect(portletDelegationManager.getParentPortletUrl(request, portletWindowId1))
            .andReturn(portletUrl1);
        
        
        replay(portletDelegationManager, portalRequestUtils, portletWindowRegistry);

        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        portletUrlSyntaxProvider.setPortletDelegationManager(portletDelegationManager);
        

        //Setup portlet window 1
        portletWindow1.setWindowState(WindowState.MAXIMIZED);
        portletWindow1.setPortletMode(PortletMode.VIEW);
        final Map<String, List<String>> parameters1 = new LinkedHashMap<String, List<String>>();
        parameters1.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters1.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters1.put("key3", Arrays.asList(new String[] { "" }) );
        portletWindow1.setRequestParameters(parameters1);
        
        //Setup delegation (wId1 delegates to wId2)
        portletWindow2.setDelegationParent(portletWindowId1);
        
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId1);

        final Map<String, List<String>> parameters2 = new LinkedHashMap<String, List<String>>();
        parameters2.put("keyA", Arrays.asList(new String[] { "valueA.A", "valueA.B" }) );
        parameters2.put("keyB", Arrays.asList(new String[] { "valueB.A" }) );
        portletUrl.setParameters(parameters2);
        
        portletUrl.setPortletMode(IPortletAdaptor.CONFIG);
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        
        String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow2, portletUrl);
        
        verify(portletDelegationManager, portalRequestUtils, portletWindowRegistry);
        
        assertEquals(
                "/uPortal/base/action.url" +
                "?pltc_target=wId1" +
                "&pltc_delegate_wId1=wId2" +
                "&pltc_type_wId1=RENDER" +
                "&pltp_wId1_newKey1=newValue1.1" +
                "&pltp_wId1_newKey1=newValue1.2" +
                "&pltp_wId1_newKey2=newValue2.1" +
                "&pltp_wId1_newKey3=" +
                "&pltc_type_wId2=RENDER" +
                "&pltc_state_wId2=maximized" +
                "&pltc_mode_wId2=config" +
                "&pltp_wId2_keyA=valueA.A" +
                "&pltp_wId2_keyA=valueA.B" +
                "&pltp_wId2_keyB=valueB.A", 
                urlString);
    }
    
    public void testGenerateMultipleDelegateDefaultPortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request);
        
        portletUrlSyntaxProvider.setPortalRequestUtils(portalRequestUtils);
        
        final IPortletEntityId portletEntityId1 = new MockPortletEntityId("eId1");
        final IPortletWindowId portletWindowId1 = new MockPortletWindowId("wId1");
        final IPortletWindow portletWindow1 = new MockPortletWindow(portletWindowId1, portletEntityId1, "portletAppA", "portletNameA");
       
        final IPortletEntityId portletEntityId2 = new MockPortletEntityId("eId2");
        final IPortletWindowId portletWindowId2 = new MockPortletWindowId("wId2");
        final MockPortletWindow portletWindow2 = new MockPortletWindow(portletWindowId2, portletEntityId2, "portletAppB", "portletNameB");
       
        final IPortletEntityId portletEntityId3 = new MockPortletEntityId("eId3");
        final IPortletWindowId portletWindowId3 = new MockPortletWindowId("wId3");
        final MockPortletWindow portletWindow3 = new MockPortletWindow(portletWindowId3, portletEntityId3, "portletAppC", "portletNameC");

        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        channelRuntimeData.setBaseActionURL("base/action.url");
        
        request.setAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA, channelRuntimeData);
        
        final MockPortletEntity portletEntity1 = new MockPortletEntity();
        portletEntity1.setPortletEntityId(portletEntityId1);
        portletEntity1.setChannelSubscribeId(portletEntityId1.getStringId());
        
        final MockPortletEntity portletEntity2 = new MockPortletEntity();
        portletEntity2.setPortletEntityId(portletEntityId2);
        portletEntity2.setChannelSubscribeId(portletEntityId2.getStringId());
        
        final MockPortletEntity portletEntity3 = new MockPortletEntity();
        portletEntity3.setPortletEntityId(portletEntityId3);
        portletEntity3.setChannelSubscribeId(portletEntityId3.getStringId());
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId1))
            .andReturn(portletEntity1)
            .anyTimes();
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId2))
            .andReturn(portletEntity2)
            .anyTimes();
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId3))
            .andReturn(portletEntity3)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId3))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId2))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId1))
            .andReturn(false)
            .anyTimes();
        
        expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId1))
            .andReturn(portletWindowId1)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId1))
            .andReturn(portletWindow1)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId2))
            .andReturn(portletWindow2)
            .anyTimes();
        
        final IPortletDelegationManager portletDelegationManager = createMock(IPortletDelegationManager.class);
        
        expect(portletDelegationManager.getParentPortletUrl(request, portletWindowId1))
            .andReturn(null);
        expect(portletDelegationManager.getParentPortletUrl(request, portletWindowId2))
            .andReturn(null);
        
        replay(portletDelegationManager, portalRequestUtils, portletWindowRegistry);

        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        portletUrlSyntaxProvider.setPortletDelegationManager(portletDelegationManager);
        
        //Setup portlet window 1
        portletWindow1.setWindowState(WindowState.MAXIMIZED);
        portletWindow1.setPortletMode(PortletMode.VIEW);
        final Map<String, List<String>> parameters1 = new LinkedHashMap<String, List<String>>();
        parameters1.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters1.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters1.put("key3", Arrays.asList(new String[] { "" }) );
        portletWindow1.setRequestParameters(parameters1);
        
        //Setup delegation (wId1 delegates to wId2)
        portletWindow2.setDelegationParent(portletWindowId1);
        portletWindow2.setWindowState(WindowState.MAXIMIZED);
        portletWindow2.setPortletMode(IPortletAdaptor.CONFIG);
        final Map<String, List<String>> parameters2 = new LinkedHashMap<String, List<String>>();
        parameters2.put("keyA", Arrays.asList(new String[] { "valueA.A", "valueA.B" }) );
        parameters2.put("keyB", Arrays.asList(new String[] { "valueB.A" }) );
        portletWindow2.setRequestParameters(parameters2);

        
        //Setup delegation (wId2 delegates to wId3)
        portletWindow3.setDelegationParent(portletWindowId2);
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId1);

        final Map<String, List<String>> parameters3 = new LinkedHashMap<String, List<String>>();
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1", "value3.2" }) );
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1" }) );
        parameters3.put("keyX", Arrays.asList(new String[] { "" }) );
        portletUrl.setParameters(parameters3);
        
        portletUrl.setPortletMode(IPortletAdaptor.CONFIG);
        portletUrl.setWindowState(WindowState.NORMAL);
        
        String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow3, portletUrl);
        
        verify(portletDelegationManager, portalRequestUtils, portletWindowRegistry);
        
        assertEquals(
                "/uPortal/base/action.url" +
                "?pltc_target=wId1" +
                "&pltc_delegate_wId1=wId2" +
                "&pltc_type_wId1=RENDER" +
                "&pltc_mode_wId1=view" +
                "&pltp_wId1_key1=value1.1" +
                "&pltp_wId1_key1=value1.2" +
                "&pltp_wId1_key2=value2.1" +
                "&pltp_wId1_key3=" +
                "&pltc_delegate_wId2=wId3" +
                "&pltc_type_wId2=RENDER" +
                "&pltc_mode_wId2=config" +
                "&pltp_wId2_keyA=valueA.A" +
                "&pltp_wId2_keyA=valueA.B" +
                "&pltp_wId2_keyB=valueB.A" +
                "&pltc_type_wId3=RENDER" +
                "&pltc_mode_wId3=config" +
                "&pltp_wId3_key3=value3.1" +
                "&pltp_wId3_keyX=", 
                urlString);
        
    }
    
    public void testGenerateMultipleDelegatePortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortletHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request);
        
        portletUrlSyntaxProvider.setPortalRequestUtils(portalRequestUtils);
        
        final IPortletEntityId portletEntityId1 = new MockPortletEntityId("eId1");
        final IPortletWindowId portletWindowId1 = new MockPortletWindowId("wId1");
        final IPortletWindow portletWindow1 = new MockPortletWindow(portletWindowId1, portletEntityId1, "portletAppA", "portletNameA");
       
        final IPortletEntityId portletEntityId2 = new MockPortletEntityId("eId2");
        final IPortletWindowId portletWindowId2 = new MockPortletWindowId("wId2");
        final MockPortletWindow portletWindow2 = new MockPortletWindow(portletWindowId2, portletEntityId2, "portletAppB", "portletNameB");
       
        final IPortletEntityId portletEntityId3 = new MockPortletEntityId("eId3");
        final IPortletWindowId portletWindowId3 = new MockPortletWindowId("wId3");
        final MockPortletWindow portletWindow3 = new MockPortletWindow(portletWindowId3, portletEntityId3, "portletAppC", "portletNameC");

        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        channelRuntimeData.setBaseActionURL("base/action.url");
        
        request.setAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA, channelRuntimeData);
        
        final MockPortletEntity portletEntity1 = new MockPortletEntity();
        portletEntity1.setPortletEntityId(portletEntityId1);
        portletEntity1.setChannelSubscribeId(portletEntityId1.getStringId());
        
        final MockPortletEntity portletEntity2 = new MockPortletEntity();
        portletEntity2.setPortletEntityId(portletEntityId2);
        portletEntity2.setChannelSubscribeId(portletEntityId2.getStringId());
        
        final MockPortletEntity portletEntity3 = new MockPortletEntity();
        portletEntity3.setPortletEntityId(portletEntityId3);
        portletEntity3.setChannelSubscribeId(portletEntityId3.getStringId());
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId1))
            .andReturn(portletEntity1)
            .anyTimes();
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId2))
            .andReturn(portletEntity2)
            .anyTimes();
        expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId3))
            .andReturn(portletEntity3)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId3))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId2))
            .andReturn(true)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, portletWindowId1))
            .andReturn(false)
            .anyTimes();
        
        expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId1))
            .andReturn(portletWindowId1)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId1))
            .andReturn(portletWindow1)
            .anyTimes();
        
        expect(portletWindowRegistry.getPortletWindow(request, portletWindowId2))
            .andReturn(portletWindow2)
            .anyTimes();
        
        final IPortletDelegationManager portletDelegationManager = createMock(IPortletDelegationManager.class);
        
        
        final PortletUrl portletUrl1 = new PortletUrl(portletWindowId1);
        final Map<String, List<String>> urlParameters1 = new LinkedHashMap<String, List<String>>();
        urlParameters1.put("newKey1", Arrays.asList(new String[] { "newValue1.1", "newValue1.2" }) );
        urlParameters1.put("newKey2", Arrays.asList(new String[] { "newValue2.1" }) );
        urlParameters1.put("newKey3", Arrays.asList(new String[] { "" }) );
        portletUrl1.setParameters(urlParameters1);
        expect(portletDelegationManager.getParentPortletUrl(request, portletWindowId1))
            .andReturn(portletUrl1);
        
        
        final PortletUrl portletUrl2 = new PortletUrl(portletWindowId2);
        final Map<String, List<String>> urlParameters2 = new LinkedHashMap<String, List<String>>();
        urlParameters2.put("newKeyA", Arrays.asList(new String[] { "newValueA.A", "newValueA.B" }) );
        urlParameters2.put("newKeyB", Arrays.asList(new String[] { "newValueB.A" }) );
        portletUrl2.setParameters(urlParameters2);
        portletUrl2.setPortletMode(PortletMode.VIEW);
        expect(portletDelegationManager.getParentPortletUrl(request, portletWindowId2))
            .andReturn(portletUrl2);
        
        replay(portletDelegationManager, portalRequestUtils, portletWindowRegistry);

        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        portletUrlSyntaxProvider.setPortletDelegationManager(portletDelegationManager);
        
        //Setup portlet window 1
        portletWindow1.setWindowState(WindowState.MAXIMIZED);
        portletWindow1.setPortletMode(PortletMode.VIEW);
        final Map<String, List<String>> parameters1 = new LinkedHashMap<String, List<String>>();
        parameters1.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters1.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters1.put("key3", Arrays.asList(new String[] { "" }) );
        portletWindow1.setRequestParameters(parameters1);
        
        //Setup delegation (wId1 delegates to wId2)
        portletWindow2.setDelegationParent(portletWindowId1);
        portletWindow2.setWindowState(WindowState.MAXIMIZED);
        portletWindow2.setPortletMode(IPortletAdaptor.CONFIG);
        final Map<String, List<String>> parameters2 = new LinkedHashMap<String, List<String>>();
        parameters2.put("keyA", Arrays.asList(new String[] { "valueA.A", "valueA.B" }) );
        parameters2.put("keyB", Arrays.asList(new String[] { "valueB.A" }) );
        portletWindow2.setRequestParameters(parameters2);

        
        //Setup delegation (wId2 delegates to wId3)
        portletWindow3.setDelegationParent(portletWindowId2);
        
        final PortletUrl portletUrl = new PortletUrl(portletWindowId1);

        final Map<String, List<String>> parameters3 = new LinkedHashMap<String, List<String>>();
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1", "value3.2" }) );
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1" }) );
        parameters3.put("keyX", Arrays.asList(new String[] { "" }) );
        portletUrl.setParameters(parameters3);
        
        portletUrl.setPortletMode(IPortletAdaptor.CONFIG);
        portletUrl.setWindowState(WindowState.NORMAL);
        
        String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow3, portletUrl);
        
        verify(portletDelegationManager, portalRequestUtils, portletWindowRegistry);
        
        assertEquals(
                "/uPortal/base/action.url" +
                "?pltc_target=wId1" +
                "&pltc_delegate_wId1=wId2" +
                "&pltc_type_wId1=RENDER" +
                "&pltp_wId1_newKey1=newValue1.1" +
                "&pltp_wId1_newKey1=newValue1.2" +
                "&pltp_wId1_newKey2=newValue2.1" +
                "&pltp_wId1_newKey3=" +
                "&pltc_delegate_wId2=wId3" +
                "&pltc_type_wId2=RENDER" +
                "&pltc_mode_wId2=view" +
                "&pltp_wId2_newKeyA=newValueA.A" +
                "&pltp_wId2_newKeyA=newValueA.B" +
                "&pltp_wId2_newKeyB=newValueB.A" +
                "&pltc_type_wId3=RENDER" +
                "&pltc_mode_wId3=config" +
                "&pltp_wId3_key3=value3.1" +
                "&pltp_wId3_keyX=", 
                urlString);
        
    }
    
    //render.userLayoutRootNode.uP?uP_fname=my-info-student-center-home&pltc_type=ACTION&pltp_action=loginAction
    public void testParsePortletFnameParameters() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            portletUrlSyntaxProvider.parsePortletUrl(null);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        final IUserInstanceManager userInstanceManager = createMock(IUserInstanceManager.class);
        final IUserInstance userInstance = createMock(IUserInstance.class);
        final IUserPreferencesManager userPreferencesManager = createMock(IUserPreferencesManager.class);
        final IUserLayoutManager userLayoutManager = createMock(IUserLayoutManager.class);
        final IUserLayoutChannelDescription userLayoutChannelDescription = createMock(IUserLayoutChannelDescription.class);
        final IPortletDefinitionRegistry portletDefinitionRegistry = createMock(IPortletDefinitionRegistry.class);
        final IPortletDefinition portletDefinition = createMock(IPortletDefinition.class);
        final IChannelDefinition channelDefinition = createMock(IChannelDefinition.class);
        final IPerson person = createMock(IPerson.class);
        final IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        final IPortletEntity portletEntity = createMock(IPortletEntity.class);
        final IPortletWindow portletWindow = createMock(IPortletWindow.class);
        
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("42");
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("subId1");
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("windowId1");
        
        expect(userInstanceManager.getUserInstance(request)).andReturn(userInstance);
        expect(userInstance.getPreferencesManager()).andReturn(userPreferencesManager);
        expect(userPreferencesManager.getUserLayoutManager()).andReturn(userLayoutManager);
        expect(userLayoutManager.getSubscribeId("my-info-student-center-home")).andReturn("subId1");
        expect(userLayoutManager.getNode("subId1")).andReturn(userLayoutChannelDescription);
        expect(userLayoutChannelDescription.getChannelPublishId()).andReturn("42");
        expect(portletDefinitionRegistry.getPortletDefinition(42)).andReturn(portletDefinition);
        expect(portletDefinition.getChannelDefinition()).andReturn(channelDefinition);
        expect(channelDefinition.isPortlet()).andReturn(true);
        expect(userInstance.getPerson()).andReturn(person);
        expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        expect(person.getID()).andReturn(37);
        expect(portletEntityRegistry.getOrCreatePortletEntity(portletDefinitionId, "subId1", 37)).andReturn(portletEntity);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        expect(portletWindowRegistry.createDefaultPortletWindow(request, portletEntityId)).andReturn(portletWindow);
        expect(portletWindow.getPortletWindowId()).andReturn(portletWindowId);
        expect(portletWindowRegistry.createTransientPortletWindowId(request, portletWindowId)).andReturn(portletWindowId);
        
        replay(portletWindowRegistry, userInstanceManager, userInstance, userPreferencesManager, userLayoutManager, 
                userLayoutChannelDescription, portletDefinitionRegistry, portletDefinition, channelDefinition, person,
                portletEntityRegistry, portletEntity, portletWindow);
        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        portletUrlSyntaxProvider.setPortletEntityRegistry(portletEntityRegistry);
        portletUrlSyntaxProvider.setPortletDefinitionRegistry(portletDefinitionRegistry);
        portletUrlSyntaxProvider.setUserInstanceManager(userInstanceManager);
        
        request.setParameter("uP_fname", "my-info-student-center-home");
        request.setParameter("pltc_type", "ACTION");
        request.setParameter("pltp_action", "loginAction");
        
        final PortletUrl portletUrl = portletUrlSyntaxProvider.parsePortletUrl(request);
        
        verify(portletWindowRegistry, userInstanceManager, userInstance, userPreferencesManager, userLayoutManager,
                userLayoutChannelDescription, portletDefinitionRegistry, portletDefinition, channelDefinition, person,
                portletEntityRegistry, portletEntity, portletWindow);
        
        
        PortletUrl portletUrl1 = new PortletUrl(new MockPortletWindowId("windowId1"));
        
        portletUrl1.setRequestType(RequestType.ACTION);
        final Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
        parameters.put("action", Arrays.asList("loginAction"));
        portletUrl1.setParameters(parameters);
        portletUrl1.setSecure(false);
        
        
        assertEquals(portletUrl1, portletUrl);
        
    }
    
    public void testParsePortletParameters() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            portletUrlSyntaxProvider.parsePortletUrl(null);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getPortletWindowId("windowId1"))
            .andReturn(new MockPortletWindowId("windowId1"))
            .anyTimes();
        
        
        replay(portletWindowRegistry);
        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        PortletUrl portletUrl = portletUrlSyntaxProvider.parsePortletUrl(request);
        assertNull(portletUrl);
        
        
        PortletUrl portletUrl1 = new PortletUrl(new MockPortletWindowId("windowId1"));
        
        request.setParameter("pltc_target", "windowId1");
        request.setParameter("pltc_type_windowId1", "RENDER");
        portletUrl1.setRequestType(RequestType.RENDER);
        portletUrl1.setParameters(Collections.EMPTY_MAP);
        portletUrl1.setSecure(false);
        
        portletUrl = portletUrlSyntaxProvider.parsePortletUrl(request);
        assertEquals(portletUrl1, portletUrl);
        
        
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }));
        
        request.setParameter("pltc_state_windowId1", "MAXIMIZED");
        request.setParameter("pltc_mode_windowId1", "HELP");
        request.setParameter("pltp_windowId1_key1", new String[] { "value1.1", "value1.2" });
        portletUrl1.setWindowState(WindowState.MAXIMIZED);
        portletUrl1.setPortletMode(PortletMode.HELP);
        portletUrl1.setParameters(parameters);
        
        portletUrl = portletUrlSyntaxProvider.parsePortletUrl(request);
        assertEquals(portletUrl1, portletUrl);
        
        
        parameters.put("post_parameter", Arrays.asList(new String[] { "post_value" }));
        
        request.setMethod("POST");
        request.setParameter("post_parameter", "post_value");
        request.setQueryString("pltc_target=windowId1&pltc_type_windowId1=RENDER&pltc_state_windowId1=MAXIMIZED&pltc_mode_windowId1=HELP&pltp_windowId1_key1=value1.1&pltp_windowId1_key1=value1.2");
        
        portletUrl = portletUrlSyntaxProvider.parsePortletUrl(request);
        assertEquals(portletUrl1, portletUrl);
    }
    
    public void testParseDelegatePortletParameters() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            portletUrlSyntaxProvider.parsePortletUrl(null);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        final MockPortletWindowId wId1 = new MockPortletWindowId("wId1");
        final MockPortletWindowId wId2 = new MockPortletWindowId("wId2");
        final MockPortletWindowId wId3 = new MockPortletWindowId("wId3");
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(portletWindowRegistry.getPortletWindowId("wId1"))
            .andReturn(wId1)
            .anyTimes();
        expect(portletWindowRegistry.getPortletWindowId("wId2"))
            .andReturn(wId2)
            .anyTimes();
        expect(portletWindowRegistry.getPortletWindowId("wId3"))
            .andReturn(wId3)
            .anyTimes();
        
        expect(portletWindowRegistry.isTransient(request, wId2))
            .andReturn(true)
            .anyTimes();
        expect(portletWindowRegistry.isTransient(request, wId3))
            .andReturn(true)
            .anyTimes();
        

        final MockPortletWindow w2 = new MockPortletWindow();
        w2.setDelegationParent(wId1);
        expect(portletWindowRegistry.getPortletWindow(request, wId2))
            .andReturn(w2)
            .anyTimes();
        
        final MockPortletWindow w3 = new MockPortletWindow();
        w3.setDelegationParent(wId2);
        expect(portletWindowRegistry.getPortletWindow(request, wId3))
            .andReturn(w3)
            .anyTimes();
        
        
        replay(portletWindowRegistry);
        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        request.setParameter("pltc_target", "wId1");
        request.setParameter("pltc_delegate_wId1", "wId2");
        request.setParameter("pltc_type_wId1", "RENDER");
        request.setParameter("pltp_wId1_key1", new String[] {"value1.1", "value1.2"} );
        request.setParameter("pltp_wId1_key2", "value2.1");
        request.setParameter("pltp_wId1_key3", "");
        request.setParameter("pltc_delegate_wId2", "wId3");
        request.setParameter("pltc_type_wId2", "RENDER");
        request.setParameter("pltp_wId2_keyA", new String[] {"valueA.A", "valueA.B"} );
        request.setParameter("pltp_wId2_keyB", "valueB.A");
        request.setParameter("pltc_type_wId3", "RENDER");
        request.setParameter("pltc_mode_wId3", "config");
        request.setParameter("pltp_wId3_key3", "value3.1");
        request.setParameter("pltp_wId3_keyX", "");
        
        final PortletUrl portletUrl = portletUrlSyntaxProvider.parsePortletUrl(request);
        
        final PortletUrl portletUrl1 = new PortletUrl(wId1);
        portletUrl1.setRequestType(RequestType.RENDER);
        portletUrl1.setSecure(false);
        final Map<String, List<String>> parameters1 = new LinkedHashMap<String, List<String>>();
        parameters1.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters1.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters1.put("key3", Arrays.asList(new String[] { "" }) );
        portletUrl1.setParameters(parameters1);
        
        
        final PortletUrl portletUrl2 = new PortletUrl(wId2);
        portletUrl2.setRequestType(RequestType.RENDER);
        portletUrl2.setSecure(false);
        final Map<String, List<String>> parameters2 = new LinkedHashMap<String, List<String>>();
        parameters2.put("keyA", Arrays.asList(new String[] { "valueA.A", "valueA.B" }) );
        parameters2.put("keyB", Arrays.asList(new String[] { "valueB.A" }) );
        portletUrl2.setParameters(parameters2);
        portletUrl1.setDelegatePortletUrl(portletUrl2);
        
        
        final PortletUrl portletUrl3 = new PortletUrl(wId3);
        portletUrl3.setRequestType(RequestType.RENDER);
        portletUrl3.setSecure(false);
        portletUrl3.setPortletMode(IPortletAdaptor.CONFIG);
        final Map<String, List<String>> parameters3 = new LinkedHashMap<String, List<String>>();
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1", "value3.2" }) );
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1" }) );
        parameters3.put("keyX", Arrays.asList(new String[] { "" }) );
        portletUrl3.setParameters(parameters3);
        portletUrl2.setDelegatePortletUrl(portletUrl3);
        
        
        assertEquals(portletUrl1, portletUrl);
    }
}
