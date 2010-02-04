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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.core.DefaultRequestAttributeService;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestAttributeServiceImpl extends DefaultRequestAttributeService {
    private IPersonAttributeDao personAttributeDao;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    @Override
    public Object getAttribute(PortletRequest portletRequest, HttpServletRequest httpServletRequest, PortletWindow plutoPortletWindow, String name) {
        if (IPortletAdaptor.MULTIVALUED_USERINFO_MAP_ATTRIBUTE.equals(name)) {
            httpServletRequest = this.portalRequestUtils.getOriginalPortletAdaptorRequest(portletRequest);
            
            //Get the list of user attributes the portal knows about the user
            final String remoteUser = portletRequest.getRemoteUser();
            if (remoteUser == null) {
                return null;
            }
            
            final IPersonAttributes personAttributes = this.personAttributeDao.getPerson(remoteUser);
            if (personAttributes == null) {
                return Collections.emptyMap();
            }
            
            final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
            final List<UserAttributeDD> expectedUserAttributes = this.getExpectedUserAttributes(httpServletRequest, portletWindow);
            
            final Map<String, List<Object>> portletMultivaluedAttributes = new HashMap<String, List<Object>>(expectedUserAttributes.size());
            
            //Copy expected attributes to the USER_INFO Map
            final Map<String, List<Object>> attributes = personAttributes.getAttributes();
            for (final UserAttributeDD userAttributeDD : expectedUserAttributes) {
                final String attributeName = userAttributeDD.getName();

                //containsKey check to handle attributes with a single null value
                if (attributes.containsKey(attributeName)) {
                    final List<Object> list_valueObjs = attributes.get(attributeName);
                    portletMultivaluedAttributes.put(attributeName, list_valueObjs);            
                }
            }
            
            return Collections.unmodifiableMap(portletMultivaluedAttributes);
        }

        return super.getAttribute(portletRequest, httpServletRequest, plutoPortletWindow, name);
    }

    @Override
    public Enumeration getAttributeNames(PortletRequest portletRequest, HttpServletRequest httpServletRequest, PortletWindow portletWindow) {
        final Enumeration<String> attributeNamesEnum = super.getAttributeNames(portletRequest, httpServletRequest, portletWindow);
        
        final String remoteUser = portletRequest.getRemoteUser();
        if (remoteUser == null) {
            return attributeNamesEnum;
        }
        
        final List<String> attributeNames = EnumerationUtils.toList(attributeNamesEnum);
        attributeNames.add(IPortletAdaptor.MULTIVALUED_USERINFO_MAP_ATTRIBUTE);
        return new IteratorEnumeration(attributeNames.iterator());
    }

    /**
     * Get the list of user attributes the portlet expects
     * 
     * @param request The current request.
     * @param portletWindow The window to get the expected user attributes for.
     * @return The List of expected user attributes for the portlet
     * @throws PortletContainerException If expected attributes cannot be determined
     */
    @SuppressWarnings("unchecked")
    protected List<UserAttributeDD> getExpectedUserAttributes(HttpServletRequest request, final IPortletWindow portletWindow) {
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindow.getPortletWindowId());
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
        final PortletAppDD portletApplicationDescriptor;
        try {
            portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        }
        catch (PortletContainerException e) {
            throw new RuntimeException(e);
        }
        
        return portletApplicationDescriptor.getUserAttributes();
    }
}
