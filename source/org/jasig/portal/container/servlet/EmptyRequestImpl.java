/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * Hides request parameters.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class EmptyRequestImpl extends HttpServletRequestWrapper {

    
    // Define empty collections that can be reused so as not to
    // create unnecessary new objects every time one of this class's
    // methods are called.
    private static final Map emptyMap = new HashMap(0);
    private static final String[] emptyStringArray = new String[] {};
    private static final Enumeration emptyEnumeration = new Vector(0).elements();

    public EmptyRequestImpl(HttpServletRequest request) {
        super(request);
    }
    
    public String getParameter(String name) {
        return null;
    }

    public Map getParameterMap() {
        return emptyMap;
    }

    public Enumeration getParameterNames() {
        return emptyEnumeration;
    }

    public String[] getParameterValues(String arg0) {
        return emptyStringArray;
    }

}
