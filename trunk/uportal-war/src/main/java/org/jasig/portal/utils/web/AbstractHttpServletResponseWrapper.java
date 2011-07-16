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
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.Validate;

/**
 * A custom HttpServletResponse wrapper that does NOT extend {@link HttpServletResponseWrapper}
 * to ensure the container can not unwrap too far.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractHttpServletResponseWrapper implements HttpServletResponse {
    public static final String PORTAL_ATTRIBUTE_PREFIX = "org.jasig.portal.";

    private final HttpServletResponse httpServletResponse;

    public AbstractHttpServletResponseWrapper(HttpServletResponse httpServletResponse) {
        Validate.notNull(httpServletResponse, "request can not be null");
        this.httpServletResponse = httpServletResponse;
    }

    public final HttpServletResponse getWrappedResponse() {
        return this.httpServletResponse;
    }

    @Override
    public void addCookie(Cookie cookie) {
        this.httpServletResponse.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.httpServletResponse.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return this.httpServletResponse.encodeURL(url);
    }

    @Override
    public String getCharacterEncoding() {
        return this.httpServletResponse.getCharacterEncoding();
    }

    @Override
    public String encodeRedirectURL(String url) {
        return this.httpServletResponse.encodeRedirectURL(url);
    }

    @Override
    public String getContentType() {
        return this.httpServletResponse.getContentType();
    }

    @Override
    public String encodeUrl(String url) {
        return this.httpServletResponse.encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return this.httpServletResponse.encodeRedirectUrl(url);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.httpServletResponse.getOutputStream();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.httpServletResponse.sendError(sc, msg);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.httpServletResponse.getWriter();
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.httpServletResponse.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.httpServletResponse.sendRedirect(location);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.httpServletResponse.setCharacterEncoding(charset);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.httpServletResponse.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.httpServletResponse.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.httpServletResponse.setHeader(name, value);
    }

    @Override
    public void setContentLength(int len) {
        this.httpServletResponse.setContentLength(len);
    }

    @Override
    public void setContentType(String type) {
        this.httpServletResponse.setContentType(type);
    }

    @Override
    public void addHeader(String name, String value) {
        this.httpServletResponse.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.httpServletResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.httpServletResponse.addIntHeader(name, value);
    }

    @Override
    public void setBufferSize(int size) {
        this.httpServletResponse.setBufferSize(size);
    }

    @Override
    public void setStatus(int sc) {
        this.httpServletResponse.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.httpServletResponse.setStatus(sc, sm);
    }

    @Override
    public int getBufferSize() {
        return this.httpServletResponse.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.httpServletResponse.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        this.httpServletResponse.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return this.httpServletResponse.isCommitted();
    }

    @Override
    public void reset() {
        this.httpServletResponse.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        this.httpServletResponse.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return this.httpServletResponse.getLocale();
    }
}
