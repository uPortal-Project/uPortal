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

import java.util.List;
import java.util.Map;

import javax.portlet.Event;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.EventProvider;
import org.apache.pluto.container.PortletActionResponseContext;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
class PortletActionResponseContextImpl extends
		PortletResponseContextImpl implements PortletActionResponseContext {

	
	PortletActionResponseContextImpl(PortletContainer container,
			HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window) {
		super(container, containerRequest, containerResponse, window);
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletActionResponseContext#getResponseURL()
	 */
	@Override
	public String getResponseURL() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletActionResponseContext#isRedirect()
	 */
	@Override
	public boolean isRedirect() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletActionResponseContext#setRedirect(java.lang.String)
	 */
	@Override
	public void setRedirect(String location) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletActionResponseContext#setRedirect(java.lang.String, java.lang.String)
	 */
	@Override
	public void setRedirect(String location, String renderURLParamName) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#getEventProvider()
	 */
	@Override
	public EventProvider getEventProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#getEvents()
	 */
	@Override
	public List<Event> getEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#getPortletMode()
	 */
	@Override
	public PortletMode getPortletMode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#getPublicRenderParameters()
	 */
	@Override
	public Map<String, String[]> getPublicRenderParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#getRenderParameters()
	 */
	@Override
	public Map<String, String[]> getRenderParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#getWindowState()
	 */
	@Override
	public WindowState getWindowState() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#setPortletMode(javax.portlet.PortletMode)
	 */
	@Override
	public void setPortletMode(PortletMode portletMode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.pluto.container.PortletStateAwareResponseContext#setWindowState(javax.portlet.WindowState)
	 */
	@Override
	public void setWindowState(WindowState windowState) {
		
	}

}
