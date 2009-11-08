package org.jasig.portal.portlets.localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;

/**
 * UserLocaleHelper contains helper methods for the user locales webflow.
 * 
 * @author Jen Bourey
 * @version $Revision$
 */
public class UserLocaleHelper {

	private IUserInstanceManager userInstanceManager;
	
	/**
	 * Set the UserInstanceManager
	 * 
	 * @param userInstanceManager
	 */
	public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
		this.userInstanceManager = userInstanceManager;
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
				locales.add(new LocaleBean(locale.toString(), 
						locale.getCountry().toLowerCase(), 
						locale.getDisplayLanguage(currentLocale)));
			} else {
				locales.add(new LocaleBean(locale.toString(), 
						locale.getCountry().toLowerCase(), 
						locale.getDisplayLanguage()));
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
	public Locale getCurrentUserLocale(HttpServletRequest request) {

		IUserInstance ui = userInstanceManager.getUserInstance(request);
		IUserPreferencesManager upm = ui.getPreferencesManager();
        LocaleManager localeManager = upm.getUserPreferences().getProfile().getLocaleManager();
        
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
        LocaleManager localeManager = upm.getUserPreferences().getProfile().getLocaleManager();

        if (localeString != null) {
        	
        	// build a new Locale[] array from the specified locale
            Locale userLocale = parseLocale(localeString);
            Locale[] locales = new Locale[] { userLocale };
            
            // set this locale in the session
            localeManager.setSessionLocales(locales);
            
            // if the current user is logged in, also update the persisted
            // user locale
            if (!ui.getPerson().isGuest()) {
                try {
                    localeManager.persistUserLocales(new Locale[] { userLocale });
                    upm.getUserLayoutManager().loadUserLayout();
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
