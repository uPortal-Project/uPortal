/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PortletServlet;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.util.Assert;

/**
 * Base class for both {@link PortletRequestContext} and {@link PortletResponseContext} implementations
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractPortletResponseResposeContextImpl {
    final PortletContainer portletContainer;
    final HttpServletRequest containerRequest;
    final HttpServletResponse containerResponse;
    final IPortletWindow portletWindow;

    //Objects provided by the PortletServlet via the init method
    //The servlet objects are from the scope of the cross-context dispatch
    HttpServletRequest servletRequest;
    HttpServletResponse servletResponse;
    
    public AbstractPortletResponseResposeContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
        Assert.notNull(portletContainer, "portletContainer cannot be null");
        Assert.notNull(containerRequest, "containerRequest cannot be null");
        Assert.notNull(containerResponse, "containerResponse cannot be null");
        Assert.notNull(portletWindow, "portletWindow cannot be null");
        
        this.portletContainer = portletContainer;
        this.containerRequest = containerRequest;
        this.containerResponse = containerResponse;
        this.portletWindow = portletWindow;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#getContainer()
     */
    public PortletContainer getContainer() {
        return this.portletContainer;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#getContainerRequest()
     */
    public HttpServletRequest getContainerRequest() {
        return this.containerRequest;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#getContainerResponse()
     */
    public HttpServletResponse getContainerResponse() {
        return this.containerResponse;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#getPortletWindow()
     */
    public PortletWindow getPortletWindow() {
        return this.portletWindow;
    }
    
    
    
    /**
     * Called by {@link PortletServlet} after the cross context dispatch but before the portlet invocation
     * 
     * @see org.apache.pluto.container.PortletResponseContext#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Assert.notNull(servletRequest, "servletRequest cannot be null");
        Assert.notNull(servletResponse, "servletResponse cannot be null");

        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getServletRequest()
     */
    public HttpServletRequest getServletRequest() {
        return this.servletRequest;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletRequestContext#getServletResponse()
     */
    public HttpServletResponse getServletResponse() {
        return this.servletResponse;
    }
}
