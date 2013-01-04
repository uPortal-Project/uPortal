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

package org.jasig.portal.utils.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.LazyPrintWriter;
import org.jasig.portal.portlet.rendering.LazyServletOutputStream;
import org.jasig.portal.portlet.rendering.PortletOutputHandler;
import org.jasig.portal.utils.Servlet3WrapperUtils;

/**
 * Wrapper for portlet responses that write out content: {@link MimeResponse}. As much as possible
 * the {@link PortletOutputHandler} for the response is delegated to
 * 
 * Lazily retrieves the PrintWriter and ServletOutputStream objects waiting
 * until a method is actually called on one or the other.
 * 
 * Also ignores the close call on either the PrintWriter or ServletOutputStream if {@link CacheControl#useCachedContent()}
 * is true
 * 
 * This is needed as servlet spec says that after a {@link RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * the {@link PrintWriter}/{@link ServletOutputStream} must be closed. This is a problem for resource responses that want to use cached content
 * as after the forward is complete the portal needs to replay the cached content.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletMimeHttpServletResponseWrapper extends PortletHttpServletResponseWrapper {
    private final PortletOutputHandler portletOutputHandler;
    private final CacheControl cacheControl;
    private ServletOutputStream servletOutputStream;
    private PrintWriter printWriter;
    
    public static HttpServletResponse create(HttpServletResponse httpServletResponse, IPortletWindow portletWindow,
            PortletOutputHandler portletOutputHandler, CacheControl cacheControl) {
        final HttpServletResponse proxy = new PortletMimeHttpServletResponseWrapper(httpServletResponse, portletWindow, portletOutputHandler, cacheControl);
        return Servlet3WrapperUtils.addServlet3Wrapper(proxy, httpServletResponse);
    }

    protected PortletMimeHttpServletResponseWrapper(HttpServletResponse httpServletResponse, IPortletWindow portletWindow,
            PortletOutputHandler portletOutputHandler, CacheControl cacheControl) {
        super(httpServletResponse, portletWindow);
        this.portletOutputHandler = portletOutputHandler;
        this.cacheControl = cacheControl;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.servletOutputStream == null) {
            this.servletOutputStream = new LazyServletOutputStream(new Callable<ServletOutputStream>() {
                @Override
                public ServletOutputStream call() throws Exception {
                    return PortletMimeHttpServletResponseWrapper.super.getOutputStream();
                }
            }) {
                @Override
                public void close() throws IOException {
                    //Don't close the ServletOutputStream if useCachedContent is true, the PortletRendererImpl will be replaying cached content
                    if (!PortletMimeHttpServletResponseWrapper.this.cacheControl.useCachedContent()) {
                        super.close();
                    }
                }
            };
        }

        return this.servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.printWriter == null) {
            this.printWriter = new LazyPrintWriter(new Callable<PrintWriter>() {
                @Override
                public PrintWriter call() throws Exception {
                    return PortletMimeHttpServletResponseWrapper.super.getWriter();
                }
            }) {
                @Override
                public void close() {
                    //Don't close the PrintWriter if useCachedContent is true, the PortletRendererImpl will be replaying cached content
                    if (!PortletMimeHttpServletResponseWrapper.this.cacheControl.useCachedContent()) {
                        super.close();
                    }
                }
            };
        }

        return this.printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.portletOutputHandler.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        this.portletOutputHandler.resetBuffer();
    }

    @Override
    public void reset() {
        this.portletOutputHandler.reset();
    }

    @Override
    public void setBufferSize(int size) {
        this.portletOutputHandler.setBufferSize(size);
    }

    @Override
    public void setContentType(String type) {
        this.portletOutputHandler.setContentType(type);
    }
    
    @Override
    public String getContentType() {
        return this.portletOutputHandler.getContentType();
    }

    @Override
    public int getBufferSize() {
        return this.portletOutputHandler.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return this.portletOutputHandler.isCommitted();
    }
}
