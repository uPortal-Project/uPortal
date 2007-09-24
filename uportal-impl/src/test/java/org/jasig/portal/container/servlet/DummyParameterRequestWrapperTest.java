/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.util.HashMap;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * Unit test for DummyParameterRequestWrapper.
 */
public class DummyParameterRequestWrapperTest extends TestCase {

    /**
     * Test the case where DummyParameterRequestWrapper is instantiated with
     * no parameter Map, such that its behavior is only to hide the parameter
     * Map of the underlying request.
     */
    public void testNoParameters() {
        
        // build the backing Request
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addParameter("paramkey", "paramvalue");
        assertEquals("paramvalue", mockRequest.getParameter("paramkey"));
        
        // wrap the backing request
        DummyParameterRequestWrapper dummyWrapper = 
            new DummyParameterRequestWrapper(mockRequest);
        
        // show that the Dummy wrapper hides the parameter of the underlying Request
        assertNull(dummyWrapper.getParameter("paramkey"));
        assertTrue(dummyWrapper.getParameterMap().isEmpty());
        assertFalse(dummyWrapper.getParameterNames().hasMoreElements());
        assertNull(dummyWrapper.getParameterValues("paramkey"));
    }
    
    /**
     * Test the case where DummyParameterRequestWrapper is instantiated with
     * a parameter Map, such that its behavior is to hide the parameter
     * Map of the underlying request and to expose a new parameter map.
     */
    public void testParameters() {
        
        // build the Map of "virtual" parameters
        HashMap paramMap = new HashMap();
        paramMap.put("yesparamkey", new String[] {"yesparamvalue"});
        
        // build the backing Request
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addParameter("paramkey", "paramvalue");
        assertEquals("paramvalue", mockRequest.getParameter("paramkey"));
        
        // wrap the backing request
        DummyParameterRequestWrapper dummyWrapper = 
            new DummyParameterRequestWrapper(mockRequest, paramMap);
        
        // show that the Dummy wrapper hides the parameter of the underlying Request
        assertNull(dummyWrapper.getParameter("paramkey"));
        assertNull(dummyWrapper.getParameterValues("paramkey"));
        
        // show that the Dummy wrapper exposes the parameter in the Map of desired params
        assertEquals("yesparamvalue", dummyWrapper.getParameter("yesparamkey"));
        assertEquals(1, dummyWrapper.getParameterValues("yesparamkey").length);
        assertEquals("yesparamvalue", dummyWrapper.getParameterValues("yesparamkey")[0]);
        
        assertEquals(paramMap, dummyWrapper.getParameterMap());
        
    }
    
}
