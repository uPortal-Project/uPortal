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

package org.jasig.portal.container.servlet;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.jasig.portal.container.services.information.PortletStateManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * A wrapper of the real HttpServletRequest that allows
 * modification of the request parameters.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ServletRequestImpl extends HttpServletRequestWrapper {
    
    protected Hashtable parameters;
    
    public ServletRequestImpl(HttpServletRequest request) {
        super(request);
        parameters = new Hashtable(request.getParameterMap());
		parameters.putAll(PortletStateManager.getURLDecodedParameters(request));
    }
    
    public String getParameter(String name) {
        return (String)parameters.get(name);
    }

    public Map getParameterMap() {
        return parameters;
    }

    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    public String[] getParameterValues(String arg0) {
        return (String[])parameters.values().toArray(new String[0]);
    }

    /**
     * Replaces the existing request parameters with a new set
     * of parameters.
     * @param parameters the new parameters
     */
    public void setParameters(Map parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

}