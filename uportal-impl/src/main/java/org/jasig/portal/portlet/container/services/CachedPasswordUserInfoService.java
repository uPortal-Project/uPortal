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
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.optional.UserInfoService;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IOpaqueCredentials;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.provider.NotSoOpaqueCredentials;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Required;

public class CachedPasswordUserInfoService implements UserInfoService  {
	
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
    @Required
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
	 * @return name of the key to save the password under
	 */
	public String getassowrdKey() {
		return passwordKey;
	}
	/**
	 * @param proxyTicketKey name of the key to save the password under
	 */
	public void setPasswordKey(String passwordKey) {
		this.passwordKey = passwordKey;
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
		if (isPasswordRequested(request, portletWindow)) {

	        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortletAdaptorRequest(request);
	        final IUserInstance userInstance = userInstanceManager.getUserInstance(httpServletRequest);
	        final IPerson person = userInstance.getPerson();
			final ISecurityContext context = person.getSecurityContext();

			// if it is, attempt to request a proxy ticket
			String password = getPassword(context);
			if (password != null)
				userInfo.put(this.passwordKey, password);

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
	public boolean isPasswordRequested(PortletRequest request, PortletWindow plutoPortletWindow) throws PortletContainerException {

    	// get the list of requested user attributes
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortletAdaptorRequest(request);
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(httpServletRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(httpServletRequest, portletWindow.getPortletWindowId());
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletEntity.getPortletEntityId());
        final PortletAppDD portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinition.getPortletDefinitionId());
        
        // check to see if the proxy ticket key is one of the requested user attributes
        List<UserAttributeDD> requestedUserAttributes = portletApplicationDescriptor.getUserAttributes();
        for (final UserAttributeDD userAttributeDD : requestedUserAttributes) {
            final String attributeName = userAttributeDD.getName();
            if (attributeName.equals(this.passwordKey))
            	return true;
        }

        // if the proxy ticket key wasn't found in the list of requested attributes
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
