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
