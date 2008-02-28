/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.cas.CasProxyTicketAcquisitionException;
import org.jasig.portal.security.provider.cas.CasSecurityContext;
import org.jasig.portal.security.provider.cas.ICasSecurityContext;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Required;

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
	protected final Log log = LogFactory.getLog(getClass());

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
	@Required
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
    @Required
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
	

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.UserInfoService#getUserInfo(javax.portlet.PortletRequest)
     */
    @Deprecated
	public Map getUserInfo(PortletRequest request)
			throws PortletContainerException {
        if (!(request instanceof InternalPortletRequest)) {
            throw new IllegalArgumentException("The CasTicketUserInfoServices requires the PortletRequest parameter to implement the '" + InternalPortletRequest.class.getName() + "' interface.");
        }
        final InternalPortletRequest internalRequest = (InternalPortletRequest)request;
        final InternalPortletWindow internalPortletWindow = internalRequest.getInternalPortletWindow();

        return this.getUserInfo(request, internalPortletWindow);
	}

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.UserInfoService#getUserInfo(javax.portlet.PortletRequest, org.apache.pluto.PortletWindow)
     */
	public Map getUserInfo(PortletRequest request, PortletWindow portletWindow)
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
    @SuppressWarnings("unchecked")
	public boolean isCasProxyTicketRequested(PortletRequest request, PortletWindow plutoPortletWindow) throws PortletContainerException {

    	// get the list of requested user attributes
        final HttpServletRequest httpServletRequest = PortletContainerUtils.getOriginalPortletAdaptorRequest(request);
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(httpServletRequest, portletWindow.getPortletWindowId());
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
        final PortletAppDD portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        
        // check to see if the proxy ticket key is one of the requested user attributes
        List<UserAttributeDD> requestedUserAttributes = portletApplicationDescriptor.getUserAttributes();
        for (final UserAttributeDD userAttributeDD : requestedUserAttributes) {
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

        final HttpServletRequest httpServletRequest = PortletContainerUtils.getOriginalPortletAdaptorRequest(request);

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
	 * @param rootContext the principal security context
	 * @return the CAS security contex, or null if not found.
	 */
	@SuppressWarnings("unchecked")
	private static ISecurityContext getCasContext(ISecurityContext context) {
		if (context instanceof CasSecurityContext) {
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
