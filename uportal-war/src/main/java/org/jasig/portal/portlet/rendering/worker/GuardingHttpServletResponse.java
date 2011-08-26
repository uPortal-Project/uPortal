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

package org.jasig.portal.portlet.rendering.worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper for a {@link HttpServletResponse} that makes the response inaccessible once the worker has been canceled. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class GuardingHttpServletResponse extends HttpServletResponseWrapper {
    private final AtomicBoolean canceled;

    public GuardingHttpServletResponse(HttpServletResponse request, AtomicBoolean canceled) {
        super(request);
        this.canceled = canceled;
    }

    private final void checkState() {
        if (this.canceled.get()) {
            throw new IllegalStateException("The portlet worker has been canceled, the response is no longer in a valid state");
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addCookie(javax.servlet.http.Cookie)
     */
    @Override
    public void addCookie(Cookie cookie) {
        this.checkState();
        super.addCookie(cookie);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#containsHeader(java.lang.String)
     */
    @Override
    public boolean containsHeader(String name) {
        this.checkState();
        return super.containsHeader(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#encodeURL(java.lang.String)
     */
    @Override
    public String encodeURL(String url) {
        this.checkState();
        return super.encodeURL(url);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#encodeRedirectURL(java.lang.String)
     */
    @Override
    public String encodeRedirectURL(String url) {
        this.checkState();
        return super.encodeRedirectURL(url);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#encodeUrl(java.lang.String)
     */
    @Override
    public String encodeUrl(String url) {
        this.checkState();
        return super.encodeUrl(url);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#encodeRedirectUrl(java.lang.String)
     */
    @Override
    public String encodeRedirectUrl(String url) {
        this.checkState();
        return super.encodeRedirectUrl(url);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int, java.lang.String)
     */
    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.checkState();
        super.sendError(sc, msg);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int)
     */
    @Override
    public void sendError(int sc) throws IOException {
        this.checkState();
        super.sendError(sc);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect(String location) throws IOException {
        this.checkState();
        super.sendRedirect(location);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String name, long date) {
        this.checkState();
        super.setDateHeader(name, date);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String name, long date) {
        this.checkState();
        super.addDateHeader(name, date);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {
        this.checkState();
        super.setHeader(name, value);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {
        this.checkState();
        super.addHeader(name, value);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String name, int value) {
        this.checkState();
        super.setIntHeader(name, value);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String name, int value) {
        this.checkState();
        super.addIntHeader(name, value);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    @Override
    public void setStatus(int sc) {
        this.checkState();
        super.setStatus(sc);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int, java.lang.String)
     */
    @Override
    public void setStatus(int sc, String sm) {
        this.checkState();
        super.setStatus(sc, sm);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getResponse()
     */
    @Override
    public ServletResponse getResponse() {
        this.checkState();
        return super.getResponse();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setResponse(javax.servlet.ServletResponse)
     */
    @Override
    public void setResponse(ServletResponse response) {
        this.checkState();
        super.setResponse(response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String charset) {
        this.checkState();
        super.setCharacterEncoding(charset);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        this.checkState();
        return super.getCharacterEncoding();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        this.checkState();
        return super.getOutputStream();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        this.checkState();
        return super.getWriter();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
     */
    @Override
    public void setContentLength(int len) {
        this.checkState();
        super.setContentLength(len);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String type) {
        this.checkState();
        super.setContentType(type);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getContentType()
     */
    @Override
    public String getContentType() {
        this.checkState();
        return super.getContentType();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int size) {
        this.checkState();
        super.setBufferSize(size);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getBufferSize()
     */
    @Override
    public int getBufferSize() {
        this.checkState();
        return super.getBufferSize();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
        this.checkState();
        super.flushBuffer();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#isCommitted()
     */
    @Override
    public boolean isCommitted() {
        this.checkState();
        return super.isCommitted();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#reset()
     */
    @Override
    public void reset() {
        this.checkState();
        super.reset();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#resetBuffer()
     */
    @Override
    public void resetBuffer() {
        this.checkState();
        super.resetBuffer();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale loc) {
        this.checkState();
        super.setLocale(loc);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getLocale()
     */
    @Override
    public Locale getLocale() {
        this.checkState();
        return super.getLocale();
    }
}
