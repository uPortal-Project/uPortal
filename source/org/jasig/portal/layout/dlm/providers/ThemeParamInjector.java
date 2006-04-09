/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.layout.dlm.processing.IParameterProcessor;
import org.jasig.portal.security.IPerson;

/**
 * A class used to inject the set of parameters used by the default 
 * theme transformation stylesheet. This is an implementation of the 
 * org.jasig.portal.layout.dlm.processing.IParameterProcessor interface.
 * If installations need more parameters added to the list than are used in this 
 * processor they are encouraged to implement their own processor and replace
 * this processor as one of the configured, fixed processors in 
 * properties/dlmContext.xml.
 *  
 * @author mark.boyd@sungardhe.com
 */
public class ThemeParamInjector implements IParameterProcessor
{
    private IPerson person = null;
    
    /**
     * Captures the passed-in IPerson object for use when processing parameters.
     * 
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#setResources(org.jasig.portal.security.IPerson, org.jasig.portal.layout.dlm.DistributedLayoutManager)
     */
    public void setResources(IPerson person, DistributedLayoutManager dlm)
    {
        this.person = person;
    }

    /**
     * Injects into the theme stylesheet preferences the "userName" stylesheet
     * parameter containing the value obtained from IPerson.getFullName().
     *  
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#processParameters(org.jasig.portal.UserPreferences, javax.servlet.http.HttpServletRequest)
     */
    public void processParameters(UserPreferences prefs, HttpServletRequest request)
    {
        ThemeStylesheetUserPreferences themePrefs = prefs
                .getThemeStylesheetUserPreferences();

        String userName = person.getFullName();
        if (userName != null && userName.trim().length() > 0)
            themePrefs.putParameterValue("userName", userName);
    }

}
