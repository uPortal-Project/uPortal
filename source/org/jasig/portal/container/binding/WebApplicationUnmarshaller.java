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

package org.jasig.portal.container.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.ParameterImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleSetImpl;
import org.jasig.portal.container.om.servlet.DistributableImpl;
import org.jasig.portal.container.om.servlet.FilterImpl;
import org.jasig.portal.container.om.servlet.FilterListImpl;
import org.jasig.portal.container.om.servlet.FilterMappingImpl;
import org.jasig.portal.container.om.servlet.FilterMappingListImpl;
import org.jasig.portal.container.om.servlet.IconImpl;
import org.jasig.portal.container.om.servlet.ListenerImpl;
import org.jasig.portal.container.om.servlet.ListenerListImpl;
import org.jasig.portal.container.om.servlet.RunAsImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionListImpl;
import org.jasig.portal.container.om.servlet.ServletMappingImpl;
import org.jasig.portal.container.om.servlet.ServletMappingListImpl;
import org.jasig.portal.container.om.servlet.TagLibImpl;
import org.jasig.portal.container.om.servlet.TagLibListImpl;
import org.jasig.portal.container.om.servlet.WebApplicationDefinitionImpl;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses a <code>web.xml</code> file and produces data structures.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WebApplicationUnmarshaller {

    private InputStream inputStream = null;
    private String contextName = null;
    private Document doc = null; // Might want to consider SAX instead of DOM parsing
    private WebApplicationDefinitionImpl webApplicationDefinition = null;
    
    public WebApplicationUnmarshaller() {
        this.webApplicationDefinition = new WebApplicationDefinitionImpl();
    }

    /**
     * Initializer that takes an input stream to the <code>web.xml</code> file as well
     * as the context name of the portlet application.
     * @param inputStream an input stream to the contents of the <code>web.xml</code> file
     * @param contextName the context name of the portlet application
     * @throws IOException
     * @throws SAXException
     */
    public void init(InputStream inputStream, String contextName) throws IOException, SAXException {
        this.inputStream = inputStream;
        this.doc = DocumentFactory.getDocumentFromStream(inputStream);
        this.contextName = contextName;
    }
    
    /**
     * Returns a WebApplicationDefinition object that was populated with data 
     * from the <code>web.xml</code> file.
     * @return the web application definition
     */
    public WebApplicationDefinition getWebApplicationDefinition() {
        Element webAppE = doc.getDocumentElement();   
        webApplicationDefinition.setId(contextName);
        webApplicationDefinition.setContextRoot("/" + contextName);
        webApplicationDefinition.setIcon(getIcon(webAppE));
        webApplicationDefinition.setDisplayNames(getDisplayNames(webAppE));
        webApplicationDefinition.setDescriptions(getDescriptions(webAppE));
        webApplicationDefinition.setDistributable(getDistributable(webAppE));
        webApplicationDefinition.setInitParameterSet(getParameters(webAppE, "context-param"));
        webApplicationDefinition.setFilters(getFilters(webAppE));
        webApplicationDefinition.setFilterMappings(getFilterMappings(webAppE));
        webApplicationDefinition.setListeners(getListeners(webAppE));
        webApplicationDefinition.setServletDefinitionList(getServletDefinitions(webAppE));    
        webApplicationDefinition.setServletMappings(getServletMappings(webAppE));
        webApplicationDefinition.setTagLibs(getTagLibs(webAppE));
        webApplicationDefinition.setSecurityRoles(getSecurityRoles(webAppE));
        return webApplicationDefinition;
    }

    private ServletDefinitionList getServletDefinitions(Element webAppE) {
        ServletDefinitionListImpl servletDefinitions = new ServletDefinitionListImpl();
         
        NodeList servletNL = webAppE.getElementsByTagName("servlet");
        for (int i = 0; i < servletNL.getLength(); i++) {
            Element servletE = (Element)servletNL.item(i);
            ServletDefinitionImpl servletDefinition = new ServletDefinitionImpl();
            servletDefinition.setIcon(getIcon(servletE));
            servletDefinition.setServletName(XML.getChildElementText(servletE, "servlet-name"));
            servletDefinition.setDisplayNames(getDisplayNames(servletE));
            servletDefinition.setDescriptions(getDescriptions(servletE));
            servletDefinition.setServletClass(XML.getChildElementText(servletE, "servlet-class"));
            servletDefinition.setJspFile(XML.getChildElementText(servletE, "jsp-file"));
            servletDefinition.setInitParameters(getParameters(servletE, "init-param"));
            servletDefinition.setLoadOnStartup(XML.getChildElementText(servletE, "load-on-startup"));
            
            // Add run as
            NodeList runAsNL = servletE.getElementsByTagName("run-as");
            if (runAsNL.getLength() > 0) {
                Element runAsE = (Element)runAsNL.item(0);
                RunAsImpl runAs = new RunAsImpl();
                runAs.setRoleName(XML.getChildElementText(runAsE, "role-name"));
                runAs.setDescriptions(getDescriptions(runAsE));
                servletDefinition.setRunAs(runAs);
            }
            
            // Add servlet security role refs
            SecurityRoleRefSetImpl securityRoleRefs = new SecurityRoleRefSetImpl();
            NodeList securityRoleRefNL = servletE.getElementsByTagName("security-role-ref");
            for (int m = 0; m < securityRoleRefNL.getLength(); m += 1) {
                Element securityRoleRefE = (Element)securityRoleRefNL.item(m);
                String roleName = XML.getChildElementText(securityRoleRefE, "role-name");
                String roleLink = XML.getChildElementText(securityRoleRefE, "role-link");
                String description = XML.getChildElementText(securityRoleRefE, "description");
                SecurityRoleRefImpl securityRoleRef = new SecurityRoleRefImpl();
                securityRoleRef.setDescription(description);
                securityRoleRef.setRoleName(roleName);
                securityRoleRef.setRoleLink(roleLink);
                securityRoleRefs.add(securityRoleRef);
            }
            servletDefinition.setSecurityRoleRefs(securityRoleRefs);
            
            // Add servlet mappings for this servlet
            NodeList servletMappingNL = webAppE.getElementsByTagName("servlet-mapping");
            for (int k = 0; k < servletMappingNL.getLength(); k += 1) {
                Element servletMappingE = (Element)servletMappingNL.item(k);
                String servletMappingName = XML.getChildElementText(servletMappingE, "servlet-name");
                String servletMappingUrlPattern = XML.getChildElementText(servletMappingE, "url-pattern");
                if (servletMappingName.equals(servletDefinition.getServletName())) {
                    servletDefinition.setServletMapping(servletMappingName, servletMappingUrlPattern);
                    break;
                }
            }

            servletDefinition.setWebApplicationDefinition(webApplicationDefinition);
            servletDefinitions.add(servletDefinition);
        }
        
        return servletDefinitions;
    }
    
    private IconImpl getIcon(Element e) {
        IconImpl icon = null;
        NodeList iconNL = e.getElementsByTagName("icon");
        if (iconNL.getLength() > 0) {
            icon = new IconImpl();
            Element iconE = (Element)iconNL.item(0);
            icon.setSmallIcon(XML.getChildElementText(iconE, "small-icon"));
            icon.setLargeIcon(XML.getChildElementText(iconE, "large-icon"));
        }
        return icon;
    }
    
    private DisplayNameSet getDisplayNames(Element e) {
        DisplayNameSetImpl displayNames = new DisplayNameSetImpl();
        NodeList displayNameNL = e.getElementsByTagName("display-name");
        for (int i = 0; i < displayNameNL.getLength(); i++) {
            Element displayNameE = (Element)displayNameNL.item(i);
            String displayName = XML.getElementText(displayNameE);
            displayNames.add(displayName, Locale.getDefault());
        }
        return displayNames;
    }
    
    private DescriptionSet getDescriptions(Element e) {
        DescriptionSetImpl descriptions = new DescriptionSetImpl();
        NodeList descriptionNL = e.getElementsByTagName("description");
        for (int i = 0; i < descriptionNL.getLength(); i++) {
            Element descriptionE = (Element)descriptionNL.item(i);
            String description = XML.getElementText(descriptionE);
            descriptions.add(description, Locale.getDefault());
        }
        return descriptions;
    }
    
    private DistributableImpl getDistributable(Element e) {
        DistributableImpl distributable = null;
        NodeList distributableNL = e.getElementsByTagName("distributable");
        if (distributableNL.getLength() > 0) {
            distributable = new DistributableImpl();
        }
        return distributable;
    }
    
    private ParameterSet getParameters(Element e, String paramElementName) {
        ParameterSetImpl parameters = new ParameterSetImpl();
        NodeList contextParamNL = e.getElementsByTagName(paramElementName);
        for (int i = 0; i < contextParamNL.getLength(); i++) {
            Element contextParamE = (Element)contextParamNL.item(i);
            String paramName = XML.getChildElementText(contextParamE, "param-name");
            String paramValue = XML.getChildElementText(contextParamE, "param-value");
            String description = XML.getChildElementText(contextParamE, "description");
            ParameterImpl parameter = new ParameterImpl();
            parameter.setName(paramName);
            parameter.setValue(paramValue);
            parameter.setDescriptionSet(getDescriptions(contextParamE));
            parameters.add(parameter);
        }        
        return parameters;
    }
    
    private FilterListImpl getFilters(Element e) {
        FilterListImpl filters = new FilterListImpl();
        NodeList filterNL = e.getElementsByTagName("filter");
        for (int i = 0; i < filterNL.getLength(); i++) {
            Element filterE = (Element)filterNL.item(i);
            IconImpl icon = getIcon(filterE);
            String filterName = XML.getChildElementText(filterE, "filter-name");
            DisplayNameSet displayNames = getDisplayNames(filterE);
            DescriptionSet descriptions = getDescriptions(filterE);
            String filterClass = XML.getChildElementText(filterE, "filter-class");
            ParameterSet initParameters = getParameters(filterE, "init-param");
            FilterImpl filter = new FilterImpl();
            filter.setIcon(icon);
            filter.setFilterName(filterName);
            filter.setDisplayNames(displayNames);
            filter.setDescriptions(descriptions);
            filter.setFilterClass(filterClass);
            filter.setInitParamteters(initParameters);
            filters.add(filter);
        }        
        return filters;
    }
    
    private FilterMappingListImpl getFilterMappings(Element e) {
        FilterMappingListImpl filterMappings = new FilterMappingListImpl();
        NodeList filterMappingNL = e.getElementsByTagName("filter-mapping");
        for (int i = 0; i < filterMappingNL.getLength(); i++) {
            Element filterMappingE = (Element)filterMappingNL.item(i);
            String filterName = XML.getChildElementText(filterMappingE, "filter-name");
            String urlPattern = XML.getChildElementText(filterMappingE, "url-pattern");
            String servletName = XML.getChildElementText(filterMappingE, "servlet-name");
            FilterMappingImpl filterMapping = new FilterMappingImpl();
            filterMapping.setFilterName(filterName);
            filterMapping.setUrlPattern(urlPattern);
            filterMapping.setServletName(servletName);
            filterMappings.add(filterMapping);
        }
        return filterMappings;
    }
    
    private SecurityRoleSet getSecurityRoles(Element e) {
        SecurityRoleSetImpl securityRoles = new SecurityRoleSetImpl();
        NodeList securityRoleNL = e.getElementsByTagName("security-role");
        for (int i = 0; i < securityRoleNL.getLength(); i++) {
            Element securityRoleE = (Element)securityRoleNL.item(i);
            String roleName = XML.getChildElementText(securityRoleE, "role-name");
            String description = XML.getChildElementText(securityRoleE, "description");
            SecurityRoleImpl securityRole = new SecurityRoleImpl();
            securityRole.setDescription(description);
            securityRole.setRoleName(roleName);
            securityRoles.add(securityRole);
        }
        return securityRoles;
    }

    private ListenerListImpl getListeners(Element e) {
        ListenerListImpl listeners = new ListenerListImpl();
        NodeList listenerNL = e.getElementsByTagName("listener");
        for (int i = 0; i < listenerNL.getLength(); i++) {
            Element listenerE = (Element)listenerNL.item(i);
            String listenerClass = XML.getChildElementText(listenerE, "listener-class");
            ListenerImpl listener = new ListenerImpl();
            listener.setListenerClass(listenerClass);
            listeners.add(listener);
        }
        return listeners;
    }
    
    private ServletMappingListImpl getServletMappings(Element e) {
        ServletMappingListImpl servletMappings = new ServletMappingListImpl();
        NodeList servletMappingNL = e.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < servletMappingNL.getLength(); i++) {
            Element servletMappingE = (Element)servletMappingNL.item(i);
            String servletName = XML.getChildElementText(servletMappingE, "servlet-name");
            String urlPattern = XML.getChildElementText(servletMappingE, "url-pattern");
            ServletMappingImpl servletMapping = new ServletMappingImpl();
            servletMapping.setServletName(servletName);
            servletMapping.setUrlPattern(urlPattern);
            servletMappings.add(servletMapping);
        }
        return servletMappings;
    } 
    
    private TagLibListImpl getTagLibs(Element e) {  
        TagLibListImpl tagLibs = new TagLibListImpl();
        NodeList tagLibNL = e.getElementsByTagName("taglib");
        for (int i = 0; i < tagLibNL.getLength(); i++) {
            Element tagLibE = (Element)tagLibNL.item(i);
            String tagLibUri = XML.getChildElementText(tagLibE, "taglib-uri");
            String tagLibLocation = XML.getChildElementText(tagLibE, "taglib-location");
            TagLibImpl tagLib = new TagLibImpl();
            tagLib.setTaglibUri(tagLibUri);
            tagLib.setTaglibLocation(tagLibLocation);
            tagLibs.add(tagLib);
        }
        return tagLibs;
    }
}
