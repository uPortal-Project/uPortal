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
import org.jasig.portal.url.IPortalUrlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletDelegationLocatorImpl implements PortletDelegationLocator {
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IPersonManager personManager;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletRenderer portletRenderer;
    private IPortalUrlProvider portalUrlProvider;
    private IPortletDelegationManager portletDelegationManager;

    @Autowired
    public void setPortletDelegationManager(IPortletDelegationManager portletDelegationManager) {
        this.portletDelegationManager = portletDelegationManager;
    }
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
    public PortletDelegationDispatcher createRequestDispatcher(PortletRequest portletRequest, IPortletDefinitionId delegatePortletDefinitionId) {
        final HttpServletRequest request = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        
        final String windowID = portletRequest.getWindowID();
        final IPortletWindowId parentPortletWindowId = this.portletWindowRegistry.getPortletWindowId(request, windowID);
        
        final IPortletEntity delegatePortletEntity = this.portletEntityRegistry.getOrCreateDelegatePortletEntity(request, parentPortletWindowId, delegatePortletDefinitionId);
        final IPortletEntityId delegatePortletEntityId = delegatePortletEntity.getPortletEntityId();
        
        final IPortletWindow delegatePortletWindow = this.portletWindowRegistry.createDelegatePortletWindow(request, delegatePortletEntityId, parentPortletWindowId);
        
        final IPerson person = this.personManager.getPerson(request);
        final int userId = person.getID();
        
        return new PortletDelegationDispatcherImpl(delegatePortletWindow, userId, portalRequestUtils, personManager, portletRenderer, portalUrlProvider, portletDelegationManager);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.api.portlet.PortletDelegationLocator#getRequestDispatcher(org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public PortletDelegationDispatcher getRequestDispatcher(PortletRequest portletRequest, IPortletWindowId portletWindowId) {
        final HttpServletRequest request = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final IPerson person = this.personManager.getPerson(request);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        
        final IPortletWindowId delegationParentId = portletWindow.getDelegationParentId();
        if (delegationParentId == null) {
            throw new IllegalArgumentException("Portlet window '" + portletWindow + "' is not a delegate window and cannot be delgated to.");
        }
        
        return new PortletDelegationDispatcherImpl(portletWindow, person.getID(), this.portalRequestUtils, this.personManager, this.portletRenderer, this.portalUrlProvider, this.portletDelegationManager);
    }
}
