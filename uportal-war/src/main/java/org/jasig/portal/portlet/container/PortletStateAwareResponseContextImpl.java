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

import java.util.LinkedList;
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
import org.apache.pluto.container.driver.PortletContextService;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortletUrlBuilder;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletStateAwareResponseContextImpl extends PortletResponseContextImpl implements PortletStateAwareResponseContext {
    private final List<Event> events = new LinkedList<Event>();
    protected final IPortletUrlBuilder portletUrlBuilder;
    protected final PortletContextService portletContextService;

    public PortletStateAwareResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortletUrlBuilder portletUrlBuilder,
            PortletContextService portletContextService, IPortletCookieService portletCookieService) {

        super(portletContainer, portletWindow, containerRequest, containerResponse,
                requestPropertiesManager, portletCookieService);
        
        this.portletUrlBuilder = portletUrlBuilder;
        this.portletContextService = portletContextService;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getEventProvider()
     */
    @Override
    public EventProvider getEventProvider() {
        return new EventProviderImpl(this.portletWindow, this.portletContextService);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getEvents()
     */
    @Override
    public List<Event> getEvents() {
        return this.events;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getPortletMode()
     */
    @Override
    public PortletMode getPortletMode() {
        this.checkContextStatus();
        return this.portletUrlBuilder.getPortletMode();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getPublicRenderParameters()
     */
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        this.checkContextStatus();
        return this.portletUrlBuilder.getPublicRenderParameters();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getRenderParameters()
     */
    @Override
    public Map<String, String[]> getRenderParameters() {
        this.checkContextStatus();
        return this.portletUrlBuilder.getParameters();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        this.checkContextStatus();
        return this.portletUrlBuilder.getWindowState();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode portletMode) {
        this.checkContextStatus();
        this.portletUrlBuilder.setPortletMode(portletMode);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState windowState) {
        this.checkContextStatus();
        this.portletUrlBuilder.setWindowState(windowState);
    }
}
