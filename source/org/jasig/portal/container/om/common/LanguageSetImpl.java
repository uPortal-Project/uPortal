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

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
    private String resourceBundleBase = null;   
    private ClassLoader classLoader = null;
    private Map languages = null; // Locale --> Language

    public LanguageSetImpl(String title, String shortTitle, String keywords, String resourceBundleBase) {
        languages = new HashMap();
        this.title = title;
        this.shortTitle = shortTitle;
        this.keywords = keywords;
        this.resourceBundleBase = resourceBundleBase;
    }

    // LanguageSet methods
    
    public Iterator iterator() {
        return languages.values().iterator();
    }

    public Iterator getLocales() {
        return languages.keySet().iterator();
    }

    public Language get(Locale locale) {
        return (Language)languages.get(locale);
    }

    public Locale getDefaultLocale() {
        // Pluto portalImpl gets the first locale in the list
        // The languages/locales implementation in Pluto is messed up
        // so we'll need to revisit how LanguageSetImpl and LanguageImpl work.
        Locale defaultLocale = Locale.getDefault();
        ResourceBundle resourceBundle = loadResourceBundle(defaultLocale);
        Language defaultLanguage = createLanguage(defaultLocale, resourceBundle);
        languages.put(defaultLocale, defaultLanguage);
        return defaultLocale;
    }
    
    // Additional methods
    
    public void setResourceBundleBase(String resourceBundleBase) {
        this.resourceBundleBase = resourceBundleBase;    
    }
    
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    public void addLanguage(Locale locale) {
        ResourceBundle resourceBundle = loadResourceBundle(locale);
        Language language = createLanguage(locale, resourceBundle);
        languages.put(language.getLocale(), language);
    }
    
    // create Language object with data from this class (title, short-title, description, keywords)
    private Language createLanguage(Locale locale, ResourceBundle bundle) {
        LanguageImpl lang = new LanguageImpl(locale, bundle, title, shortTitle, keywords);
        return (Language)lang;
    }      
    
    // loads resource bundle files from WEB-INF/classes directory
    private ResourceBundle loadResourceBundle(Locale locale) {
        ResourceBundle resourceBundle = null;
        try {
            if (classLoader != null) {
                resourceBundle = ResourceBundle.getBundle(resourceBundleBase, locale, classLoader);
            } else {
                resourceBundle = ResourceBundle.getBundle(resourceBundleBase, locale, Thread.currentThread().getContextClassLoader());
            }
        } catch (MissingResourceException x) {
        }
        return resourceBundle;
    }
    

}
