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

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Creates {@link PortletPreferences} objects
 * 
 * @author Eric Dalquist
 */
@Service
public class PortletPreferencesFactoryImpl implements PortletPreferencesFactory {
    private IPersonManager personManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private TransactionOperations transactionOperations;
    
    private boolean storeGuestPreferencesInMemory = true;
    
    @Autowired    
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired    
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired    
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    @Autowired    
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired    
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Value("${org.jasig.portal.portlet.container.services.PortletPreferencesFactoryImpl.storeGuestPreferencesInMemory:true}")
    public void setStoreGuestPreferencesInMemory(boolean storeGuestPreferencesInMemory) {
        this.storeGuestPreferencesInMemory = storeGuestPreferencesInMemory;
    }

    @Override
    public PortletPreferences createPortletPreferences(final PortletRequestContext requestContext, boolean render) {
        final HttpServletRequest containerRequest = requestContext.getContainerRequest();
        final PortletWindow plutoPortletWindow = requestContext.getPortletWindow();
        final IPortletWindow portletWindow = portletWindowRegistry.convertPortletWindow(containerRequest, plutoPortletWindow);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        
        final boolean configMode = IPortletRenderer.CONFIG.equals(portletWindow.getPortletMode());
        if (configMode) {
            final IPortletDefinitionId portletDefinitionId = portletEntity.getPortletDefinitionId();
            return new PortletDefinitionPreferencesImpl(portletDefinitionRegistry, transactionOperations, portletDefinitionId, render);
        }
        else if (this.isStoreInMemory(containerRequest)) {
            final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
            return new GuestPortletEntityPreferencesImpl(requestContext, portletEntityRegistry, portletDefinitionRegistry, portletEntityId, render);
        }
        else {
            final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
            return new PortletEntityPreferencesImpl(requestContext, portletEntityRegistry, portletDefinitionRegistry, transactionOperations, portletEntityId, render);
        }
    }
    
    protected boolean isStoreInMemory(HttpServletRequest containerRequest) { 
        if (this.storeGuestPreferencesInMemory && isGuestUser(containerRequest)){
            return true;
        }

        return false; 
    }
    
    protected boolean isGuestUser(HttpServletRequest containerRequest) {
        //Checking for isAuth instead of isGuest to allow for authenticated guest customization of prefs
        final IPerson person = this.personManager.getPerson(containerRequest);
        final ISecurityContext securityContext = person.getSecurityContext();
        return !securityContext.isAuthenticated();
    }
}
