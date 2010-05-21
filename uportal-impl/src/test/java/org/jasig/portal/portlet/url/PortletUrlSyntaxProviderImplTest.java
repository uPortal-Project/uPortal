/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.easymock.EasyMock;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlSyntaxProviderImplTest {
    
	@Test
    public void testEncodeAndAppend() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        StringBuilder url = new StringBuilder();
        portletUrlSyntaxProvider.encodeAndAppend(url, "UTF-8", "name");
        Assert.assertEquals("name=", url.toString());
        
        url = new StringBuilder();
        portletUrlSyntaxProvider.encodeAndAppend(url, "UTF-8", "name", "value1");
        Assert.assertEquals("name=value1", url.toString());
        
        url = new StringBuilder();
        portletUrlSyntaxProvider.encodeAndAppend(url, "UTF-8", "name", "value1", "value2");
        Assert.assertEquals("name=value1&name=value2", url.toString());
        
        portletUrlSyntaxProvider.encodeAndAppend(url.append("&"), "UTF-8", "name2", "value21", "value22");
        Assert.assertEquals("name=value1&name=value2&name2=value21&name2=value22", url.toString());
    }
    
    
    
    public void testParsePortletParameters() throws Exception {
        final PortletUrlSyntaxProviderImpl portletUrlSyntaxProvider = new PortletUrlSyntaxProviderImpl();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            portletUrlSyntaxProvider.parsePortletParameters(null);
            Assert.fail("generatePortletUrl should have thrown an IllegalArgumentException with a null request");
        }
        catch (IllegalArgumentException iae) {
        }
        
        
        final ITransientPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(ITransientPortletWindowRegistry.class);
        EasyMock.expect(portletWindowRegistry.getPortletWindowId("windowId1"))
            .andReturn(new MockPortletWindowId("windowId1"))
            .anyTimes();
        
        
        EasyMock.replay(portletWindowRegistry);
        
        portletUrlSyntaxProvider.setPortletWindowRegistry(portletWindowRegistry);
        
        Tuple<IPortletWindowId, PortletUrl> parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        Assert.assertNull(parsedPortletUrl);
        
        IPortletWindowId portletWindowId = new MockPortletWindowId("windowId1");
        PortletUrl portletUrl1 = new PortletUrl(portletWindowId);
        Tuple<IPortletWindowId, PortletUrl> expectedParsedUrl = new Tuple<IPortletWindowId, PortletUrl>(portletWindowId, portletUrl1);
        
        request.setParameter("pltc_target", "windowId1");
        request.setParameter("pltc_type", "RENDER");
        portletUrl1.setRequestType(TYPE.RENDER);
        portletUrl1.setParameters(Collections.EMPTY_MAP);
        portletUrl1.setSecure(false);
        
        parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        Assert.assertEquals(expectedParsedUrl, parsedPortletUrl);
        
        
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("key1", Arrays.asList(new String[] { "value1.1", "value1.2" }));
        
        request.setParameter("pltc_state", "MAXIMIZED");
        request.setParameter("pltc_mode", "HELP");
        request.setParameter("pltp_key1", new String[] { "value1.1", "value1.2" });
        portletUrl1.setWindowState(WindowState.MAXIMIZED);
        portletUrl1.setPortletMode(PortletMode.HELP);
        portletUrl1.setParameters(parameters);
        
        parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        Assert.assertEquals(expectedParsedUrl, parsedPortletUrl);
        
        
        parameters.put("post_parameter", Arrays.asList(new String[] { "post_value" }));
        
        request.setMethod("POST");
        request.setParameter("post_parameter", "post_value");
        request.setQueryString("pltc_target=windowId1&pltc_type=RENDER&pltc_state=MAXIMIZED&pltc_mode=HELP&pltp_key1=value1.1&pltp_key1=value1.2");
        
        parsedPortletUrl = portletUrlSyntaxProvider.parsePortletParameters(request);
        Assert.assertEquals(expectedParsedUrl, parsedPortletUrl);
    }
}
