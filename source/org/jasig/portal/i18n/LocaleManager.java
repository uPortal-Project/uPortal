/**
 * Copyright ‰© 2003 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CommonUtils;

/**
 * Locale Manager
 * @author Shoji Kajita <a href="mailto:">kajita@itc.nagoya-u.ac.jp</a>
 * @version $Revision$
 */
public class LocaleManager  {

    static boolean localeAware=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware");
    private Locale[] DEFAULT_LOCALES;
    private Locale   localeFromSessionParameter;
    private Locale[] localesFromBrowserSetting;
    private Locale   localeForAdmin;

    public LocaleManager() {

        if (localeAware) {

            String default_locales=PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleManager.portal_locales");

            if (default_locales == null) {

                LogService.log(LogService.ERROR, "LocaleManager.LocaleManager: default_locales = null. It must be specified in portal.properties. " + default_locales);

                localeAware = false;

            }  else {

                String [] strarr = CommonUtils.getSplitStringByCommas(default_locales, ",");

                DEFAULT_LOCALES = new Locale[strarr.length]; 

                LogService.log(LogService.DEBUG, "LocaleManager.LocaleManager: default_locales = " + default_locales);

                for (int i=0; i < strarr.length; i++) {
                    DEFAULT_LOCALES[i] = getLocaleForLanguage(strarr[i]);
                    LogService.log(LogService.DEBUG, "LocaleManager.LocaleManager: uPortal-wide default locale #" + i + " = " + DEFAULT_LOCALES[i]);
                }
            }
        }
    }
    
    public boolean localeAware() {
        return localeAware;
    }

    public LocaleManager (HttpServletRequest request) {

        // get uPortal-wide default locales from "properties/portal.properties"
        this();
        
        // set user preferred languages from accept-language http header
        setLocalesFromBrowserSetting(request);
        
        // set the current locale from `locale' session parameter
        setLocaleFromSessionParameter(request);
    
        if (localeAware) {
            LogService.log(LogService.DEBUG, "LocaleManager - yes, locale aware!");
        }  
    }

    public void  setLocaleFromSessionParameter(HttpServletRequest request) {

        String locale=request.getParameter("locale");
        
        if (locale != null) {
            // 
            localeFromSessionParameter = getLocaleForLanguage(locale);
        }  else {
            // the same locale 
            localeFromSessionParameter = getLocaleForLanguage((String)request.getSession().getAttribute("locale"));
        }
        
        LogService.log(LogService.DEBUG, "LocaleManager.LocaleManager: localeFromSessionParameter = " + localeFromSessionParameter);

    }

    public Locale getLocaleForAdmin() {
        if (localeForAdmin == null) {
            String admin_locale=PropertiesManager.getProperty("org.jasig.portal.i18n.LocaleManager.admin_locale");
            localeForAdmin = getLocaleForLanguage(admin_locale);
        }
        return localeForAdmin;
    }

    public Locale getLocaleFromSessionParameter() {
        return localeFromSessionParameter;
    }

    private Locale getLocaleForLanguage(String language) {
        Locale locale;
        int index;
        
        if (language == null) return null;
        
        if ((index = language.indexOf('-')) != -1 || (index = language.indexOf('_')) != -1) {
            locale = new Locale(language.substring(0, index), language.substring(index+1));
        }  else {
            // No _ or - means no country value 
            locale = defaultLocale(language);
        }
        
        LogService.log(LogService.DEBUG, "LocaleManager.getLocaleForLanguage: language = " + locale.getLanguage() + ", country = " + locale.getCountry());
        
        return locale;
    }

    private Locale defaultLocale(String language) {
    
        if (language.equals("ca")) return Locale.CANADA;
        if (language.equals("en")) return Locale.US;
        if (language.equals("de")) return Locale.GERMANY;
        if (language.equals("it")) return Locale.ITALY;
        if (language.equals("ja")) return Locale.JAPAN;
        if (language.equals("sv")) return new Locale("sv", "SE");
        if (language.equals("zh")) return Locale.CHINA;
        
        LogService.log(LogService.ERROR, "LocaleManager.defaultLocale: There is no default locale for " + language + ". Instead of it, en_US is used.");
        
        return new Locale("en", "US");
    
    }

    public boolean isLocaleAware() {
        return localeAware;
    }

    private int getLengthOfBrowserSetting() {
        if (localesFromBrowserSetting != null) {
            return localesFromBrowserSetting.length;
        }  else {
            return 0;
        }
    }

    private int getLengthOfSessionParameter() {
        if (localeFromSessionParameter != null) {
            return 1;
        }  else {
            return 0;
        }
    }

    private int getLengthOfDefaultLocales() {
        if (DEFAULT_LOCALES != null) {
            return DEFAULT_LOCALES.length;
        }  else {
            return 0;
        }
    }

    public Locale[] getLocales() {
    
        int i=0;
        
        if (localeAware == false)  return null;
        
        int totalLength = getLengthOfSessionParameter() + getLengthOfBrowserSetting() + getLengthOfDefaultLocales();
        
        Locale[] allLocales = new Locale[totalLength];
        
        // the highest priority is locale from session parameter `locale'
        if (localeFromSessionParameter != null) {
            allLocales[i++] = localeFromSessionParameter;
        }
        
        // the second priority is locales from browser setting
        if (localesFromBrowserSetting != null) {
            System.arraycopy(localesFromBrowserSetting, 0, allLocales, i, localesFromBrowserSetting.length);
        }
        
        // the third priority is uPortal-wide default locales 
        if (DEFAULT_LOCALES != null) {
            System.arraycopy(DEFAULT_LOCALES, 0, allLocales, getLengthOfBrowserSetting()+i, DEFAULT_LOCALES.length);
        }
        
        return allLocales;

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
                locales[i] = getLocaleForLanguage(lang);
                LogService.log(LogService.DEBUG, "LocaleManager.setLocalesFromBrowserSetting: localesFromBrowserSetting #" + i + " = " + locales[i]);
            }
            localesFromBrowserSetting = locales;
        }  else {
            localesFromBrowserSetting = null;
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
}
