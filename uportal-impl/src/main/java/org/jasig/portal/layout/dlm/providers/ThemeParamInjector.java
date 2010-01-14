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
 * properties/context/layoutContext.xml.
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

        final String fullName = person.getFullName();
        
        if (fullName != null && fullName.trim().length() > 0) {
            themePrefs.putParameterValue("userName", fullName);
        }
        
        final String userName = (String)person.getAttribute(IPerson.USERNAME);
        themePrefs.putParameterValue("USER_ID", userName);
    }

}
