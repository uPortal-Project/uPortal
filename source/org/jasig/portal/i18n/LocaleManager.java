/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.LogService;

/**
 * Manages locales on behalf of a user.
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LocaleManager  {

    private IPerson person;
    private static boolean localeAware = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware");
    private static Locale jvmLocale;
    private static Locale[] portalLocales;
    private Locale[] sessionLocales;
    private Locale[] browserLocales;
    private Locale localeForAdmin;
    private Locale[] userLocales;

    /**
     * Constructor.
     */
    public LocaleManager(IPerson person) {
        this.person = person;
        if (localeAware) {
            jvmLocale = Locale.getDefault();
            portalLocales = loadPortalLocales();
            try {
                userLocales = LocaleStoreFactory.getLocaleStoreImpl().getUserLocales(person);
            } catch (Exception e) {
                LogService.log(LogService.ERROR, e);
            }
        }
    }

    /**
     * Constructor which uses servlet request to determine locale.
     * @param request
     */
    public LocaleManager(HttpServletRequest request) {
        this((IPerson)request.getSession().getAttribute(IPersonManager.PERSON_SESSION_KEY));
        
        // Set user-preferred languages from accept-language http header
        setLocalesFromBrowserSetting(request);
        
        // set the current locale from locale session parameter
        setLocaleFromSessionParameter(request);
    
        if (isLocaleAware()) {
            LogService.log(LogService.INFO, "LocaleManager - yes, locale aware!");
        }  
    }
    
    // Getters
    public boolean isLocaleAware() { return localeAware; }
    public Locale getJvmLocale() { return jvmLocale; }
    public Locale[] getPortalLocales() { return portalLocales; }
    public Locale[] getBrowserLocales() { return browserLocales; }
    public Locale[] getUserLocales() { return userLocales; }
    public Locale[] getSessionLocales() { return sessionLocales; }

    // Setters
    public void setJvmLocale(Locale jvmLocale) { LocaleManager.jvmLocale = jvmLocale; }
    public void setPortalLocales(Locale[] portalLocales) { LocaleManager.portalLocales = portalLocales; }
    public void setBrowserLocales(Locale[] browserLocales) { this.browserLocales = browserLocales; }
    public void setUserLocales(Locale[] userLocales) { this.userLocales = userLocales; }
    public void setSessionLocales(Locale[] sessionLocales) { this.sessionLocales = sessionLocales; }

    public void setLocaleFromSessionParameter(HttpServletRequest request) {
        String locale = request.getParameter("locale");
        if (locale != null) {
            sessionLocales = parseLocales(locale);
        }  else {
            // the same locale 
            sessionLocales = parseLocales((String)request.getSession().getAttribute("locale"));
        }        
        LogService.log(LogService.DEBUG, "LocaleManager.LocaleManager: sessionLocales = " + sessionLocales);
    }
    
    /**
     * Read and parse portal_locales from portal.properties.
     * portal_locales will be in the form of a comma-separated 
     * list, e.g. en_US,ja_JP,sv_SE 
     */
    private Locale[] loadPortalLocales() {
        String portalLocalesString = PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleManager.portal_locales");
        return parseLocales(portalLocalesString);
    }    

    public Locale getLocaleForAdmin() {
        if (localeForAdmin == null) {
            String admin_locale = PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleManager.admin_locale");
            localeForAdmin = parseLocale(admin_locale);
        }
        return localeForAdmin;
    }

    public Locale[] getLocales() {
        // Need logic to construct ordered locale list.
        // Consider creating a separate ILocaleResolver 
        // interface to do this work.
        List locales = new ArrayList();
        // Add highest priority locales first
        addToLocaleList(locales, sessionLocales);
        addToLocaleList(locales, userLocales);
        addToLocaleList(locales, browserLocales);
        addToLocaleList(locales, portalLocales);
        addToLocaleList(locales, new Locale[] { jvmLocale });
        return (Locale[])locales.toArray(new Locale[0]);
    }
    
        
    /**
     * Add locales to the locale list if they aren't in there already
     */
    private void addToLocaleList(List localeList, Locale[] locales) {
        if (locales != null) {
            for (int i = 0; i < locales.length; i++) {
                if (!localeList.contains(locales[i]))
                  localeList.add(locales[i]);
            }
        }
    }
    
    public Locale getLocale() {
        Locale[] locales=getLocales();         
        if (locales == null)  return null;        
        LogService.log(LogService.DEBUG, "LocaleManager.getLocale() - the current locale is " + locales[0]);       
        return locales[0];
    }
    
    public String getLocale(String loc) {
        Locale[] locales=getLocales(); 
        if (locales == null)  return null;
        LogService.log(LogService.DEBUG, "LocaleManager.getLocale() - the current locale is " + locales[0]);
        return locales[0].toString();    
    }
    
    public Locale[] getLocales(int chanId) {
        return getLocales();
    }

    public Locale getLocale(int chanId, String requestedLocale) {
        return getLocale();  // for the time being by Shoji
    }

    public void setLocalesFromBrowserSetting(HttpServletRequest request) {
    
    String language = request.getHeader("Accept-Language");
    
        if (language != null) {
            StringTokenizer  tokens = new StringTokenizer(language, ",");
            Locale[] locales = new Locale[tokens.countTokens()];
            int index;
    
            LogService.log(LogService.DEBUG, "LocaleManager.setLocalesFromBrowserSetting: Accept-Language = " + language);
    
            for (int i=0; tokens.hasMoreTokens(); i++) {
                String lang = tokens.nextToken();
                if ((index = lang.indexOf(';')) != -1) {
                    lang = lang.substring(0, index);
                }
                lang = lang.trim();
                locales[i] = parseLocale(lang);
                LogService.log(LogService.DEBUG, "LocaleManager.setLocalesFromBrowserSetting: localesFromBrowserSetting #" + i + " = " + locales[i]);
            }
            browserLocales = locales;
        }  else {
            browserLocales = null;
        }
    }

    public boolean isLocaleChanged(HttpServletRequest req) {
    
        Locale previous_locale=getLocale();
        
        setLocaleFromSessionParameter(req);
        setLocalesFromBrowserSetting(req);
        
        Locale current_locale=getLocale();
        
        LogService.log(LogService.DEBUG, "LocaleManager.isLocaleChanged: previous=" + previous_locale + ", current_locale=" + current_locale);
        if (previous_locale != current_locale) {
            return false;
        }
        return false;
    }
    
    /**
     * Helper method to produce a java.util.Locale array from
     * a comma-delimited locale string list, e.g. "en_US,ja_JP"
     * @param localeStringList the locales to parse
     * @return an array of locales representing the locale string list
     */
    public static Locale[] parseLocales(String localeStringList) {
        Locale[] locales = null;
        if (localeStringList != null) {
            StringTokenizer st = new StringTokenizer(localeStringList, ",");
            locales = new Locale[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                String localeString = st.nextToken().trim();
                locales[i] = parseLocale(localeString);
            }
        }
        return locales;
    }    
    
    /**
     * Helper method to produce a <code>java.util.Locale</code> object from
     * a locale string such as en_US or ja_JP.
     * @param localeString a locale string such as en_US
     * @return a java.util.Locale object representing the locale string
     */
    public static Locale parseLocale(String localeString) {
        String language = null;
        String country = null;
        String variant = null;
        
        StringTokenizer st = new StringTokenizer(localeString, "_");

        if (st.hasMoreTokens()) {
            language = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            country = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            variant = st.nextToken();
        }
        
        Locale locale = null;
        
        if (variant != null) {
            locale = new Locale(language, country, variant);
        } else if (country != null) {
            locale = new Locale(language, country);
        } else if (language != null) {
            locale = new Locale(language);
        }
        
        return locale;
    }
    
    public void updateUserLocales(Locale[] userLocales) throws Exception {
        setUserLocales(userLocales);
        LocaleStoreFactory.getLocaleStoreImpl().updateUserLocales(person, userLocales);
    }
}
