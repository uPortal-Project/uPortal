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

package org.jasig.portal.container.om.common.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.pluto.om.common.Language;
import org.apache.pluto.util.Enumerator;
import org.jasig.portal.utils.CommonUtils;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LanguageImpl implements Language, Serializable {

    private Locale locale = null;
    private String title = null;
    private String shortTitle = null;
    private Collection keywords = null; // contains Strings
    private ResourceBundle resourceBundle = null;

    /*
    public LanguageImpl() {
        this.keywords = new ArrayList();
    }
    
    public LanguageImpl(Locale locale, String title) {
        this();
        this.locale = locale;
        this.title = title;
    }
    */
    
    public LanguageImpl(Locale locale, ResourceBundle bundle, String defaultTitle, String defaultShortTitle, String defaultKeyWords) {
        this.resourceBundle = new ResourceBundleImpl(bundle, new DefaultsResourceBundle(defaultTitle, defaultShortTitle, defaultKeyWords));
        this.locale = locale;
        title       = this.resourceBundle.getString("javax.portlet.title");
        shortTitle  = this.resourceBundle.getString("javax.portlet.short-title");
        keywords    = toList(this.resourceBundle.getString("javax.portlet.keywords"));
    }    
    
    // Language methods

    public Locale getLocale() {
        return locale;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public Iterator getKeywords() {
        return keywords.iterator();
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    // Additional methods
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }
    
    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }
    
    public void addKeywords(Collection keywords) {
        this.keywords.addAll(keywords);
    }
    
    public void addKeywords(String keywordsString) {
        StringTokenizer st = new StringTokenizer(keywordsString, ",");
        while (st.hasMoreTokens()) {
            keywords.add(st.nextToken());
        }
    }
    
    public void setKeywords(Collection keywords) {
        this.keywords = keywords;
    }
 
    public void setKeywords(String keywordsString) {
        keywords.clear();
        StringTokenizer st = new StringTokenizer(keywordsString, ",");
        while (st.hasMoreTokens()) {
            keywords.add(st.nextToken());
        }
    }
    
    private List toList(String value) {
        List keywords = new ArrayList();
        for (StringTokenizer st = new StringTokenizer(value, ","); st.hasMoreTokens();) {
            keywords.add(st.nextToken().trim());
        }
        return keywords;
    }
    
    private static class DefaultsResourceBundle extends ListResourceBundle {
        private Object[][] resources;

        public DefaultsResourceBundle(String defaultTitle, String defaultShortTitle, String defaultKeyWords) {
            resources = new Object[][] { { 
                    "javax.portlet.title", defaultTitle }, {
                    "javax.portlet.short-title", defaultShortTitle }, {
                    "javax.portlet.keywords", defaultKeyWords }
            };
        }

        protected Object[][] getContents() {
            return resources;
        }
    }

    private static class ResourceBundleImpl extends ResourceBundle {
        private Map data;

        public ResourceBundleImpl(ResourceBundle bundle, ResourceBundle defaults) {
            data = new HashMap();
            importData(defaults);
            importData(bundle);
        }

        private void importData(ResourceBundle bundle) {
            if (bundle != null) {
                for (Enumeration enum = bundle.getKeys(); enum.hasMoreElements();) {
                    String key = (String)enum.nextElement();
                    Object value = bundle.getObject(key);
                    data.put(key, value);
                }
            }
        }

        protected Object handleGetObject(String key) {
            return data.get(key);
        }

        public Enumeration getKeys() {
            return new Enumerator(data.keySet());
        }
    }


}
