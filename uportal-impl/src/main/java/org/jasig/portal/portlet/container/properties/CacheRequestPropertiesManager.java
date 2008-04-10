/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.Collections;
import java.util.Map;

import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.jasig.portal.PortalException;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Deals with seting and retrieving the {@link RenderResponse#EXPIRATION_CACHE} property.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheRequestPropertiesManager extends BaseRequestPropertiesManager {
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortalRequestUtils portalRequestUtils;
    
    
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
        Validate.notNull(portletEntityRegistry);
        this.portletEntityRegistry = portletEntityRegistry;
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
        Validate.notNull(portletDefinitionRegistry);
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.BaseRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    @Override
    public Map<String, String[]> getRequestProperties(HttpServletRequest portletRequest, IPortletWindow portletWindow) {
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortletAdaptorRequest(portletRequest);
        
        Integer expirationCache = portletWindow.getExpirationCache();
        
        if (expirationCache == null) {
            final PortletDD portletDeployment = this.getPortletDeployment(httpServletRequest, portletWindow);
            final int descriptorExpirationCache = portletDeployment.getExpirationCache();
            
            if (PortletDD.EXPIRATION_CACHE_UNSET != descriptorExpirationCache) {
                expirationCache = descriptorExpirationCache;
            }
        }

        if (expirationCache != null) {
            return Collections.singletonMap(RenderResponse.EXPIRATION_CACHE, new String[] { expirationCache.toString() });
        }

        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.BaseRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    @Override
    public void setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value) {
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortletAdaptorRequest(portletRequest);
        
        if (RenderResponse.EXPIRATION_CACHE.equals(property)) {
            final PortletDD portletDeployment = this.getPortletDeployment(httpServletRequest, portletWindow);
            final int descriptorExpirationCache = portletDeployment.getExpirationCache();
            
            //Only set the cache expiration if the descriptor has a cache expiration set (PLT.18.1)
            if (PortletDD.EXPIRATION_CACHE_UNSET != descriptorExpirationCache) {
                Integer cacheExpiration = portletWindow.getExpirationCache();
                try {
                    cacheExpiration = Integer.valueOf(value);
                }
                catch (NumberFormatException nfe) {
                    this.logger.info("Portlet '" + portletWindow + "' tried to set a cache expiration time of '" + value + "' which could not be parsed into an Integer. The previous value of '" + cacheExpiration + "' will be used.");
                }
            
                portletWindow.setExpirationCache(cacheExpiration);
            }
        }
    }

    /**
     * Gets the Portlet Deployment for a IPortletWindow object
     * 
     * @param httpServletRequest The portal's request (not the portlet context request)
     * @param portletWindow The window to get the parent PortletDD for.
     * @return The parent portlet descriptor for the window
     * @throws PortalException if the PortletDD fails to load.
     */
    protected PortletDD getPortletDeployment(HttpServletRequest httpServletRequest, IPortletWindow portletWindow) {
        final IPortletDefinition portletDefinition = this.portletEntityRegistry.getParentPortletDefinition(portletWindow.getPortletEntityId());
        
        try {
            return this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinition.getPortletDefinitionId());
        }
        catch (PortletContainerException pce) {
            throw new PortalException("Failed to retrieve the PortletDD for portlet window: " + portletWindow, pce);
        }
    }
}
