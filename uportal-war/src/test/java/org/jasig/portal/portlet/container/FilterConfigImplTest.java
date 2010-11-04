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

package org.jasig.portal.portlet.container;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletContext;

import org.apache.pluto.container.om.portlet.impl.InitParamType;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
public class FilterConfigImplTest {

	/**
	 * 
	 */
	@Test
	public void testControl() {
		final List<InitParamType> initParams = new ArrayList<InitParamType>();
		final String filterName = "controlFilter";
		PortletContext portletContext = EasyMock.createMock(PortletContext.class);
		EasyMock.replay(portletContext);
		FilterConfigImpl impl = new FilterConfigImpl(filterName, initParams, portletContext);
		
		Assert.assertEquals(filterName, impl.getFilterName());
		Assert.assertNull(impl.getInitParameter("someparam"));
		Assert.assertNull(impl.getInitParameter("someparam2"));
		Assert.assertNotNull(impl.getPortletContext());
	}
	
	/**
	 * 
	 */
	@Test
	public void testParams() {
		final List<InitParamType> initParams = new ArrayList<InitParamType>();
		InitParamType p1 = new InitParamType();
		p1.setParamName("param1");
		p1.setParamValue("value1");
		InitParamType p2 = new InitParamType();
		p2.setParamName("param2");
		p2.setParamValue("value2");
		initParams.add(p1);
		initParams.add(p2);
		
		final String filterName = "filterWithParams";
		PortletContext portletContext = EasyMock.createMock(PortletContext.class);
		EasyMock.replay(portletContext);
		FilterConfigImpl impl = new FilterConfigImpl(filterName, initParams, portletContext);
		
		Assert.assertEquals(filterName, impl.getFilterName());
		Assert.assertEquals("value1", impl.getInitParameter("param1"));
		Assert.assertEquals("value2", impl.getInitParameter("param2"));
		Assert.assertNotNull(impl.getPortletContext());
	}
}
