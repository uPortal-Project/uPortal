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
import org.apache.pluto.om.common.SecurityRole;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;
import org.jasig.portal.container.om.servlet.AuthConstraintImpl;
import org.jasig.portal.container.om.servlet.DistributableImpl;
import org.jasig.portal.container.om.servlet.EjbLocalRefImpl;
import org.jasig.portal.container.om.servlet.EjbRefImpl;
import org.jasig.portal.container.om.servlet.EnvEntryImpl;
import org.jasig.portal.container.om.servlet.ErrorPageImpl;
import org.jasig.portal.container.om.servlet.FilterImpl;
import org.jasig.portal.container.om.servlet.FilterMappingImpl;
import org.jasig.portal.container.om.servlet.FormLoginConfigImpl;
import org.jasig.portal.container.om.servlet.IconImpl;
import org.jasig.portal.container.om.servlet.ListenerImpl;
import org.jasig.portal.container.om.servlet.LoginConfigImpl;
import org.jasig.portal.container.om.servlet.MimeMappingImpl;
import org.jasig.portal.container.om.servlet.ResourceEnvRefImpl;
import org.jasig.portal.container.om.servlet.ResourceRefImpl;
import org.jasig.portal.container.om.servlet.RunAsImpl;
import org.jasig.portal.container.om.servlet.SecurityConstraintImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionImpl;
import org.jasig.portal.container.om.servlet.ServletMappingImpl;
import org.jasig.portal.container.om.servlet.ServletMappingListImpl;
import org.jasig.portal.container.om.servlet.SessionConfigImpl;
import org.jasig.portal.container.om.servlet.TagLibImpl;
import org.jasig.portal.container.om.servlet.TagLibListImpl;
import org.jasig.portal.container.om.servlet.UserDataConstraintImpl;
import org.jasig.portal.container.om.servlet.WebApplicationDefinitionImpl;
import org.jasig.portal.container.om.servlet.WebResourceCollectionImpl;
import org.jasig.portal.container.om.servlet.WelcomeFileListImpl;
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
        FilterImpl[] filters = webAppImpl.getFilters();
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                FilterImpl filter = filters[i];
                Element filterE = doc.createElement("filter");
                addIcon(filterE, filter.getIcon());
                addTextElement(filterE, "filter-name", filter.getFilterName());
                addDisplayNames(filterE, filter.getDisplayNames());
                addDescriptions(filterE, filter.getDescriptions());                               
                addTextElement(filterE, "filter-class", filter.getFilterClass());
                addParameters(filterE, "init-param", (ParameterSetImpl)webAppImpl.getInitParameterSet());
                webAppE.appendChild(filterE);                
            }
        }
        
        // <filter-mapping>
        FilterMappingImpl[] filterMappings = webAppImpl.getFilterMappings();
        if (filterMappings != null) {
            for (int i = 0; i < filterMappings.length; i++) {
                FilterMappingImpl filterMapping = filterMappings[i];
                Element filterMappingE = doc.createElement("filter-mapping");
                addTextElement(filterMappingE, "filter-name", filterMapping.getFilterName());
                addTextElement(filterMappingE, "url-pattern", filterMapping.getUrlPattern());
                addTextElement(filterMappingE, "servlet-name", filterMapping.getServletName());
                webAppE.appendChild(filterMappingE);
            }
        }
        
        // <listener>
        ListenerImpl[] listeners = webAppImpl.getListeners();
        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                ListenerImpl listener = listeners[i];
                Element listenerE = doc.createElement("listener");
                addTextElement(listenerE, "listener-class", listener.getListenerClass());
                webAppE.appendChild(listenerE);
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
                    for (Iterator iter2 = securityRoleRefs.iterator(); iter2.hasNext();) {
                        SecurityRoleRefImpl securityRoleRef = (SecurityRoleRefImpl)iter2.next();
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
        SessionConfigImpl sessionConfig = webAppImpl.getSessionConfig();
        if (sessionConfig != null) {
            Element sessionConfigE = doc.createElement("session-config");
            addTextElement(sessionConfigE, "session-timeout", sessionConfig.getSessionTimeout());
            webAppE.appendChild(sessionConfigE);
        }
        
        // <mime-mapping>
        MimeMappingImpl[] mimeMappings = webAppImpl.getMimeMappings();
        if (mimeMappings != null) {
            for (int i = 0; i < mimeMappings.length; i++) {
                MimeMappingImpl mimeMapping = mimeMappings[i];
                Element mimeMappingE = doc.createElement("mime-mapping");
                addTextElement(mimeMappingE, "extension", mimeMapping.getExtension());
                addTextElement(mimeMappingE, "mime-type", mimeMapping.getMimeType());
                webAppE.appendChild(mimeMappingE);
            }
        }
        
        // <welcome-file-list>
        
        WelcomeFileListImpl welcomeFiles = webAppImpl.getWelcomeFiles();
        if (welcomeFiles != null) {
            Element welcomeFileListE = doc.createElement("welcome-file-list");
            for (Iterator iter = welcomeFiles.iterator(); iter.hasNext();) {
                String welcomeFile = (String)iter.next();
                addTextElement(welcomeFileListE, "welcome-file", welcomeFile);
            }
            webAppE.appendChild(welcomeFileListE);
        }
        
        // <error-page>
        ErrorPageImpl[] errorPages = webAppImpl.getErrorPages();
        if(errorPages != null) {
            for (int i = 0; i < errorPages.length; i++) {
                ErrorPageImpl errorPage = errorPages[i];
                Element errorPageE = doc.createElement("error-page");
                addTextElement(errorPageE, "error-code", errorPage.getErrorCode());
                addTextElement(errorPageE, "exception-type", errorPage.getExceptionType());
                addTextElement(errorPageE, "location", errorPage.getLocation());
                webAppE.appendChild(errorPageE);
            }
        }
        
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
        ResourceEnvRefImpl[] resourceEnvRefs = webAppImpl.getResourceEnvRefs();
        if (resourceEnvRefs != null) {
            for (int i = 0; i < resourceEnvRefs.length; i++) {
                ResourceEnvRefImpl resourceEnvRef = resourceEnvRefs[i];
                Element resourceEnvRefE = doc.createElement("resource-env-ref");
                addDescriptions(resourceEnvRefE, resourceEnvRef.getDescriptions());
                addTextElement(resourceEnvRefE, "resource-env-ref-name", resourceEnvRef.getResourceEnvRefName());
                addTextElement(resourceEnvRefE, "resource-env-ref-type", resourceEnvRef.getResourceEnvRefType());
                webAppE.appendChild(resourceEnvRefE);
            }        
        }
        
        // <resource-ref>
        ResourceRefImpl[] resourceRefs = webAppImpl.getResourceRefs();
        if (resourceRefs != null) {
            for (int i = 0; i < resourceRefs.length; i++) {
                ResourceRefImpl resourceRef = resourceRefs[i];
                Element resourceRefE = doc.createElement("resource-ref");
                addDescriptions(resourceRefE, resourceRef.getDescriptions());
                addTextElement(resourceRefE, "res-ref-name", resourceRef.getResRefName());
                addTextElement(resourceRefE, "res-type", resourceRef.getResType());
                addTextElement(resourceRefE, "res-auth", resourceRef.getResAuth());
                addTextElement(resourceRefE, "res-sharing-scope", resourceRef.getResSharingScope());
                webAppE.appendChild(resourceRefE);
            }        
        }
        
        // <security-constraint>
        SecurityConstraintImpl[] securityConstraints = webAppImpl.getSecurityConstraints();
        if (securityConstraints != null) {
            for (int i = 0; i < securityConstraints.length; i++) {
                SecurityConstraintImpl securityConstraint = securityConstraints[i];
                Element securityConstraintE = doc.createElement("security-constraint");
                addDisplayNames(securityConstraintE, securityConstraint.getDisplayNames());
                WebResourceCollectionImpl[] webResourceCollections = securityConstraint.getWebResourceCollections();
                if (webResourceCollections != null) {
                    for (int j = 0; j < webResourceCollections.length; j++) {
                        WebResourceCollectionImpl webResourceCollection = webResourceCollections[j];
                        Element webResourceCollectionE = doc.createElement("web-resource-collection");
                        addTextElement(webResourceCollectionE, "web-resource-name", webResourceCollection.getWebResourceName());
                        addDescriptions(webResourceCollectionE, webResourceCollection.getDescriptions());
                        String[] urlPatterns = webResourceCollection.getUrlPatterns();
                        for (int k = 0; k < urlPatterns.length; k++) {
                            addTextElement(webResourceCollectionE, "url-pattern", urlPatterns[k]);
                        }
                        String[] httpMethods = webResourceCollection.getHttpMethods();
                        for (int k = 0; k < httpMethods.length; k++) {
                            addTextElement(webResourceCollectionE, "http-method", httpMethods[k]);
                        }
                        securityConstraintE.appendChild(webResourceCollectionE);
                    }
                }
                AuthConstraintImpl authConstraint = securityConstraint.getAuthConstraint();
                if (authConstraint != null) {
                    Element authConstraintE = doc.createElement("auth-constraint");
                    addDescriptions(authConstraintE, authConstraint.getDescriptions());
                    String[] roleNames = authConstraint.getRoleNames();
                    for (int j = 0; j < roleNames.length; j++) {
                        addTextElement(authConstraintE, "role-name", roleNames[j]);
                    }
                    securityConstraintE.appendChild(authConstraintE);
                }
                UserDataConstraintImpl userDataConstraint = securityConstraint.getUserDataConstraint();
                if (userDataConstraint != null) {
                    Element userDataConstraintE = doc.createElement("user-data-constraint");
                    addDescriptions(userDataConstraintE, userDataConstraint.getDescriptions());
                    addTextElement(userDataConstraintE, "transport-guarantee", userDataConstraint.getTransportGuarantee());
                    securityConstraintE.appendChild(userDataConstraintE);
                }
                webAppE.appendChild(securityConstraintE);
            }
        }
        
        // <login-config>
        LoginConfigImpl loginConfig = webAppImpl.getLoginConfig();
        if (loginConfig != null) {
            Element loginConfigE = doc.createElement("login-config");
            addTextElement(loginConfigE, "auth-method", loginConfig.getAuthMethod());
            addTextElement(loginConfigE, "realm-name", loginConfig.getRealmName());
            FormLoginConfigImpl formLoginConfigImpl = loginConfig.getFormLoginConfig();
            if (formLoginConfigImpl != null) {
                Element formLoginConfigE = doc.createElement("form-login-config");
                addTextElement(formLoginConfigE, "form-login-page", formLoginConfigImpl.getFormLoginPage());
                addTextElement(formLoginConfigE, "form-error-page", formLoginConfigImpl.getFormErrorPage());
                loginConfigE.appendChild(formLoginConfigE);
            }
            webAppE.appendChild(loginConfigE);
        }
        
        // <security-role>
        SecurityRoleSet securityRoles = webAppImpl.getSecurityRoles();
        if (securityRoles != null) {
            for (Iterator iter = securityRoles.iterator(); iter.hasNext();) {
                SecurityRole securityRole = (SecurityRole)iter.next();
                Element securityRoleE = doc.createElement("security-role");
                addTextElement(securityRoleE, "description", securityRole.getDescription());
                addTextElement(securityRoleE, "role-name", securityRole.getRoleName());
                webAppE.appendChild(securityRoleE);
            }
        }
        
        // <env-entry>
        EnvEntryImpl[] envEntries = webAppImpl.getEnvEntries();
        if (envEntries != null) {
            for (int i = 0; i < envEntries.length; i++) {
                Element envEntryE = doc.createElement("env-entry");
                EnvEntryImpl envEntry = envEntries[i];
                addDescriptions(envEntryE, envEntry.getDescriptions());
                addTextElement(envEntryE, "env-entry-name", envEntry.getEnvEntryName());
                addTextElement(envEntryE, "env-entry-value", envEntry.getEnvEntryValue());
                addTextElement(envEntryE, "env-entry-type", envEntry.getEnvEntryType());
                webAppE.appendChild(envEntryE);
            }
        }
        
        // <ejb-ref>
        EjbRefImpl[] ejbRefs = webAppImpl.getEjbRefs();
        if (ejbRefs != null) {
            for (int i = 0; i < ejbRefs.length; i++) {
                Element ejbRefE = doc.createElement("ejb-ref");
                EjbRefImpl ejbRef = ejbRefs[i];
                addDescriptions(ejbRefE, ejbRef.getDescriptions());
                addTextElement(ejbRefE, "ejb-ref-name", ejbRef.getEjbRefName());
                addTextElement(ejbRefE, "ejb-ref-type", ejbRef.getEjbRefType());
                addTextElement(ejbRefE, "home", ejbRef.getHome());
                addTextElement(ejbRefE, "remote", ejbRef.getRemote());
                addTextElement(ejbRefE, "ejb-link", ejbRef.getEjbLink());
                webAppE.appendChild(ejbRefE);
            }
        }
        
        // <ejb-local-ref>
        EjbLocalRefImpl[] ejbLocalRefs = webAppImpl.getEjbLocalRefs();
        if (ejbLocalRefs != null) {
            for (int i = 0; i < ejbLocalRefs.length; i++) {
                Element ejbLocalRefE = doc.createElement("ejb-local-ref");
                EjbLocalRefImpl ejbLocalRef = ejbLocalRefs[i];
                addDescriptions(ejbLocalRefE, ejbLocalRef.getDescriptions());
                addTextElement(ejbLocalRefE, "ejb-ref-name", ejbLocalRef.getEjbRefName());
                addTextElement(ejbLocalRefE, "ejb-ref-type", ejbLocalRef.getEjbRefType());
                addTextElement(ejbLocalRefE, "local-home", ejbLocalRef.getLocalHome());
                addTextElement(ejbLocalRefE, "local", ejbLocalRef.getLocal());
                addTextElement(ejbLocalRefE, "ejb-link", ejbLocalRef.getEjbLink());
                webAppE.appendChild(ejbLocalRefE);
            }
        }        
        
        doc.appendChild(webAppE);
    }
    
    private void addTextElement(Element parent, String elementName, String text) {
        if (text != null && text.length() > 0) {
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
