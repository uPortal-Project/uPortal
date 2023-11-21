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
package org.apereo.portal.portlets.localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.PortalException;
import org.apereo.portal.i18n.ILocaleStore;
import org.apereo.portal.i18n.LocaleManager;
import org.apereo.portal.i18n.LocaleManagerFactory;
import org.apereo.portal.layout.dlm.Constants;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** UserLocaleHelper contains helper methods for the user locales webflow. */
@Service
public class UserLocaleHelper {

    private IUserInstanceManager userInstanceManager;
    private IPortalRequestUtils portalRequestUtils;
    private ILocaleStore localeStore;
    private LocaleManagerFactory localeManagerFactory;

    @Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }

    /**
     * Set the UserInstanceManager
     *
     * @param userInstanceManager The {@link IUserInstanceManager}
     */
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setLocaleManagerFactory(LocaleManagerFactory localeManagerFactory) {
        this.localeManagerFactory = localeManagerFactory;
    }

    /**
     * Return a list of LocaleBeans matching the currently available locales for the portal.
     *
     * @param currentLocale
     * @return
     */
    public List<LocaleBean> getLocales(Locale currentLocale) {
        List<LocaleBean> locales = new ArrayList<>();

        // get the array of locales available from the portal
        List<Locale> portalLocales = localeManagerFactory.getPortalLocales();
        for (Locale locale : portalLocales) {
            if (currentLocale != null) {
                // if a current locale is available, display language names
                // using the current locale
                locales.add(new LocaleBean(locale, currentLocale));
            } else {
                locales.add(new LocaleBean(locale));
            }
        }
        return locales;
    }

    /**
     * Return the current user's locale.
     *
     * @param request The current {@link PortletRequest}
     * @return
     */
    public Locale getCurrentUserLocale(PortletRequest request) {
        final HttpServletRequest originalPortalRequest =
                this.portalRequestUtils.getPortletHttpRequest(request);
        IUserInstance ui = userInstanceManager.getUserInstance(originalPortalRequest);
        IUserPreferencesManager upm = ui.getPreferencesManager();
        final IUserProfile userProfile = upm.getUserProfile();
        LocaleManager localeManager = userProfile.getLocaleManager();

        // first check the session locales
        List<Locale> sessionLocales = localeManager.getSessionLocales();
        if (sessionLocales != null && sessionLocales.size() > 0) {
            return sessionLocales.get(0);
        }

        // if no session locales were found, check the user locales
        List<Locale> userLocales = localeManager.getUserLocales();
        if (userLocales != null && userLocales.size() > 0) {
            return userLocales.get(0);
        }

        // if no selected locale was found either in the session or user layout,
        // just return null
        return null;
    }

    /**
     * Update the current user's locale to match the selected locale. This implementation will
     * update the session locale, and if the user is not a guest, will also update the locale in the
     * user's persisted preferences.
     *
     * @param request
     * @param localeString
     */
    public void updateUserLocale(HttpServletRequest request, String localeString) {

        IUserInstance ui = userInstanceManager.getUserInstance(request);
        IUserPreferencesManager upm = ui.getPreferencesManager();
        final IUserProfile userProfile = upm.getUserProfile();
        LocaleManager localeManager = userProfile.getLocaleManager();

        if (localeString != null) {

            // build a new List<Locale> from the specified locale
            Locale userLocale = localeManagerFactory.parseLocale(localeString);
            List<Locale> locales = Collections.singletonList(userLocale);

            // set this locale in the session
            localeManager.setSessionLocales(locales);

            // if the current user is logged in, also update the persisted
            // user locale
            final IPerson person = ui.getPerson();
            if (!person.isGuest()) {
                try {
                    localeManager.setUserLocales(Collections.singletonList(userLocale));
                    localeStore.updateUserLocales(person, new Locale[] {userLocale});

                    // remove person layout fragment from session since it contains some of the data
                    // in previous
                    // translation and won't be cleared until next logout-login (applies when using
                    // RDBMDistributedLayoutStore as user layout store).
                    person.setAttribute(Constants.PLF, null);
                    upm.getUserLayoutManager().loadUserLayout(true);
                } catch (Exception e) {
                    throw new PortalException(e);
                }
            }
        }
    }
}
