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

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.security.IPerson;
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
     * Constructor that associates a locale manager with a user.
     * @param person the user
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
     * Constructor that sets up locales according to
     * the <code>Accept-Language</code> request header
     * from a user's browser.
     * @param person the user
     * @param acceptLanguage the Accept-Language request header from a user's browser
     */
    public LocaleManager(IPerson person, String acceptLanguage) {
        this(person);
        this.browserLocales = parseLocales(acceptLanguage);
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
    
    /**
     * Read and parse portal_locales from portal.properties.
     * portal_locales will be in the form of a comma-separated 
     * list, e.g. en_US,ja_JP,sv_SE 
     */
    private Locale[] loadPortalLocales() {
        String portalLocalesString = PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleManager.portal_locales");
        return parseLocales(portalLocalesString);
    }    

    /**
     * This needs to be removed as soon as DbLoader is modified to
     * depend on a parameter rather than the LocaleManager.localeForAdmin
     * @return the admin locale
     */
    public Locale getLocaleForAdmin() {
        if (localeForAdmin == null) {
            String admin_locale = PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleManager.admin_locale");
            localeForAdmin = parseLocale(admin_locale);
        }
        return localeForAdmin;
    }

    /**
     * Produces a sorted list of locales according to locale preferences
     * obtained from several places.  The following priority is given:
     * session, user, browser, portal, and jvm.
     * @return the sorted list of locales
     */
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
    
    /**
     * Helper method to produce a <code>java.util.Locale</code> array from
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
    
    /**
     * Constructs a comma-delimited list of locales.
     * This string could be parsed back into a Locale
     * array with parseLocales(String localeStringList).
     * @param locales the list of locales
     * @return a string representing the list of locales
     */
    public String toString(Locale[] locales) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            sb.append(locale.toString());
            if (i < locales.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Stores the user locales persistantly.
     * @param userLocales the user locales preference
     * @throws Exception
     */
    public void persistUserLocales(Locale[] userLocales) throws Exception {
        setUserLocales(userLocales);
        LocaleStoreFactory.getLocaleStoreImpl().updateUserLocales(person, userLocales);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append("LocaleManager's locales").append("\n");
        sb.append("-----------------------").append("\n");
        sb.append("Session locales: ");
        if (sessionLocales != null) {
            sb.append(toString(sessionLocales));
        }
        sb.append("\n");
        sb.append("User locales: ");
        if (userLocales != null) {
            sb.append(toString(userLocales));
        }
        sb.append("\n");
        sb.append("Browser locales: ");
        if (browserLocales != null) {
            sb.append(toString(browserLocales));
        }
        sb.append("\n");
        sb.append("Portal locales: ");
        if (portalLocales != null) {
            sb.append(toString(portalLocales));
        }
        sb.append("\n");
        sb.append("JVM locale: ");
        if (jvmLocale != null) {
            sb.append(jvmLocale.toString());
        }
        sb.append("\n");
        sb.append("Sorted locales: ");
        Locale[] sortedLocales = getLocales();
        if (sortedLocales != null) {
            sb.append(toString(sortedLocales));
        }
        sb.append("\n");
        return sb.toString();
    }

}
