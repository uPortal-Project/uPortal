/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.Language;
import org.apache.pluto.om.common.LanguageSet;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LanguageSetImpl implements LanguageSet, Serializable {
    
    private String title = null;
    private String shortTitle = null; 
    private String keywords = null;
    private String resources = null;   
    private ClassLoader classLoader = null;
    private Map languages = null; // Locale --> Language
    private boolean resourceBundleInitialized = false;
    private boolean hasResourceBundle = false;
    private static final Log log = LogFactory.getLog(LanguageSetImpl.class);

    public LanguageSetImpl(String title, String shortTitle, String keywords, String resources) {
        languages = new HashMap();
        this.title = title;
        this.shortTitle = shortTitle;
        this.keywords = keywords;
        this.resources = resources;
        
        hasResourceBundle = resources != null;
    }

    // LanguageSet methods
    
    public Iterator iterator() {
        if (!resourceBundleInitialized) {
            initResourceBundle();
            resourceBundleInitialized = true;
        }
        return languages.values().iterator();
    }

    public Iterator getLocales() {
        return languages.keySet().iterator();
    }

    public Language get(Locale locale) {
        if (!resourceBundleInitialized) {
            initResourceBundle();
            resourceBundleInitialized = true;
        }
        return (Language)languages.get(locale);
    }

    public Locale getDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        return defaultLocale;
    }
    
    // Additional methods
    
    public void setResources(String resourceBundleBase) {
        this.resources = resourceBundleBase;    
    }
    
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public void addLocale(Locale locale) {
        // The null Language values will be replaced
        // during the initResourceBundle() method
        // since we don't have the right class loader at the
        // time this method is called
        languages.put(locale, null);
    }
    
    private void addLanguage(Locale locale) {
        ResourceBundle resourceBundle = null;
        if (hasResourceBundle) {
            resourceBundle = loadResourceBundle(locale);
        }
        Language language = createLanguage(locale, resourceBundle);
        languages.put(language.getLocale(), language);
    }
    
    // Create Language object with data from this class (title, short-title, description, keywords)
    private Language createLanguage(Locale locale, ResourceBundle bundle) {
        LanguageImpl lang = new LanguageImpl(locale, bundle, title, shortTitle, keywords);
        return (Language)lang;
    }      
    
    // Loads resource bundle files from WEB-INF/classes directory
    private ResourceBundle loadResourceBundle(Locale locale) {
        ResourceBundle resourceBundle = null;
        try {
            if (classLoader != null) {
                resourceBundle = ResourceBundle.getBundle(resources, locale, classLoader);
            } else {
                resourceBundle = ResourceBundle.getBundle(resources, locale, Thread.currentThread().getContextClassLoader());
            }
        } catch (MissingResourceException mre) {
            if (log.isErrorEnabled()) {
                log.error("Cannot obtain portlet resource bundle '" + resources + "'", mre);
            }
        }
        return resourceBundle;
    }
    
    private void initResourceBundle() {
        // Assume that by now, we have a proper webapp class loader
        addLocale(Locale.getDefault());
        for (Iterator iter = languages.keySet().iterator(); iter.hasNext();) {
            Locale locale = (Locale)iter.next();
            addLanguage(locale);
        }
    }
}
