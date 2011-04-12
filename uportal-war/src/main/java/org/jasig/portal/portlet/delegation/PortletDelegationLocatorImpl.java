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

package org.jasig.portal.portlet.delegation;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletDelegationLocatorImpl implements PortletDelegationLocator, IPortletDelegationManager {
    private static final String DELEGATE_PARENT_PORTLET_URL_PREFIX = "DELEGATE_PARENT_PORTLET_URL_";
    private static final String DELEGATE_PORTLET_ACTION_REDIRECT_URL = "DELEGATE_PORTLET_ACTION_REDIRECT_URL";

    
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IPersonManager personManager;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletRenderer portletRenderer;
    private IPortalUrlProvider portalUrlProvider;
    
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    @Autowired
    public void setPortletRenderer(IPortletRenderer portletRenderer) {
        this.portletRenderer = portletRenderer;
    }
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationLocator#createRequestDispatcher(java.lang.String)
     */
    @Override
    public PortletDelegationDispatcher createRequestDispatcher(PortletRequest portletRequest, String fName) {
    	final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinitionByFname(fName);
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        
        return this.createRequestDispatcher(portletRequest, portletDefinitionId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationLocator#createRequestDispatcher(org.jasig.portal.portlet.om.IPortletDefinitionId)
     */
    @Override
    public PortletDelegationDispatcher createRequestDispatcher(PortletRequest portletRequest, IPortletDefinitionId portletDefinitionId) {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        final HttpServletResponse response = this.portalRequestUtils.getOriginalPortalResponse(portletRequest);
        final IPerson person = this.personManager.getPerson(request);
        
        final String transientChannelSubscribeId = "CONFIG_" + portletDefinitionId;
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinitionId, transientChannelSubscribeId, person.getID());
        
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        
        
        IPortletWindowId portletWindowId = this.portletWindowRegistry.getDefaultPortletWindowId(portletEntityId);
        IPortletWindow internalPortletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);

        final IPortletWindow parentPortletWindow = this.portletWindowRegistry.convertPortletWindow(request, internalPortletWindow);
        final IPortletWindowId parentPortletWindowId = parentPortletWindow.getPortletWindowId();
        final IPortletWindow portletWindow = this.portletWindowRegistry.createDelegatePortletWindow(request, portletEntityId, parentPortletWindowId);

        //Initialize the window since we just created it
        /*final ContainerInvocation invocation = ContainerInvocation.getInvocation();
        try {*/
            //this.portletRenderer.doInit(portletEntity, portletWindow.getPortletWindowId(), request, response);
        this.portletRenderer.doInit(portletEntity, portletWindowId, request, response);
        /*}
        finally {
            if (invocation != null) {
                ContainerInvocation.setInvocation(invocation.getPortletContainer(), invocation.getPortletWindow());
            }
        }*/
        
        return new PortletDelegationDispatcherImpl(portletWindow, parentPortletWindow, person.getID(), this.portalRequestUtils, this.personManager, this.portletRenderer, this.portalUrlProvider, this);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationLocator#getRequestDispatcher(org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public PortletDelegationDispatcher getRequestDispatcher(PortletRequest portletRequest, IPortletWindowId portletWindowId) {
        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        final IPerson person = this.personManager.getPerson(request);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        
        final IPortletWindowId delegationParentId = portletWindow.getDelegationParent();
        if (delegationParentId == null) {
            throw new IllegalArgumentException("Portlet window '" + portletWindow + "' is not a delegate window and cannot be delgated to.");
        }
        
        final IPortletWindow parentPortletWindow = this.portletWindowRegistry.getPortletWindow(request, delegationParentId);
        
        return new PortletDelegationDispatcherImpl(portletWindow, parentPortletWindow, person.getID(), this.portalRequestUtils, this.personManager, this.portletRenderer, this.portalUrlProvider, this);
    }

    @Override
    public void setParentPortletUrl(HttpServletRequest request, IPortalUrlBuilder parentPortletUrl) {
        //TODO this doesn't work so well if a parent wants to delegate to multiple portlets in a single request
        final IPortletWindowId parentPortletWindowId = parentPortletUrl.getTargetPortletWindowId();
        request.setAttribute(DELEGATE_PARENT_PORTLET_URL_PREFIX + parentPortletWindowId.getStringId(), parentPortletUrl);
    }

    @Override
    public IPortalUrlBuilder getParentPortletUrl(HttpServletRequest request, IPortletWindowId parentPortletWindowId) {
        return (IPortalUrlBuilder)request.getAttribute(DELEGATE_PARENT_PORTLET_URL_PREFIX + parentPortletWindowId.getStringId());
    }

    @Override
    public void setDelegatePortletActionRedirectUrl(HttpServletRequest request, IPortalUrlBuilder portletUrl) {
//        final HttpServletRequest portletAdaptorParentRequest = this.portalRequestUtils.getPortletAdaptorParentRequest(request);
//        portletAdaptorParentRequest.setAttribute(DELEGATE_PORTLET_ACTION_REDIRECT_URL, portletUrl);
    }

    @Override
    public IPortalUrlBuilder getDelegatePortletActionRedirectUrl(HttpServletRequest request) {
//        request = this.portalRequestUtils.getOriginalPortletAdaptorRequest(request);
//        return (IPortalUrlBuilder)request.getAttribute(DELEGATE_PORTLET_ACTION_REDIRECT_URL);
        throw new UnsupportedOperationException();
    }

    @Override
    public IPortalUrlBuilder getDelegatePortletActionRedirectUrl(PortletRequest portletRequest) {
//        final HttpServletRequest request = this.portalRequestUtils.getOriginalPortletAdaptorRequest(portletRequest);
//        return (IPortalUrlBuilder)request.getAttribute(DELEGATE_PORTLET_ACTION_REDIRECT_URL);
        throw new UnsupportedOperationException();
    }
}
