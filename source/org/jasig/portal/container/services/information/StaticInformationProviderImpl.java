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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.pluto.services.information.PortalContextProvider;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.jasig.portal.container.binding.PortletApplicationUnmarshaller;
import org.jasig.portal.container.binding.WebApplicationUnmarshaller;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.portlet.PortletApplicationDefinitionListImpl;
import org.jasig.portal.services.LogService;

/**
 * Implementation of Apache Pluto StaticInformationProvider.
 * The current implementation gets its data by parsing the web applciation
 * deployment descriptor (web.xml) and the portlet application deployment
 * descriptor (portlet.xml) of all installed portlet contexts.  Contexts which
 * contain a valid portlet.xml file in the WEB-INF directory are considered
 * portlet contexts.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class StaticInformationProviderImpl implements StaticInformationProvider {
    
    private ServletConfig servletConfig = null;
    private Properties properties = null;
    private static PortletApplicationDefinitionListImpl portletApplicationDefinitionList = null;
    private static PortalContextProvider portalContextProvider = null;
    
    // StaticInformationProvider methods
    
    public PortalContextProvider getPortalContextProvider() {
        if (portalContextProvider == null)
            portalContextProvider = new PortalContextProviderImpl();
        return portalContextProvider;
    }

    public PortletDefinition getPortletDefinition(ObjectID portletGUID) {
        // I think this method should throw an exception,
        // but Pluto currently defines it to throw no exception.
        String portletDefinitionId = portletGUID.toString();
        String contextName = portletDefinitionId.substring(0, portletDefinitionId.indexOf("."));
        PortletApplicationDefinition portletApplicationDefinition = portletApplicationDefinitionList.get(ObjectIDImpl.createFromString(contextName));
        PortletDefinition portletDefinition = null;
        if (portletApplicationDefinition != null) {
            PortletDefinitionList portletDefinitionList = portletApplicationDefinition.getPortletDefinitionList();
            portletDefinition = portletDefinitionList.get(ObjectIDImpl.createFromString(portletDefinitionId));
        }
        return portletDefinition;
    }

    // Additional methods
    
    public void init(ServletConfig servletConfig, Properties properties) {
        this.servletConfig = servletConfig;
        this.properties = properties;
        portletApplicationDefinitionList = new PortletApplicationDefinitionListImpl();
        initPortletDefinitions();
    }
    
    /**
     * Go through the webapps directory, look for all web.xml and portlet.xml files
     * for portlet web applications. Then parse these files and create data structures
     * representing the portlet application definitions and servlet definitions.
     * This should occur just once as the portlet container starts up.
     */
    private void initPortletDefinitions() {
        String portalDirName = servletConfig.getServletContext().getRealPath("/"); //root
        File webappsDir = new File(portalDirName).getParentFile();
        File[] files1 = webappsDir.listFiles(); // portlet app candidates
        for (int i = 0; i < files1.length; i++) {
            File webapp = files1[i];
            if (webapp.isDirectory()) {
                File[] files2 = webapp.listFiles(); // WEB-INF candidates
                for (int j = 0; j < files2.length; j++) {
                    File webinf = files2[j];
                    if (webinf.isDirectory() && webinf.getName().equals("WEB-INF")) {
                        File webXmlFile = null;
                        File portletXmlFile = null;
                        boolean isPortletApp = false;
                        boolean gotWebXml = false;
                        boolean gotPortletXml = false;
                        File[] files3 = webinf.listFiles(); // web.xml and portlet.xml candidates
                        for (int k = 0; k < files3.length; k++) {
                            File file = files3[k];
                            if (file.getName().equals("web.xml")) {
                                gotWebXml = true;
                                webXmlFile = files3[k];
                            } else if (file.getName().equals("portlet.xml")) {
                                gotPortletXml = true;
                                portletXmlFile = files3[k];
                            }
                        }
                        isPortletApp = gotWebXml && gotPortletXml;
                        if (isPortletApp) {
                            String contextName = resolveUri(files1[i].getName());
                            
                            String xmlFile = null;
                            LogService.log(LogService.INFO, "Found portlet application " + contextName);

                            try {
                                // Parse the web.xml file --> WebApplicationDefinition
                                xmlFile = "web.xml";
                                WebApplicationUnmarshaller wau = new WebApplicationUnmarshaller();
                                wau.init(new FileInputStream(webXmlFile), contextName);
                                WebApplicationDefinition webApplicationDefinition = wau.getWebApplicationDefinition();
                                
                                // Parse the portlet.xml file --> PortletApplicationDefinition
                                xmlFile = "portlet.xml";
                                PortletApplicationUnmarshaller pau = new PortletApplicationUnmarshaller();
                                pau.init(new FileInputStream(portletXmlFile), contextName);
                                PortletApplicationDefinition portletApplicationDefinition = pau.getPortletApplicationDefinition(webApplicationDefinition);
                            
                                // Add this PortletApplicationDefinition to the list
                                portletApplicationDefinitionList.add(portletApplicationDefinition.getId().toString(), portletApplicationDefinition);
                            } catch (Exception e) {
                                LogService.log(LogService.ERROR, "Unable to parse " + xmlFile + " for context '" + contextName + "'", e);
                            }
                        }
                    }
                }
            }
        }     
    }
    
    /**
     * Handles resolution of a web module's file system name to its URI identifier.
     * @param webModule the file system name.
     * @return the URI part.
     */
    private String resolveUri(String webModule) {
        // The initial portion of the web module prefix used by JBoss.
        final String INITIAL_TMP_PREFIX = "tmp";

        // The length of the full web module prefix used by JBoss plus numeric portion).
        final int FULL_TMP_PREFIX_LEN = INITIAL_TMP_PREFIX.length() + 5;

        // The file extension for web application archives (including the leading dot).
        final String WAR_FILE_EXT = ".war";
        
        // The length of the file extension for web application archives (including the leading dot).
        final int WAR_FILE_EXT_LEN = WAR_FILE_EXT.length();

        // For JBoss compatibility, change webModule from the form
        // of "tmp12345foo.war" to "foo".
        int len = webModule.length();
        if (webModule.endsWith(WAR_FILE_EXT)) {
            if (webModule.startsWith(INITIAL_TMP_PREFIX) && len > FULL_TMP_PREFIX_LEN + WAR_FILE_EXT_LEN) {
                webModule = webModule.substring(FULL_TMP_PREFIX_LEN, len - WAR_FILE_EXT_LEN);
            } else {
                webModule = webModule.substring(0, len - WAR_FILE_EXT_LEN);
            }
        }
        // else assumed literal.
        return webModule;
    }    
}
