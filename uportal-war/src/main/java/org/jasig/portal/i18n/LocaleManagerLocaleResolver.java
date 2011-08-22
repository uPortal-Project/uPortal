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
package org.jasig.portal.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.LocaleResolver;

/**
 * {@link LocaleResolver} backed-up by {@link LocaleManager} for retrieving and {@link ILocaleStore}
 * for locale storing locales. This allows simplified portal locale resolution if 'localeResolver'
 * context attribute is instance of this class.
 * 
 * @see LocaleManagementInterceptor
 * @author Arvids Grabovskis
 * @version $Revision$
 */
public class LocaleManagerLocaleResolver implements LocaleResolver {
    
    private IUserInstanceManager userInstanceManager;
    
    private ILocaleStore localeStore;
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    @Autowired
    public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final LocaleManager localeManager = userInstance.getLocaleManager();
        
        Locale[] locales = localeManager.getLocales();
        if (locales != null && locales.length > 0) {
            return locales[0];
        }
        
        // if there was no LocaleManager was not able to determine the locale, return the locale
        // specified by "accept-locale" HTTP header
        return request.getLocale();
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final LocaleManager localeManager = userInstance.getLocaleManager();
        localeManager.setSessionLocales(new Locale[] { locale });
        
        // if the current user is logged in, also update the persisted user locale
        final IUserInstance ui = userInstanceManager.getUserInstance(request);
        final IPerson person = ui.getPerson();
        if (!person.isGuest()) {
            try {
                localeManager.persistUserLocales(new Locale[] { locale });
                localeStore.updateUserLocales(person, new Locale[] { locale });
                final IUserPreferencesManager upm = ui.getPreferencesManager();
                upm.getUserLayoutManager().loadUserLayout();
            } catch (Exception e) {
                throw new PortalException(e);
            }
        }
    }
}
