/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.PortalHttpServletRequestWrapper;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterManagerTest extends TestCase {
    public void testNoParameters() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request).times(2);
        
        EasyMock.replay(portalRequestUtils);
        
        parameterManager.setPortalRequestUtils(portalRequestUtils);
        parameterManager.setTargetedPortletUrl(request, null);
        
        final IPortletWindowId targetedPortletWindowId = parameterManager.getTargetedPortletWindowId(request);
        assertNull(targetedPortletWindowId);
        
        final PortletUrl portletUrl = parameterManager.getPortletRequestInfo(request, new MockPortletWindowId("test"));
        assertNull("portletRequestType should be null", portletUrl);
        
        EasyMock.verify(portalRequestUtils);
    }
    
    public void testParameters() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
        parameters.put("p1", new Object[] { "v1.1" });
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request).times(3);
        
        EasyMock.replay(portalRequestUtils);
        
        parameterManager.setPortalRequestUtils(portalRequestUtils);
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("id");
        final PortletUrl portletUrl = new PortletUrl(portletWindowId);
        portletUrl.setRequestType(RequestType.RENDER);
        parameterManager.setTargetedPortletUrl(request, portletUrl);
        
        final IPortletWindowId targetedPortletWindowId = parameterManager.getTargetedPortletWindowId(request);
        assertEquals(portletWindowId, targetedPortletWindowId);
        
        final PortletUrl portletUrl2 = parameterManager.getPortletRequestInfo(request, targetedPortletWindowId);
        assertEquals(portletUrl, portletUrl2);
        
        EasyMock.verify(portalRequestUtils);
    }

    public void testNoParsing() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request).times(1);
        
        EasyMock.replay(portalRequestUtils);
        
        parameterManager.setPortalRequestUtils(portalRequestUtils);
        
        try {
            parameterManager.getTargetedPortletWindowId(request);
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getTargetedPortletWindowIds before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        try {
            parameterManager.getPortletRequestInfo(request, new MockPortletWindowId(""));
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getPortletRequestType before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        EasyMock.verify(portalRequestUtils);
    }
}
