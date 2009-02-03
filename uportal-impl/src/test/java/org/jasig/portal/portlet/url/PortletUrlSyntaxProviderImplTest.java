/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.url.AttributeScopingHttpServletRequestWrapper;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.Tuple;
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
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        request.setContextPath("/uPortal");
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(request)).andReturn(request).times(5);
        
        EasyMock.replay(portalRequestUtils);
        portletUrlSyntaxProvider.setPortalRequestUtils(portalRequestUtils);
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("entityId1");
        final IPortletWindowId portletWindowId = new MockPortletWindowId("windowId1");
        final IPortletWindow portletWindow = new MockPortletWindow(portletWindowId, portletEntityId, "portletApp", "portletName");
       
        final boolean useAnchors = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.ChannelManager.use_anchors", false);
 
        final PortletUrl portletUrl = new PortletUrl();
        
        try {
            portletUrlSyntaxProvider.generatePortletUrl(null, portletWindow, portletUrl);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        try {
            portletUrlSyntaxProvider.generatePortletUrl(request, null, portletUrl);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null portletWindow");
        }
        catch (IllegalArgumentException iae) {
        }
        
        try {
            portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, null);
            fail("generatePortletUrl should have thrown an IllegalArgumentException with a null portletUrl");
        }
        catch (IllegalArgumentException iae) {
        }
        
        try {
            portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
            fail("generatePortletUrl should have thrown IllegalStateException with no ChannelRuntimeData in the request");
        }
        catch (IllegalStateException ise) {
        }
        
        final ChannelRuntimeData channelRuntimeData = new ChannelRuntimeData();
        channelRuntimeData.setBaseActionURL("base/action.url");
        
        request.setAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA, channelRuntimeData);
        
        MockPortletEntity portletEntity = new MockPortletEntity();
        portletEntity.setPortletEntityId(portletEntityId);
        portletEntity.setChannelSubscribeId(portletEntityId.getStringId());
        
        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getParentPortletEntity(request, portletWindowId))
            .andReturn(portletEntity)
            .anyTimes();
        
        EasyMock.replay(portletWindowRegistry);

        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        //String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        //assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=RENDER", urlString);
        String urlString = "";       
 
        if(useAnchors) {
        	urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
           	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=RENDER#entityId1", urlString);
        }
        else {
        	urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=RENDER", urlString);
        }      
 
        final Map<String, String[]> parameters = new TreeMap<String, String[]>(); //Use a treemap so the output string is deterministic
        parameters.put("key1", new String[] { "value1.1", "value1.2" } );
        parameters.put("key2", new String[] { "value2.1" } );
        parameters.put("key3", new String[] { "" } );
        
        portletUrl.setPortletMode(PortletMode.EDIT);
        portletUrl.setWindowState(WindowState.MINIMIZED);
        portletUrl.setParameters(parameters);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        if(useAnchors) {
        	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=RENDER&pltc_state=minimized&uP_root=root&uP_tcattr=minimized&minimized_channelId=entityId1&minimized_entityId1_value=true&pltc_mode=edit&pltp_key1=value1.1&pltp_key1=value1.2&pltp_key2=value2.1&pltp_key3=#entityId1", urlString);
        }
        else {
        	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=RENDER&pltc_state=minimized&uP_root=root&uP_tcattr=minimized&minimized_channelId=entityId1&minimized_entityId1_value=true&pltc_mode=edit&pltp_key1=value1.1&pltp_key1=value1.2&pltp_key2=value2.1&pltp_key3=", urlString);
        }

        portletUrl.setRequestType(RequestType.ACTION);
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        if(useAnchors) {
        	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=ACTION&pltc_state=maximized&uP_root=entityId1&pltc_mode=edit&pltp_key1=value1.1&pltp_key1=value1.2&pltp_key2=value2.1&pltp_key3=", urlString);
        }
        else {
        	assertEquals("/uPortal/base/action.url?pltc_target=windowId1&pltc_type=ACTION&pltc_state=maximized&uP_root=entityId1&pltc_mode=edit&pltp_key1=value1.1&pltp_key1=value1.2&pltp_key2=value2.1&pltp_key3=", urlString);
        }
        portletUrl.setWindowState(new WindowState("EXCLUSIVE"));
        portletUrl.setRequestType(RequestType.RENDER);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        if(useAnchors) {
       		assertEquals("/uPortal/worker/download/worker.download.uP?pltc_target=windowId1&pltc_type=RENDER&pltc_state=exclusive&pltc_mode=edit&pltp_key1=value1.1&pltp_key1=value1.2&pltp_key2=value2.1&pltp_key3=#entityId1", urlString);
        }
        else {
        	assertEquals("/uPortal/worker/download/worker.download.uP?pltc_target=windowId1&pltc_type=RENDER&pltc_state=exclusive&pltc_mode=edit&pltp_key1=value1.1&pltp_key1=value1.2&pltp_key2=value2.1&pltp_key3=", urlString);
        }
        EasyMock.verify(portalRequestUtils);
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
        
        
        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindowId("windowId1"))
            .andReturn(new MockPortletWindowId("windowId1"))
            .anyTimes();
        
        
        EasyMock.replay(portletWindowRegistry);
        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        Tuple<IPortletWindowId, PortletUrl> parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertNull(parsedPortletUrl);
        
        
        PortletUrl portletUrl1 = new PortletUrl();
        Tuple<IPortletWindowId, PortletUrl> expectedParsedUrl = new Tuple<IPortletWindowId, PortletUrl>(new MockPortletWindowId("windowId1"), portletUrl1);
        
        request.setParameter("pltc_target", "windowId1");
        request.setParameter("pltc_type", "RENDER");
        portletUrl1.setRequestType(RequestType.RENDER);
        portletUrl1.setParameters(Collections.EMPTY_MAP);
        portletUrl1.setSecure(false);
        
        parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrl, parsedPortletUrl);
        
        
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("key1", new String[] { "value1.1", "value1.2" });
        
        request.setParameter("pltc_state", "MAXIMIZED");
        request.setParameter("pltc_mode", "HELP");
        request.setParameter("pltp_key1", new String[] { "value1.1", "value1.2" });
        portletUrl1.setWindowState(WindowState.MAXIMIZED);
        portletUrl1.setPortletMode(PortletMode.HELP);
        portletUrl1.setParameters(parameters);
        
        parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrl, parsedPortletUrl);
        
        
        parameters.put("post_parameter", new String[] { "post_value" });
        
        request.setMethod("POST");
        request.setParameter("post_parameter", "post_value");
        request.setQueryString("pltc_target=windowId1&pltc_type=RENDER&pltc_state=MAXIMIZED&pltc_mode=HELP&pltp_key1=value1.1&pltp_key1=value1.2");
        
        parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrl, parsedPortletUrl);
    }
}
