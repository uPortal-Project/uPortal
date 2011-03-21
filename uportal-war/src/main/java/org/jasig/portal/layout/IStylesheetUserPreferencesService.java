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

package org.jasig.portal.layout;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.om.IStylesheetUserPreferences;

/**
 * Provides access to stylesheet user preference data. This is how any runtime code that needs access to theme or structure
 * stylesheet user preferences. All of the details around default values, scoping and persistence are taken care of here.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IStylesheetUserPreferencesService {
    /**
     * The theme stylesheet user preferences for the current request
     */
    public IStylesheetUserPreferences getThemeStylesheetUserPreferences(HttpServletRequest request);

    /**
     * The structure stylesheet user preferences for the current request
     */
    public IStylesheetUserPreferences getStructureStylesheetUserPreferences(HttpServletRequest request);
    
    /**
     * Save any modifications made to the {@link IStylesheetUserPreferences} object. The {@link IStylesheetUserPreferences} instance
     * passed in MUST be from either {@link #getThemeStylesheetUserPreferences(HttpServletRequest)} or
     * {@link #getStructureStylesheetUserPreferences(HttpServletRequest)}
     */
    public void updateStylesheetUserPreferences(HttpServletRequest request, IStylesheetUserPreferences stylesheetUserPreferences);

}
