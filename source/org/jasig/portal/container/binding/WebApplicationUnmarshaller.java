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
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionImpl;
import org.jasig.portal.container.om.servlet.ServletDefinitionListImpl;
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
        webApplicationDefinition.setDisplayNames(getDisplayNames(webAppE));
        webApplicationDefinition.setDescriptions(getDescriptions(webAppE));
        webApplicationDefinition.setContextRoot("/" + contextName);
        webApplicationDefinition.setServletDefinitionList(getServletDefinitions(webAppE));    
        return webApplicationDefinition;
    }

    private ServletDefinitionList getServletDefinitions(Element webAppE) {
        ServletDefinitionListImpl servletDefinitions = new ServletDefinitionListImpl();
         
        NodeList servletNL = webAppE.getElementsByTagName("servlet");
        for (int i = 0; i < servletNL.getLength(); i += 1) {
            Element servletE = (Element)servletNL.item(i);
            ServletDefinitionImpl servletDefinition = new ServletDefinitionImpl();
            servletDefinition.setServletName(XML.getChildElementText(servletE, "servlet-name"));
            servletDefinition.setServletClass(XML.getChildElementText(servletE, "servlet-class"));
            servletDefinition.setDisplayNames(getDisplayNames(servletE));
            servletDefinition.setDescriptions(getDescriptions(servletE));
            
            NodeList initParamNL = servletE.getElementsByTagName("init-param");
            for (int j = 0; j < initParamNL.getLength(); j += 1) {
                Element initParamE = (Element)initParamNL.item(j);
                ParameterSetImpl parameters = new ParameterSetImpl();
                parameters.add(XML.getChildElementText(initParamE, "param-name"), XML.getChildElementText(initParamE, "param-value"));
                servletDefinition.setInitParameters(parameters);
            }

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
    
    private DisplayNameSet getDisplayNames(Element webAppE) {
        DisplayNameSetImpl displayNames = new DisplayNameSetImpl();
        NodeList displayNameNL = webAppE.getElementsByTagName("display-name");
        for (int i = 0; i < displayNameNL.getLength(); i += 1) {
            Element displayNameE = (Element)displayNameNL.item(i);
            String displayName = XML.getElementText(displayNameE);
            displayNames.add(displayName, Locale.getDefault());
        }
        return displayNames;
    }
    
    private DescriptionSet getDescriptions(Element webAppE) {
        DescriptionSetImpl descriptions = new DescriptionSetImpl();
        NodeList descriptionNL = webAppE.getElementsByTagName("description");
        for (int i = 0; i < descriptionNL.getLength(); i += 1) {
            Element descriptionE = (Element)descriptionNL.item(i);
            String description = XML.getElementText(descriptionE);
            descriptions.add(description, Locale.getDefault());
        }
        return descriptions;
    }

}
