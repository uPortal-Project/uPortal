/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.portlet;

import java.io.IOException;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.LanguageSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.portlet.ContentTypeSet;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionCtrl;
import org.apache.pluto.om.servlet.ServletDefinition;
import org.jasig.portal.container.om.common.LanguageSetImpl;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.common.PreferenceSetImpl;
import org.jasig.portal.container.om.common.SecurityRoleRefSetImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletDefinitionImpl implements PortletDefinition, PortletDefinitionCtrl {
    private ObjectID objectId = null;
    private String className = null;
    private String name = null;
    private DescriptionSet descriptions = null;
    private LanguageSet languages = null;
    private ParameterSet parameters = null;
    private SecurityRoleRefSet securityRoleRefs = null;
    private PreferenceSet preferences = null;
    private ContentTypeSet contentTypes = null;
    private PortletApplicationDefinition portletApplicationDefinition = null;
    private ServletDefinition servletDefinition = null;
    private DisplayNameSet displayNames = null;
    private String expirationCache = null;
    private ClassLoader portletClassLoader = null;
    
    public PortletDefinitionImpl() {
        this.securityRoleRefs = new SecurityRoleRefSetImpl();
    }
    
    // PortletDefinition methods
    
    public ObjectID getId() {
        return objectId;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    public LanguageSet getLanguageSet() {
        ((LanguageSetImpl)languages).setClassLoader(getPortletClassLoader());
        return languages;
    }

    public ParameterSet getInitParameterSet() {
        return parameters;
    }

    public SecurityRoleRefSet getInitSecurityRoleRefSet() {
        return securityRoleRefs;
    }

    public PreferenceSet getPreferenceSet() {
        ((PreferenceSetImpl)preferences).setClassLoader(this.getPortletClassLoader());
        return preferences;
    }

    public ContentTypeSet getContentTypeSet() {
        return contentTypes;
    }

    public PortletApplicationDefinition getPortletApplicationDefinition() {
        return portletApplicationDefinition;
    }

    public ServletDefinition getServletDefinition() {
        return servletDefinition;
    }

    public DisplayName getDisplayName(Locale locale) {
        return displayNames.get(locale);
    }

    public String getExpirationCache() {
        return expirationCache;
    }

    public ClassLoader getPortletClassLoader() {
        return portletClassLoader;
    }
    
    // PortletDefinitionCtrl methods
    
    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }

    public void setPortletClassLoader(ClassLoader classLoader) {
        this.portletClassLoader = classLoader;
    }    

    public void store() throws IOException {
    }
    
    // Additional methods
    
    public void setInitParameters(ParameterSet parameters) {
        this.parameters = parameters;
    }
    
    public void setExpirationCache(String expirationCache) {
        this.expirationCache = expirationCache;
    }

    public void setLanguages(LanguageSet languages) {
       this.languages = languages;
    }

    public void setInitSecurityRoleRefSet(SecurityRoleRefSet securityRoleRefs) {
        this.securityRoleRefs = securityRoleRefs;
    }

    public void setPreferences(PreferenceSet preferences) {
        this.preferences = preferences;
    }
    

    public void setContentTypes(ContentTypeSet contentTypes) {
        this.contentTypes = contentTypes;
    }

    public void setPortletApplicationDefinition(PortletApplicationDefinition definition) {
        this.portletApplicationDefinition = definition;
    }

    public void setServletDefinition(ServletDefinition definition) {
        this.servletDefinition = definition;
    }
    

}
