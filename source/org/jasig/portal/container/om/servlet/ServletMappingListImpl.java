/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ServletMappingListImpl implements Serializable {

    private Map servletMappings = null; // Servlet name --> ServletMappingImpl
    
    public ServletMappingListImpl() {
        servletMappings = new HashMap();
    }
    
    public Iterator iterator() {
        return servletMappings.values().iterator();
    }

    public ServletMappingImpl get(String name) {
        return (ServletMappingImpl)servletMappings.get(name);
    }

    public ServletMappingImpl remove(String name) {
        return (ServletMappingImpl)servletMappings.remove(name);
    }

    public void remove(ServletMappingImpl servletMapping) {
        servletMappings.remove(servletMapping.getServletName());
    }
        
    public void add(ServletMappingImpl servletMapping) {
        servletMappings.put(servletMapping.getServletName(), servletMapping);
    }
    
    public int size() {
        return servletMappings.size();
    }

}
