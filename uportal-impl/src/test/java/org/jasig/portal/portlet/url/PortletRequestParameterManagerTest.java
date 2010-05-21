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

import java.util.HashMap;
import java.util.Map;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.easymock.EasyMock;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.PortalHttpServletRequestWrapper;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestParameterManagerTest {
	
	@Test
    public void testNoParameters() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request).times(3);
        
        EasyMock.replay(portalRequestUtils);
        
        parameterManager.setPortalRequestUtils(portalRequestUtils);
        parameterManager.setNoPortletRequest(request);
        
        final IPortletWindowId targetedPortletWindowId = parameterManager.getTargetedPortletWindowId(request);
        Assert.assertNull(targetedPortletWindowId);
        
        final PortletRequestInfo portletRequestInfo = parameterManager.getPortletRequestInfo(request);
        Assert.assertNull("portletRequestType should be null", portletRequestInfo);
        
        EasyMock.verify(portalRequestUtils);
    }
    
	@Test
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
        parameterManager.setRequestInfo(request, portletWindowId, new PortletRequestInfo(TYPE.RENDER));
        
        final IPortletWindowId targetedPortletWindowId = parameterManager.getTargetedPortletWindowId(request);
        Assert.assertEquals(portletWindowId, targetedPortletWindowId);
        
        final PortletRequestInfo portletRequestInfo = parameterManager.getPortletRequestInfo(request);
        Assert.assertEquals(new PortletRequestInfo(TYPE.RENDER), portletRequestInfo);
        
        EasyMock.verify(portalRequestUtils);
    }

	@Test
    public void testNoParsing() throws Exception {
        final PortletRequestParameterManager parameterManager = new PortletRequestParameterManager();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(PortalHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, request);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortalRequest(request)).andReturn(request).times(2);
        
        EasyMock.replay(portalRequestUtils);
        
        parameterManager.setPortalRequestUtils(portalRequestUtils);
        
        try {
            parameterManager.getTargetedPortletWindowId(request);
            Assert.fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getTargetedPortletWindowIds before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        try {
            parameterManager.getPortletRequestInfo(request);
            Assert.fail("A RequestParameterProcessingIncompleteException should have been thrown for calling getPortletRequestType before any set method");
        }
        catch (RequestParameterProcessingIncompleteException ise) {
            //expected
        }
        
        EasyMock.verify(portalRequestUtils);
    }
}
