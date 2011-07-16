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

package org.jasig.portal.utils.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


/**
 * Scopes set request attributes to just this request.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletHttpServletRequestWrapper extends AbstractHttpServletRequestWrapper {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that this {@link HttpServletRequest} object
     * will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_REQUEST = PortletHttpServletRequestWrapper.class.getName() + ".PORTLET_HTTP_SERVLET_REQUEST";
    
    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    
    public PortletHttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }

    @Override
    public Object getAttribute(String name) {
        if (ATTRIBUTE__HTTP_SERVLET_REQUEST.equals(name)) {
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
