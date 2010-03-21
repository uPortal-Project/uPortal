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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.ResourceURLProvider;
import org.apache.pluto.driver.core.ResourceURLProviderImpl;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Main callback service Pluto uses when servicing portlet requests. Delegates to {@link IRequestPropertiesManager}
 * for request/response property information, {@link IPortletWindowRegistry} for portlet window information,
 * {@link IPortletUrlSyntaxProvider} for URL generation, and {@link FOO} for dynamic title handling.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortalCallbackServiceImpl /* *implements PortalCallbackService */ {
    private IRequestPropertiesManager requestPropertiesManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletUrlSyntaxProvider portletUrlSyntaxProvider;
    
    /**
     * @return the requestPropertiesManager
     */
    public IRequestPropertiesManager getRequestPropertiesManager() {
        return requestPropertiesManager;
    }
    /**
     * @param requestPropertiesManager the requestPropertiesManager to set
     */
    @Autowired(required=true)
    public void setRequestPropertiesManager(@Qualifier("main") IRequestPropertiesManager requestPropertiesManager) {
        this.requestPropertiesManager = requestPropertiesManager;
    }

    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired(required=true)
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    /**
     * @return the portletUrlSyntaxProvider
     */
    public IPortletUrlSyntaxProvider getPortletUrlSyntaxProvider() {
        return portletUrlSyntaxProvider;
    }
    /**
     * @param portletUrlSyntaxProvider the portletUrlSyntaxProvider to set
     */
    @Autowired(required=true)
    public void setPortletUrlSyntaxProvider(IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        this.portletUrlSyntaxProvider = portletUrlSyntaxProvider;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#setResponseProperty(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String, java.lang.String)
     */
    public void setResponseProperty(HttpServletRequest request, PortletWindow plutoPortletWindow, String property, String value) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        this.requestPropertiesManager.setResponseProperty(request, portletWindow, property, value);
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#addResponseProperty(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String, java.lang.String)
     */
    public void addResponseProperty(HttpServletRequest request, PortletWindow plutoPortletWindow, String property, String value) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        this.requestPropertiesManager.addResponseProperty(request, portletWindow, property, value);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#getRequestProperties(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        return this.requestPropertiesManager.getRequestProperties(request, portletWindow);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#getPortletURLProvider(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    /*
    public PortletURLProvider getPortletURLProvider(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        return new PortletURLProviderImpl(portletWindow, request, this.portletUrlSyntaxProvider);
    }*/

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#getResourceURLProvider(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public ResourceURLProvider getResourceURLProvider(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        return new ResourceURLProviderImpl(request, plutoPortletWindow);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#setTitle(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String)
     */
    public void setTitle(HttpServletRequest request, PortletWindow plutoPortletWindow, String title) {
        request.setAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE, title);
    }
}
