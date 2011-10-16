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
package org.jasig.portal.portlet.container.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.om.portlet.impl.PortletAppType;
import org.apache.pluto.container.om.portlet.impl.UserAttributeType;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class RequestAttributeServiceImplTest {

	@Test
	public void testNull() {
		MockHttpServletRequest httpServletRequest  = new MockHttpServletRequest();
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		
		RequestAttributeServiceImpl service = new RequestAttributeServiceImpl();
		Assert.assertNull(service.getAttribute(httpServletRequest, plutoPortletWindow, null));
		Assert.assertNull(service.getAttribute(httpServletRequest, plutoPortletWindow, "someotherattribute"));
	}
	
	/**
	 * Default test for function, returns the multivalued attribute map with one multi-valued attribute.
	 */
	@Test
	public void testControl() {
		MockHttpServletRequest httpServletRequest  = new MockHttpServletRequest();
		httpServletRequest.setRemoteUser("username");
		
		Map<String, List<Object>> attributes = new HashMap<String, List<Object>>();
		attributes.put("attribute1", Arrays.asList(new Object[] { "value1", "value2", "value3" }));
		NamedPersonImpl personAttributes = new NamedPersonImpl("username", attributes);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		IPortletEntity portletEntity = mock(IPortletEntity.class);
		when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
		IPortletDefinition portletDefinition = mock(IPortletDefinition.class);
		when(portletEntity.getPortletDefinition()).thenReturn(portletDefinition);
		IPortletDefinitionId portletDefinitionId = mock(IPortletDefinitionId.class);
		when(portletDefinition.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		IPersonAttributeDao personAttributeDao = mock(IPersonAttributeDao.class);
		when(personAttributeDao.getPerson("username")).thenReturn(personAttributes);
	
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		when(portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow)).thenReturn(portletWindow);
		
		List<UserAttributeType> userAttributesList = new ArrayList<UserAttributeType>();
		UserAttributeType userAttribute = new UserAttributeType();
		userAttribute.setName("attribute1");
		userAttributesList.add(userAttribute);
		PortletAppType portletApplicationDefinition = new PortletAppType();
		portletApplicationDefinition.addUserAttribute("attribute1");
		IPortletDefinitionRegistry portletDefinitionRegistry = mock(IPortletDefinitionRegistry.class);
		when(portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinitionId)).thenReturn(portletApplicationDefinition);
		
		RequestAttributeServiceImpl service = new RequestAttributeServiceImpl();
		service.setPersonAttributeDao(personAttributeDao);
		service.setPortletDefinitionRegistry(portletDefinitionRegistry);
		service.setPortletWindowRegistry(portletWindowRegistry);
		
		Object attribute = service.getAttribute(httpServletRequest, plutoPortletWindow, IPortletRenderer.MULTIVALUED_USERINFO_MAP_ATTRIBUTE);
		Assert.assertNotNull(attribute);
		Assert.assertTrue(attribute instanceof Map);
		
		@SuppressWarnings("unchecked")
		Map<String, List<Object>> attributeMap = (Map<String, List<Object>>) attribute;
		List<Object> values = attributeMap.get("attribute1");
		Assert.assertEquals(3, values.size());
		Assert.assertTrue(values.contains("value1"));
		Assert.assertTrue(values.contains("value2"));
		Assert.assertTrue(values.contains("value3"));
	}
		
}
