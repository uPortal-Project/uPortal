/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.iterators.IteratorEnumeration;


/**
 * HttpServletRequest wrapper that tracks a set of request attributes
 * that are local to this request.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Id$
 */
public class PortletAttributeRequestWrapper extends HttpServletRequestWrapper {
    private final Map scopedAttributes = new Hashtable();
    
    public PortletAttributeRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    
    /**
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        if (name == null)
            throw new IllegalArgumentException("Attribute name cannot be null");
        
        final Object value = this.scopedAttributes.get(name);

        if (value != null)
            return value;
        else
            return super.getAttribute(name);
    }

    /**
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        final Set namesSet = new HashSet();
        
        for (final Enumeration parentNames = super.getAttributeNames(); parentNames.hasMoreElements();) {
            namesSet.add(parentNames.nextElement());
        }
        
        namesSet.addAll(this.scopedAttributes.keySet());
        
        return new IteratorEnumeration(namesSet.iterator());
    }
    
    /**
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        if (name == null)
            throw new IllegalArgumentException("Attribute name cannot be null");
        
        this.scopedAttributes.remove(name);
        super.removeAttribute(name);
    }
    
    /**
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (name == null)
            throw new IllegalArgumentException("Attribute name cannot be null");
        
        if (value == null)
            this.scopedAttributes.remove(name);
        else
            this.scopedAttributes.put(name, value);
    }
}
