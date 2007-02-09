/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HttpServletRequestWrapper that wraps the attributes of the parent request.
 * 
 * @author Eric Andresen eandresen at unicon.net
 */
public class AttributeRequestWrapper extends HttpServletRequestWrapper {
	private final Map attributes;

    public AttributeRequestWrapper(final HttpServletRequest request) {
        super(request);
        this.attributes = new Hashtable();
    }

    public Object getAttribute(final String name) {
    	Object rslt = this.attributes.get(name);
    	
    	if (rslt == null)
    		rslt = super.getAttribute(name);
    	
    	return rslt;
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(getFullAttributesMap().keySet());
    }
    
    public void setAttribute(String name, Object obj) {
    	this.attributes.put(name, obj);
    }

    private Map getFullAttributesMap() {
    	Map rslt = new HashMap();
    	
    	Enumeration en = super.getAttributeNames();
    	while (en.hasMoreElements()) {
    		String key = (String)en.nextElement();
    		rslt.put(key, super.getAttribute(key));
    	}
    	
    	rslt.putAll(this.attributes);
    	
        return Collections.unmodifiableMap(rslt);
    }
}
