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

/**
 * 
 */
package org.jasig.portal.portlet.container.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletEventResponseContext;
import org.apache.pluto.container.PortletRenderResponseContext;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletResourceResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PortletContextService;
import org.jasig.portal.portlet.container.PortletActionResponseContextImpl;
import org.jasig.portal.portlet.container.PortletEventResponseContextImpl;
import org.jasig.portal.portlet.container.PortletRenderResponseContextImpl;
import org.jasig.portal.portlet.container.PortletRequestContextImpl;
import org.jasig.portal.portlet.container.PortletResourceRequestContextImpl;
import org.jasig.portal.portlet.container.PortletResourceResponseContextImpl;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalActionUrlBuilder;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * uPortal implementation of {@link PortletRequestContextService}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@Service("portletRequestContextService")
public class LocalPortletRequestContextServiceImpl implements PortletRequestContextService {
    private IPortletWindowRegistry portletWindowRegistry;
    private IRequestPropertiesManager requestPropertiesManager;
    private IPortalUrlProvider portalUrlProvider;
    private IUrlSyntaxProvider urlSyntaxProvider;
    private PortletContextService portletContextService;
    private IPortletCookieService portletCookieService;
    private RequestAttributeService requestAttributeService;

    @Autowired
    public void setPortletContextService(PortletContextService portletContextService) {
        this.portletContextService = portletContextService;
    }
    @Autowired
	public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    @Autowired
    public void setRequestPropertiesManager(IRequestPropertiesManager requestPropertiesManager) {
        this.requestPropertiesManager = requestPropertiesManager;
    }
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }
    @Autowired
    public void setPortletCookieService(IPortletCookieService portletCookieService) {
        this.portletCookieService = portletCookieService;
    }  
    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
    @Autowired
	public void setRequestAttributeService(
			RequestAttributeService requestAttributeService) {
		this.requestAttributeService = requestAttributeService;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletActionRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletActionRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
	    
	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
	    final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(containerRequest);
	    return new PortletRequestContextImpl(container, portletWindow, containerRequest, containerResponse, this.requestPropertiesManager, portalRequestInfo, portletCookieService, requestAttributeService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletActionResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletActionResponseContext getPortletActionResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
	    
	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
	    
	    final IPortalActionUrlBuilder portalActionUrlBuilder = this.portalUrlProvider.getPortalActionUrlBuilder(containerRequest);
	    final IPortletUrlBuilder portletUrlBuilder = portalActionUrlBuilder.getPortletUrlBuilder(portletWindow.getPortletWindowId());
	    
        return new PortletActionResponseContextImpl(container, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portalActionUrlBuilder, portletUrlBuilder, this.portletContextService, this.portletCookieService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletEventRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletEventRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
	    
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(containerRequest);
        return new PortletRequestContextImpl(container, portletWindow, containerRequest, containerResponse, this.requestPropertiesManager, portalRequestInfo, portletCookieService, requestAttributeService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletEventResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletEventResponseContext getPortletEventResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
	    
	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
        
	    final IPortalActionUrlBuilder portalActionUrlBuilder = this.portalUrlProvider.getPortalActionUrlBuilder(containerRequest);
        final IPortletUrlBuilder portletUrlBuilder = portalActionUrlBuilder.getPortletUrlBuilder(portletWindow.getPortletWindowId());
        
	    return new PortletEventResponseContextImpl(container, portletWindow, containerRequest, containerResponse, requestPropertiesManager, portletUrlBuilder, this.portletContextService, this.portletCookieService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletRenderRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRequestContext getPortletRenderRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
	    final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(containerRequest);
        return new PortletRequestContextImpl(container, portletWindow, containerRequest, containerResponse, this.requestPropertiesManager, portalRequestInfo, portletCookieService, requestAttributeService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletRenderResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletRenderResponseContext getPortletRenderResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
	    
	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
	    return new PortletRenderResponseContextImpl(container, portletWindow, containerRequest, containerResponse, this.requestPropertiesManager, this.portalUrlProvider, this.portletCookieService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletResourceRequestContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletResourceRequestContext getPortletResourceRequestContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
	    final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(containerRequest);
        return new PortletResourceRequestContextImpl(container, portletWindow, containerRequest, containerResponse, this.requestPropertiesManager, portalRequestInfo, this.portletCookieService, requestAttributeService);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContextService#getPortletResourceResponseContext(org.apache.pluto.container.PortletContainer, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.pluto.container.PortletWindow)
	 */
	@Override
	public PortletResourceResponseContext getPortletResourceResponseContext(
			PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {

	    final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(containerRequest, window);
        
		return new PortletResourceResponseContextImpl(container, portletWindow, containerRequest, containerResponse, this.requestPropertiesManager, this.portalUrlProvider, this.portletCookieService);
	}
}
