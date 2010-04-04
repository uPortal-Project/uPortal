/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container;

import javax.portlet.PortletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletResponseContext;
import org.apache.pluto.container.ResourceURLProvider;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.url.IPortletUrlCreator;
import org.springframework.util.Assert;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResponseContextImpl extends AbstractPortletResponseResposeContextImpl implements PortletResponseContext {
    private boolean closed = false;
    private boolean released = false;
    
    protected final IRequestPropertiesManager requestPropertiesManager;
    protected final IPortletUrlCreator portletUrlCreator;
    
    public PortletResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortletUrlCreator portletUrlCreator) {
        super(portletContainer, portletWindow, containerRequest, containerResponse);
        
        Assert.notNull(requestPropertiesManager, "requestPropertiesManager can not be null");
        Assert.notNull(portletUrlCreator, "portletUrlCreator can not be null");
        
        this.requestPropertiesManager = requestPropertiesManager;
        this.portletUrlCreator = portletUrlCreator;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#addProperty(javax.servlet.http.Cookie)
     */
    @Override
    public void addProperty(Cookie cookie) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#addProperty(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void addProperty(String key, Element element) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#addProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void addProperty(String key, String value) {
        this.requestPropertiesManager.addResponseProperty(this.servletRequest, this.portletWindow, key, value);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void setProperty(String key, String value) {
        this.requestPropertiesManager.setResponseProperty(this.servletRequest, this.portletWindow, key, value);
    }

    /**
     * @see PortletResponse#createElement(String)
     * 
     * @see org.apache.pluto.container.PortletResponseContext#createElement(java.lang.String)
     */
    @Override
    public Element createElement(String tagName) throws DOMException {
        // TODO this is terribly inefficient
        final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder;
        try {
            docBuilder = dbfac.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();
            return doc.createElement(tagName);
        }
        catch (ParserConfigurationException e) {
            throw new DOMException((short) 0, "Initialization failure");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#getResourceURLProvider()
     */
    @Override
    public ResourceURLProvider getResourceURLProvider() {
        return this.portletUrlCreator.createResourceUrlProvider(this.portletWindow, this.containerRequest, this.containerResponse);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#close()
     */
    @Override
    public void close() {
        this.closed = true;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletResponseContext#release()
     */
    @Override
    public void release() {
        this.closed = true;
        this.released = true;
        this.servletRequest = null;
        this.servletResponse = null;
    }


    protected boolean isClosed() {
        return this.closed;
    }

    protected boolean isReleased() {
        return this.released;
    }
}
