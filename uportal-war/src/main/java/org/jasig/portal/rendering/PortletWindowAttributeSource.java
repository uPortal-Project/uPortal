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
package org.jasig.portal.rendering;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Adds information about the portlet window to the layout xml data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWindowAttributeSource implements AttributeSource, BeanNameAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private String name;
    private IPortletWindowRegistry portletWindowRegistry;

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }
    

    @Override
    public final Iterator<Attribute> getAdditionalAttributes(HttpServletRequest request, HttpServletResponse response, StartElement event) {
        final QName eventName = event.getName();
        final String localEventName = eventName.getLocalPart();
        
        //Only pay attention to channel events
        if (!IUserLayoutManager.CHANNEL.equals(localEventName)) {
            return null;
        }
        
        final Tuple<IPortletWindow, StartElement> portletWindowAndElement = this.portletWindowRegistry.getPortletWindow(request, event);
        if (portletWindowAndElement == null) {
            this.logger.warn("No IPortletWindow could be found or created for element: " + event);
            return null;
        }        
        
        //Lookup the portlet window for the layout node
        final IPortletWindow portletWindow = portletWindowAndElement.first;
        
        //Create the attributes
        final Collection<Attribute> attributes = new LinkedList<Attribute>();
        
        //Add window state data
        final WindowState windowState = getWindowState(request, portletWindow);
        final Attribute windowStateAttribute = xmlEventFactory.createAttribute("windowState", windowState.toString());
        attributes.add(windowStateAttribute);
        
        //Add portlet mode data
        final PortletMode portletMode = portletWindow.getPortletMode();
        final Attribute portletModeAttribute = xmlEventFactory.createAttribute("portletMode", portletMode.toString());
        attributes.add(portletModeAttribute);
        
        return attributes.iterator();
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final Set<IPortletWindow> portletWindows = this.portletWindowRegistry.getAllLayoutPortletWindows(request);

        final CacheKeyBuilder cacheKeyBuilder = CacheKey.builder(this.name);
        
        for (final IPortletWindow portletWindow : portletWindows) {
        	if(portletWindow != null) {
        		final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        		final WindowState windowState = portletWindow.getWindowState();
        		final PortletMode portletMode = portletWindow.getPortletMode();
        		cacheKeyBuilder.addAll(portletWindowId, windowState.toString(), portletMode.toString());
        	} else {
        		this.logger.warn("portletWindowRegistry#getAllLayoutPortletWindows() returned a null portletWindow"); 
        	}
        }
        
        return cacheKeyBuilder.build();
    }


    /**
     * Get the window state for the given IPortletWindow object.
     *
     * Extension point.  Used by the FixedWindowStateAttributeSource to render a theme specific
     * window state instead of the actual window state specified by the portletWindow object.
     *
     * @param request the HTTP request
     * @param window the portlet window to get the state for
     * @return the window state to use for 'window'
     */
    protected WindowState getWindowState(HttpServletRequest request, IPortletWindow window) {
        return window.getWindowState();
    }
}