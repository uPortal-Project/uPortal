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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

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
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.cas.CasProxyTicketAcquisitionException;
import org.jasig.portal.security.provider.cas.ICasSecurityContext;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Requests a CAS Proxy ticket for the current portlet and adds it
 * to the Pluto UserInfoService.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class CasTicketUserInfoService implements UserInfoService  {
	
    private IUserInstanceManager userInstanceManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
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
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
	/**
	 * The default name of the preferences attribute used to pass the 
	 * PT to the portlet.
	 */
	private String proxyTicketKey = "casProxyTicket";

	/**
	 * @return the UserInstanceManager
	 */
	public IUserInstanceManager getUserInstanceManager() {
		return userInstanceManager;
	}
	/**
	 * @param userInstanceManager the UserInstanceManager
	 */
	@Autowired
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
    @Autowired
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
    
	/**
	 * @return name of the key to save the proxy ticket under
	 */
	public String getProxyTicketKey() {
		return proxyTicketKey;
	}
	/**
	 * @param proxyTicketKey name of the key to save the proxy ticket under
	 */
	public void setProxyTicketKey(String proxyTicketKey) {
		this.proxyTicketKey = proxyTicketKey;
	}

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.UserInfoService#getUserInfo(javax.portlet.PortletRequest, org.apache.pluto.container.PortletWindow)
     */
	public Map<String, String> getUserInfo(PortletRequest request, PortletWindow portletWindow)
			throws PortletContainerException {
		
		Map<String, String> userInfo = new HashMap<String, String>();

		// check to see if a CAS proxy ticket is expected by this portlet
		if (isCasProxyTicketRequested(request, portletWindow)) {

			// if it is, attempt to request a proxy ticket
			String proxyTicket = getProxyTicket(request);
			if (proxyTicket != null)
				userInfo.put(this.proxyTicketKey, proxyTicket);

		}
		return userInfo;
		
	}
	
	/**
	 * Determine whether the portlet has expects a CAS proxy ticket as one of the 
	 * user attributes.
	 * 
	 * @param request portlet request
	 * @param plutoPortletWindow portlet window
	 * @return <code>true</code> if a CAS proxy ticket is expected, <code>false</code>
	 *         otherwise
	 * @throws PortletContainerException if expeced attributes cannot be determined
	 */
	public boolean isCasProxyTicketRequested(PortletRequest request, PortletWindow plutoPortletWindow) throws PortletContainerException {

    	// get the list of requested user attributes
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final PortletApplicationDefinition portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        
        // check to see if the proxy ticket key is one of the requested user attributes
        List<? extends UserAttribute> requestedUserAttributes = portletApplicationDescriptor.getUserAttributes();
        for (final UserAttribute userAttributeDD : requestedUserAttributes) {
            final String attributeName = userAttributeDD.getName();
            if (attributeName.equals(this.proxyTicketKey))
            	return true;
        }

        // if the proxy ticket key wasn't found in the list of requested attributes
        return false;

    }
	
	/**
	 * Attempt to get a proxy ticket for the current portlet.
	 * 
	 * @param request portlet request
	 * @return a proxy ticket, or <code>null</code> if we were unsuccessful
	 */
	private String getProxyTicket(PortletRequest request) {

        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getPortletHttpRequest(request);

        // try to determine the URL for our portlet
		String targetService = null;
		try {
			URL url = null;
			
			// if the server port is 80 or 443, don't include it in the URL 
			int port = request.getServerPort();
			if (port == 80 || port == 443)
				url = new URL(request.getScheme(), request.getServerName(), request.getContextPath());
			else 
				url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
			targetService = url.toString();

		} catch (MalformedURLException e) {
			log.error("Failed to create a URL for the target portlet", e);
			e.printStackTrace();
			return null;
		}
		
		// get the CasSecurityContext
        final IUserInstance userInstance = userInstanceManager.getUserInstance(httpServletRequest);
        final IPerson person = userInstance.getPerson();
		final ISecurityContext context = person.getSecurityContext();
		if (context == null) {
			log.error("no security context, no proxy ticket passed to the portlet");
			return null;
		}
		ISecurityContext casContext = getCasContext(context);
		if (casContext == null) {
			log.debug("no CAS security context, no proxy ticket passed to the portlet");
			return null;
		}
		if (!casContext.isAuthenticated()) {
			log.debug("no CAS authentication, no proxy ticket passed to the portlet");
			return null;
		}
		
		// get a proxy ticket for our portlet from the CasSecurityContext
		String proxyTicket = null;
		try {
			proxyTicket = ((ICasSecurityContext) casContext)
					.getCasServiceToken(targetService);
			log.debug("Put proxy ticket in userinfo: " + proxyTicket);
		} catch (CasProxyTicketAcquisitionException e) {
			log.error("no proxy ticket passed to the portlet: " + e);
		}

        return proxyTicket;
	}

	/**
	 * Looks for a security context
	 * @param context the principal security context
	 * @return the CAS security contex, or null if not found.
	 */
	@SuppressWarnings("unchecked")
	private static ISecurityContext getCasContext(ISecurityContext context) {
		if (context instanceof ICasSecurityContext) {
			return context;
		}
		Enumeration contextEnum = context.getSubContexts();
		while (contextEnum.hasMoreElements()) {
			ISecurityContext subContext = (ISecurityContext) contextEnum.nextElement();
			if (subContext instanceof ICasSecurityContext) {
				return subContext;
			}
		}
		return null;
	}	
}
