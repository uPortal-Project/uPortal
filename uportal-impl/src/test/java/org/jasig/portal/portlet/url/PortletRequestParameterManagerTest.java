/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.PortletWindowIdImpl;
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
        
        parameterManager.setNoPortletRequest(request);
        
        try {
            parameterManager.setRequestType(request, new PortletWindowIdImpl("id"), RequestType.RENDER);
            fail("An IllegalStateException should have been thrown for calling setRequestType after setNoPortletRequest");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        final boolean portletTargeted = parameterManager.isPortletTargeted(request);
        assertFalse(portletTargeted);
        
        final Set<IPortletWindowId> targetedPortletWindowIds = parameterManager.getTargetedPortletWindowIds(request);
        assertNull("targetedPortletWindowIds should be null", targetedPortletWindowIds);
        
        final RequestType portletRequestType = parameterManager.getPortletRequestType(request, new PortletWindowIdImpl("id"));
        assertNull("portletRequestType should be null", portletRequestType);
    }
    
    public void testParameters() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final Map<String, Object[]> parameters = new HashMap<String, Object[]>();
        parameters.put("p1", new Object[] { "v1.1" });
        
        final PortletWindowIdImpl portletWindowId = new PortletWindowIdImpl("id");
        parameterManager.setRequestType(request, portletWindowId, RequestType.RENDER);
        
        try {
            parameterManager.setNoPortletRequest(request);
            fail("An IllegalStateException should have been thrown for calling setNoPortletRequest after setRequestType");
        }
        catch (IllegalStateException ise) {
            //expected
        }
        
        final Set<IPortletWindowId> targetedPortletWindowIds = parameterManager.getTargetedPortletWindowIds(request);
        assertEquals(Collections.singleton(portletWindowId), targetedPortletWindowIds);
        
        final RequestType portletRequestType = parameterManager.getPortletRequestType(request, new PortletWindowIdImpl("id"));
        assertEquals(RequestType.RENDER, portletRequestType);
    }

    public void testNoParsing() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        try {
            parameterManager.getTargetedPortletWindowIds(request);
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getTargetedPortletWindowIds before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        try {
            parameterManager.getPortletRequestType(request, new PortletWindowIdImpl("id"));
            fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getPortletRequestType before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
    }
}
