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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.Event;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.EventProvider;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletStateAwareResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;

/**
 * @author Nicholas Blair, npblair@wisc.edu
 * @version $Revision$
 */
abstract class PortletStateAwareResponseContextImpl extends PortletResponseContextImpl implements
PortletStateAwareResponseContext {
	private List<Event> events;
	private PortletURLProvider portletURLProvider;
	private EventProvider eventProvider;

	public PortletStateAwareResponseContextImpl(PortletContainer container, HttpServletRequest containerRequest,
			HttpServletResponse containerResponse, PortletWindow window, PortletURLProvider portletURLProvider, EventProvider eventProvider)
	{
		super(container, containerRequest, containerResponse, window);
		this.portletURLProvider = portletURLProvider;
		this.eventProvider = eventProvider;
	}

	protected PortletURLProvider getPorletURLProvider()
	{
		return portletURLProvider;
	}

	@Override
	public void close()
	{
		if (!isClosed())
		{
			super.close();
			/*
			new PortletURLImpl(this, portletURLProvider).filterURL();
			PortalURL url = portletURLProvider.apply();
			PortalRequestContext.getContext(getServletRequest()).mergePortalURL(url, getPortletWindow().getId().getStringId());
			*/
		}
	}

	@Override
	public void release()
	{
		events = null;
		portletURLProvider = null;
		super.release();
	}

	public List<Event> getEvents()
	{
		if (isReleased())
		{
			return null;
		}
		if (events == null)
		{
			events = new ArrayList<Event>();
		}
		return events;
	}

	public PortletMode getPortletMode()
	{
		return isClosed() ? null : portletURLProvider.getPortletMode();
	}

	public Map<String, String[]> getPublicRenderParameters()
	{
		return isClosed() ? null : portletURLProvider.getPublicRenderParameters();
	}

	public Map<String, String[]> getRenderParameters()
	{
		return isClosed() ? null : portletURLProvider.getRenderParameters();
	}

	public WindowState getWindowState()
	{
		return isClosed() ? null : portletURLProvider.getWindowState();
	}

	public void setPortletMode(PortletMode portletMode)
	{
		if (!isClosed())
		{
			portletURLProvider.setPortletMode(portletMode);
		}
	}

	public void setWindowState(WindowState windowState)
	{
		if (!isClosed())
		{
			portletURLProvider.setWindowState(windowState);
		}
	}

	public EventProvider getEventProvider()
	{
		return isClosed() ? null : this.eventProvider;
	}
}
