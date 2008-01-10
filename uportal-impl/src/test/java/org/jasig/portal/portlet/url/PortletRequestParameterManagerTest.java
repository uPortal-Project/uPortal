/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.PortalHttpServletRequest;
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
        request.setAttribute(PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        parameterManager.setNoPortletRequest(request);
        
        final IPortletWindowId targetedPortletWindowId = parameterManager.getTargetedPortletWindowId(request);
        assertNull(targetedPortletWindowId);
        
        final PortletRequestInfo portletRequestInfo = parameterManager.getPortletRequestInfo(request);
        assertNull("portletRequestType should be null", portletRequestInfo);
    }
    
    public void testParameters() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
        parameters.put("p1", new Object[] { "v1.1" });
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("id");
        parameterManager.setRequestInfo(request, portletWindowId, new PortletRequestInfo(RequestType.RENDER));
        
        final IPortletWindowId targetedPortletWindowId = parameterManager.getTargetedPortletWindowId(request);
        assertEquals(portletWindowId, targetedPortletWindowId);
        
        final PortletRequestInfo portletRequestInfo = parameterManager.getPortletRequestInfo(request);
        assertEquals(new PortletRequestInfo(RequestType.RENDER), portletRequestInfo);
    }

    public void testNoParsing() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequest.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        try {
            parameterManager.getTargetedPortletWindowId(request);
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getTargetedPortletWindowIds before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        try {
            parameterManager.getPortletRequestInfo(request);
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getPortletRequestType before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
    }
}
