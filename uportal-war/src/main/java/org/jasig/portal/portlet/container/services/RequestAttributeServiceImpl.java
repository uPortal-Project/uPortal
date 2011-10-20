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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.UserAttribute;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link RequestAttributeService} that can construct 
 * the multi-valued user attribute map that corresponds to the attribute with the name
 * {@link IPortletRenderer#MULTIVALUED_USERINFO_MAP_ATTRIBUTE}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class RequestAttributeServiceImpl implements RequestAttributeService  {

	private IPersonAttributeDao personAttributeDao;
	private IPortletWindowRegistry portletWindowRegistry;
	private IPortletDefinitionRegistry portletDefinitionRegistry;

	@Autowired
	public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
		this.personAttributeDao = personAttributeDao;
	}
	@Autowired
	public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
		this.portletWindowRegistry = portletWindowRegistry;
	}
	@Autowired
	public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.container.services.RequestAttributeService#getAttribute(javax.portlet.PortletRequest, org.apache.pluto.container.PortletWindow, java.lang.String)
	 */
	@Override
	public Object getAttribute(HttpServletRequest httpServletRequest, PortletWindow plutoPortletWindow, String attributeName) {
		if (IPortletRenderer.MULTIVALUED_USERINFO_MAP_ATTRIBUTE.equals(attributeName)) {

			//Get the list of user attributes the portal knows about the user
			final String remoteUser = httpServletRequest.getRemoteUser();
			if (remoteUser == null) {
				return null;
			}

			final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
			final List<? extends UserAttribute> expectedUserAttributes = this.getExpectedUserAttributes(httpServletRequest, portletWindow);

			return getMultiValuedUserInfoMap(remoteUser, expectedUserAttributes);
		}

		return null;
	}

	/**
	 * 
	 * @param remoteUser
	 * @param expectedUserAttributes
	 * @return an unmodifiable map containing the multi-valued attributes for the user within the list of expected attributes
	 */
	protected Map<String, List<Object>> getMultiValuedUserInfoMap(String remoteUser, List<? extends UserAttribute> expectedUserAttributes) {
		final IPersonAttributes personAttributes = this.personAttributeDao.getPerson(remoteUser);
		if (personAttributes == null) {
			return Collections.emptyMap();
		}

		final Map<String, List<Object>> resultUserInfoMap = new HashMap<String, List<Object>>(expectedUserAttributes.size());

		//Copy expected attributes to the USER_INFO Map
		final Map<String, List<Object>> attributes = personAttributes.getAttributes();
		for (final UserAttribute userAttributeDD : expectedUserAttributes) {
			final String userAttributeName = userAttributeDD.getName();

			//containsKey check to handle attributes with a single null value
			if (attributes.containsKey(userAttributeName)) {
				final List<Object> list_valueObjs = attributes.get(userAttributeName);
				resultUserInfoMap.put(userAttributeName, list_valueObjs);            
			}
		}

		return Collections.unmodifiableMap(resultUserInfoMap);
	}
	/**
	 * Get the list of user attributes the portlet expects.
	 * 
	 * @param request The current request.
	 * @param portletWindow The window to get the expected user attributes for.
	 * @return The List of expected user attributes for the portlet
	 */
	protected List<? extends UserAttribute> getExpectedUserAttributes(HttpServletRequest request, final IPortletWindow portletWindow) {
		final IPortletEntity portletEntity = portletWindow.getPortletEntity();
		final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
		final PortletApplicationDefinition portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());

		return portletApplicationDescriptor.getUserAttributes();
	}
}
