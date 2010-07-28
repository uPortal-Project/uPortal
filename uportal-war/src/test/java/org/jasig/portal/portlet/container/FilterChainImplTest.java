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

/**
 * 
 */
package org.jasig.portal.portlet.container;

import javax.portlet.Portlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.pluto.container.om.portlet.InitParam;
import org.apache.pluto.container.om.portlet.impl.FilterType;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class FilterChainImplTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmpty() throws Exception {
		RenderRequest mockRequest = EasyMock.createMock(RenderRequest.class);
		RenderResponse mockResponse = EasyMock.createMock(RenderResponse.class);
		
		Portlet mockPortlet = EasyMock.createMock(Portlet.class);
		mockPortlet.render(EasyMock.isA(RenderRequest.class), EasyMock.isA(RenderResponse.class));
		EasyMock.expectLastCall();
		
		EasyMock.replay(mockRequest, mockResponse, mockPortlet);
		
		FilterChainImpl chain = new FilterChainImpl("RENDER");
		chain.setPortlet(mockPortlet);
		chain.doFilter(mockRequest, mockResponse);
		
		EasyMock.verify(mockRequest, mockResponse, mockPortlet);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRenderFilter() throws Exception {
		FilterType filter = new FilterType();
		filter.setFilterClass("org.jasig.portal.portlet.container.MockRenderFilter");
		filter.setFilterName("filter-one");
		filter.addLifecycle("RENDER");
		InitParam p = filter.addInitParam("paramName");
		p.setParamValue("paramValue");
		
		RenderRequest mockRequest = new MockRenderRequest();
		RenderResponse mockResponse = new MockRenderResponse();
		
		FilterChainImpl chain = new FilterChainImpl("RENDER");
		chain.addFilter(filter);
		chain.doFilter(mockRequest, mockResponse);
		Assert.assertEquals("was here", mockRequest.getAttribute("MockRenderFilter doFilter"));
	}
	
}
