/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HttpServletRequest wrapper that tracks a set of request attributes that are local to
 * this request wrapper.
 * 
 * @author Eric Dalquist
 * @version $Id: AttributeScopingRequestWrapper.java,v 1.1 2006/04/25 22:15:26 dalquist Exp $
 */
public class AttributeScopingRequestWrapper implements HttpServletRequest {
    private static final Log LOG = LogFactory.getLog(AttributeScopingRequestWrapper.class);

    private final Map scopedAttributes = new Hashtable();
    private final HttpServletRequest delegate;

    public AttributeScopingRequestWrapper(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Wrapped request can no be null.");
        }

        this.delegate = request;
    }

    /**
     * Provides access to the attributes stored in this wrapper.
     * @return A READ-ONLY Map of the attributes stored in this wrapper.
     */
    public Map getScopedAttributeMap() {
        return Collections.unmodifiableMap(this.scopedAttributes);
    }

    //*********************************************
    // Over-ridden methods to scope attributes.
    //*********************************************

    /**
     * If the attribute isn't in the Map for this wrapper the wrapped request is
     * consulted.
     * 
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }

        Object value = this.scopedAttributes.get(name);
        if (value != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAttribute(" + name + ") - from scoped Map with value='" + value + "'");
            }
        }
        else {
            value = this.delegate.getAttribute(name);

            if (LOG.isDebugEnabled()) {
                LOG.debug("getAttribute(" + name + ") - from parent request with value='" + value + "'");
            }
        }

        return value;
    }

    /**
     * The set of attribute names is merged from the attributes stored in this request and the
     * attributes from the parent requests.
     * 
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        final Set namesSet = new HashSet();

        for (final Enumeration parentNames = this.delegate.getAttributeNames(); parentNames.hasMoreElements();) {
            namesSet.add(parentNames.nextElement());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributeNames() - parent request attribute names=" + namesSet + "");
        }

        namesSet.addAll(this.scopedAttributes.keySet());

        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributeNames() - all attribute names=" + namesSet + "");
        }

        return new IteratorEnumeration(namesSet.iterator());
    }

    /**
     * Removes the attribute from this request and the wrapped request.
     * 
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAttribute(" + name + ")");
        }

        this.scopedAttributes.remove(name);
        this.delegate.removeAttribute(name);
    }

    /**
     * Stores the attribute in the request wrapper.
     * 
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("Attribute name cannot be null");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("setAttribute(" + name + ", " + value + ")");
        }

        if (value == null) {
            this.scopedAttributes.remove(name);
        }
        else {
            this.scopedAttributes.put(name, value);
        }
    }

    //*********************************************
    // Delegating methods
    //*********************************************

    /**
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return this.delegate.getAuthType();
    }

    /**
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return this.delegate.getCharacterEncoding();
    }

    /**
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        return this.delegate.getContentLength();
    }

    /**
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        return this.delegate.getContentType();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return this.delegate.getContextPath();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return this.delegate.getCookies();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String arg0) {
        return this.delegate.getDateHeader(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String arg0) {
        return this.delegate.getHeader(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return this.delegate.getHeaderNames();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String arg0) {
        return this.delegate.getHeaders(arg0);
    }

    /**
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        return this.delegate.getInputStream();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String arg0) {
        return this.delegate.getIntHeader(arg0);
    }

    /**
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        return this.delegate.getLocale();
    }

    /**
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        return this.delegate.getLocales();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    public String getMethod() {
        return this.delegate.getMethod();
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String arg0) {
        return this.delegate.getParameter(arg0);
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.delegate.getParameterMap();
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return this.delegate.getParameterNames();
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String arg0) {
        return this.delegate.getParameterValues(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return this.delegate.getPathInfo();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        return this.delegate.getPathTranslated();
    }

    /**
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return this.delegate.getProtocol();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return this.delegate.getQueryString();
    }

    /**
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        return this.delegate.getReader();
    }

    /**
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     * @deprecated
     */
    public String getRealPath(String arg0) {
        return this.delegate.getRealPath(arg0);
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return this.delegate.getRemoteAddr();
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return this.delegate.getRemoteHost();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return this.delegate.getRemoteUser();
    }

    /**
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return this.delegate.getRequestDispatcher(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return this.delegate.getRequestedSessionId();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        return this.delegate.getRequestURI();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return this.delegate.getRequestURL();
    }

    /**
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return this.delegate.getScheme();
    }

    /**
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        return this.delegate.getServerName();
    }

    /**
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        return this.delegate.getServerPort();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        return this.delegate.getServletPath();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        return this.delegate.getSession();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean arg0) {
        return this.delegate.getSession(arg0);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return this.delegate.getUserPrincipal();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return this.delegate.isRequestedSessionIdFromCookie();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl() {
        return this.delegate.isRequestedSessionIdFromUrl();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return this.delegate.isRequestedSessionIdFromURL();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return this.delegate.isRequestedSessionIdValid();
    }

    /**
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        return this.delegate.isSecure();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String arg0) {
        return this.delegate.isUserInRole(arg0);
    }

    /**
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        this.delegate.setCharacterEncoding(arg0);
    }
}
