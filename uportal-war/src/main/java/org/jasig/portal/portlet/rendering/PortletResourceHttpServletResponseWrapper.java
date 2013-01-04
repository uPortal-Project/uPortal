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

import java.util.Locale;

import javax.portlet.CacheControl;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.utils.Servlet3WrapperUtils;
import org.jasig.portal.utils.web.PortletMimeHttpServletResponseWrapper;

/**
 * Wrapper for the portal response when a portlet is handling a resource request. As much is
 * delegated to the {@link PortletResourceOutputHandler} as possible
 * 
 * @author Eric Dalquist
 */
public class PortletResourceHttpServletResponseWrapper extends PortletMimeHttpServletResponseWrapper {
    private final PortletResourceOutputHandler portletResourceOutputHandler;

    public static HttpServletResponse create(HttpServletResponse httpServletResponse, IPortletWindow portletWindow,
            PortletResourceOutputHandler portletResourceOutputHandler, CacheControl cacheControl) {
        final HttpServletResponse proxy = new PortletResourceHttpServletResponseWrapper(httpServletResponse, portletWindow, portletResourceOutputHandler, cacheControl);
        return Servlet3WrapperUtils.addServlet3Wrapper(proxy, httpServletResponse);
    }

    PortletResourceHttpServletResponseWrapper(HttpServletResponse httpServletResponse, IPortletWindow portletWindow,
            PortletResourceOutputHandler portletResourceOutputHandler, CacheControl cacheControl) {
        super(httpServletResponse, portletWindow, portletResourceOutputHandler, cacheControl);
        this.portletResourceOutputHandler = portletResourceOutputHandler;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.portletResourceOutputHandler.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        this.portletResourceOutputHandler.setContentLength(len);
    }

    @Override
    public void setLocale(Locale locale) {
        this.portletResourceOutputHandler.setLocale(locale);
    }

    @Override
    public void setStatus(int status) {
        this.portletResourceOutputHandler.setStatus(status);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.portletResourceOutputHandler.setStatus(sc);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.portletResourceOutputHandler.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.portletResourceOutputHandler.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.portletResourceOutputHandler.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.portletResourceOutputHandler.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.portletResourceOutputHandler.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.portletResourceOutputHandler.addIntHeader(name, value);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.portletResourceOutputHandler.containsHeader(name);
    }

    @Override
    public String getCharacterEncoding() {
        return this.portletResourceOutputHandler.getCharacterEncoding();
    }

    @Override
    public Locale getLocale() {
        return this.portletResourceOutputHandler.getLocale();
    }
}
