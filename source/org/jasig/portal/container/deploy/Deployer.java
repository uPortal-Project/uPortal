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

package org.jasig.portal.container.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.pluto.om.ControllerFactory;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterCtrl;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.ParameterSetCtrl;
import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.common.SecurityRoleRefSetCtrl;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionCtrl;
import org.apache.pluto.om.servlet.ServletDefinitionListCtrl;
import org.jasig.portal.container.binding.PortletApplicationUnmarshaller;
import org.jasig.portal.container.binding.WebApplicationMarshaller;
import org.jasig.portal.container.binding.WebApplicationUnmarshaller;
import org.jasig.portal.container.factory.ControllerFactoryImpl;
import org.jasig.portal.container.om.common.DescriptionImpl;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionImpl;
import org.jasig.portal.container.om.servlet.ServletMappingImpl;
import org.jasig.portal.container.om.servlet.ServletMappingListImpl;
import org.jasig.portal.container.om.servlet.TagLibImpl;
import org.jasig.portal.container.om.servlet.WebApplicationDefinitionImpl;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.Serializer;
import org.jasig.portal.serialize.XMLSerializer;

/**
 * A tool that deploys a portlet application into the servlet container.
 * In the process, this tool adds the necessary portlet-wrapping servlets
 * into the portlet application deployment descriptor (web.xml file).
 * This code was borrowed from Pluto's Deploy tool and then modified to
 * fit the uPortal environment.  Pluto used Castor to marshall and unmarshall
 * XML files.  We are doing this work ourselves.  This tool is intended to be
 * launched from a command line tool such as Apache Ant and the buid.xml for
 * uPortal includes a corresponding ant target called.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class Deployer {

    public static final String WEB_PORTLET_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WEB_PORTLET_DTD = "http://java.sun.com/dtd/web-app_2_3.dtd";
    public static final String WEB_PORTLET_TAGLIB_URI = "http://java.sun.com/portlet";
    public static final String WEB_PORTLET_TAGLIB_LOCATION = "/WEB-INF/tld/portlet.tld";

    private static boolean debug = false;
    private static String dirDelim = File.separator;
    private static String webInfDir = dirDelim + "WEB-INF" + dirDelim;
    private static String webAppsDir;
    private static final String portletAppDir = "lib" + dirDelim + "portlets";

    public static void deployArchive(String webAppsDir, String warFile) throws IOException {
        String warFileName = warFile;
        if (warFileName.indexOf("/") != -1)
            warFileName = warFileName.substring(warFileName.lastIndexOf("/") + 1);
        if (warFileName.indexOf(dirDelim) != -1)
            warFileName = warFileName.substring(warFileName.lastIndexOf(dirDelim) + 1);
        if (warFileName.endsWith(".war"))
            warFileName = warFileName.substring(0, warFileName.lastIndexOf("."));

        System.out.println("deploying '" + warFileName + "' ...");

        String destination = webAppsDir + warFileName;

        JarFile jarFile = new JarFile(warFile);
        Enumeration files = jarFile.entries();
        while (files.hasMoreElements()) {
            JarEntry entry = (JarEntry)files.nextElement();

            File file = new File(destination, entry.getName());
            File dirF = new File(file.getParent());
            dirF.mkdirs();
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                byte[] buffer = new byte[1024];
                int length = 0;
                InputStream fis = jarFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(file);
                while ((length = fis.read(buffer)) >= 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
            }

        }

        System.out.println("finished!");
    }

    public static void prepareWebArchive(String webAppsDir, String warFile) throws Exception, IOException {
        String webModule = warFile;
        if (webModule.indexOf("/") != -1)
            webModule = webModule.substring(webModule.lastIndexOf("/") + 1);
        if (webModule.indexOf(dirDelim) != -1)
            webModule = webModule.substring(webModule.lastIndexOf(dirDelim) + 1);
        if (webModule.endsWith(".war"))
            webModule = webModule.substring(0, webModule.lastIndexOf("."));

        System.out.println("prepare web archive '" + webModule + "' ...");


        File portletXml = new File(webAppsDir + webModule + webInfDir + "portlet.xml");
        File webXml = new File(webAppsDir + webModule + webInfDir + "web.xml");

        try {                        
            // Generate the web application definition out of web.xml
            // If one doesn't exist, create it
            WebApplicationDefinitionImpl webApplicationDefinition = null;
            if (webXml.exists()) {
                WebApplicationUnmarshaller wau = new WebApplicationUnmarshaller();
                wau.init(new FileInputStream(webXml), webModule);
                webApplicationDefinition = (WebApplicationDefinitionImpl)wau.getWebApplicationDefinition();
            } else {
                webApplicationDefinition = new WebApplicationDefinitionImpl();
                DisplayNameImpl dispName = new DisplayNameImpl();
                dispName.setDisplayName(webModule);
                dispName.setLocale(Locale.getDefault());
                DisplayNameSetImpl dispSet = new DisplayNameSetImpl();
                dispSet.add(dispName);
                webApplicationDefinition.setDisplayNames((DisplayNameSet)dispSet);
                DescriptionImpl desc = new DescriptionImpl();
                desc.setDescription("Automated generated Application Wrapper");
                desc.setLocale(Locale.getDefault());
                DescriptionSetImpl descSet = new DescriptionSetImpl();
                descSet.add(desc);
                webApplicationDefinition.setDescriptions((DescriptionSet)descSet);
            }
            
            // Now get the portlet application definition out of portlet.xml
            PortletApplicationUnmarshaller pau = new PortletApplicationUnmarshaller();
            pau.init(new FileInputStream(portletXml), webModule);
            PortletApplicationDefinition portletApplicationDefinition = pau.getPortletApplicationDefinition(webApplicationDefinition);
          
            ControllerFactory controllerFactory = new ControllerFactoryImpl();

            ServletDefinitionListCtrl servletDefinitionSetCtrl = (ServletDefinitionListCtrl)controllerFactory.get(webApplicationDefinition.getServletDefinitionList());
            ServletMappingListImpl servletMappings = webApplicationDefinition.getServletMappings();

            Iterator portlets = portletApplicationDefinition.getPortletDefinitionList().iterator();
            while (portlets.hasNext()) {

                PortletDefinition portlet = (PortletDefinition)portlets.next();

                // Check if already exists
                ServletDefinition servlet = webApplicationDefinition.getServletDefinitionList().get(portlet.getName());
                if (servlet != null) {
                    if (!servlet.getServletClass().equals("org.apache.pluto.core.PortletServlet")) {
                        System.out.println("Note: Replaced already existing the servlet with the name '" + portlet.getName() + "' with the wrapper servlet.");
                    }
                    ServletDefinitionCtrl _servletCtrl = (ServletDefinitionCtrl)controllerFactory.get(servlet);
                    _servletCtrl.setServletClass("org.apache.pluto.core.PortletServlet");
                } else {
                    servlet = servletDefinitionSetCtrl.add(portlet.getName(), "org.apache.pluto.core.PortletServlet");
                }

                ServletDefinitionCtrl servletCtrl = (ServletDefinitionCtrl)controllerFactory.get(servlet);

                DisplayNameImpl dispName = new DisplayNameImpl();
                dispName.setDisplayName(portlet.getName() + " Wrapper");
                dispName.setLocale(Locale.getDefault());
                DisplayNameSetImpl dispSet = new DisplayNameSetImpl();
                dispSet.add(dispName);
                servletCtrl.setDisplayNames((DisplayNameSet)dispSet);
                DescriptionImpl desc = new DescriptionImpl();
                desc.setDescription("Automated generated Portlet Wrapper");
                desc.setLocale(Locale.getDefault());
                DescriptionSetImpl descSet = new DescriptionSetImpl();
                descSet.add(desc);
                servletCtrl.setDescriptions(descSet);
                ParameterSet parameters = servlet.getInitParameterSet();

                ParameterSetCtrl parameterSetCtrl = (ParameterSetCtrl)controllerFactory.get(parameters);

                Parameter parameter1 = parameters.get("portlet-class");
                if (parameter1 == null) {
                    parameterSetCtrl.add("portlet-class", portlet.getClassName());
                } else {
                    ParameterCtrl parameterCtrl = (ParameterCtrl)controllerFactory.get(parameter1);
                    parameterCtrl.setValue(portlet.getClassName());

                }
                Parameter parameter2 = parameters.get("portlet-guid");
                if (parameter2 == null) {
                    parameterSetCtrl.add("portlet-guid", portlet.getId().toString());
                } else {
                    ParameterCtrl parameterCtrl = (ParameterCtrl)controllerFactory.get(parameter2);
                    parameterCtrl.setValue(portlet.getId().toString());

                }

                boolean found = false;
                Iterator mappings = servletMappings.iterator();
                while (mappings.hasNext()) {
                    ServletMappingImpl servletMapping = (ServletMappingImpl)mappings.next();
                    if (servletMapping.getServletName().equals(portlet.getName())) {
                        found = true;
                        servletMapping.setUrlPattern("/" + portlet.getName().replace(' ', '_') + "/*");
                    }
                }
                if (!found) {
                    ServletMappingImpl servletMapping = new ServletMappingImpl();
                    servletMapping.setServletName(portlet.getName());
                    servletMapping.setUrlPattern("/" + portlet.getName().replace(' ', '_') + "/*");
                    servletMappings.add(servletMapping);
                }
                
                // Add in portlet taglib
                TagLibImpl tagLib = new TagLibImpl();
                tagLib.setTaglibUri(WEB_PORTLET_TAGLIB_URI);
                tagLib.setTaglibLocation(WEB_PORTLET_TAGLIB_LOCATION);
                webApplicationDefinition.getTagLibs().add(tagLib);                

                SecurityRoleRefSet servletSecurityRoleRefs = ((ServletDefinitionImpl)servlet).getSecurityRoleRefs();

                SecurityRoleRefSetCtrl servletSecurityRoleRefSetCtrl = (SecurityRoleRefSetCtrl)controllerFactory.get(servletSecurityRoleRefs);

                SecurityRoleSet webAppSecurityRoles = webApplicationDefinition.getSecurityRoles();

                SecurityRoleRefSet portletSecurityRoleRefs = portlet.getInitSecurityRoleRefSet();

                SecurityRoleRefSetCtrl portletSecurityRoleRefSetCtrl = (SecurityRoleRefSetCtrl)controllerFactory.get(portletSecurityRoleRefs);

                Iterator p = portletSecurityRoleRefs.iterator();

                while (p.hasNext()) {
                    SecurityRoleRef portletSecurityRoleRef = (SecurityRoleRef)p.next();

                    if (portletSecurityRoleRef.getRoleLink() == null && webAppSecurityRoles.get(portletSecurityRoleRef.getRoleName()) == null) {
                        System.out.println(
                            "Note: The web application has no security role defined which matches the role name \""
                                + portletSecurityRoleRef.getRoleName()
                                + "\" of the security-role-ref element defined for the wrapper-servlet with the name '"
                                + portlet.getName()
                                + "'.");
                        break;
                    }
                    SecurityRoleRef servletSecurityRoleRef = servletSecurityRoleRefs.get(portletSecurityRoleRef.getRoleName());
                    if (null != servletSecurityRoleRef) {
                        System.out.println(
                            "Note: Replaced already existing element of type <security-role-ref> with value \""
                                + portletSecurityRoleRef.getRoleName()
                                + "\" for subelement of type <role-name> for the wrapper-servlet with the name '"
                                + portlet.getName()
                                + "'.");
                        servletSecurityRoleRefSetCtrl.remove(servletSecurityRoleRef);
                    }
                    servletSecurityRoleRefSetCtrl.add(portletSecurityRoleRef);
                }

            }

            if (debug) {
                System.out.println(webApplicationDefinition);
            }

            OutputFormat of = new OutputFormat();
            of.setIndenting(true);
            of.setIndent(4); // 2-space indention
            of.setLineWidth(16384);
            // As large as needed to prevent linebreaks in text nodes
            of.setDoctype(WEB_PORTLET_PUBLIC_ID, WEB_PORTLET_DTD);

            FileWriter writer = new FileWriter(webAppsDir + webModule + File.separator + "WEB-INF" + File.separator + "web.xml");
            Serializer serializer = new XMLSerializer(writer, of);
            try {
                WebApplicationMarshaller wam = new WebApplicationMarshaller();
                wam.init(webApplicationDefinition, serializer);
                wam.marshall();
            } catch (Exception e) {
                writer.close();
                e.printStackTrace(System.out);
                throw new Exception();
            }
            String strTo = dirDelim + "WEB-INF" + dirDelim + "tld" + dirDelim + "portlet.tld";
            String strFrom = "webpages" + strTo;

            copy(strFrom, webAppsDir + webModule + strTo);
        } catch (Exception e) {

            e.printStackTrace(System.out);
            throw new Exception();
        }

        System.out.println("finished!");
    }

    public static void copy(String from, String to) throws IOException {
        File f = new File(to);
        f.getParentFile().mkdirs();

        byte[] buffer = new byte[1024];
        int length = 0;
        InputStream fis = new FileInputStream(from);
        FileOutputStream fos = new FileOutputStream(f);

        while ((length = fis.read(buffer)) >= 0) {
            fos.write(buffer, 0, length);
        }
        fos.close();
    }

    public static void main(String args[]) {
        String warFile;

        if (args.length < 2) {
            System.out.println("No argument specified. This command hast to be issued as:");
            System.out.println(
                "deploy <TOMCAT-webapps-directory> <web-archive> [-debug]");
            return;
        }

        if (args.length > 2) {
            if ((args[2].equals("-debug")) || (args[4].equals("/debug"))) {
                debug = true;
            }
        }

        if (debug) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("args[" + i + "]:" + args[i]);
            }
        }

        webAppsDir = args[0];
        if (!webAppsDir.endsWith(dirDelim))
            webAppsDir += dirDelim;

        warFile = args[1];

        if (args.length > 2) {
            if ((args[2].equals("-debug")) || (args[4].equals("/debug"))) {
                debug = true;
            }
        }

        try {
            if (warFile.equals("all")) {
                // In the case of "all", we loop through all the portlet
                // application archives and deploy each one
                File portletDir = new File(portletAppDir);
                if (portletDir.exists()) {
                    File[] portletApps = portletDir.listFiles();
                    for (int i = 0; i < portletApps.length; i++) {
                        String portletApp = portletApps[i].getPath();
                        if (portletApp.endsWith(".war")) {
                            deployArchive(webAppsDir, portletApp);
                            prepareWebArchive(webAppsDir, portletApp);
                        }
                    }
                }
                
            } else {
                // A specific portlet application was specified, so deploy it
                deployArchive(webAppsDir, warFile);
                prepareWebArchive(webAppsDir, warFile);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

}
