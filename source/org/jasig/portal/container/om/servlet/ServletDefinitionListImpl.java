/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.ServletDefinitionListCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ServletDefinitionListImpl implements ServletDefinitionList, ServletDefinitionListCtrl, Serializable {

    private Map servletDefinitions = null; // name String --> ServletDefinition
    
    public ServletDefinitionListImpl() {
        servletDefinitions = new HashMap();
    }

    // ServletDefinitionList methods
    
    public Iterator iterator() {
        return servletDefinitions.values().iterator();
    }

    public ServletDefinition get(String name) {
        return (ServletDefinition)servletDefinitions.get(name);
    }

    // ServletDefinitionListCtrl methods
    
    public ServletDefinition add(String name, String className) {
        ServletDefinitionImpl servletDefinition = new ServletDefinitionImpl(name, className);
        servletDefinitions.put(name, servletDefinition);
        return servletDefinition;
    }

    public ServletDefinition remove(String name) {
        return (ServletDefinition)servletDefinitions.remove(name);
    }

    public void remove(ServletDefinition servletDefinition) {
        servletDefinitions.remove(servletDefinition.getServletName());
    }
    
    // Additional methods
    
    public void add(ServletDefinition servletDefinition) {
        servletDefinitions.put(servletDefinition.getServletName(), servletDefinition);
    }
    
    /**
     * Indicates the number of servlet definitions in this list
     * @return size the number of servlets definitions in this list
     */
    public int size() {
        return servletDefinitions.size();
    }

}
