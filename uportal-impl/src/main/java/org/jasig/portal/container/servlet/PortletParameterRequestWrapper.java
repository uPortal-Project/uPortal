/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.jasig.portal.container.services.information.PortletStateManager;


/**
 * A {@link javax.servlet.http.HttpServletRequestWrapper} that only allows
 * non-portal paramters to be seen. The determination is done by hidding
 * parameters that begin with {@link PortletStateManager#UP_PARAM_PREFIX}.
 * All other paramters are let through.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class PortletParameterRequestWrapper extends AttributeRequestWrapper {
    private static final String ESCAPE_PREFIX = PortletStateManager.UP_PARAM_PREFIX + PortletStateManager.UP_PARAM_PREFIX;
    
    
    /**
     * Creates a new wrapper and wraps the specified request.
     * 
     * @param request The requst to wrap.
     * @see HttpServletRequestWrapper#HttpServletRequestWrapper(javax.servlet.http.HttpServletRequest)
     */
    public PortletParameterRequestWrapper(final HttpServletRequest request) {
        super(request);
    }
    
    /* 
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(final String name) {
        if (name != null && name.startsWith(PortletStateManager.UP_PARAM_PREFIX)) {
            return null;
        }
        else {
            return super.getParameter(name);
        }
    }

    /* 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.getPortletParameterMap();
    }

    /* 
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.getParameterMap().keySet());
    }

    /* 
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(final String name) {
        if (name != null && name.startsWith(PortletStateManager.UP_PARAM_PREFIX)) {
            return null;
        }
        else {
            return super.getParameterValues(name);
        }
    }
    
    /**
     * Does the work of creating a {@link Map} of non-portal parameters
     * for the public methods of this class to use.
     * 
     * @return A {@link Map} of non-portal parameters.
     */
    private Map getPortletParameterMap() {
        final Map allParams = super.getParameterMap();
        final Map portletParams = new HashMap(allParams.size() * 2);
        
        for (final Iterator pNameItr = allParams.keySet().iterator(); pNameItr.hasNext(); ) {
            final String pName = (String)pNameItr.next();
            
            if (pName == null || !pName.startsWith(PortletStateManager.UP_PARAM_PREFIX)) {
                final Object pVal = allParams.get(pName);
                
                portletParams.put(pName, pVal);
            }
            //Deals with parameters that are ok and start with the prefix
            else if (pName.startsWith(ESCAPE_PREFIX)) {
                final Object pVal = allParams.get(pName);
                
                portletParams.put(pName.substring(PortletStateManager.UP_PARAM_PREFIX.length()), pVal);
            }
        }
        
        return Collections.unmodifiableMap(portletParams);
    }
}
