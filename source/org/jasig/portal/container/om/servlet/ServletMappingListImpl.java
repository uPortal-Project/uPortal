/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
