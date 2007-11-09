/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.PortletURLProvider;
import org.apache.pluto.spi.ResourceURLProvider;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider;
import org.jasig.portal.portlet.url.PortletURLProviderImpl;
import org.springframework.beans.factory.annotation.Required;

/**
 * Main callback service Pluto uses when servicing portlet requests. Delegates to {@link IRequestPropertiesManager}
 * for request/response property information, {2link IPortletWindowRegistry} for portlet window information,
 * {@link FOO} for URL generation, and {@link FOO} for dynamic title handling.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalCallbackServiceImpl implements PortalCallbackService {
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
    @Required
    public void setRequestPropertiesManager(IRequestPropertiesManager requestPropertiesManager) {
        Validate.notNull(requestPropertiesManager, "requestPropertiesManager can not be null");
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
    @Required
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry, "portletWindowRegistry can not be null");
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
    @Required
    public void setPortletUrlSyntaxProvider(IPortletUrlSyntaxProvider portletUrlSyntaxProvider) {
        Validate.notNull(portletUrlSyntaxProvider, "portletUrlSyntaxProvider can not be null");
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
    public PortletURLProvider getPortletURLProvider(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        return new PortletURLProviderImpl(portletWindow, request, this.portletUrlSyntaxProvider);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#getResourceURLProvider(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public ResourceURLProvider getResourceURLProvider(HttpServletRequest request, PortletWindow plutoPortletWindow) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortalCallbackService#setTitle(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String)
     */
    public void setTitle(HttpServletRequest request, PortletWindow plutoPortletWindow, String title) {
        //This is called by the portlet if RenderResponse#setTitle(String) is called.
        // TODO determine how to tie this into uPortal channel title rendering
        throw new UnsupportedOperationException("Dynamic portlet titles are not yet supported");
    }
}
