/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

/**
 * WritableHttpServletRequestImpl description.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public class WritableHttpServletRequestImpl implements IWritableHttpServletRequest {
    private final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
    private final HttpServletRequest request;

    /**
     * Construct a writable this.request wrapping a real this.request
     * @param this.request Request to wrap, can not be null.
     */
    @SuppressWarnings("unchecked")
    public WritableHttpServletRequestImpl(HttpServletRequest request) {
        Validate.notNull(request, "HttpServletRequest can not be null");
        this.request = request;

        // place all parameters into the map
        this.parameterMap.putAll(request.getParameterMap());
    }

    public boolean addParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        final String[] currentValues = this.parameterMap.get(name);
        final String[] newValues;
        
        if (currentValues == null) {
            newValues = new String[] { value };
        }
        else {
            newValues = (String[])ArrayUtils.add(currentValues,  value);
        }
        
        this.parameterMap.put(name, newValues);
        return currentValues != null;
    }

    public boolean setParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        return this.parameterMap.put(name, new String[] { value }) != null;
    }

    public boolean setParameterValues(String name, String[] values) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(values, "values can not be null");
        Validate.noNullElements(values, "values can not contain null elements");
        
        return this.parameterMap.put(name, values) != null;
    }

    public boolean deleteParameter(String name) {
        Validate.notNull(name, "name can not be null");
        
        return this.parameterMap.remove(name) != null;
    }

    public boolean deleteParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        final String[] currentValues = this.parameterMap.get(name);
        if (currentValues == null) {
            return false;
        }

        final List<String> newValues = new ArrayList<String>(currentValues.length);
        for (final String currentValue : currentValues) {
            if (value != currentValue && !value.equals(currentValue)) {
                newValues.add(currentValue);
            }
        }
        
        this.parameterMap.put(name, newValues.toArray(new String[newValues.size()]));

        return currentValues.length != newValues.size();
    }

    
    public String getParameter(String name) {
        Validate.notNull(name, "name can not be null");
        
        final String[] pValues = this.parameterMap.get(name);
        
        if (pValues != null && pValues.length > 0) {
            return pValues[0];
        }

        return null;
    }

    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameterMap.keySet());
    }

    public String[] getParameterValues(String name) {
        return this.parameterMap.get(name);
    }
    
    

    public Object getAttribute(String name) {
        return this.request.getAttribute(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        return this.request.getAttributeNames();
    }

    public String getAuthType() {
        return this.request.getAuthType();
    }

    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }

    public int getContentLength() {
        return this.request.getContentLength();
    }

    public String getContentType() {
        return this.request.getContentType();
    }

    public String getContextPath() {
        return this.request.getContextPath();
    }

    public Cookie[] getCookies() {
        return this.request.getCookies();
    }

    public long getDateHeader(String name) {
        return this.request.getDateHeader(name);
    }

    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {
        return this.request.getHeaderNames();
    }

    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(String name) {
        return this.request.getHeaders(name);
    }

    public ServletInputStream getInputStream() throws IOException {
        return this.request.getInputStream();
    }

    public int getIntHeader(String name) {
        return this.request.getIntHeader(name);
    }

    public Locale getLocale() {
        return this.request.getLocale();
    }

    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {
        return this.request.getLocales();
    }

    public String getMethod() {
        return this.request.getMethod();
    }

    public String getPathInfo() {
        return this.request.getPathInfo();
    }

    public String getPathTranslated() {
        return this.request.getPathTranslated();
    }

    public String getProtocol() {
        return this.request.getProtocol();
    }

    public String getQueryString() {
        return this.request.getQueryString();
    }

    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }

    /**
     * @deprecated
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public String getRealPath(String arg0) {
        return this.request.getRealPath(arg0);
    }

    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }

    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }

    public String getRemoteUser() {
        return this.request.getRemoteUser();
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return this.request.getRequestDispatcher(arg0);
    }

    public String getRequestedSessionId() {
        return this.request.getRequestedSessionId();
    }

    public String getRequestURI() {
        return this.request.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return this.request.getRequestURL();
    }

    public String getScheme() {
        return this.request.getScheme();
    }

    public String getServerName() {
        return this.request.getServerName();
    }

    public int getServerPort() {
        return this.request.getServerPort();
    }

    public String getServletPath() {
        return this.request.getServletPath();
    }

    public HttpSession getSession() {
        return this.request.getSession();
    }

    public HttpSession getSession(boolean arg0) {
        return this.request.getSession(arg0);
    }

    public Principal getUserPrincipal() {
        return this.request.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return this.request.isRequestedSessionIdFromCookie();
    }

    /**
     * @deprecated
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {
        return this.request.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdFromURL() {
        return this.request.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdValid() {
        return this.request.isRequestedSessionIdValid();
    }

    public boolean isSecure() {
        return this.request.isSecure();
    }

    public boolean isUserInRole(String role) {
        return this.request.isUserInRole(role);
    }

    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    public void setAttribute(String name, Object o) {
        this.request.setAttribute(name, o);
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.request.setCharacterEncoding(env);
    }

    public String getLocalAddr() {
        return this.request.getLocalAddr();
    }

    public String getLocalName() {
        return this.request.getLocalName();
    }

    public int getLocalPort() {
        return this.request.getLocalPort();
    }

    public int getRemotePort() {
        return this.request.getRemotePort();
    }
}
