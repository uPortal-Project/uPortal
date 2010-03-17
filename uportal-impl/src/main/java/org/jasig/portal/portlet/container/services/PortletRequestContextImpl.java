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
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.url.PortalURL;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
class PortletRequestContextImpl implements PortletRequestContext {

	private PortletConfig portletConfig;
	private ServletContext servletContext;
	private PortletContainer portletContainer;
	
	private PortletWindow portletWindow;
	private PortalURL portalURL;
	
	private HttpServletRequest containerRequest;
	private HttpServletResponse containerResponse;
	private HttpServletRequest servletRequest;
	private HttpServletResponse servletResponse;
	
	PortletRequestContextImpl(PortletContainer container, HttpServletRequest containerRequest,
            HttpServletResponse containerResponse, PortletWindow portletWindow) {
		this.portletContainer = container;
		this.containerRequest = containerRequest;
		this.containerResponse = containerResponse;
		this.portletWindow = portletWindow;
		
		this.portalURL =  PortalRequestContext.getContext(containerRequest).createPortalURL();
	}
	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getAttributeNames()
	 */
	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getContainer()
	 */
	@Override
	public PortletContainer getContainer() {
		return this.portletContainer;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getContainerRequest()
	 */
	@Override
	public HttpServletRequest getContainerRequest() {
		return this.containerRequest;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getContainerResponse()
	 */
	@Override
	public HttpServletResponse getContainerResponse() {
		return this.containerResponse;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getCookies()
	 */
	@Override
	public Cookie[] getCookies() {
		return this.servletRequest.getCookies();
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getPortletConfig()
	 */
	@Override
	public PortletConfig getPortletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getPortletWindow()
	 */
	@Override
	public PortletWindow getPortletWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getPreferredLocale()
	 */
	@Override
	public Locale getPreferredLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getPrivateParameterMap()
	 */
	@Override
	public Map<String, String[]> getPrivateParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getProperties()
	 */
	@Override
	public Map<String, String[]> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getPublicParameterMap()
	 */
	@Override
	public Map<String, String[]> getPublicParameterMap() {
		this.portalURL.getPublicParameters();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getServletRequest()
	 */
	@Override
	public HttpServletRequest getServletRequest() {
		return this.servletRequest;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#getServletResponse()
	 */
	@Override
	public HttpServletResponse getServletResponse() {
		return this.servletResponse;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#init(javax.portlet.PortletConfig, javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void init(PortletConfig portletConfig,
			ServletContext servletContext, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		this.portletConfig = portletConfig;
		this.servletContext = servletContext;
		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletRequestContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		// an unwritten requirement of this method is to call "removeAttribute" if null is passed in for value
		if(null == value) {
			this.servletRequest.removeAttribute(name);
		} else {
			this.servletRequest.setAttribute(name, value);
		}
	}

	/**
	 * 
	 * @param attributeName
	 * @return true if the attributeName argument starts with java. or javax.
	 */
	protected boolean isRestrictedAttributeName(String attributeName) {
		return attributeName.startsWith("java.") || attributeName.startsWith("javax.");
	}
}
