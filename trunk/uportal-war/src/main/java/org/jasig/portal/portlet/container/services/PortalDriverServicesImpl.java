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

import javax.portlet.PortalContext;

import org.apache.pluto.container.CCPPProfileService;
import org.apache.pluto.container.EventCoordinationService;
import org.apache.pluto.container.FilterManagerService;
import org.apache.pluto.container.NamespaceMapper;
import org.apache.pluto.container.PortletEnvironmentService;
import org.apache.pluto.container.PortletInvokerService;
import org.apache.pluto.container.PortletPreferencesService;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletURLListenerService;
import org.apache.pluto.container.RequestDispatcherService;
import org.apache.pluto.container.UserInfoService;
import org.apache.pluto.container.driver.PortalAdministrationService;
import org.apache.pluto.container.driver.PortalDriverServices;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service API for working with Pluto. Pluto uses all of these interfaces for various tasks when
 * dispatching to portlets. uPortal provides implementations of most of them and all impls are
 * configured as spring beans.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portalDriverServices")
public class PortalDriverServicesImpl implements PortalDriverServices {
    //RequiredContainerServices
    private PortalContext portalContext;
    private EventCoordinationService eventCoordinationService;
    private FilterManagerService filterManagerService;
    private PortletRequestContextService portletRequestContextService;
    private PortletURLListenerService portletURLListenerService;
    
    //OptionalContainerServices
    private UserInfoService userInfoService;
    private PortalAdministrationService portalAdministrationService;
    private PortletPreferencesService portletPreferencesService;
    private PortletEnvironmentService portletEnvironmentService;
    private PortletRegistryService portletRegistryService;
    private PortletContextService portletContextService;
    private PortletInvokerService portletInvokerService;
    private NamespaceMapper namespaceMapper;
    private CCPPProfileService ccppProfileService;
    private RequestDispatcherService requestDispatcherService;
    
    @Autowired
    public void setPortalContext(PortalContext portalContext) {
        this.portalContext = portalContext;
    }

    @Autowired
    public void setEventCoordinationService(EventCoordinationService eventCoordinationService) {
        this.eventCoordinationService = eventCoordinationService;
    }

    @Autowired
    public void setFilterManagerService(FilterManagerService filterManagerService) {
        this.filterManagerService = filterManagerService;
    }

    @Autowired
    public void setPortletRequestContextService(PortletRequestContextService portletRequestContextService) {
        this.portletRequestContextService = portletRequestContextService;
    }

    @Autowired
    public void setPortletURLListenerService(PortletURLListenerService portletURLListenerService) {
        this.portletURLListenerService = portletURLListenerService;
    }

    @Autowired
    public void setUserInfoService(@Qualifier("main") UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Autowired
    public void setPortalAdministrationService(PortalAdministrationService portalAdministrationService) {
        this.portalAdministrationService = portalAdministrationService;
    }

    @Autowired
    public void setPortletPreferencesService(PortletPreferencesService portletPreferencesService) {
        this.portletPreferencesService = portletPreferencesService;
    }

    @Autowired
    public void setPortletEnvironmentService(PortletEnvironmentService portletEnvironmentService) {
        this.portletEnvironmentService = portletEnvironmentService;
    }

    @Autowired
    public void setPortletRegistryService(PortletRegistryService portletRegistryService) {
        this.portletRegistryService = portletRegistryService;
    }

    @Autowired
    public void setPortletContextService(PortletContextService portletContextService) {
        this.portletContextService = portletContextService;
    }

    @Autowired
    public void setPortletInvokerService(PortletInvokerService portletInvokerService) {
        this.portletInvokerService = portletInvokerService;
    }

    @Autowired
    public void setNamespaceMapper(NamespaceMapper namespaceMapper) {
        this.namespaceMapper = namespaceMapper;
    }

    @Autowired
    public void setCcppProfileService(CCPPProfileService ccppProfileService) {
        this.ccppProfileService = ccppProfileService;
    }

    @Autowired
    public void setRequestDispatcherService(RequestDispatcherService requestDispatcherService) {
        this.requestDispatcherService = requestDispatcherService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortalContext()
     */
    @Override
    public PortalContext getPortalContext() {
        return this.portalContext;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getEventCoordinationService()
     */
    @Override
    public EventCoordinationService getEventCoordinationService() {
        return this.eventCoordinationService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletRequestContextService()
     */
    @Override
    public PortletRequestContextService getPortletRequestContextService() {
        return this.portletRequestContextService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getFilterManagerService()
     */
    @Override
    public FilterManagerService getFilterManagerService() {
        return this.filterManagerService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletURLListenerService()
     */
    @Override
    public PortletURLListenerService getPortletURLListenerService() {
        return this.portletURLListenerService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletPreferencesService()
     */
    @Override
    public PortletPreferencesService getPortletPreferencesService() {
        return this.portletPreferencesService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletEnvironmentService()
     */
    @Override
    public PortletEnvironmentService getPortletEnvironmentService() {
        return this.portletEnvironmentService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletInvokerService()
     */
    @Override
    public PortletInvokerService getPortletInvokerService() {
        return this.portletInvokerService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getUserInfoService()
     */
    @Override
    public UserInfoService getUserInfoService() {
        return this.userInfoService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getNamespaceMapper()
     */
    @Override
    public NamespaceMapper getNamespaceMapper() {
        return this.namespaceMapper;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getCCPPProfileService()
     */
    @Override
    public CCPPProfileService getCCPPProfileService() {
        return this.ccppProfileService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getRequestDispatcherService()
     */
    @Override
    public RequestDispatcherService getRequestDispatcherService() {
        return this.requestDispatcherService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.driver.PortalDriverContainerServices#getPortletContextService()
     */
    @Override
    public PortletContextService getPortletContextService() {
        return this.portletContextService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.driver.PortalDriverContainerServices#getPortletRegistryService()
     */
    @Override
    public PortletRegistryService getPortletRegistryService() {
        return this.portletRegistryService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.driver.PortalDriverContainerServices#getPortalAdministrationService()
     */
    @Override
    public PortalAdministrationService getPortalAdministrationService() {
        return this.portalAdministrationService;
    }
}
