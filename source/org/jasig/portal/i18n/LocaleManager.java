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

import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manages locales on behalf of a user. 
 * This class currently keeps track of locales at the following levels:<br>
 * <ol>
 *   <li>User's locale preferences (associated with a user ID)</li>
 *   <li>Browser's locale preferences (from the Accept-Language request header)</li>
 *   <li>Session's locale preferences (set via the portal request parameter uP_locales)</li>
 *   <li>Portal's locale preferences (set in portal.properties)</li>
 * </ol>
 * Eventually, this class will also keep track of locale preferences at
 * the following levels:<br>
 * <ol>
 *   <li>Layout node's locale preferences</li>
 *   <li>User profile's locale preferences</li>
 * </ol>
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LocaleManager  {

    private static final Log log = LogFactory.getLog(LocaleManager.class);
    
    private IPerson person;
    private static boolean localeAware = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware");
    private static Locale jvmLocale;
    private static Locale[] portalLocales;
    private Locale[] sessionLocales;
    private Locale[] browserLocales;
    private Locale[] userLocales;

    /**
     * Constructor that associates a locale manager with a user.
     * @param person the user
     */
    public LocaleManager(IPerson person) {
        this.person = person;
        jvmLocale = Locale.getDefault();
		if (localeAware) {
            portalLocales = loadPortalLocales();
            try {
                userLocales = LocaleStoreFactory.getLocaleStoreImpl().getUserLocales(person);
            } catch (Exception e) {
                log.error( e);
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
    public static boolean isLocaleAware() { return localeAware; }
    public static Locale getJvmLocale() { return jvmLocale; }
    public static Locale[] getPortalLocales() { return portalLocales; }
    public Locale[] getBrowserLocales() { return browserLocales; }
    public Locale[] getUserLocales() { return userLocales; }
    public Locale[] getSessionLocales() { return sessionLocales; }

    // Setters
    public static void setJvmLocale(Locale jvmLocale) { LocaleManager.jvmLocale = jvmLocale; }
    public static void setPortalLocales(Locale[] portalLocales) { LocaleManager.portalLocales = portalLocales; }
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
        // We will ignore browser locales until we know how to
        // translate them into proper java.util.Locales
        //addToLocaleList(locales, browserLocales);
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
                if (locales[i] != null && !localeList.contains(locales[i]))
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
        
        // Sometimes people specify "en-US" instead of "en_US", so
        // we'll try to clean that up.
        localeString = CommonUtils.replaceText(localeString, "-", "_");
        
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
            // Uncomment the following line
            // when we can count on JDK 1.4!
            //locale = new Locale(language);
            locale = new Locale(language, "");
        }
        
        return locale;
    }
    
    /**
     * Constructs a comma-delimited list of locales
     * that could be parsed back into a Locale
     * array with parseLocales(String localeStringList).
     * @param locales the list of locales
     * @return a string representing the list of locales
     */
    public static String stringValueOf(Locale[] locales) {
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
    
    /**
     * Creates an XML representation of a list of locales.
     * @param locales the locale list
     * @return the locale list as XML
     */
    public static Document xmlValueOf(Locale[] locales) {
        return xmlValueOf(locales, null);
    }
    
    /**
     * Creates an XML representation of a list of locales.
     * If a selected locale is supplied, the XML element representing
     * the selected locale will have an attribute of selected with value
     * of true.  This is helpful when constructing user interfaces that
     * indicate which locale is selected.
     * @param locales the locale list
     * @param selectedLocale a locale that should be selected if it is in the list
     * @return the locale list as XML
     */
    public static Document xmlValueOf(Locale[] locales, Locale selectedLocale) {
        Document doc = DocumentFactory.getNewDocument();

        // <locales>
        Element localesE = doc.createElement("locales");
        for (int i = 0; i < locales.length; i++) {
          Element locE = doc.createElement("locale");
          locE.setAttribute("displayName", locales[i].getDisplayName(locales[0]));
          locE.setAttribute("code", locales[i].toString());

          // Mark which locale is the user's preference
          if (selectedLocale != null && selectedLocale.equals(locales[i])) {
              locE.setAttribute("selected", "true");
          }

          // <language iso2="..." iso3="..." displayName="..."/>
          Element languageE = doc.createElement("language");
          languageE.setAttribute("iso2", locales[i].getLanguage());
          try {
              languageE.setAttribute("iso3", locales[i].getISO3Language());
          } catch (Exception e) {
              // Do nothing
          }
          languageE.setAttribute("displayName", locales[i].getDisplayLanguage(locales[0]));
          locE.appendChild(languageE);

          // <country iso2="..." iso3="..." displayName="..."/>
          Element countryE = doc.createElement("country");
          countryE.setAttribute("iso2", locales[i].getCountry());
          try {
              countryE.setAttribute("iso3", locales[i].getISO3Country());
          } catch (Exception e) {
              // Do nothing
          }
          countryE.setAttribute("displayName", locales[i].getDisplayCountry(locales[0]));
          locE.appendChild(countryE);

          // <variant code="..." displayName="..."/>
          Element variantE = doc.createElement("variant");
          variantE.setAttribute("code", locales[i].getVariant());
          variantE.setAttribute("displayName", locales[i].getDisplayVariant(locales[0]));
          locE.appendChild(variantE);

          localesE.appendChild(locE);
        }
        doc.appendChild(localesE);
        return doc;  
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(1024);
        sb.append("LocaleManager's locales").append("\n");
        sb.append("-----------------------").append("\n");
        sb.append("Session locales: ");
        if (sessionLocales != null) {
            sb.append(stringValueOf(sessionLocales));
        }
        sb.append("\n");
        sb.append("User locales: ");
        if (userLocales != null) {
            sb.append(stringValueOf(userLocales));
        }
        sb.append("\n");
        sb.append("Browser locales: ");
        if (browserLocales != null) {
            sb.append(stringValueOf(browserLocales));
        }
        sb.append("\n");
        sb.append("Portal locales: ");
        if (portalLocales != null) {
            sb.append(stringValueOf(portalLocales));
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
            sb.append(stringValueOf(sortedLocales));
        }
        sb.append("\n");
        return sb.toString();
    }

}
