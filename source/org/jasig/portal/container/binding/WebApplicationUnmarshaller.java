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
import org.jasig.portal.container.deploy.WebAppDtdResolver;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.ParameterImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleSetImpl;
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
import org.jasig.portal.container.om.servlet.ServletDefinitionListImpl;
import org.jasig.portal.container.om.servlet.ServletMappingImpl;
import org.jasig.portal.container.om.servlet.ServletMappingListImpl;
import org.jasig.portal.container.om.servlet.SessionConfigImpl;
import org.jasig.portal.container.om.servlet.TagLibImpl;
import org.jasig.portal.container.om.servlet.TagLibListImpl;
import org.jasig.portal.container.om.servlet.UserDataConstraintImpl;
import org.jasig.portal.container.om.servlet.WebApplicationDefinitionImpl;
import org.jasig.portal.container.om.servlet.WebResourceCollectionImpl;
import org.jasig.portal.container.om.servlet.WelcomeFileListImpl;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Parses a <code>web.xml</code> file and produces data structures.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WebApplicationUnmarshaller {

    private InputStream inputStream;
    private String contextName;
    private Document doc; // Might want to consider SAX instead of DOM parsing
    private WebApplicationDefinitionImpl webApplicationDefinition;
    private static EntityResolver webAppDtdResolver;
    
    public WebApplicationUnmarshaller() {
        this.webApplicationDefinition = new WebApplicationDefinitionImpl();
        webAppDtdResolver = new WebAppDtdResolver();
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
        this.doc = DocumentFactory.getDocumentFromStream(inputStream, webAppDtdResolver);
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
        webApplicationDefinition.setSessionConfig(getSessionConfig(webAppE));
        webApplicationDefinition.setMimeMappings(getMimeMappings(webAppE));
        webApplicationDefinition.setWelcomeFiles(getWelcomeFiles(webAppE));
        webApplicationDefinition.setErrorPages(getErrorPages(webAppE));
        webApplicationDefinition.setTagLibs(getTagLibs(webAppE));
        webApplicationDefinition.setResourceEnvRefs(getResourceEnvRefs(webAppE));
        webApplicationDefinition.setResourceRefs(getResourceRefs(webAppE));
        webApplicationDefinition.setSecurityConstraints(getSecurityConstraints(webAppE));
        webApplicationDefinition.setLoginConfig(getLoginConfig(webAppE));
        webApplicationDefinition.setSecurityRoles(getSecurityRoles(webAppE));
        webApplicationDefinition.setEnvEntries(getEnvEntries(webAppE));
        webApplicationDefinition.setEjbRefs(getEjbRefs(webAppE));
        webApplicationDefinition.setEjbLocalRefs(getEjbLocalRefs(webAppE));
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
    
    private FilterImpl[] getFilters(Element e) {
        NodeList filterNL = e.getElementsByTagName("filter");
        FilterImpl[] filters = new FilterImpl[filterNL.getLength()];
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
            filters[i]= filter;
        }        
        return filters;
    }
    
    private FilterMappingImpl[] getFilterMappings(Element e) {
        NodeList filterMappingNL = e.getElementsByTagName("filter-mapping");
        FilterMappingImpl[] filterMappings = new FilterMappingImpl[filterMappingNL.getLength()];
        for (int i = 0; i < filterMappingNL.getLength(); i++) {
            Element filterMappingE = (Element)filterMappingNL.item(i);
            String filterName = XML.getChildElementText(filterMappingE, "filter-name");
            String urlPattern = XML.getChildElementText(filterMappingE, "url-pattern");
            String servletName = XML.getChildElementText(filterMappingE, "servlet-name");
            FilterMappingImpl filterMapping = new FilterMappingImpl();
            filterMapping.setFilterName(filterName);
            filterMapping.setUrlPattern(urlPattern);
            filterMapping.setServletName(servletName);
            filterMappings[i] = filterMapping;
        }
        return filterMappings;
    }
    
    private ListenerImpl[] getListeners(Element e) {
        NodeList listenerNL = e.getElementsByTagName("listener");
        ListenerImpl[] listeners = new ListenerImpl[listenerNL.getLength()];
        for (int i = 0; i < listenerNL.getLength(); i++) {
            Element listenerE = (Element)listenerNL.item(i);
            String listenerClass = XML.getChildElementText(listenerE, "listener-class");
            ListenerImpl listener = new ListenerImpl();
            listener.setListenerClass(listenerClass);
            listeners[i] = listener;
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

    private SessionConfigImpl getSessionConfig(Element e) {  
        SessionConfigImpl sessionConfig = null;
        NodeList sessionConfigNL = e.getElementsByTagName("session-config");
        if (sessionConfigNL.getLength() > 0) {
            sessionConfig = new SessionConfigImpl();
            sessionConfig.setSessionTimeout(XML.getChildElementText(e, "session-timeout"));
        }
        return sessionConfig;
    }
    
    private MimeMappingImpl[] getMimeMappings(Element e) {  
        NodeList mimeMappingNL = e.getElementsByTagName("mime-mapping");
        MimeMappingImpl[] mimeMappings = new MimeMappingImpl[mimeMappingNL.getLength()];
        for (int i = 0; i < mimeMappingNL.getLength(); i++) {
            Element mimeMappingE = (Element)mimeMappingNL.item(i);
            String extension = XML.getChildElementText(mimeMappingE, "extension");
            String mimeType = XML.getChildElementText(mimeMappingE, "mime-type");
            MimeMappingImpl mimeMapping = new MimeMappingImpl();
            mimeMapping.setExtension(extension);
            mimeMapping.setMimeType(mimeType);
            mimeMappings[i] = mimeMapping;
        }
        return mimeMappings;
    }    
    
    private WelcomeFileListImpl getWelcomeFiles(Element e) {  
        WelcomeFileListImpl welcomeFileList = null;
        NodeList welcomeFileListNL = e.getElementsByTagName("welcome-file-list");
        if (welcomeFileListNL.getLength() > 0) {
            welcomeFileList = new WelcomeFileListImpl();
            NodeList welcomeFileNL = e.getElementsByTagName("welcome-file");
            for (int i = 0; i < welcomeFileNL.getLength(); i++) {
                Element welcomeFileE = (Element)welcomeFileNL.item(i);
                welcomeFileList.add(XML.getElementText(welcomeFileE));
            }
        }
        return welcomeFileList;
    } 
    
    private ErrorPageImpl[] getErrorPages(Element e) {  
        NodeList errorPageNL = e.getElementsByTagName("error-page");
        ErrorPageImpl[] errorPages = new ErrorPageImpl[errorPageNL.getLength()];
        for (int i = 0; i < errorPageNL.getLength(); i++) {
            Element errorPageE = (Element)errorPageNL.item(i);
            String errorCode = XML.getChildElementText(errorPageE, "error-code");
            String exceptionType = XML.getChildElementText(errorPageE, "exception-type");
            String location = XML.getChildElementText(errorPageE, "location");
            ErrorPageImpl errorPage = new ErrorPageImpl();
            errorPage.setErrorCode(errorCode);
            errorPage.setExceptionType(exceptionType);
            errorPage.setLocation(location);
            errorPages[i] = errorPage;
        }
        return errorPages;
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
    
    private ResourceEnvRefImpl[] getResourceEnvRefs(Element e) {
        NodeList resourceEnvRefNL = e.getElementsByTagName("resource-env-ref");
        ResourceEnvRefImpl[] resourceEnvRefs = new ResourceEnvRefImpl[resourceEnvRefNL.getLength()];
        for (int i = 0; i < resourceEnvRefNL.getLength(); i++) {
            Element resourceEnvRefE = (Element)resourceEnvRefNL.item(i);
            DescriptionSet descriptions = getDescriptions(resourceEnvRefE);
            String resourceEnvRefName = XML.getChildElementText(resourceEnvRefE, "resource-env-ref-name");
            String resourceEnvRefType = XML.getChildElementText(resourceEnvRefE, "resource-env-ref-type");
            ResourceEnvRefImpl resourceEnvRef = new ResourceEnvRefImpl();
            resourceEnvRef.setDescriptions(descriptions);
            resourceEnvRef.setResourceEnvRefName(resourceEnvRefName);
            resourceEnvRef.setResourceEnvRefType(resourceEnvRefType);
            resourceEnvRefs[i] = resourceEnvRef;
        }
        return resourceEnvRefs;
    }
    
    private ResourceRefImpl[] getResourceRefs(Element e) {
        NodeList resourceRefNL = e.getElementsByTagName("resource-ref");
        ResourceRefImpl[] resourceRefs = new ResourceRefImpl[resourceRefNL.getLength()];
        for (int i = 0; i < resourceRefNL.getLength(); i++) {
            Element resourceRefE = (Element)resourceRefNL.item(i);
            DescriptionSet descriptions = getDescriptions(resourceRefE);
            String resRefName = XML.getChildElementText(resourceRefE, "res-ref-name");
            String resType = XML.getChildElementText(resourceRefE, "res-type");
            String resAuth = XML.getChildElementText(resourceRefE, "res-auth");
            String resSharingScope = XML.getChildElementText(resourceRefE, "res-sharing-scope");
            ResourceRefImpl resourceRef = new ResourceRefImpl();
            resourceRef.setDescriptions(descriptions);
            resourceRef.setResRefName(resRefName);
            resourceRef.setResType(resType);
            resourceRef.setResAuth(resAuth);
            resourceRef.setResSharingScope(resSharingScope);
            resourceRefs[i] = resourceRef;
        }
        return resourceRefs;
    }
    
    private SecurityConstraintImpl[] getSecurityConstraints(Element e) {
        NodeList securityConstraintNL = e.getElementsByTagName("security-constraint");
        SecurityConstraintImpl[] securityConstraints = new SecurityConstraintImpl[securityConstraintNL.getLength()];
        for (int i = 0; i < securityConstraintNL.getLength(); i++) {
            SecurityConstraintImpl securityConstraint = new SecurityConstraintImpl();
            Element securityConstraintE = (Element)securityConstraintNL.item(i);
            DisplayNameSet displayNames = getDisplayNames(securityConstraintE);
            securityConstraint.setDisplayNames(displayNames);
            NodeList webResourceCollectionNL = securityConstraintE.getElementsByTagName("web-resource-collection");
            WebResourceCollectionImpl[] webResourceCollections = new WebResourceCollectionImpl[webResourceCollectionNL.getLength()];
            for (int j = 0; j < webResourceCollectionNL.getLength(); j++) {
                WebResourceCollectionImpl webResourceCollection = new WebResourceCollectionImpl();
                Element webResourceCollectionE = (Element)webResourceCollectionNL.item(j);
                String webResourceName = XML.getChildElementText(webResourceCollectionE, "web-resource-name");
                DescriptionSet descriptions = getDescriptions(webResourceCollectionE);
                NodeList urlPatternNL = webResourceCollectionE.getElementsByTagName("url-pattern");
                String[] urlPatterns = new String[urlPatternNL.getLength()];
                for (int k = 0; k < urlPatternNL.getLength(); k++) {
                    Element urlPatternE = (Element)urlPatternNL.item(k);
                    urlPatterns[k] = XML.getElementText(urlPatternE);
                }
                NodeList httpMethodNL = webResourceCollectionE.getElementsByTagName("http-method");
                String[] httpMethods = new String[httpMethodNL.getLength()];
                for (int k = 0; k < httpMethodNL.getLength(); k++) {
                    Element httpMethodE = (Element)httpMethodNL.item(k);
                    httpMethods[k] = XML.getElementText(httpMethodE);
                }
                webResourceCollection.setWebResourceName(webResourceName);
                webResourceCollection.setDescriptions(descriptions);
                webResourceCollection.setUrlPatterns(urlPatterns);
                webResourceCollection.setHttpMethods(httpMethods);
                webResourceCollections[j] = webResourceCollection;
            }
            securityConstraint.setWebResourceCollections(webResourceCollections);
            NodeList authConstraintNL = securityConstraintE.getElementsByTagName("auth-constraint");
            if (authConstraintNL.getLength() > 0) {
                Element authConstraintE = (Element)authConstraintNL.item(0);
                AuthConstraintImpl authConstraint = new AuthConstraintImpl();
                DescriptionSet descriptions = getDescriptions(authConstraintE);
                NodeList roleNameNL = authConstraintE.getElementsByTagName("role-name");
                String[] roleNames = new String[roleNameNL.getLength()];
                for (int k = 0; k < roleNameNL.getLength(); k++) {
                    Element roleNameE = (Element)roleNameNL.item(k);
                    roleNames[k] = XML.getElementText(roleNameE);
                }
                authConstraint.setDescriptions(descriptions);
                authConstraint.setRoleNames(roleNames);
                securityConstraint.setAuthConstraint(authConstraint);        
            }
            NodeList userDataConstraintNL = securityConstraintE.getElementsByTagName("user-data-constraint");
            if (userDataConstraintNL.getLength() > 0) {
                Element userDataConstraintE = (Element)userDataConstraintNL.item(0);
                UserDataConstraintImpl userDataConstraint = new UserDataConstraintImpl();
                DescriptionSet descriptions = getDescriptions(userDataConstraintE);
                String transportGuarantee = XML.getChildElementText(userDataConstraintE, "transport-guarantee");               
                userDataConstraint.setDescriptions(descriptions);
                userDataConstraint.setTransportGuarantee(transportGuarantee);
                securityConstraint.setUserDataConstraint(userDataConstraint);
            }
            securityConstraints[i] = securityConstraint;
        }
        return securityConstraints;
    }
    
    private LoginConfigImpl getLoginConfig(Element e) {
        LoginConfigImpl loginConfig = null;
        NodeList loginConfigNL = e.getElementsByTagName("login-config");
        if (loginConfigNL.getLength() > 0) {
            Element loginConfigE = (Element)loginConfigNL.item(0);
            loginConfig = new LoginConfigImpl();
            loginConfig.setAuthMethod(XML.getChildElementText(loginConfigE, "auth-method"));
            loginConfig.setRealmName(XML.getChildElementText(loginConfigE, "realm-name"));
            NodeList formLoginConfigNL = loginConfigE.getElementsByTagName("form-login-config");
            if (formLoginConfigNL.getLength() > 0) {
                Element formLoginConfigE = (Element)formLoginConfigNL.item(0);
                FormLoginConfigImpl formLoginConfig = new FormLoginConfigImpl();
                formLoginConfig.setFormLoginPage(XML.getChildElementText(formLoginConfigE, "form-login-page"));
                formLoginConfig.setFormErrorPage(XML.getChildElementText(formLoginConfigE, "form-error-page"));
                loginConfig.setFormLoginConfig(formLoginConfig);
            }
        }
        return loginConfig;
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
    
    private EnvEntryImpl[] getEnvEntries(Element e) {
        NodeList envEntryNL = e.getElementsByTagName("env-entry");
        EnvEntryImpl[] envEntries = new EnvEntryImpl[envEntryNL.getLength()];
        for (int i = 0; i < envEntryNL.getLength(); i++) {
            Element envEntryE = (Element)envEntryNL.item(i);
            envEntries[i].setDescriptions(getDescriptions(envEntryE));
            envEntries[i].setEnvEntryName(XML.getChildElementText(envEntryE, "env-entry-name"));
            envEntries[i].setEnvEntryValue(XML.getChildElementText(envEntryE, "env-entry-value"));
            envEntries[i].setEnvEntryType(XML.getChildElementText(envEntryE, "env-entry-type"));
        }
        return envEntries;
    }

    private EjbRefImpl[] getEjbRefs(Element e) {
        NodeList ejbRefNL = e.getElementsByTagName("ejb-ref");
        EjbRefImpl[] ejbRefs = new EjbRefImpl[ejbRefNL.getLength()];
        for (int i = 0; i < ejbRefNL.getLength(); i++) {
            Element ejbRefE = (Element)ejbRefNL.item(i);
            ejbRefs[i].setDescriptions(getDescriptions(ejbRefE));
            ejbRefs[i].setEjbRefName(XML.getChildElementText(ejbRefE, "ejb-ref-name"));
            ejbRefs[i].setEjbRefType(XML.getChildElementText(ejbRefE, "ejb-ref-type"));
            ejbRefs[i].setHome(XML.getChildElementText(ejbRefE, "home"));
            ejbRefs[i].setRemote(XML.getChildElementText(ejbRefE, "remote"));
            ejbRefs[i].setEjbLink(XML.getChildElementText(ejbRefE, "ejb-link"));
        }
        return ejbRefs;
    }

    private EjbLocalRefImpl[] getEjbLocalRefs(Element e) {
        NodeList ejbRefNL = e.getElementsByTagName("ejb-local-ref");
        EjbLocalRefImpl[] ejbLocalRefs = new EjbLocalRefImpl[ejbRefNL.getLength()];
        for (int i = 0; i < ejbRefNL.getLength(); i++) {
            Element ejbLocalRefE = (Element)ejbRefNL.item(i);
            ejbLocalRefs[i].setDescriptions(getDescriptions(ejbLocalRefE));
            ejbLocalRefs[i].setEjbRefName(XML.getChildElementText(ejbLocalRefE, "ejb-ref-name"));
            ejbLocalRefs[i].setEjbRefType(XML.getChildElementText(ejbLocalRefE, "ejb-ref-type"));
            ejbLocalRefs[i].setLocalHome(XML.getChildElementText(ejbLocalRefE, "local-home"));
            ejbLocalRefs[i].setLocal(XML.getChildElementText(ejbLocalRefE, "local"));
            ejbLocalRefs[i].setEjbLink(XML.getChildElementText(ejbLocalRefE, "ejb-link"));
        }
        return ejbLocalRefs;
    }
    
}
