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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.portlet.CacheControl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletMimeResponseContext;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.portlet.container.properties.IRequestPropertiesManager;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletMimeResponseContextImpl extends PortletResponseContextImpl implements PortletMimeResponseContext {
    private CacheControl cacheControl;
    
    public PortletMimeResponseContextImpl(PortletContainer portletContainer, IPortletWindow portletWindow,
            HttpServletRequest containerRequest, HttpServletResponse containerResponse,
            IRequestPropertiesManager requestPropertiesManager, IPortalUrlProvider portalUrlProvider) {

        super(portletContainer, portletWindow, containerRequest, containerResponse, 
                requestPropertiesManager, portalUrlProvider);

    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
        if (!this.isClosed()) {
            this.servletResponse.flushBuffer();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getBufferSize()
     */
    @Override
    public int getBufferSize() {
        return this.servletResponse.getBufferSize();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getCacheControl()
     */
    @Override
    public CacheControl getCacheControl() {
        if (this.isClosed()) {
            return null;
        }
        if (this.cacheControl == null) {
            this.cacheControl = new CacheControlImpl();
        }
        return this.cacheControl;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        return this.isClosed() ? null : this.servletResponse.getCharacterEncoding();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getContentType()
     */
    @Override
    public String getContentType() {
        return this.isClosed() ? null : this.servletResponse.getContentType();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getLocale()
     */
    @Override
    public Locale getLocale() {
        return this.isClosed() ? null : this.servletResponse.getLocale();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException, IllegalStateException {
        return this.isClosed() ? null : this.servletResponse.getOutputStream();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getPortletURLProvider(org.apache.pluto.container.PortletURLProvider.TYPE)
     */
    @Override
    public PortletURLProvider getPortletURLProvider(TYPE type) {
        return this.portalUrlProvider.getPortletUrl(type, this.containerRequest, this.portletWindow.getPortletWindowId());
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException, IllegalStateException {
        return this.isClosed() ? null : this.servletResponse.getWriter();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#isCommitted()
     */
    @Override
    public boolean isCommitted() {
        return this.servletResponse.isCommitted();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#reset()
     */
    @Override
    public void reset() {
        this.servletResponse.reset();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#resetBuffer()
     */
    @Override
    public void resetBuffer() {
        if (!this.isClosed()) {
            this.servletResponse.resetBuffer();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int size) {
        if (!this.isClosed()) {
            this.servletResponse.setBufferSize(size);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.container.PortletMimeResponseContext#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String contentType) {
        if (!this.isClosed()) {
            this.servletResponse.setContentType(contentType);
        }
    }

}
