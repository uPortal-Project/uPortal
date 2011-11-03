/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.translator;

import java.io.Serializable;

/**
 * This domain class represents the original and localized translation of portlet definiton.
 * 
 * @author Arvids Grabovskis
 * @version $Revision$
 */
class PortletDefinitionTranslation implements Serializable {
    
    private static final long serialVersionUID = -7503311752482005801L;
    
    private String id;
    
    private String locale;
    
    private LocalizedPortletDefinition original;
    
    private LocalizedPortletDefinition localized;
    
    /**
     * Get the portlet definition id.
     * 
     * @return the portlet definition id.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set the portle definition id.
     * 
     * @param id portlet definition id.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Locale of translation.
     * 
     * @return translation locale.
     */
    public String getLocale() {
        return locale;
    }
    
    /**
     * Set the locale of translated portlet definition.
     * 
     * @param locale locale of translated portlet definition.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Get the default translation of portlet definition.
     * 
     * @return default portlet definition translation.
     */
    public LocalizedPortletDefinition getOriginal() {
        return original;
    }
    
    /**
     * Set the default translation of portlet definition.
     * 
     * @param original default translation of portlet definition.
     */
    public void setOriginal(LocalizedPortletDefinition original) {
        this.original = original;
    }
    
    /**
     * Set the portlet definition translated into locale specified by {@link #locale}.
     * 
     * @param localized translated portlet definition.
     */
    public void setLocalized(LocalizedPortletDefinition localized) {
        this.localized = localized;
    }
    
    /**
     * Get the translated portelt definition.
     * 
     * @return translated portlet definition.
     */
    public LocalizedPortletDefinition getLocalized() {
        return localized;
    }
}
