/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindow;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlSyntaxProviderImplTest extends TestCase {
    
    public void testParseParameterName() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        Tuple<String, String> parsedParameter = portletUrlSyntaxProvider.parseParameterName("alpha_beta_gamma");
        assertEquals("beta", parsedParameter.first);
        assertEquals("gamma", parsedParameter.second);
        
        parsedParameter = portletUrlSyntaxProvider.parseParameterName("alpha_beta_gamma_delta");
        assertEquals("beta", parsedParameter.first);
        assertEquals("gamma_delta", parsedParameter.second);
        
        parsedParameter = portletUrlSyntaxProvider.parseParameterName("alpha_beta_gamma_delta");
        assertEquals("beta", parsedParameter.first);
        assertEquals("gamma_delta", parsedParameter.second);
        
        parsedParameter = portletUrlSyntaxProvider.parseParameterName("alpha");
        assertEquals(null, parsedParameter);
        
        parsedParameter = portletUrlSyntaxProvider.parseParameterName("alpha_beta");
        assertEquals(null, parsedParameter);
        
        parsedParameter = portletUrlSyntaxProvider.parseParameterName("alpha_beta_");
        assertEquals("beta", parsedParameter.first);
        assertEquals("", parsedParameter.second);
        
    }
    
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
        request.setContextPath("/uPortal");
        
        final IPortletEntityId portletEntityId = new MockPortletEntityId("entityId1");
        final IPortletWindowId portletWindowId = new MockPortletWindowId("windowId1");
        final IPortletWindow portletWindow = new MockPortletWindow(portletWindowId, portletEntityId, "portletApp", "portletName");
        
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
        
        String urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        assertEquals("/uPortal/base/action.url?plt_type_windowId1=RENDER", urlString);
        
        
        final Map<String, String[]> parameters = new TreeMap<String, String[]>(); //Use a treemap so the output string is deterministic
        parameters.put("key1", new String[] { "value1.1", "value1.2" } );
        parameters.put("key2", new String[] { "value2.1" } );
        parameters.put("key3", new String[] { "" } );
        
        portletUrl.setPortletMode(PortletMode.EDIT);
        portletUrl.setWindowState(WindowState.MINIMIZED);
        portletUrl.setParameters(parameters);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        assertEquals("/uPortal/base/action.url?plt_type_windowId1=RENDER&plt_state_windowId1=minimized&plt_mode_windowId1=edit&plt_windowId1_key1=value1.1&plt_windowId1_key1=value1.2&plt_windowId1_key2=value2.1&plt_windowId1_key3=", urlString);


        portletUrl.setRequestType(RequestType.ACTION);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        assertEquals("/uPortal/base/action.url?plt_type_windowId1=ACTION&plt_state_windowId1=minimized&plt_mode_windowId1=edit&plt_windowId1_key1=value1.1&plt_windowId1_key1=value1.2&plt_windowId1_key2=value2.1&plt_windowId1_key3=", urlString);
        
        portletUrl.setWindowState(new WindowState("EXCLUSIVE"));
        portletUrl.setRequestType(RequestType.RENDER);
        
        urlString = portletUrlSyntaxProvider.generatePortletUrl(request, portletWindow, portletUrl);
        assertEquals("/uPortal/worker/download/worker.download.uP?plt_type_windowId1=RENDER&plt_state_windowId1=exclusive&plt_mode_windowId1=edit&plt_windowId1_key1=value1.1&plt_windowId1_key1=value1.2&plt_windowId1_key2=value2.1&plt_windowId1_key3=", urlString);
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
        
        Map<IPortletWindowId, PortletUrl> expectedParsedUrls = new HashMap<IPortletWindowId, PortletUrl>();
        
        
        Map<IPortletWindowId, PortletUrl> parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrls, parsedPortletUrls);
        
        
        PortletUrl portletUrl1 = new PortletUrl();
        expectedParsedUrls.put(new MockPortletWindowId("windowId1"), portletUrl1);
        
        
        request.setParameter("plt_type_windowId1", "RENDER");
        portletUrl1.setRequestType(RequestType.RENDER);
        
        parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrls, parsedPortletUrls);
        
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("key1", new String[] { "value1.1", "value1.2" });
        
        request.setParameter("plt_state_windowId1", "MAXIMIZED");
        request.setParameter("plt_mode_windowId1", "HELP");
        request.setParameter("plt_windowId1_key1", new String[] { "value1.1", "value1.2" });
        portletUrl1.setWindowState(WindowState.MAXIMIZED);
        portletUrl1.setPortletMode(PortletMode.HELP);
        portletUrl1.setParameters(parameters);
        
        parsedPortletUrls = portletUrlSyntaxProvider.parsePortletParameters(request);
        assertEquals(expectedParsedUrls, parsedPortletUrls);
    }
}
