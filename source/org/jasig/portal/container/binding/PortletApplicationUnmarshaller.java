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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.portlet.PortletMode;

import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.LanguageSet;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.portlet.ContentTypeSet;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.DescriptionSetImpl;
import org.jasig.portal.container.om.common.DisplayNameSetImpl;
import org.jasig.portal.container.om.common.LanguageSetImpl;
import org.jasig.portal.container.om.common.ParameterSetImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.portlet.ContentTypeImpl;
import org.jasig.portal.container.om.portlet.ContentTypeSetImpl;
import org.jasig.portal.container.om.portlet.PortletApplicationDefinitionImpl;
import org.jasig.portal.container.om.portlet.PortletDefinitionImpl;
import org.jasig.portal.container.om.portlet.PortletDefinitionListImpl;
import org.jasig.portal.container.om.portlet.UserAttributeImpl;
import org.jasig.portal.container.om.portlet.UserAttributeListImpl;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses a <code>portlet.xml</code> file and produces data structures.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletApplicationUnmarshaller {

    private InputStream inputStream = null;
    private String contextName = null;
    private Document doc = null; // Might want to consider SAX instead of DOM parsing
    private PortletApplicationDefinitionImpl portletApplicationDefinition = null;
    
    public PortletApplicationUnmarshaller() {
        portletApplicationDefinition = new PortletApplicationDefinitionImpl();
    }

    /**
     * Initializer that takes an input stream to the <code>portlet.xml</code> file.
     * @param inputStream an input stream to the contents of the <code>portlet.xml</code> file
     * @throws IOException
     * @throws SAXException
     */
    public void init(InputStream inputStream, String contextName) throws IOException, SAXException {
        this.inputStream = inputStream;
        this.contextName = contextName;
        this.doc = DocumentFactory.getDocumentFromStream(inputStream);    
    }
    
    /**
     * Returns a PortletApplicationDefinition object that was populated with data 
     * from the <code>portlet.xml</code> file.
     * @return the portlet application definition
     */
    public PortletApplicationDefinition getPortletApplicationDefinition(WebApplicationDefinition webApplicationDefinition) {
        Element portletAppE = doc.getDocumentElement();
        portletApplicationDefinition.setId(contextName);
        portletApplicationDefinition.setVersion(portletAppE.getAttribute("version"));
        portletApplicationDefinition.setPortletDefinitionList(getPortletDefinitions(portletAppE, webApplicationDefinition));
        portletApplicationDefinition.setUserAttributes(getUserAttributes(portletAppE));
        portletApplicationDefinition.setWebApplicationDefinition(webApplicationDefinition);
        return portletApplicationDefinition;
    }
    
    private PortletDefinitionList getPortletDefinitions(Element portletAppE, WebApplicationDefinition webApplicationDefinition) {
        PortletDefinitionListImpl portletDefinitions = new PortletDefinitionListImpl();
        
        NodeList portletNL = portletAppE.getElementsByTagName("portlet");
        for (int i = 0; i < portletNL.getLength(); i += 1) {
            Element portletE = (Element)portletNL.item(i);
            String portletName = XML.getChildElementText(portletE, "portlet-name");
            String portletDefinitionId = contextName + "." + portletName;
            PortletDefinitionImpl portletDefinition = new PortletDefinitionImpl();
            portletDefinition.setId(portletDefinitionId);
            portletDefinition.setClassName(XML.getChildElementText(portletE, "portlet-class"));           
            portletDefinition.setName(portletName);
            portletDefinition.setDisplayNames(getDisplayNames(portletE));
            portletDefinition.setDescriptions(getDescriptions(portletE));
            portletDefinition.setLanguages(getLanguages(portletE));
            portletDefinition.setInitParameters(getInitParameters(portletE));
            portletDefinition.setPreferences(getPreferences(portletE));
            portletDefinition.setContentTypes(getContentTypes(portletE));
            portletDefinition.setServletDefinition(webApplicationDefinition.getServletDefinitionList().get(portletName));
            portletDefinition.setPortletApplicationDefinition(portletApplicationDefinition);
            portletDefinition.setExpirationCache(XML.getChildElementText(portletE, "expiration-cache"));
            
            portletDefinitions.add(portletDefinitionId, portletDefinition);
        }        
        return portletDefinitions;
    }

    private DisplayNameSet getDisplayNames(Element portletE) {
        DisplayNameSetImpl displayNames = new DisplayNameSetImpl();
        NodeList displayNameNL = portletE.getElementsByTagName("display-name");
        for (int i = 0; i < displayNameNL.getLength(); i += 1) {
            Element displayNameE = (Element)displayNameNL.item(i);
            String displayName = XML.getElementText(displayNameE);
            displayNames.add(displayName, Locale.getDefault());
        }
        return displayNames;
    }
    
    private DescriptionSet getDescriptions(Element portletE) {
        DescriptionSetImpl descriptions = new DescriptionSetImpl();
        NodeList descriptionNL = portletE.getElementsByTagName("description");
        for (int i = 0; i < descriptionNL.getLength(); i += 1) {
            Element descriptionE = (Element)descriptionNL.item(i);
            String description = XML.getElementText(descriptionE);
            descriptions.add(description, Locale.getDefault());
        }
        return descriptions;
    }
    
    private LanguageSet getLanguages(Element portletE) {
        NodeList portletInfoNL = portletE.getElementsByTagName("portlet-info");
        Element portletInfoE = (Element)portletInfoNL.item(0); // there should only be one
        String title = XML.getChildElementText(portletInfoE, "title");
        String shortTitle = XML.getChildElementText(portletInfoE, "short-title");
        String keywords = XML.getChildElementText(portletInfoE, "keywords");
        LanguageSetImpl languages = new LanguageSetImpl(title, shortTitle, keywords, contextName);
        languages.setClassLoader(Thread.currentThread().getContextClassLoader());
        NodeList supportedLocaleNL = portletE.getElementsByTagName("supported-locale");
        for (int i = 0; i < supportedLocaleNL.getLength(); i += 1) {
            Element supportedLocaleE = (Element)supportedLocaleNL.item(i);
            languages.addLanguage(LocaleManager.parseLocale(XML.getElementText(supportedLocaleE)));
        }
        return languages;
    }
    
    private ParameterSet getInitParameters(Element portletE) {
        ParameterSetImpl parameters = new ParameterSetImpl();
        NodeList initParamNL = portletE.getElementsByTagName("init-param");
        for (int i = 0; i < initParamNL.getLength(); i += 1) {
            Element initParamE = (Element)initParamNL.item(i);
            parameters.add(XML.getChildElementText(initParamE, "name"), XML.getChildElementText(initParamE, "value"));
        }
        return parameters;
    }
    
    private PreferenceSet getPreferences(Element portletE) {
        PreferenceSetImpl preferences = new PreferenceSetImpl();
        NodeList portletPreferencesNL = portletE.getElementsByTagName("portlet-preferences");
        if (portletPreferencesNL.getLength() > 0) {
            Element portletPreferencesE = (Element)portletPreferencesNL.item(0); // there should only be one
            NodeList preferenceNL = portletPreferencesE.getElementsByTagName("preference");
            for (int i = 0; i < preferenceNL.getLength(); i += 1) {
                Element preferenceE = (Element)preferenceNL.item(i);
                String name = XML.getChildElementText(preferenceE, "name");
                Collection values = new ArrayList(1); // There is usually just one value
                NodeList valueNL = preferenceE.getElementsByTagName("value");
                for (int j = 0; j < valueNL.getLength(); j += 1) {
                    Element valueE = (Element)valueNL.item(j);
                    values.add(XML.getElementText(valueE));
                }
                boolean readOnly = Boolean.valueOf(XML.getChildElementText(preferenceE, "read-only")).booleanValue();
                preferences.add(name, values, readOnly);
            }
            preferences.setPreferencesValidator(XML.getChildElementText(portletPreferencesE, "preferences-validator"));
        }
        return preferences;
    }
    
    private ContentTypeSet getContentTypes(Element portletE) {
        ContentTypeSetImpl contentTypes = new ContentTypeSetImpl();
        NodeList supportsNL = portletE.getElementsByTagName("supports");
        for (int i = 0; i < supportsNL.getLength(); i += 1) {
            Element supportsE = (Element)supportsNL.item(i);
            ContentTypeImpl contentType = new ContentTypeImpl();
            contentType.setContentType(XML.getChildElementText(supportsE, "mime-type"));
            NodeList portletModeNL = supportsE.getElementsByTagName("portlet-mode");
            for (int j = 0; j < portletModeNL.getLength(); j += 1) {
                Element portletModeE = (Element)portletModeNL.item(j);
                contentType.addPortletMode(new PortletMode(XML.getElementText(portletModeE)));
            }
            contentTypes.add(contentType);          
        }
        return contentTypes;
    }

    private UserAttributeListImpl getUserAttributes(Element portletAppE) {
        UserAttributeListImpl userAttributes = new UserAttributeListImpl();
        NodeList userAttributesNL = portletAppE.getElementsByTagName("user-attribute");
        for (int i = 0; i < userAttributesNL.getLength(); i +=1) {
            Element userAttributeE = (Element)userAttributesNL.item(i);
            UserAttributeImpl userAttribute = new UserAttributeImpl();
            userAttribute.setDescription(XML.getChildElementText(userAttributeE, "description"));
            userAttribute.setName(XML.getChildElementText(userAttributeE, "name"));
            userAttributes.add(userAttribute);
        }
        return userAttributes;
    }

}
