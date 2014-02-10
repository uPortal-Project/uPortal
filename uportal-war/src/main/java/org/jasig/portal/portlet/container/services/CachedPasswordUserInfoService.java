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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.IStringEncryptionService;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;

public class CachedPasswordUserInfoService implements UserInfoService  {
	
    private IUserInstanceManager userInstanceManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IStringEncryptionService stringEncryptionService;
    protected final Log log = LogFactory.getLog(getClass());
    
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired(required=true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }
    
	/**
	 * The default name of the preferences attribute used to pass the 
	 * PT to the portlet.
	 */
	private String passwordKey = "password";

	/**
	 * @return the UserInstanceManager
	 */
	public IUserInstanceManager getUserInstanceManager() {
		return userInstanceManager;
	}
	/**
	 * @param userInstanceManager the UserInstanceManager
	 */
    @Autowired(required=true)
	public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
		this.userInstanceManager = userInstanceManager;
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
    @Autowired(required=true)
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
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
    @Autowired(required=true)
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
    @Autowired(required=true)
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
	public void setStringEncryptionService(IStringEncryptionService stringEncryptionService) {
		this.stringEncryptionService = stringEncryptionService;
	}
	
	private boolean decryptPassword = false;
	
	/**
	 * Set whether the password should be decrypted before adding it to the 
	 * user info map.
	 * 
	 * @param decryptPassword
	 */
	public void setDecryptPassword(boolean decryptPassword) {
		this.decryptPassword = decryptPassword;
	}

	/**
	 * @return name of the key to save the password under
	 */
	public String getPasswordKey() {
		return passwordKey;
	}
	/**
	 * @param passwordKey name of the key to save the password under
	 */
	public void setPasswordKey(String passwordKey) {
		this.passwordKey = passwordKey;
	}

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.UserInfoService#getUserInfo(javax.portlet.PortletRequest, org.apache.pluto.container.PortletWindow)
     */
	@Override
	public Map<String, String> getUserInfo(PortletRequest request, PortletWindow portletWindow)
			throws PortletContainerException {
		
		Map<String, String> userInfo = new HashMap<String, String>();

		// check to see if a password is expected by this portlet
		if (isPasswordRequested(request, portletWindow)) {

	        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
	        final IUserInstance userInstance = userInstanceManager.getUserInstance(httpServletRequest);
	        final IPerson person = userInstance.getPerson();
			final ISecurityContext context = person.getSecurityContext();

			// if it is, attempt to request a password
			String password = getPassword(context);
			if (this.decryptPassword && password != null) {
				password = stringEncryptionService.decrypt(password);
			}
			if (password != null)
				userInfo.put(this.passwordKey, password);

		}
		return userInfo;
		
	}
	
	/**
	 * Determine whether the portlet has expects a password as one of the
	 * user attributes.
	 * 
	 * @param request portlet request
	 * @param plutoPortletWindow portlet window
	 * @return <code>true</code> if a password is expected, <code>false</code>
	 *         otherwise
	 * @throws PortletContainerException if expeced attributes cannot be determined
	 */
	public boolean isPasswordRequested(PortletRequest request, PortletWindow plutoPortletWindow) throws PortletContainerException {

    	// get the list of requested user attributes
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletApplicationDefinition portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        
        // check to see if the password key is one of the requested user attributes
        List<? extends UserAttribute> requestedUserAttributes = portletApplicationDescriptor.getUserAttributes();
        for (final UserAttribute userAttributeDD : requestedUserAttributes) {
            final String attributeName = userAttributeDD.getName();
            if (attributeName.equals(this.passwordKey))
            	return true;
        }

        // if the password key wasn't found in the list of requested attributes
        return false;

    }
	
    /**
     * Retrieves the users password by iterating over
     * the user's security contexts and returning the first
     * available cached password.
     *
     * @param baseContext The security context to start looking for a password from.
     * @return the users password
     */
	private String getPassword(ISecurityContext baseContext) {
        String password = null;
        IOpaqueCredentials oc = baseContext.getOpaqueCredentials();

        if (oc instanceof NotSoOpaqueCredentials) {
            NotSoOpaqueCredentials nsoc = (NotSoOpaqueCredentials)oc;
            password = nsoc.getCredentials();
        }

        // If still no password, loop through subcontexts to find cached credentials
        Enumeration en = baseContext.getSubContexts();
        while (password == null && en.hasMoreElements()) {
            ISecurityContext subContext = (ISecurityContext)en.nextElement();
            password = this.getPassword(subContext);
        }

        return password;
    }

}
