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

import java.util.Iterator;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;
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
import org.jasig.portal.container.om.servlet.ServletMappingImpl;
import org.jasig.portal.container.om.servlet.ServletMappingListImpl;
import org.jasig.portal.container.om.servlet.TagLibImpl;
import org.jasig.portal.container.om.servlet.TagLibListImpl;
import org.jasig.portal.container.om.servlet.WebApplicationDefinitionImpl;
import org.jasig.portal.serialize.Serializer;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Produces a <code>web.xml</code> file based on a WebApplicationDefinition object.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WebApplicationMarshaller {

    private WebApplicationDefinition webApplicationDefinition;
    private Serializer serializer;
    
    public void init(WebApplicationDefinition webApplicationDefinition, Serializer serializer) {
        this.webApplicationDefinition = webApplicationDefinition;
        this.serializer = serializer;
    }
    
    public void marshall() throws Exception {
        Document doc = DocumentFactory.getNewDocument();
        buildDocument(webApplicationDefinition, doc);
        serializer.asDOMSerializer().serialize(doc);
    }
    
    private void buildDocument(WebApplicationDefinition webApplicationDefinition, Document doc) {
        WebApplicationDefinitionImpl webAppImpl = (WebApplicationDefinitionImpl)webApplicationDefinition;
        
        // <web-app>
        Element webAppE = doc.createElement("web-app");
        
        // <icon>
        addIcon(webAppE, webAppImpl.getIcon());
        
        // <display-name>
        addDisplayNames(webAppE, webAppImpl.getDisplayNames());
        
        // <description>
        addDescriptions(webAppE, webAppImpl.getDescriptions());
        
        // <distributable>
        DistributableImpl distributable = webAppImpl.getDistributable();
        if (distributable != null) {
            Element distributableE = doc.createElement("distributable");
            webAppE.appendChild(distributableE);
        }
        
        // <context-param>
        addParameters(webAppE, "context-param", (ParameterSetImpl)webAppImpl.getInitParameterSet());
        
        // <filter>
        FilterListImpl filters = webAppImpl.getFilters();
        if (filters != null) {
            for (Iterator iter = filters.iterator(); iter.hasNext();) {
                FilterImpl filter = (FilterImpl)iter.next();
                Element filterE = doc.createElement("filter");
                addIcon(filterE, filter.getIcon());
                addTextElement(filterE, "filter-name", filter.getFilterName());
                addDisplayNames(filterE, filter.getDisplayNames());
                addDescriptions(filterE, filter.getDescriptions());                               
                addTextElement(filterE, "filter-class", filter.getFilterClass());
                addParameters(filterE, "init-param", (ParameterSetImpl)webAppImpl.getInitParameterSet());                
            }
        }
        
        // <filter-mapping>
        FilterMappingListImpl filterMappings = webAppImpl.getFilterMappings();
        if (filterMappings != null) {
            for (Iterator iter = filterMappings.iterator(); iter.hasNext();) {
                FilterMappingImpl filterMapping = (FilterMappingImpl)iter.next();
                Element filterMappingE = doc.createElement("filter-mapping");
                addTextElement(filterMappingE, "filter-name", filterMapping.getFilterName());
                addTextElement(filterMappingE, "url-pattern", filterMapping.getUrlPattern());
                addTextElement(filterMappingE, "servlet-name", filterMapping.getServletName());
            }
        }
        
        // <listener>
        ListenerListImpl listeners = webAppImpl.getListeners();
        if (listeners != null) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                ListenerImpl listener = (ListenerImpl)iter.next();
                Element listenerE = doc.createElement("listener");
                addTextElement(listenerE, "listener-class", listener.getListenerClass());
            }        
        }
        
        // <servlet>
        ServletDefinitionList servletDefinitions = webAppImpl.getServletDefinitionList();
        if (servletDefinitions != null) {
            for (Iterator iter = servletDefinitions.iterator(); iter.hasNext();) {
                Element servletE = doc.createElement("servlet");
                ServletDefinitionImpl servletDefinition = (ServletDefinitionImpl)iter.next();
                addIcon(servletE, servletDefinition.getIcon());
                addTextElement(servletE, "servlet-name", servletDefinition.getServletName());
                addDisplayNames(servletE, servletDefinition.getDisplayNames());
                addDescriptions(servletE, servletDefinition.getDescriptions());
                addTextElement(servletE, "servlet-class", servletDefinition.getServletClass());
                addTextElement(servletE, "jsp-file", servletDefinition.getJspFile());
                addParameters(servletE, "init-param", (ParameterSetImpl)servletDefinition.getInitParameterSet());
                addTextElement(servletE, "load-on-startup", servletDefinition.getLoadOnStartup());
                RunAsImpl runAs = servletDefinition.getRunAs();
                if (runAs != null) {
                    Element runAsE = doc.createElement("run-as");
                    addDescriptions(runAsE, runAs.getDescritpions());
                    addTextElement(runAsE, "role-name", runAs.getRoleName());
                    servletE.appendChild(runAsE);
                }
                SecurityRoleRefSetImpl securityRoleRefs = (SecurityRoleRefSetImpl)servletDefinition.getSecurityRoleRefs();
                if (securityRoleRefs != null) {
                    for (Iterator iter2 = securityRoleRefs.iterator(); iter.hasNext();) {
                        SecurityRoleRefImpl securityRoleRef = (SecurityRoleRefImpl)iter.next();
                        Element securityRoleRefE = doc.createElement("security-role-ref");
                        addDescriptions(securityRoleRefE, securityRoleRef.getDescriptions());
                        addTextElement(securityRoleRefE, "role-name", securityRoleRef.getRoleName());
                        addTextElement(securityRoleRefE, "role-link", securityRoleRef.getRoleLink());
                        servletE.appendChild(securityRoleRefE);
                    }
                }
                webAppE.appendChild(servletE);
            }
        }
        
        // <servlet-mapping>
        ServletMappingListImpl servletMappings = webAppImpl.getServletMappings();
        if (servletMappings != null) {
            for (Iterator iter = servletMappings.iterator(); iter.hasNext();) {
                ServletMappingImpl servletMapping = (ServletMappingImpl)iter.next();
                Element servletMappingE = doc.createElement("servlet-mapping");
                addTextElement(servletMappingE, "servlet-name", servletMapping.getServletName());
                addTextElement(servletMappingE, "url-pattern", servletMapping.getUrlPattern());
                webAppE.appendChild(servletMappingE);
            }
        }
        
        // <session-config>
        
        // <mime-mapping>
        
        // <welcome-file-list>
        
        // <error-page>
        
        // <taglib>
        TagLibListImpl tagLibs = webAppImpl.getTagLibs();
        if(tagLibs != null) {
            for (Iterator iter = tagLibs.iterator(); iter.hasNext();) {
                TagLibImpl tagLib = (TagLibImpl)iter.next();
                Element tagLibE = doc.createElement("taglib");
                addTextElement(tagLibE, "taglib-uri", tagLib.getTaglibUri());
                addTextElement(tagLibE, "taglib-location", tagLib.getTaglibLocation());
                webAppE.appendChild(tagLibE);
            }
        }
        
        // <resource-env-ref>
        
        // <resource-ref>
        
        // <security-contraint>
        
        // <login-config>
        
        // <security-role>
        
        // <env-entry>
        
        // <ejb-ref>
        
        // <ejb-local-ref>
        
        doc.appendChild(webAppE);
    }
    
    private void addTextElement(Element parent, String elementName, String text) {
        if (text != null) {
            Document doc = parent.getOwnerDocument();
            Element e = doc.createElement(elementName);
            e.appendChild(doc.createTextNode(text));
            parent.appendChild(e);
        }
    }
    
    private void addDisplayNames(Element parent, DisplayNameSet displayNames) {
        DisplayName displayName = displayNames.get(Locale.getDefault());
        if (displayName != null) {
            addTextElement(parent, "display-name", displayName.getDisplayName());
        }
    }
    
    private void addDescriptions(Element parent, DescriptionSet descriptions) {
        Description description = descriptions.get(Locale.getDefault());
        if (description != null) {
            addTextElement(parent, "description", description.getDescription());
        }
    }
    
    private void addParameters(Element parent, String paramElementName, ParameterSetImpl parameters) {
        if (parameters != null && parameters.size() > 0) {
            for (Iterator iter = parameters.iterator(); iter.hasNext();) {
                Parameter parameter = (Parameter)iter.next();
                Document doc = parent.getOwnerDocument();
                Element paramE = doc.createElement(paramElementName);
                addTextElement(paramE, "param-name", parameter.getName());
                addTextElement(paramE, "param-value", parameter.getValue());
                Description paramDescription = parameter.getDescription(Locale.getDefault());
                if (paramDescription != null) {
                    addTextElement(paramE, "description", paramDescription.getDescription());
                }            
                parent.appendChild(paramE);
            }
        }        
    }
    
    private void addIcon(Element parent, IconImpl icon) {
        if (icon != null) {
            Document doc = parent.getOwnerDocument();
            Element iconE = doc.createElement("icon");
            addTextElement(iconE, "small-icon", icon.getSmallIcon());
            addTextElement(iconE, "large-icon", icon.getLargeIcon());
            parent.appendChild(iconE);
        }
    }

}
