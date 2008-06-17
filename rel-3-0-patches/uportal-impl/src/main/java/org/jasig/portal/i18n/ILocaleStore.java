/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.i18n;

import java.util.Locale;

import org.jasig.portal.security.IPerson;

/**
 * Interface defining how the portal reads and 
 * writes locale preferences.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public interface ILocaleStore {

    /**
     * Retrieves the locale preferences for a particular user.
     * @param person the user
     * @return the user's locale preferences
     * @throws Exception
     */
    public Locale[] getUserLocales(IPerson person) throws Exception;

    /**
     * Persists the locale preferences for a particular user.
     * @param person the user
     * @param locales the user's new locale preferences
     * @throws Exception
     */
    public void updateUserLocales(IPerson person, Locale[] locales) throws Exception;
    
}
