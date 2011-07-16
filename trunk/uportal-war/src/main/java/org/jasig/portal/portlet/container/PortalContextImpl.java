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

package org.jasig.portal.portlet.container;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.lang.Validate;

/**
 * Provides basic information about uPortal and features it supports. The
 * class will function with no configuration but no properties, states or
 * modes are set by default.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalContextImpl implements PortalContext {
    private Properties portalProperties = new Properties();
    private Set<PortletMode> portletModes = Collections.emptySet();
    private Set<WindowState> windowStates = Collections.emptySet();
    private String portalVersion;
    
    public void setPortalVersion(String portalVersion) {
        this.portalVersion = portalVersion;
    }

    /**
     * @return the portalProperties, will not be null.
     */
    public Properties getPortalProperties() {
        return portalProperties;
    }

    /**
     * @param portalProperties the portalProperties to set, can not be null.
     */
    public void setPortalProperties(Properties portalProperties) {
        Validate.notNull(portalProperties, "portalProperties can not be null");
        this.portalProperties = portalProperties;
    }

    /**
     * @return the portletModes, will not be null.
     */
    public Set<PortletMode> getPortletModes() {
        return portletModes;
    }

    /**
     * @param portletModes the portletModes to set, can not be null.
     */
    public void setPortletModes(Set<PortletMode> portletModes) {
        Validate.notNull(portletModes, "portletModes can not be null");
        this.portletModes = portletModes;
    }

    /**
     * @return the windowStates, will not be null.
     */
    public Set<WindowState> getWindowStates() {
        return windowStates;
    }

    /**
     * @param windowStates the windowStates to set, can not be null.
     */
    public void setWindowStates(Set<WindowState> windowStates) {
        Validate.notNull(windowStates, "windowStates can not be null");
        this.windowStates = windowStates;
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getPortalInfo()
     */
    @Override
    public String getPortalInfo() {
        return "uPortal/" + this.portalVersion;
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getProperty(java.lang.String)
     */
    @Override
    public String getProperty(String name) {
        Validate.notNull(name, "Property name can not be null");
        return this.portalProperties.getProperty(name);
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getPropertyNames()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getPropertyNames() {
        return (Enumeration<String>) this.portalProperties.propertyNames();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getSupportedPortletModes()
     */
    @Override
    public Enumeration<PortletMode> getSupportedPortletModes() {
        return Collections.enumeration(this.portletModes);
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortalContext#getSupportedWindowStates()
     */
    @Override
    public Enumeration<WindowState> getSupportedWindowStates() {
        return Collections.enumeration(this.windowStates);
    }
}
