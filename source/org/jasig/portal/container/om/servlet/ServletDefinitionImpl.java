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
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionCtrl;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ServletDefinitionImpl implements ServletDefinition, ServletDefinitionCtrl, Serializable {

    private ObjectID objectId = null;
    private String servletName = null;
    private DisplayNameSet displayNames = null;
    private DescriptionSet descriptions = null;
    private String servletClass = null;
    private ParameterSet parameters = null;
    private SecurityRoleRefSet initSecurityRoleRefs = null;
    private WebApplicationDefinition webApplicationDefinition = null;
    private long available = 0;
    
    private ServletMappingImpl servletMapping = null;
    
    public ServletDefinitionImpl() {
        displayNames = new DisplayNameSetImpl();
        descriptions = new DescriptionSetImpl();
        parameters = new ParameterSetImpl();
        initSecurityRoleRefs = new SecurityRoleRefSetImpl();
    }
    
    public ServletDefinitionImpl(String servletName, String servletClass) {
        this();
        setServletName(servletName);
        setServletClass(servletClass);
    }

    // ServletDefinition methods

    public ObjectID getId() {
        return objectId;
    }

    public String getServletName() {
        return servletName;
    }

    public DisplayName getDisplayName(Locale locale) {
        return displayNames.get(locale);
    }

    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    public String getServletClass() {
        return servletClass;
    }

    public ParameterSet getInitParameterSet() {
        return parameters;
    }

    public WebApplicationDefinition getWebApplicationDefinition() {
        return webApplicationDefinition;
    }

    public RequestDispatcher getRequestDispatcher(ServletContext servletContext) {
        RequestDispatcher requestDispatcher = null;
        ServletContext newContext = webApplicationDefinition.getServletContext(servletContext);
        if (newContext != null) {
            requestDispatcher = newContext.getRequestDispatcher(servletMapping.getUrlPattern());
        }
        return requestDispatcher;
    }

    public long getAvailable() {
        return available;
    }

    public boolean isUnavailable() {
        boolean unavailable = true;
        if (available == 0) {
            unavailable = false;
        } else if (available <= System.currentTimeMillis()) {
            available = 0;
            unavailable = false;
        }
        return unavailable;
    }

    // ServletDefinitionCtrl methods
    
    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }

    public void setServletName(String name) {
        this.servletName = name;
    }

    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }

    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }
    
    public void setInitParameters(ParameterSet parameters) {
        this.parameters = parameters;
    }
    
    public void setWebApplicationDefinition(WebApplicationDefinition webApplicationDefinition) {
        this.webApplicationDefinition = webApplicationDefinition;
    }

    public void setAvailable(long available) {
        this.available = available;
    }
    
    // Additional methods
    
    public ServletMappingImpl getServletMapping() {
        return this.servletMapping;
    }
    
    public void setServletMapping(String servletName, String urlPattern) {
        this.servletMapping = new ServletMappingImpl(servletName, urlPattern);
    }
    
    public SecurityRoleRefSet getSecurityRoleRefs() {
        return this.initSecurityRoleRefs;
    }
    
    public void setSecurityRoleRefs(SecurityRoleRefSet initSecurityRoleRefs) {
        this.initSecurityRoleRefs = initSecurityRoleRefs;
    }

}
