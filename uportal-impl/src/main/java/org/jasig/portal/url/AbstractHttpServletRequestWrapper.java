/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;

/**
 * A custom HttpServletRequest wrapper that does NOT extend {@link HttpServletRequestWrapper}
 * to ensure the container can not unwrap too far.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractHttpServletRequestWrapper implements HttpServletRequest {
    private final HttpServletRequest httpServletRequest;
    
    public AbstractHttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        Validate.notNull(httpServletRequest, "request can not be null");
        this.httpServletRequest = httpServletRequest;
    }
    
    public final HttpServletRequest getWrappedRequest() {
        return this.httpServletRequest;
    }

    public Object getAttribute(String name) {
        return this.httpServletRequest.getAttribute(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        return this.httpServletRequest.getAttributeNames();
    }

    public String getAuthType() {
        return this.httpServletRequest.getAuthType();
    }

    public String getCharacterEncoding() {
        return this.httpServletRequest.getCharacterEncoding();
    }

    public int getContentLength() {
        return this.httpServletRequest.getContentLength();
    }

    public String getContentType() {
        return this.httpServletRequest.getContentType();
    }

    public String getContextPath() {
        return this.httpServletRequest.getContextPath();
    }

    public Cookie[] getCookies() {
        return this.httpServletRequest.getCookies();
    }

    public long getDateHeader(String name) {
        return this.httpServletRequest.getDateHeader(name);
    }

    public String getHeader(String name) {
        return this.httpServletRequest.getHeader(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {
        return this.httpServletRequest.getHeaderNames();
    }

    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(String name) {
        return this.httpServletRequest.getHeaders(name);
    }

    public ServletInputStream getInputStream() throws IOException {
        return this.httpServletRequest.getInputStream();
    }

    public int getIntHeader(String name) {
        return this.httpServletRequest.getIntHeader(name);
    }

    public String getLocalAddr() {
        return this.httpServletRequest.getLocalAddr();
    }

    public Locale getLocale() {
        return this.httpServletRequest.getLocale();
    }

    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {
        return this.httpServletRequest.getLocales();
    }

    public String getLocalName() {
        return this.httpServletRequest.getLocalName();
    }

    public int getLocalPort() {
        return this.httpServletRequest.getLocalPort();
    }

    public String getMethod() {
        return this.httpServletRequest.getMethod();
    }

    public String getParameter(String name) {
        return this.httpServletRequest.getParameter(name);
    }

    @SuppressWarnings("unchecked")
    public Map getParameterMap() {
        return this.httpServletRequest.getParameterMap();
    }

    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        return this.httpServletRequest.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return this.httpServletRequest.getParameterValues(name);
    }

    public String getPathInfo() {
        return this.httpServletRequest.getPathInfo();
    }

    public String getPathTranslated() {
        return this.httpServletRequest.getPathTranslated();
    }

    public String getProtocol() {
        return this.httpServletRequest.getProtocol();
    }

    public String getQueryString() {
        return this.httpServletRequest.getQueryString();
    }

    public BufferedReader getReader() throws IOException {
        return this.httpServletRequest.getReader();
    }

    /**
     * @deprecated
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public String getRealPath(String path) {
        return this.httpServletRequest.getRealPath(path);
    }

    public String getRemoteAddr() {
        return this.httpServletRequest.getRemoteAddr();
    }

    public String getRemoteHost() {
        return this.httpServletRequest.getRemoteHost();
    }

    public int getRemotePort() {
        return this.httpServletRequest.getRemotePort();
    }

    public String getRemoteUser() {
        return this.httpServletRequest.getRemoteUser();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.httpServletRequest.getRequestDispatcher(path);
    }

    public String getRequestedSessionId() {
        return this.httpServletRequest.getRequestedSessionId();
    }

    public String getRequestURI() {
        return this.httpServletRequest.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return this.httpServletRequest.getRequestURL();
    }

    public String getScheme() {
        return this.httpServletRequest.getScheme();
    }

    public String getServerName() {
        return this.httpServletRequest.getServerName();
    }

    public int getServerPort() {
        return this.httpServletRequest.getServerPort();
    }

    public String getServletPath() {
        return this.httpServletRequest.getServletPath();
    }

    public HttpSession getSession() {
        return this.httpServletRequest.getSession();
    }

    public HttpSession getSession(boolean create) {
        return this.httpServletRequest.getSession(create);
    }

    public Principal getUserPrincipal() {
        return this.httpServletRequest.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return this.httpServletRequest.isRequestedSessionIdFromCookie();
    }

    /**
     * @deprecated
     * @see HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {
        return this.httpServletRequest.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdFromURL() {
        return this.httpServletRequest.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdValid() {
        return this.httpServletRequest.isRequestedSessionIdValid();
    }

    public boolean isSecure() {
        return this.httpServletRequest.isSecure();
    }

    public boolean isUserInRole(String role) {
        return this.httpServletRequest.isUserInRole(role);
    }

    public void removeAttribute(String name) {
        this.httpServletRequest.removeAttribute(name);
    }

    public void setAttribute(String name, Object o) {
        this.httpServletRequest.setAttribute(name, o);
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.httpServletRequest.setCharacterEncoding(env);
    }
}
