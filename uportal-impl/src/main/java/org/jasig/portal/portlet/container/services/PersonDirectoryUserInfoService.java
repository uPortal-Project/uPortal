/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.optional.UserInfoService;
import org.jasig.portal.portlet.container.PortletContainerUtils;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Required;

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
    
    
    /**
     * @return the portletEntityRegistry
     */
    public IPortletEntityRegistry getPortletEntityRegistry() {
        return this.portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Required
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
    @Required
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
    @Required
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
    @Required
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.UserInfoService#getUserInfo(javax.portlet.PortletRequest)
     */
    @Deprecated
    public Map<String, String> getUserInfo(PortletRequest request) throws PortletContainerException {
        if (!(request instanceof InternalPortletRequest)) {
            throw new IllegalArgumentException("The PersonDirectoryUserInfoServices requires the PortletRequest parameter to implement the '" + InternalPortletRequest.class.getName() + "' interface.");
        }
        final InternalPortletRequest internalRequest = (InternalPortletRequest)request;
        final InternalPortletWindow internalPortletWindow = internalRequest.getInternalPortletWindow();

        return this.getUserInfo(request, internalPortletWindow);
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
        
        final HttpServletRequest httpServletRequest = PortletContainerUtils.getHttpServletRequest(request);
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
        final Map<String, Object> portalUserAttributes = (Map<String, Object>)this.personAttributeDao.getUserAttributes(remoteUser);
        if (portalUserAttributes == null) {
            return Collections.emptyMap();
        }
        final List<UserAttributeDD> expectedUserAttributes = this.getExpectedUserAttributes(httpServletRequest, portletWindow);
        
        final Map<String, String> portletUserAttributes = this.generateUserInfo(portalUserAttributes, expectedUserAttributes);
        return portletUserAttributes;
    }

    /**
     * Using the Map of portal user attributes and a List of expected attributes generate the USER_INFO map for the portlet
     * 
     * @param portalUserAttributes All the attributes the portal knows about the user
     * @param expectedUserAttributes The attributes the portlet expects to get
     * @return The Map to use for the USER_INFO attribute
     */
    protected Map<String, String> generateUserInfo(final Map<String, Object> portalUserAttributes, final List<UserAttributeDD> expectedUserAttributes) {
        final Map<String, String> portletUserAttributes = new HashMap<String, String>(expectedUserAttributes.size());
        
        //Copy expected attributes to the USER_INFO Map
        for (final UserAttributeDD userAttributeDD : expectedUserAttributes) {
            final String attributeName = userAttributeDD.getName();
            final Object valueObj = portalUserAttributes.get(attributeName);
            
            final String value = this.convertAttributeValue(valueObj);
            portletUserAttributes.put(attributeName, value);
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
            
            final String value = this.convertAttributeValue(valueObj);
            portletUserAttributes.put(attributeName, value);
        }
        
        return portletUserAttributes;
    }
    
    /**
     * Converts a person directory value object to a String. Checks if the value is a String and
     * does a cast, else if value is a List it uses the first value as a String, if the value type
     * is unknown toString is used.
     * 
     * TODO this seems like a utility that should come with PersonDirectory
     * 
     * @param valueObj The person directory value
     * @return The string version of the value
     */
    protected String convertAttributeValue(Object valueObj) {
        if (valueObj instanceof String) {
            return (String)valueObj;
        }
        else if (valueObj instanceof List) {
            final Object firstValue = ((List<?>)valueObj).get(1);
            
            if (firstValue instanceof String) {
                return (String)firstValue;
            }

            return firstValue + "";
        }
        else {
            return valueObj + "";
        }
    }

    /**
     * Get the list of user attributes the portlet expects
     * 
     * @param request The current request.
     * @param portletWindow The window to get the expected user attributes for.
     * @return The List of expected user attributes for the portlet
     * @throws PortletContainerException If expected attributes cannot be determined
     */
    protected List<UserAttributeDD> getExpectedUserAttributes(HttpServletRequest request, final IPortletWindow portletWindow) throws PortletContainerException {
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindow.getPortletWindowId());
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
        final PortletAppDD portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        
        return (List<UserAttributeDD>)portletApplicationDescriptor.getUserAttributes();
    }
}
