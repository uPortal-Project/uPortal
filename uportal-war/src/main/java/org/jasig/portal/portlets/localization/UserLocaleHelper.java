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

package org.jasig.portal.portlets.localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.i18n.ILocaleStore;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.dlm.Constants;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UserLocaleHelper contains helper methods for the user locales webflow.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
@Service
public class UserLocaleHelper {

	private IUserInstanceManager userInstanceManager;
	private IPortalRequestUtils portalRequestUtils;
	private ILocaleStore localeStore;
	
	@Autowired
	public void setLocaleStore(ILocaleStore localeStore) {
        this.localeStore = localeStore;
    }

    /**
	 * Set the UserInstanceManager
	 * 
	 * @param userInstanceManager
	 */
	@Autowired
	public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
		this.userInstanceManager = userInstanceManager;
	}
	
	@Autowired
	public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }



    /**
	 * Return a list of LocaleBeans matching the currently available locales
	 * for the portal.
	 * 
	 * @param currentLocale
	 * @return
	 */
	public List<LocaleBean> getLocales(Locale currentLocale) {
		List<LocaleBean> locales = new ArrayList<LocaleBean>();
		
		// get the array of locales available from the portal
		Locale[] portalLocales = getPortalLocales();
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
	 * @param request
	 * @return
	 */
	public Locale getCurrentUserLocale(PortletRequest request) {
	    final HttpServletRequest originalPortalRequest = this.portalRequestUtils.getPortletHttpRequest(request);
		IUserInstance ui = userInstanceManager.getUserInstance(originalPortalRequest);
		IUserPreferencesManager upm = ui.getPreferencesManager();
		final IUserProfile userProfile = upm.getUserProfile();
        LocaleManager localeManager = userProfile.getLocaleManager();
        
        // first check the session locales
        Locale[] sessionLocales = localeManager.getSessionLocales();
        if (sessionLocales != null && sessionLocales.length > 0) {
        	return sessionLocales[0];
        }
        
        // if no session locales were found, check the user locales
        Locale[] userLocales = localeManager.getUserLocales();
        if (userLocales != null && userLocales.length > 0) {
            return userLocales[0];
        }
        
        // if no selected locale was found either in the session or user layout,
        // just return null
        return null;
        
	}
	
	/**
	 * Update the current user's locale to match the selected locale.  This 
	 * implementation will update the session locale, and if the user is not
	 * a guest, will also update the locale in the user's persisted preferences.
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
        	
        	// build a new Locale[] array from the specified locale
            Locale userLocale = parseLocale(localeString);
            Locale[] locales = new Locale[] { userLocale };
            
            // set this locale in the session
            localeManager.setSessionLocales(locales);
            
            // if the current user is logged in, also update the persisted
            // user locale
            final IPerson person = ui.getPerson();
            if (!person.isGuest()) {
                try {
                    localeManager.persistUserLocales(new Locale[] { userLocale });
                    localeStore.updateUserLocales(person, new Locale[] { userLocale });
                    
                    // remove person layout framgent from session since it contains some of the data in previous
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
	
	
	/*
	 * Convenience methods to enhance testability by wrapping static methods
	 */
	
	/**
	 * Get the available portal locales.
	 * 
	 * @return
	 */
	protected Locale[] getPortalLocales() {
		return LocaleManager.getPortalLocales();
	}
	
	/**
	 * Parse a string representation of a locale and return the matching Locale.
	 * 
	 * @param localeString
	 * @return
	 */
	protected Locale parseLocale(String localeString) {
	    return LocaleManager.parseLocale(localeString);		
	}
	
}
