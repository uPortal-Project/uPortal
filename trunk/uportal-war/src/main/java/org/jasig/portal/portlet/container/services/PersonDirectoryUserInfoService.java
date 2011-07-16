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

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.UserInfoService;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.UserAttribute;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Ties the IPersonAttributeDao to the Pluto UserInfoService
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonDirectoryUserInfoService implements UserInfoService {
    private IPersonAttributeDao personAttributeDao;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    /**
     * @return the portletEntityRegistry
     */
    public IPortletEntityRegistry getPortletEntityRegistry() {
        return this.portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }
    /**
     * @return the personAttributeDao
     */
    public IPersonAttributeDao getPersonAttributeDao() {
        return this.personAttributeDao;
    }
    /**
     * @param personAttributeDao the personAttributeDao to set
     */
    @Autowired
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return this.portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return this.portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.UserInfoService#getUserInfo(javax.portlet.PortletRequest, org.apache.pluto.PortletWindow)
     */
    public Map<String, String> getUserInfo(PortletRequest request, PortletWindow plutoPortletWindow) throws PortletContainerException {
        //Get the remote user
        final String remoteUser = request.getRemoteUser();
        if (remoteUser == null) {
            return null;
        }
        
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        
        return this.getUserInfo(remoteUser, httpServletRequest, portletWindow);
    }
    
    /**
     * Commons logic to get a subset of the user's attributes for the specified portlet window.
     * 
     * @param remoteUser The user to get attributes for.
     * @param httpServletRequest The current, underlying httpServletRequest
     * @param portletWindow The window to filter attributes for
     * @return A Map of user attributes for the user and windows
     * @throws PortletContainerException
     */
    protected Map<String, String> getUserInfo(String remoteUser, HttpServletRequest httpServletRequest, IPortletWindow portletWindow) throws PortletContainerException {
        //Get the list of user attributes the portal knows about the user
        final IPersonAttributes personAttributes = this.personAttributeDao.getPerson(remoteUser);
        if (personAttributes == null) {
            return Collections.emptyMap();
        }
        final List<? extends UserAttribute> expectedUserAttributes = this.getExpectedUserAttributes(httpServletRequest, portletWindow);
        
        final Map<String, String> portletUserAttributes = this.generateUserInfo(personAttributes, expectedUserAttributes, httpServletRequest);
        return portletUserAttributes;
    }

    /**
     * Using the Map of portal user attributes and a List of expected attributes generate the USER_INFO map for the portlet
     * 
     * @param portalUserAttributes All the attributes the portal knows about the user
     * @param expectedUserAttributes The attributes the portlet expects to get
     * @return The Map to use for the USER_INFO attribute
     */
    protected Map<String, String> generateUserInfo(final IPersonAttributes personAttributes, final List<? extends UserAttribute> expectedUserAttributes,HttpServletRequest httpServletRequest) {
        final Map<String, String> portletUserAttributes = new HashMap<String, String>(expectedUserAttributes.size());
        
        //Copy expected attributes to the USER_INFO Map
        final Map<String, List<Object>> attributes = personAttributes.getAttributes();
        for (final UserAttribute userAttributeDD : expectedUserAttributes) {
            final String attributeName = userAttributeDD.getName();
            
            //TODO a personAttributes.hasAttribute(String) API is needed here, if hasAttribute and null then put the key with no value in the returned map
            if (attributes.containsKey(attributeName)) {
                final Object valueObj = personAttributes.getAttributeValue(attributeName);
				final String value = valueObj == null ? null : String.valueOf(valueObj);
				portletUserAttributes.put(attributeName, value);				
            }
        }
		
        return portletUserAttributes;
    }
    
    /**
     * Converts the full portal user attribute Map to a USER_INFO map for the portlet
     * 
     * @param portalUserAttributes All the attributes the portal knows about the user
     * @return The Map to use for the USER_INFO attribute
     */
    protected Map<String, String> generateUserInfo(final Map<String, Object> portalUserAttributes) {
        final Map<String, String> portletUserAttributes = new HashMap<String, String>(portalUserAttributes.size());
        
        //Copy expected attributes to the USER_INFO Map
        for (final Map.Entry<String, Object> portalUserAttributeEntry : portalUserAttributes.entrySet()) {
            final String attributeName = portalUserAttributeEntry.getKey();
            final Object valueObj = portalUserAttributeEntry.getValue();
            
            final String value = String.valueOf(valueObj);
            portletUserAttributes.put(attributeName, value);
        }
        
        return portletUserAttributes;
    }

    /**
     * Get the list of user attributes the portlet expects
     * 
     * @param request The current request.
     * @param portletWindow The window to get the expected user attributes for.
     * @return The List of expected user attributes for the portlet
     * @throws PortletContainerException If expected attributes cannot be determined
     */
    protected List<? extends UserAttribute> getExpectedUserAttributes(HttpServletRequest request, final IPortletWindow portletWindow) throws PortletContainerException {
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletApplicationDefinition portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        
        return portletApplicationDescriptor.getUserAttributes();
    }
}
