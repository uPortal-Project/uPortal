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

package org.jasig.portal.container.services.information;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletMode;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.services.information.PortalContextProvider;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.LanguageSetImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.portlet.ContentTypeImpl;
import org.jasig.portal.container.om.portlet.ContentTypeSetImpl;
import org.jasig.portal.container.om.portlet.PortletApplicationDefinitionImpl;
import org.jasig.portal.container.om.portlet.PortletDefinitionImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionImpl;
import org.jasig.portal.container.om.servlet.WebApplicationDefinitionImpl;

/**
 * Implementation of Apache Pluto StaticInformationProvider.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class StaticInformationProviderImpl implements StaticInformationProvider {
    
    public static Map portletDefinitions = null;
    public static PortalContextProvider portalContextProvider = null;
    
    public StaticInformationProviderImpl() {
        portletDefinitions = new HashMap();
        initPortletDefinitions();
    }

    // StaticInformationProvider methods
    
    public PortalContextProvider getPortalContextProvider() {
       if (  portalContextProvider == null )	
        portalContextProvider = new PortalContextProviderImpl();
       return portalContextProvider; 
    }

    public PortletDefinition getPortletDefinition(ObjectID portletGUID) {
        return (PortletDefinition)portletDefinitions.get(portletGUID.toString());
    }

    // Additional methods
    
    private void initPortletDefinitions() {
        // We should initialize by going though the base web modules directory,
        // which is "webapps" in Tomcat and look through all subdirectories
        // for web.xml and portlet.xml files.  Then we need to parse these files
        // and register each WebApplicationDefinition, ServletDefinition, 
        // PortletApplicationDefinition and PortletDefinition 
        // by their GUID into a static HashMap. What a pain!
        
        // For now we will just hard-code some PortletDefinitions
        try {            
            // Comes out of the portlet context's web.xml file
            WebApplicationDefinitionImpl webApplicationDefinition = new WebApplicationDefinitionImpl();
            webApplicationDefinition.setId("testsuite");
            webApplicationDefinition.addDisplayName("testsuite", Locale.getDefault());
            webApplicationDefinition.addDescription("Automated generated Application Wrapper", Locale.getDefault());
            webApplicationDefinition.setContextRoot("/testsuite");

            ServletDefinitionImpl servletDefinition1 = new ServletDefinitionImpl("TestPortlet1", "org.apache.pluto.core.PortletServlet");
            servletDefinition1.setWebApplicationDefinition(webApplicationDefinition);
            servletDefinition1.setServletMapping("TestPortlet1", "/TestPortlet1/*");

            ServletDefinitionImpl servletDefinition2 = new ServletDefinitionImpl("TestPortlet2", "org.apache.pluto.core.PortletServlet");
            servletDefinition2.setWebApplicationDefinition(webApplicationDefinition);
            servletDefinition2.setServletMapping("TestPortlet2", "/TestPortlet2/*");

            PortletApplicationDefinitionImpl portletApplicationDefinition = new PortletApplicationDefinitionImpl();
            portletApplicationDefinition.setId("testsuite"); // ???
            portletApplicationDefinition.setVersion("1.0");
            portletApplicationDefinition.setWebApplicationDefinition(webApplicationDefinition);

            // Add 1st portlet definition
            PortletDefinitionImpl portletDefinition1 = null;
            String portletDefinitionId1 = "testsuite.TestPortlet1";
            
            portletDefinition1 = new PortletDefinitionImpl();
            portletDefinition1.setId(portletDefinitionId1);
            portletDefinition1.setClassName("org.apache.pluto.portalImpl.portlet.TestPortlet");           
            portletDefinition1.setName("TestPortlet1");
                
            DescriptionSetImpl descriptions = new DescriptionSetImpl();
            descriptions.add("TestSuiteDescription", Locale.getDefault());
            portletDefinition1.setDescriptions(descriptions);
                
            LanguageSetImpl languages = new LanguageSetImpl("Test Portlet #1", "Test1", "Test, Testen", "TestPortlet");
            languages.setClassLoader(Thread.currentThread().getContextClassLoader());
            languages.addLanguage(new Locale("en", ""));
            languages.addLanguage(new Locale("de", ""));
            
            portletDefinition1.setLanguages(languages);
                
            ParameterSetImpl parameters = new ParameterSetImpl();
            parameters.add("dummyName", "dummyValue");
            portletDefinition1.setInitParameters(parameters);
                
            PreferenceSetImpl preferences = new PreferenceSetImpl();
            Collection values1 = new ArrayList();
            values1.add("dummyValue");
            preferences.add("dummyName", values1);
            Collection values2 = new ArrayList();
            values2.add("dummyValue");
            preferences.add("dummyName", values2);
            //preferences.setPreferencesValidator("org.apache.pluto.core.impl.PreferencesValidatorImpl");
            portletDefinition1.setPreferences(preferences);
                
            ContentTypeSetImpl contentTypes = new ContentTypeSetImpl();
            ContentTypeImpl contentType = new ContentTypeImpl();
            contentType.setContentType("text/html");
            contentType.addPortletMode(PortletMode.VIEW);
            contentType.addPortletMode(PortletMode.EDIT);
            contentType.addPortletMode(PortletMode.HELP);
            contentTypes.add(contentType);
            portletDefinition1.setContentTypes(contentTypes);
    
            portletDefinition1.setServletDefinition(servletDefinition1);
            
            portletDefinition1.setPortletApplicationDefinition(portletApplicationDefinition);
                
            DisplayNameSetImpl displayNames1 = new DisplayNameSetImpl();
            displayNames1.add("Test Portlet #1", Locale.getDefault());
            portletDefinition1.setDisplayNames(displayNames1);
                
            portletDefinition1.setExpirationCache("-1");
                       
            portletDefinitions.put(portletDefinitionId1, portletDefinition1);

            // Add 2nd portlet definition
            PortletDefinitionImpl portletDefinition2 = null;
            String portletDefinitionId2 = "testsuite.TestPortlet2";
            
            portletDefinition2 = new PortletDefinitionImpl();
            portletDefinition2.setId(portletDefinitionId2);
            portletDefinition2.setClassName("org.apache.pluto.portalImpl.portlet.TestPortlet");           
            portletDefinition2.setName("TestPortlet2");
                
            DescriptionSetImpl descriptions2 = new DescriptionSetImpl();
            descriptions2.add("TestSuiteDescription", Locale.getDefault());
            portletDefinition2.setDescriptions(descriptions2);
                
            LanguageSetImpl languages2 = new LanguageSetImpl("Test Portlet #2", "Test2", "Test, Testen", "TestPortlet");
            languages2.setClassLoader(Thread.currentThread().getContextClassLoader());
            languages2.addLanguage(new Locale("en", ""));
            languages2.addLanguage(new Locale("de", ""));
            
            portletDefinition2.setLanguages(languages);
                
            ParameterSetImpl parameters2 = new ParameterSetImpl();
            parameters2.add("dummyName", "dummyValue");
            portletDefinition2.setInitParameters(parameters2);
                
            PreferenceSetImpl preferences2 = new PreferenceSetImpl();
            Collection values21 = new ArrayList();
            values2.add("dummyValue");
            preferences2.add("dummyName", values21);
            Collection values22 = new ArrayList();
            values2.add("dummyValue");
            preferences2.add("dummyName", values22);
            //preferences.setPreferencesValidator("org.apache.pluto.core.impl.PreferencesValidatorImpl");
            portletDefinition2.setPreferences(preferences);
                
            ContentTypeSetImpl contentTypes2 = new ContentTypeSetImpl();
            ContentTypeImpl contentType2 = new ContentTypeImpl();
            contentType2.setContentType("text/html");
            contentType2.addPortletMode(PortletMode.VIEW);
            contentType2.addPortletMode(PortletMode.EDIT);
            contentType2.addPortletMode(PortletMode.HELP);
            contentTypes2.add(contentType2);
            portletDefinition2.setContentTypes(contentTypes2);
    
            portletDefinition2.setServletDefinition(servletDefinition2);
    
            portletDefinition2.setPortletApplicationDefinition(portletApplicationDefinition);

            DisplayNameSetImpl displayNames2 = new DisplayNameSetImpl();
            displayNames2.add("Test Portlet #2", Locale.getDefault());
            portletDefinition2.setDisplayNames(displayNames2);
                
            portletDefinition2.setExpirationCache("-1");
            
            portletDefinitions.put(portletDefinitionId2, portletDefinition2);
        } catch (Exception e) {
            e.printStackTrace();
        }      
    }
}
