/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Scopes set request attributes to just this request. Attribute retrieval methods fall through
 * to the parent request on a miss. Only the scoped attribute names are enumerated by {@link #getAttributeNames()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeScopingHttpServletRequestWrapper extends AbstractHttpServletRequestWrapper {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the portal's {@link HttpServletRequest} object
     * will be available.
     */
    public static final String ATTRIBUTE__PORTAL_HTTP_SERVLET_REQUEST = "org.jasig.portal.servlet.PORTAL_HTTP_SERVLET_REQUEST";
    
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    
    /**
     * @param httpServletRequest
     */
    public AttributeScopingHttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }

    @Override
    public Object getAttribute(String name) {
        if (ATTRIBUTE__PORTAL_HTTP_SERVLET_REQUEST.equals(name)) {
            return this;
        }
        
        final Object attribute = this.attributes.get(name);
        if (attribute != null) {
            return attribute;
        }
        
        return super.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        final Set<String> attributeNames = this.attributes.keySet();
        return Collections.enumeration(attributeNames);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);
    }
}
