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

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Deals with seting and retrieving the {@link RenderResponse#EXPIRATION_CACHE} property.
 * 
 * TODO this needs to be implemented to hook into the adapter's caching APIs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheRequestPropertiesManager extends BaseRequestPropertiesManager {
    private OptionalContainerServices optionalContainerServices;
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.BaseRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    @Override
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        Integer expirationCache = portletWindow.getExpirationCache();
        
        if (expirationCache == null) {
            final PortletDD portletDeployment = this.getPortletDeployment(portletWindow);
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
    public void setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        if (RenderResponse.EXPIRATION_CACHE.equals(property)) {
            final PortletDD portletDeployment = this.getPortletDeployment(portletWindow);
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
     * TODO needs to get moved to a registry
     * 
     * @param portletWindow
     * @return
     */
    protected PortletDD getPortletDeployment(IPortletWindow portletWindow) {
        final PortletRegistryService portletRegistryService = this.optionalContainerServices.getPortletRegistryService();
        
        final String contextPath = portletWindow.getContextPath();
        final String portletName = portletWindow.getPortletName();
        final PortletDD portletDescriptor;
        try {
            portletDescriptor = portletRegistryService.getPortletDescriptor(contextPath, portletName);
        }
        catch (PortletContainerException pce) {
            // TODO Auto-generated catch block
            throw new RuntimeException(pce);
        }
        return portletDescriptor;
    }
}
