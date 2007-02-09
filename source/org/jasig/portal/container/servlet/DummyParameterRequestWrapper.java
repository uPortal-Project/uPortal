/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * This {@link javax.servlet.http.HttpServletRequestWrapper} overrides the
 * parameter related methods of a {@link javax.servlet.http.HttpServletRequest}.
 * It will either use an empty {@link Map} or the {@link Map} provided to the
 * constructer to answer parameter questions.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class DummyParameterRequestWrapper extends AttributeRequestWrapper {
    final Map params;
    
    /**
     * Creates a wrapper that has no parameters. This will hide and parameters
     * from the wrapped request.
     * 
     * @param request The request to wrap.
     */
    public DummyParameterRequestWrapper(final HttpServletRequest request) {
        this(request, null);
    }
    
    /**
     * Creates a wrapper that uses the specified parameters. Passing
     * <code>null</code> in is the same as calling {@link DummyParameterRequestWrapper#DummyParameterRequestWrapper(HttpServletRequest)}.
     * 
     * @param request The request to wrap.
     * @param dummyParameters The {@link Map} to use as the backing for the paramters of this request, may be <code>null</code>.
     */
    public DummyParameterRequestWrapper(final HttpServletRequest request, final Map dummyParameters) {
        super(request);
        
        if (dummyParameters != null)
            params = Collections.unmodifiableMap(dummyParameters);
        else
            params = Collections.EMPTY_MAP;
    }
    
    /* 
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(final String name) {
        final String[] values = this.getParameterValues(name);
        
        if (values != null)
            return values[0];
        else
            return null;
    }

    /* 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.params;
    }

    /* 
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.params.keySet());
    }

    /* 
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(final String name) {
        return (String[])this.params.get(name);
    }
}
