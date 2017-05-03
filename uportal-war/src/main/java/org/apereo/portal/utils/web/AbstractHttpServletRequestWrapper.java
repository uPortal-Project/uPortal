/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.lang.Validate;

/**
 * A custom HttpServletRequest wrapper that does NOT extend {@link HttpServletRequestWrapper} to
 * ensure the container can not unwrap too far.
 *
 */
public abstract class AbstractHttpServletRequestWrapper implements HttpServletRequest {
    public static final String PORTAL_ATTRIBUTE_PREFIX = "org.apereo.portal.";

    private final HttpServletRequest httpServletRequest;

    public AbstractHttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        Validate.notNull(httpServletRequest, "request can not be null");
        this.httpServletRequest = httpServletRequest;
    }

    public final HttpServletRequest getWrappedRequest() {
        return this.httpServletRequest;
    }

    @Override
    public Object getAttribute(String name) {
        return this.httpServletRequest.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return this.httpServletRequest.getAttributeNames();
    }

    @Override
    public String getAuthType() {
        return this.httpServletRequest.getAuthType();
    }

    @Override
    public String getCharacterEncoding() {
        return this.httpServletRequest.getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return this.httpServletRequest.getContentLength();
    }

    @Override
    public String getContentType() {
        return this.httpServletRequest.getContentType();
    }

    @Override
    public String getContextPath() {
        return this.httpServletRequest.getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return this.httpServletRequest.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return this.httpServletRequest.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return this.httpServletRequest.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return this.httpServletRequest.getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return this.httpServletRequest.getHeaders(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.httpServletRequest.getInputStream();
    }

    @Override
    public int getIntHeader(String name) {
        return this.httpServletRequest.getIntHeader(name);
    }

    @Override
    public String getLocalAddr() {
        return this.httpServletRequest.getLocalAddr();
    }

    @Override
    public Locale getLocale() {
        return this.httpServletRequest.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return this.httpServletRequest.getLocales();
    }

    @Override
    public String getLocalName() {
        return this.httpServletRequest.getLocalName();
    }

    @Override
    public int getLocalPort() {
        return this.httpServletRequest.getLocalPort();
    }

    @Override
    public String getMethod() {
        return this.httpServletRequest.getMethod();
    }

    @Override
    public String getParameter(String name) {
        return this.httpServletRequest.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.httpServletRequest.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return this.httpServletRequest.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.httpServletRequest.getParameterValues(name);
    }

    @Override
    public String getPathInfo() {
        return this.httpServletRequest.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return this.httpServletRequest.getPathTranslated();
    }

    @Override
    public String getProtocol() {
        return this.httpServletRequest.getProtocol();
    }

    @Override
    public String getQueryString() {
        return this.httpServletRequest.getQueryString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return this.httpServletRequest.getReader();
    }

    @Override
    public String getRealPath(String path) {
        return this.getServletContext().getRealPath(path);
    }

    @Override
    public String getRemoteAddr() {
        return this.httpServletRequest.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return this.httpServletRequest.getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return this.httpServletRequest.getRemotePort();
    }

    @Override
    public String getRemoteUser() {
        return this.httpServletRequest.getRemoteUser();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.httpServletRequest.getRequestDispatcher(path);
    }

    @Override
    public String getRequestedSessionId() {
        return this.httpServletRequest.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return this.httpServletRequest.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return this.httpServletRequest.getRequestURL();
    }

    @Override
    public String getScheme() {
        return this.httpServletRequest.getScheme();
    }

    @Override
    public String getServerName() {
        return this.httpServletRequest.getServerName();
    }

    @Override
    public int getServerPort() {
        return this.httpServletRequest.getServerPort();
    }

    @Override
    public String getServletPath() {
        return this.httpServletRequest.getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return this.httpServletRequest.getSession();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return this.httpServletRequest.getSession(create);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.httpServletRequest.getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.httpServletRequest.isRequestedSessionIdFromCookie();
    }

    /** @see HttpServletRequest#isRequestedSessionIdFromUrl() */
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return this.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.httpServletRequest.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.httpServletRequest.isRequestedSessionIdValid();
    }

    @Override
    public boolean isSecure() {
        return this.httpServletRequest.isSecure();
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.httpServletRequest.isUserInRole(role);
    }

    @Override
    public void removeAttribute(String name) {
        this.httpServletRequest.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.httpServletRequest.setAttribute(name, o);
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.httpServletRequest.setCharacterEncoding(env);
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.httpServletRequest.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.httpServletRequest.getDispatcherType();
    }

    @Override
    public ServletContext getServletContext() {
        return this.httpServletRequest.getServletContext();
    }

    @Override
    public boolean isAsyncStarted() {
        return this.httpServletRequest.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return this.httpServletRequest.isAsyncSupported();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return this.httpServletRequest.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response)
            throws IllegalStateException {
        return this.httpServletRequest.startAsync(request, response);
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return this.httpServletRequest.authenticate(response);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return this.httpServletRequest.getPart(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return this.httpServletRequest.getParts();
    }

    @Override
    public void login(String uid, String password) throws ServletException {
        this.httpServletRequest.login(uid, password);
    }

    @Override
    public void logout() throws ServletException {
        this.httpServletRequest.logout();
    }
}
