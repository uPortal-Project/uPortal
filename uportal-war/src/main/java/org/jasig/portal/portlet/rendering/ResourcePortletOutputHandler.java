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
package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

/**
 * PortletOutputHandler that delegates all methods directly to a {@link HttpServletResponse}
 * 
 * @author Eric Dalquist
 */
public class ResourcePortletOutputHandler implements PortletResourceOutputHandler {
    private final HttpServletResponse response;
    
    public ResourcePortletOutputHandler(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public PrintWriter getPrintWriter() throws IOException {
        return this.response.getWriter();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.response.getOutputStream();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.response.flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return this.response.getBufferSize();
    }

    @Override
    public boolean isCommitted() {
        return this.response.isCommitted();
    }

    @Override
    public void reset() {
        this.response.reset();
    }

    @Override
    public void resetBuffer() {
        this.response.resetBuffer();
    }

    @Override
    public void setBufferSize(int size) {
        this.response.setBufferSize(size);
    }

    
    @Override
    public void setContentType(String contentType) {
        this.response.setContentType(contentType);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.response.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        this.response.setContentLength(len);
    }

    @Override
    public void setLocale(Locale locale) {
        this.response.setLocale(locale);
    }

    @Override
    public void setStatus(int status) {
        this.response.setStatus(status);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.response.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.response.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.response.setHeader(name, value);        
    }

    @Override
    public void addHeader(String name, String value) {
        this.response.addHeader(name, value);        
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.response.setIntHeader(name, value);        
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.response.addIntHeader(name, value);
    }
}
