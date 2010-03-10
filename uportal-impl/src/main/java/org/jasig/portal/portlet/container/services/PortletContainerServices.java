/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container.services;

import javax.portlet.PortalContext;

import org.apache.pluto.container.CCPPProfileService;
import org.apache.pluto.container.ContainerServices;
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
import org.apache.pluto.container.impl.RequestDispatcherServiceImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * TODO do we need to depend on the pluto-portal-driver JAR? Especially for PortletInvokerService?
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletContainerServices implements ContainerServices, InitializingBean {
    private PortalContext portalContext;
    private PortletEnvironmentService portletEnvironmentService;
    private PortletPreferencesService portletPreferencesService;
    private RequestDispatcherService requestDispatcherService;
    private UserInfoService userInfoService;
    
    @Autowired(required=true)
    public void setPortalContext(PortalContext portalContext) {
        this.portalContext = portalContext;
    }
    
    @Autowired(required=true)
    public void setPortletEnvironmentService(PortletEnvironmentService portletEnvironmentService) {
        this.portletEnvironmentService = portletEnvironmentService;
    }
    
    @Autowired(required=true)
    public void setPortletPreferencesService(PortletPreferencesService portletPreferencesService) {
        this.portletPreferencesService = portletPreferencesService;
    }

    @Autowired(required=false)
    public void setRequestDispatcherService(RequestDispatcherService requestDispatcherService) {
        this.requestDispatcherService = requestDispatcherService;
    }
    
    @Autowired(required=true)
    public void setUserInfoService(@Qualifier("main") UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.requestDispatcherService == null) {
            this.requestDispatcherService = new RequestDispatcherServiceImpl();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getCCPPProfileService()
     */
    @Override
    public CCPPProfileService getCCPPProfileService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getEventCoordinationService()
     */
    @Override
    public EventCoordinationService getEventCoordinationService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getFilterManagerService()
     */
    @Override
    public FilterManagerService getFilterManagerService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getNamespaceMapper()
     */
    @Override
    public NamespaceMapper getNamespaceMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortalContext()
     */
    @Override
    public PortalContext getPortalContext() {
        return this.portalContext;
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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletPreferencesService()
     */
    @Override
    public PortletPreferencesService getPortletPreferencesService() {
        return this.portletPreferencesService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletRequestContextService()
     */
    @Override
    public PortletRequestContextService getPortletRequestContextService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getPortletURLListenerService()
     */
    @Override
    public PortletURLListenerService getPortletURLListenerService() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getRequestDispatcherService()
     */
    @Override
    public RequestDispatcherService getRequestDispatcherService() {
        return this.requestDispatcherService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.ContainerServices#getUserInfoService()
     */
    @Override
    public UserInfoService getUserInfoService() {
        return this.userInfoService;
    }

}
