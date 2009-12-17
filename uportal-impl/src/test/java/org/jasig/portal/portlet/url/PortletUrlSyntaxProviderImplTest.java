/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.mock.portlet.om.MockPortletEntity;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindow;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry;
import org.jasig.portal.url.AttributeScopingHttpServletRequestWrapper;
import org.jasig.portal.url.IPortalRequestUtils;
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
        request.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request).times(4);
        
        EasyMock.replay(portalRequestUtils);
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
        
        final ITransientPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(ITransientPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId))
            .andReturn(portletEntity)
            .anyTimes();
        
        final MockPortletWindowId transientPortletWindowId = new MockPortletWindowId("tp.windowId1");
        EasyMock.expect(portletWindowRegistry.createTransientPortletWindowId(request, portletWindowId))
            .andReturn(transientPortletWindowId)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, portletWindowId))
            .andReturn(true)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId))
            .andReturn(portletWindowId)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getPortletWindow(request, portletWindowId))
            .andReturn(portletWindow)
            .anyTimes();
        
        EasyMock.replay(portletWindowRegistry);

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
    	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type_windowId1=RENDER&pltc_state_windowId1=minimized&uP_root=root&uP_tcattr=minimized&minimized_channelId=entityId1&minimized_entityId1_value=true&pltc_mode_windowId1=edit&pltp_windowId1_key1=value1.1&pltp_windowId1_key1=value1.2&pltp_windowId1_key2=value2.1&pltp_windowId1_key3=#entityId1", urlString);

        portletUrl.setRequestType(RequestType.ACTION);
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
    	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type_windowId1=ACTION&pltc_state_windowId1=maximized&uP_root=entityId1&pltc_mode_windowId1=edit&pltp_windowId1_key1=value1.1&pltp_windowId1_key1=value1.2&pltp_windowId1_key2=value2.1&pltp_windowId1_key3=", urlString);
        portletUrl.setWindowState(new WindowState("EXCLUSIVE"));
        portletUrl.setRequestType(RequestType.RENDER);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
    	assertEquals("/uPortal/worker/download/worker.download.uP?pltc_target=tp.windowId1&pltc_type_tp.windowId1=RENDER&pltc_state_tp.windowId1=exclusive&pltc_mode_tp.windowId1=edit&pltp_tp.windowId1_key1=value1.1&pltp_tp.windowId1_key1=value1.2&pltp_tp.windowId1_key2=value2.1&pltp_tp.windowId1_key3=", urlString);
        EasyMock.verify(portalRequestUtils);
    }
    

    
    public void testGenerateSingleDelegatePortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request).times(5);
        
        EasyMock.replay(portalRequestUtils);
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
        
        final ITransientPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(ITransientPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId1))
            .andReturn(portletEntity1)
            .anyTimes();
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId2))
            .andReturn(portletEntity2)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, portletWindowId2))
            .andReturn(true)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, portletWindowId1))
            .andReturn(false)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId1))
            .andReturn(portletWindowId1)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getPortletWindow(request, portletWindowId1))
            .andReturn(portletWindow1)
            .anyTimes();
        
        EasyMock.replay(portletWindowRegistry);

        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
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
        assertEquals(
                "/uPortal/base/action.url" +
        		"?pltc_target=wId1" +
        		"&pltc_delegate_wId1=wId2" +
        		"&pltc_type_wId1=RENDER" +
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
    

    
    public void testGenerateMultipleDelegatePortletUrl() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        portletUrlSyntaxProvider.setUseAnchors(true);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request).times(5);
        
        EasyMock.replay(portalRequestUtils);
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
        
        final ITransientPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(ITransientPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId1))
            .andReturn(portletEntity1)
            .anyTimes();
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId2))
            .andReturn(portletEntity2)
            .anyTimes();
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId3))
            .andReturn(portletEntity3)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, portletWindowId3))
            .andReturn(true)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, portletWindowId2))
            .andReturn(true)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, portletWindowId1))
            .andReturn(false)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getDefaultPortletWindowId(portletEntityId1))
            .andReturn(portletWindowId1)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getPortletWindow(request, portletWindowId1))
            .andReturn(portletWindow1)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.getPortletWindow(request, portletWindowId2))
            .andReturn(portletWindow2)
            .anyTimes();
        
        EasyMock.replay(portletWindowRegistry);

        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
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
        portletWindow1.setWindowState(WindowState.MAXIMIZED);
        portletWindow1.setPortletMode(IPortletAdaptor.CONFIG);
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
        assertEquals(
                "/uPortal/base/action.url" +
                "?pltc_target=wId1" +
                "&pltc_delegate_wId1=wId2" +
                "&pltc_type_wId1=RENDER" +
                "&pltp_wId1_key1=value1.1" +
                "&pltp_wId1_key1=value1.2" +
                "&pltp_wId1_key2=value2.1" +
                "&pltp_wId1_key3=" +
                "&pltc_delegate_wId2=wId3" +
                "&pltc_type_wId2=RENDER" +
                "&pltp_wId2_keyA=valueA.A" +
                "&pltp_wId2_keyA=valueA.B" +
                "&pltp_wId2_keyB=valueB.A" +
                "&pltc_type_wId3=RENDER" +
                "&pltc_mode_wId3=config" +
                "&pltp_wId3_key3=value3.1" +
                "&pltp_wId3_keyX=", 
                urlString);
    }
    
    public void testParsePortletParameters() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            portletUrlSyntaxProvider.parsePortletParameters(null);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        
        final ITransientPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(ITransientPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindowId("windowId1"))
            .andReturn(new MockPortletWindowId("windowId1"))
            .anyTimes();
        
        
        EasyMock.replay(portletWindowRegistry);
        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        List<PortletUrl> parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertNull(parsedPortletUrls);
        
        
        PortletUrl portletUrl1 = new PortletUrl(new MockPortletWindowId("windowId1"));
        List<PortletUrl> expectedParsedUrl = new LinkedList<PortletUrl>();
        expectedParsedUrl.add(portletUrl1);
        
        request.setParameter("pltc_target", "windowId1");
        request.setParameter("pltc_type_windowId1", "RENDER");
        portletUrl1.setRequestType(RequestType.RENDER);
        portletUrl1.setParameters(Collections.EMPTY_MAP);
        portletUrl1.setSecure(false);
        
        parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrl, parsedPortletUrls);
        
        
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }));
        
        request.setParameter("pltc_state_windowId1", "MAXIMIZED");
        request.setParameter("pltc_mode_windowId1", "HELP");
        request.setParameter("pltp_windowId1_key1", new String[] { "value1.1", "value1.2" });
        portletUrl1.setWindowState(WindowState.MAXIMIZED);
        portletUrl1.setPortletMode(PortletMode.HELP);
        portletUrl1.setParameters(parameters);
        
        parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrl, parsedPortletUrls);
        
        
        parameters.put("post_parameter", Arrays.asList(new String[] { "post_value" }));
        
        request.setMethod("POST");
        request.setParameter("post_parameter", "post_value");
        request.setQueryString("pltc_target=windowId1&pltc_type_windowId1=RENDER&pltc_state_windowId1=MAXIMIZED&pltc_mode_windowId1=HELP&pltp_windowId1_key1=value1.1&pltp_windowId1_key1=value1.2");
        
        parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrl, parsedPortletUrls);
    }
    
    public void testParseDelegatePortletParameters() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            portletUrlSyntaxProvider.parsePortletParameters(null);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        final MockPortletWindowId wId1 = new MockPortletWindowId("wId1");
        final MockPortletWindowId wId2 = new MockPortletWindowId("wId2");
        final MockPortletWindowId wId3 = new MockPortletWindowId("wId3");
        
        final ITransientPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(ITransientPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindowId("wId1"))
            .andReturn(wId1)
            .anyTimes();
        EasyMock.expect(portletWindowRegistry.getPortletWindowId("wId2"))
            .andReturn(wId2)
            .anyTimes();
        EasyMock.expect(portletWindowRegistry.getPortletWindowId("wId3"))
            .andReturn(wId3)
            .anyTimes();
        
        EasyMock.expect(portletWindowRegistry.isTransient(request, wId2))
            .andReturn(true)
            .anyTimes();
        EasyMock.expect(portletWindowRegistry.isTransient(request, wId3))
            .andReturn(true)
            .anyTimes();
        

        final MockPortletWindow w2 = new MockPortletWindow();
        w2.setDelegationParent(wId1);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(request, wId2))
            .andReturn(w2)
            .anyTimes();
        
        final MockPortletWindow w3 = new MockPortletWindow();
        w3.setDelegationParent(wId2);
        EasyMock.expect(portletWindowRegistry.getPortletWindow(request, wId3))
            .andReturn(w3)
            .anyTimes();
        
        
        EasyMock.replay(portletWindowRegistry);
        
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
        
        final List<PortletUrl> parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        
        final List<PortletUrl> expectedParsedUrl = new LinkedList<PortletUrl>();
        
        final PortletUrl portletUrl1 = new PortletUrl(wId1);
        portletUrl1.setRequestType(RequestType.RENDER);
        portletUrl1.setSecure(false);
        final Map<String, List<String>> parameters1 = new LinkedHashMap<String, List<String>>();
        parameters1.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }) );
        parameters1.put("key2", Arrays.asList(new String[] { "value2.1" }) );
        parameters1.put("key3", Arrays.asList(new String[] { "" }) );
        portletUrl1.setParameters(parameters1);
        expectedParsedUrl.add(portletUrl1);
        
        
        final PortletUrl portletUrl2 = new PortletUrl(wId2);
        portletUrl2.setRequestType(RequestType.RENDER);
        portletUrl2.setSecure(false);
        final Map<String, List<String>> parameters2 = new LinkedHashMap<String, List<String>>();
        parameters2.put("keyA", Arrays.asList(new String[] { "valueA.A", "valueA.B" }) );
        parameters2.put("keyB", Arrays.asList(new String[] { "valueB.A" }) );
        portletUrl2.setParameters(parameters2);
        expectedParsedUrl.add(portletUrl2);
        
        
        final PortletUrl portletUrl3 = new PortletUrl(wId3);
        portletUrl3.setRequestType(RequestType.RENDER);
        portletUrl3.setSecure(false);
        portletUrl3.setPortletMode(IPortletAdaptor.CONFIG);
        final Map<String, List<String>> parameters3 = new LinkedHashMap<String, List<String>>();
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1", "value3.2" }) );
        parameters3.put("key3", Arrays.asList(new String[] { "value3.1" }) );
        parameters3.put("keyX", Arrays.asList(new String[] { "" }) );
        portletUrl3.setParameters(parameters3);
        expectedParsedUrl.add(portletUrl3);
        
        
        assertEquals(expectedParsedUrl, parsedPortletUrls);
    }
}
