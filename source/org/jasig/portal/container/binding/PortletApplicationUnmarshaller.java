/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletMode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.jasig.portal.container.om.common.SecurityRoleRefImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;
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
    private Log log = LogFactory.getLog( getClass( ) );

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

            // If the <expiration-cache> element isn't present in portlet.xml, then we shouldn't
            // set it as a property of the PortletDefinition.  <expiration-cache> is an optional element
            // of the portlet.xml deployment descriptor.
            //
            // N.B. o.j.p.utils.XML getChildElementText(Element, String) returns an
            // empty string if the <expiration-cache> element doesn't exist.
            String expirationCache = XML.getChildElementText( portletE, "expiration-cache" );
            if ( expirationCache != null && !expirationCache.equals( "" ) ) {
	            try {
	                Integer.parseInt( expirationCache );
	                portletDefinition.setExpirationCache( expirationCache );
	            } catch ( NumberFormatException nfe )  {
	                log.error( "The specified <expiration-cache> value" + expirationCache + " is not a number.", nfe );
	                log.error( "Please check the value of the <expiration-cache> element of the " + portletDefinitionId + " portlet in portlet.xml." );
	                throw nfe;
	            }
            }

            portletDefinition.setInitSecurityRoleRefSet(getSecurityRoleRefs(portletE));

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
        // Check for a resource bundle element
        String resources = null;
        NodeList resourceBundleNL = portletE.getElementsByTagName("resource-bundle");
        if (resourceBundleNL.getLength() > 0)  {
            Element resourceBundleE = (Element)resourceBundleNL.item(0); // there should only be one
            String resourceBundle = XML.getElementText(resourceBundleE);
            if (resourceBundle.trim().length() > 0) {
                resources = resourceBundle;
            }
        }

        // The portlet-info element is optional if the resource-bundle element is specified
        String title = null;
        String shortTitle = null;
        String keywords = null;
        NodeList portletInfoNL = portletE.getElementsByTagName("portlet-info");
        if (portletInfoNL.getLength() > 0) {
            Element portletInfoE = (Element)portletInfoNL.item(0); // there should only be one
            title = XML.getChildElementText(portletInfoE, "title");
            shortTitle = XML.getChildElementText(portletInfoE, "short-title");
            keywords = XML.getChildElementText(portletInfoE, "keywords");
        }

        LanguageSetImpl languages = new LanguageSetImpl(title, shortTitle, keywords, resources);
        languages.setClassLoader(Thread.currentThread().getContextClassLoader());
        NodeList supportedLocaleNL = portletE.getElementsByTagName("supported-locale");
        for (int i = 0; i < supportedLocaleNL.getLength(); i += 1) {
            Element supportedLocaleE = (Element)supportedLocaleNL.item(i);
            languages.addLocale(LocaleManager.parseLocale(XML.getElementText(supportedLocaleE)));
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
                List values = new ArrayList(1); // There is usually just one value
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

    private SecurityRoleRefSetImpl getSecurityRoleRefs(Element portletE) {
        SecurityRoleRefSetImpl securityRoleRefs = new SecurityRoleRefSetImpl();
        NodeList securityRoleRefsNL = portletE.getElementsByTagName("security-role-ref");
        for (int i = 0; i < securityRoleRefsNL.getLength(); i += 1) {
            Element securityRoleRefE = (Element)securityRoleRefsNL.item(i);
            SecurityRoleRefImpl securityRoleRef = new SecurityRoleRefImpl();
            securityRoleRef.setDescription(XML.getChildElementText(securityRoleRefE, "description"));
            securityRoleRef.setRoleName(XML.getChildElementText(securityRoleRefE, "role-name"));
            securityRoleRef.setRoleLink(XML.getChildElementText(securityRoleRefE, "role-link"));
            securityRoleRefs.add(securityRoleRef);
        }
        return securityRoleRefs;
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
