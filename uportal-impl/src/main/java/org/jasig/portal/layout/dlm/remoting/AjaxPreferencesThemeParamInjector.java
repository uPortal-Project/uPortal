package org.jasig.portal.layout.dlm.remoting;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.layout.dlm.processing.IParameterProcessor;
import org.jasig.portal.security.IPerson;

/**
 * Turns on AJAX preferences functionality
 * 
 * @author jennifer.bourey@yale.edu
 * @version $Revision$ $Date$
 */
public class AjaxPreferencesThemeParamInjector implements IParameterProcessor {

	Log log = LogFactory.getLog(getClass());
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
		themePrefs.putParameterValue("USE_AJAX", "true");

    }

}
