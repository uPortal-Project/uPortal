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
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResourceRequestContext;
import org.apache.pluto.container.PortletWindow;

/**
 * Per portlet spec:
 * <ul>
 * <li>Default cacheability is set to {@link PortletResourceRequestCacheability#PAGE}.</li>
 * <li>Default resourceID is null.</li>
 * </ul>
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
class PortletResourceRequestContextImpl extends
		PortletRequestContextImpl implements PortletResourceRequestContext {

	// PAGE is default
	private PortletResourceRequestCacheability cacheability = PortletResourceRequestCacheability.PAGE;
	private String resourceID;
	
	/**
	 * 
	 * @param container
	 * @param containerRequest
	 * @param containerResponse
	 * @param portletWindow
	 */
	PortletResourceRequestContextImpl(PortletContainer container,
			HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow portletWindow) {
		super(container, containerRequest, containerResponse, portletWindow);
	}

	
	/**
	 * @param cacheability the cacheability to set
	 */
	public void setCacheability(PortletResourceRequestCacheability cacheability) {
		this.cacheability = cacheability;
	}
	/**
	 * @param resourceID the resourceID to set
	 */
	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}


	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResourceRequestContext#getCacheability()
	 */
	@Override
	public String getCacheability() {
		return this.cacheability.toString();
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletResourceRequestContext#getPrivateRenderParameterMap()
	 */
	@Override
	public Map<String, String[]> getPrivateRenderParameterMap() {
		return super.getPrivateParameterMap();
	}

	/**
	 * May return null if not previously set (PLT 13.5).
	 * 
	 * @see org.apache.pluto.container.PortletResourceRequestContext#getResourceID()
	 */
	@Override
	public String getResourceID() {
		return this.resourceID;
	}

}
