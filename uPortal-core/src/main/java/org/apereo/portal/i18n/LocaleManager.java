/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.i18n;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Manages locales on behalf of a user. This class currently keeps track of locales at the following
 * levels:<br>
 *
 * <ol>
 *   <li>User's locale preferences (associated with a user ID)
 *   <li>Browser's locale preferences (from the Accept-Language request header)
 *   <li>Session's locale preferences (set via the portal request parameter uP_locales)
 *   <li>Portal's locale preferences (set in portal.properties)
 * </ol>
 *
 * Eventually, this class will also keep track of locale preferences at the following levels:<br>
 *
 * <ol>
 *   <li>Layout node's locale preferences
 *   <li>User profile's locale preferences
 * </ol>
 */
public class LocaleManager implements ILocaleManager, Serializable {

    private List<Locale> sessionLocales;
    private List<Locale> userLocales;
    private List<Locale> portalLocales;

    /**
     * Constructor that associates a locale manager with a user.
     *
     * @param person the user
     */
    /* package-private */ LocaleManager(List<Locale> userLocales, List<Locale> portalLocales) {
        this.userLocales = userLocales;
        this.portalLocales = portalLocales;
    }

    @Override
    public List<Locale> getUserLocales() {
        return userLocales;
    }

    @Override
    public void setUserLocales(List<Locale> userLocales) {
        this.userLocales = userLocales;
        this.sessionLocales = userLocales;
    }

    @Override
    public List<Locale> getSessionLocales() {
        return sessionLocales;
    }

    @Override
    public void setSessionLocales(List<Locale> sessionLocales) {
        this.sessionLocales = sessionLocales;
    }

    /**
     * Produces a sorted list of locales according to locale preferences obtained from several
     * places. The following priority is given: session, user, browser, portal, and jvm.
     *
     * @return the sorted list of locales
     */
    public List<Locale> getLocales() {
        // Need logic to construct ordered locale list.
        // Consider creating a separate ILocaleResolver
        // interface to do this work.
        final List<Locale> rslt = new ArrayList<>();
        // Add highest priority locales first
        addToLocaleList(rslt, sessionLocales);
        addToLocaleList(rslt, userLocales);
        // We will ignore browser locales until we know how to
        // translate them into proper java.util.Locales
        // addToLocaleList(locales, browserLocales);
        addToLocaleList(rslt, portalLocales);
        return rslt;
    }

    /** Add locales to the locale list if they aren't in there already */
    private void addToLocaleList(List<Locale> localeList, List<Locale> locales) {
        if (locales != null) {
            for (Locale locale : locales) {
                if (locale != null && !localeList.contains(locale)) {
                    localeList.add(locale);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
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
        sb.append("Portal locales: ");
        if (portalLocales != null) {
            sb.append(stringValueOf(portalLocales));
        }
        sb.append("\n");
        sb.append("Sorted locales: ");
        List<Locale> sortedLocales = getLocales();
        if (sortedLocales != null) {
            sb.append(stringValueOf(sortedLocales));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Constructs a comma-delimited list of locales that could be parsed back into a Locale array
     * with parseLocales(String localeStringList).
     *
     * @param locales the list of locales
     * @return a string representing the list of locales
     */
    private String stringValueOf(List<Locale> locales) {
        StringBuilder sb = new StringBuilder();
        for (Locale locale : locales) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(locale.toString());
        }
        return sb.toString();
    }
}
