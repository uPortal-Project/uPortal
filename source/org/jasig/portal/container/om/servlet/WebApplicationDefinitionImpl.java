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

import javax.servlet.ServletContext;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WebApplicationDefinitionImpl implements WebApplicationDefinition, Serializable {

    private ObjectID objectId;
    private IconImpl icon;
    private DisplayNameSet displayNames;
    private DescriptionSet descriptions;
    private DistributableImpl distributable;
    private ParameterSet parameters;
    private FilterListImpl filters;
    private FilterMappingListImpl filterMappings;
    private ListenerListImpl listeners;
    private ServletDefinitionList servletDefinitions;
    private ServletMappingListImpl servletMappings;
    private TagLibListImpl tagLibs;
    private SecurityRoleSet securityRoles;
    private String contextPath;        
    
    public WebApplicationDefinitionImpl() {
        displayNames = new DisplayNameSetImpl();
        descriptions = new DescriptionSetImpl();
        parameters = new ParameterSetImpl();
        servletDefinitions = new ServletDefinitionListImpl();
    }
    
    // WebApplicationDefinition methods

    public ObjectID getId() {
        return objectId;
    }

    public DisplayName getDisplayName(Locale locale) {
        return displayNames.get(locale);
    }

    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    public ParameterSet getInitParameterSet() {
        return parameters;
    }

    public ServletDefinitionList getServletDefinitionList() {
        return servletDefinitions;
    }

    public ServletContext getServletContext(ServletContext servletContext) {
        return servletContext.getContext(contextPath);
    }

    public String getContextRoot() {
        return contextPath;
    }
    
    // Additional methods
    
    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }
    
    public IconImpl getIcon() {
        return this.icon;
    }
    
    public void setIcon(IconImpl icon) {
        this.icon = icon;
    }
    
    public DisplayNameSet getDisplayNames() {
        return this.displayNames;
    }

    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }   
    
    public void addDisplayName(String displayName, Locale locale) {
        ((DisplayNameSetImpl)displayNames).add(displayName, locale);
    }
    
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }
    
    public DescriptionSet getDescriptions() {
        return this.descriptions;
    }
    
    public void addDescription(String description, Locale locale) {
        ((DescriptionSetImpl)descriptions).add(description, locale);
    }
    
    public void setDistributable(DistributableImpl distributable) {
        this.distributable = distributable;
    }
    
    public DistributableImpl getDistributable() {
        return this.distributable;
    }
    
    public void setInitParameterSet(ParameterSet parameters) {
        this.parameters = parameters;
    }
    
    public FilterListImpl getFilters() {
        return this.filters;
    }
    
    public void setFilters(FilterListImpl filters) {
        this.filters = filters;
    }
    
    public FilterMappingListImpl getFilterMappings() {
        return this.filterMappings;
    }
    
    public void setFilterMappings(FilterMappingListImpl filterMappings) {
        this.filterMappings = filterMappings;
    }

    public ListenerListImpl getListeners() {
        return this.listeners;
    }
    
    public void setListeners(ListenerListImpl listeners) {
        this.listeners = listeners;
    }

    public void setServletDefinitionList(ServletDefinitionList servletDefinitions) {
        this.servletDefinitions = servletDefinitions;
    }
    
    public ServletMappingListImpl getServletMappings() {
        return this.servletMappings;
    }
    
    public void setServletMappings(ServletMappingListImpl servletMappings) {
        this.servletMappings = servletMappings;
    }
    
    public TagLibListImpl getTagLibs() {
        return this.tagLibs;
    }
    
    public void setTagLibs(TagLibListImpl tagLibs) {
        this.tagLibs = tagLibs;
    }
    
    public SecurityRoleSet getSecurityRoles() {
        return this.securityRoles;
    }
    
    public void setSecurityRoles(SecurityRoleSet securityRoles) {
        this.securityRoles = securityRoles;
    }

    public void setContextRoot(String contextPath) {
        this.contextPath = contextPath;
    }

}
