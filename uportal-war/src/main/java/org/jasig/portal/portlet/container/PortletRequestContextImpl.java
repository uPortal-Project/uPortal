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
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.driver.PortletServlet;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.util.Assert;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletRequestContextImpl extends AbstractPortletResponseResposeContextImpl implements PortletRequestContext {
    private final IRequestPropertiesManager requestPropertiesManager;
    
    //Objects provided by the PortletServlet via the init method
    //The servlet objects are from the scope of the cross-context dispatch
    private PortletConfig portletConfig;
    private ServletContext servletContext;
    
    private Cookie[] cookies;
    
    public PortletRequestContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager) {
        
        super(portletContainer, portletWindow, containerRequest, containerResponse);
        
        Assert.notNull(requestPropertiesManager, "requestPropertiesManager cannot be null");
        
        this.requestPropertiesManager = requestPropertiesManager;
    }

    /**
     * Called by {@link PortletServlet} after the cross context dispatch but before the portlet invocation
     * 
     * @see org.apache.pluto.container.PortletRequestContext#init(javax.portlet.PortletConfig, javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void init(PortletConfig portletConfig, ServletContext servletContext, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        super.init(servletRequest, servletResponse);
        Assert.notNull(portletConfig, "portletConfig cannot be null");
        Assert.notNull(servletContext, "servletContext cannot be null");

        this.portletConfig = portletConfig;
        this.servletContext = servletContext;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getPortletConfig()
     */
    @Override
    public PortletConfig getPortletConfig() {
        return this.portletConfig;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getServletContext()
     */
    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    
    
    
    

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
        return this.servletRequest.getAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getAttributeNames()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getAttributeNames() {
        return this.servletRequest.getAttributeNames();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getCookies()
     */
    @Override
    public Cookie[] getCookies() {
        if (this.cookies == null) {
            this.cookies = this.servletRequest.getCookies();
            if (this.cookies == null) {
                this.cookies = new Cookie[0];
            }
        }
        return this.cookies.length > 0 ? this.cookies.clone() : null;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getPreferredLocale()
     */
    @Override
    public Locale getPreferredLocale() {
        return this.servletRequest.getLocale();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getPrivateParameterMap()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String[]> getPrivateParameterMap() {
        // TODO this may need to come out of the portlet window
        return this.servletRequest.getParameterMap();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getProperties()
     */
    @Override
    public Map<String, String[]> getProperties() {
        return this.requestPropertiesManager.getRequestProperties(this.servletRequest, this.portletWindow);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getPublicParameterMap()
     */
    @Override
    public Map<String, String[]> getPublicParameterMap() {
        // TODO this may need to come out of the portlet window
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {
        this.servletRequest.setAttribute(name, value);
    }

}
