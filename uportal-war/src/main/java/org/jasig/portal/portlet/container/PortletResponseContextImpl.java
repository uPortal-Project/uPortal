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
import org.jasig.portal.portlet.container.services.IPortletCookieService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.ResourceUrlProviderImpl;
import org.springframework.util.Assert;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletResponseContextImpl extends AbstractPortletContextImpl implements PortletResponseContext {
    private boolean closed = false;
    private boolean released = false;
    
    protected final IRequestPropertiesManager requestPropertiesManager;
    
    public PortletResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortletCookieService portletCookieService) {
        super(portletContainer, portletWindow, containerRequest, containerResponse, portletCookieService);
        
        Assert.notNull(requestPropertiesManager, "requestPropertiesManager can not be null");
        
        this.requestPropertiesManager = requestPropertiesManager;
    }

    @Override
    public void addProperty(Cookie cookie) {
        final IPortletWindowId portletWindowId = this.portletWindow.getPortletWindowId();
        this.portletCookieService.addCookie(this.servletRequest, portletWindowId, cookie);
    }

    @Override
    public final void addProperty(String key, Element element) {
        //uPortal doesn't support XML properties
    }

    @Override
    public final void addProperty(String key, String value) {
        managerAddProperty(key, value);
    }

    @Override
    public final void setProperty(String key, String value) {
        managerSetProperty(key, value);
    }

    protected boolean managerSetProperty(String key, String value) {
        return this.requestPropertiesManager.setResponseProperty(this.servletRequest, this.portletWindow, key, value);
    }

    protected boolean managerAddProperty(String key, String value) {
        return this.requestPropertiesManager.addResponseProperty(this.servletRequest, this.portletWindow, key, value);
    }

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

    @Override
    public ResourceURLProvider getResourceURLProvider() {
        return new ResourceUrlProviderImpl(portletWindow, containerRequest);
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public void release() {
        this.closed = true;
        this.released = true;
        this.servletRequest = null;
        this.servletResponse = null;
    }

    /**
     * Check if the status of the response, if the context has been closed or released an {@link IllegalStateException}
     * is thrown. 
     */
    protected void checkContextStatus() {
        if (this.closed || this.released) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " has been closed");
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean isReleased() {
        return this.released;
    }
}
