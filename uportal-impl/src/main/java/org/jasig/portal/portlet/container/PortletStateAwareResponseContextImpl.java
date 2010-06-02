/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletStateAwareResponseContextImpl extends PortletResponseContextImpl implements PortletStateAwareResponseContext {
    private final List<Event> events = new LinkedList<Event>();
    private final IPortletPortalUrl portletUrl;

    public PortletStateAwareResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider) {

        super(portletContainer, portletWindow, containerRequest, containerResponse,
                requestPropertiesManager, portalUrlProvider);
        
        this.portletUrl = this.portalUrlProvider.getPortletUrl(TYPE.RENDER, containerRequest, this.portletWindow.getPortletWindowId());
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getEventProvider()
     */
    @Override
    public EventProvider getEventProvider() {
        return new EventProviderImpl(this.portletWindow);
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
        return this.isClosed() ? null : this.portletUrl.getPortletMode();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getPublicRenderParameters()
     */
    @Override
    public Map<String, String[]> getPublicRenderParameters() {
        return this.isClosed() ? null : this.portletUrl.getPublicRenderParameters();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getRenderParameters()
     */
    @Override
    public Map<String, String[]> getRenderParameters() {
        return this.isClosed() ? null : this.portletUrl.getRenderParameters();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#getWindowState()
     */
    @Override
    public WindowState getWindowState() {
        return this.isClosed() ? null : this.portletUrl.getWindowState();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#setPortletMode(javax.portlet.PortletMode)
     */
    @Override
    public void setPortletMode(PortletMode portletMode) {
        if (!this.isClosed()) {
            this.portletUrl.setPortletMode(portletMode);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletStateAwareResponseContext#setWindowState(javax.portlet.WindowState)
     */
    @Override
    public void setWindowState(WindowState windowState) {
        if (!this.isClosed()) {
            this.portletUrl.setWindowState(windowState);
        }
    }
}
