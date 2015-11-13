/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.search.PortletUrl;
import org.jasig.portal.search.PortletUrlParameter;
import org.jasig.portal.search.PortletUrlType;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Utilities for portlets
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */

@Component
public final class PortletUtils {
	
	  private static IPortletWindowRegistry portletWindowRegistry;
	  private static IPortalUrlProvider portalUrlProvider;

	  @Autowired(required = true)
	  private void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
	        PortletUtils.portletWindowRegistry = portletWindowRegistry;
	  }
	  @Autowired(required = true)
	  private void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
		  PortletUtils.portalUrlProvider = urlProvider;
	  }
	
    private static final Map<String, PortletMode> PORTLET_MODES = 
            ImmutableSortedMap.<String, PortletMode>orderedBy(String.CASE_INSENSITIVE_ORDER)
                .put(PortletMode.VIEW.toString(), PortletMode.VIEW)
                .put(PortletMode.EDIT.toString(), PortletMode.EDIT)
                .put(PortletMode.HELP.toString(), PortletMode.HELP)
                .put(IPortletRenderer.ABOUT.toString(), IPortletRenderer.ABOUT)
                .put(IPortletRenderer.CONFIG.toString(), IPortletRenderer.CONFIG)
                .build();
    
    private static final Map<String, WindowState> WINDOW_STATES =
            ImmutableSortedMap.<String, WindowState>orderedBy(String.CASE_INSENSITIVE_ORDER)
                .put(WindowState.NORMAL.toString(), WindowState.NORMAL)
                .put(WindowState.MAXIMIZED.toString(), WindowState.MAXIMIZED)
                .put(WindowState.MINIMIZED.toString(), WindowState.MINIMIZED)
                .put(IPortletRenderer.DASHBOARD.toString(), IPortletRenderer.DASHBOARD)
                .put(IPortletRenderer.DETACHED.toString(), IPortletRenderer.DETACHED)
                .put(IPortletRenderer.EXCLUSIVE.toString(), IPortletRenderer.EXCLUSIVE)
                .build();
    
    private static final Set<WindowState> TARGETED_WINDOW_STATES =
            ImmutableSet.<WindowState>builder()
            .add(WindowState.MAXIMIZED)
            .add(IPortletRenderer.DETACHED)
            .add(IPortletRenderer.EXCLUSIVE)
            .build();
                    
    private PortletUtils() {
    }
    
    /**
     * Checks if the specified window state is for a specifically targeted portlet
     * 
     * @param state The WindowState to check
     * @return true if the window state is targeted
     */
    public static boolean isTargetedWindowState(WindowState state) {
        return TARGETED_WINDOW_STATES.contains(state);
    }
    
    /**
     * Convert a String into a {@link PortletMode}
     */
    public static PortletMode getPortletMode(String mode) {
        if (mode == null) {
            return null;
        }
        
        final PortletMode portletMode = PORTLET_MODES.get(mode);
        if (portletMode != null) {
            return portletMode;
        }
        
        return new PortletMode(mode);
    }
    
    /**
     * Convert a String into a {@link WindowState}
     */
    public static WindowState getWindowState(String state) {
        if (state == null) {
            return null;
        }
        
        final WindowState windowState = WINDOW_STATES.get(state);
        if (windowState != null) {
            return windowState;
        }
        
        return new WindowState(state);
    }
    
    /**
     * A static EL function that is defined in portletUrl.tld
     * Takes a portletUrl object and coverts it into an actual Url
     * Example is in search, returns a marketplace entry Url
     * 
     * @param portletUrl
     * @param request
     * @return the Url represented by portletUrl
     */
    public static String getStringFromPortletUrl(PortletUrl portletUrl, HttpServletRequest request){
    	if(portletUrl == null){
    		return null;
    	}
    	//Default urlType
    	UrlType urlType = UrlType.RENDER;
    	final PortletUrlType type = portletUrl.getType();
    	switch(type){
    		case ACTION:
    			urlType=UrlType.ACTION;
    			break;
    		case RESOURCE:
    			urlType=UrlType.RESOURCE;
    			break;
    		default:
    			urlType=UrlType.RENDER;
    			break;
    	}
    	
    	IPortletWindow marketplaceWindow = portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(request, MarketplacePortletDefinition.MARKETPLACE_FNAME);
    	IPortalUrlBuilder portalUrlBuilder = portalUrlProvider.getPortalUrlBuilderByPortletWindow(request, marketplaceWindow.getPortletWindowId(), urlType);
    	IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getTargetedPortletUrlBuilder();
    	
        final String portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            portletUrlBuilder.setPortletMode(PortletUtils.getPortletMode(portletMode));
        }
        final String windowState = portletUrl.getWindowState();
        if (windowState != null) {
            portletUrlBuilder.setWindowState(PortletUtils.getWindowState(windowState));
        }
        for (final PortletUrlParameter param : portletUrl.getParam()) {
            final String name = param.getName();
            final List<String> values = param.getValue();
            portletUrlBuilder.addParameter(name, values.toArray(new String[values.size()]));
        }
        
    	return portalUrlBuilder.getUrlString();
    }
    
}
