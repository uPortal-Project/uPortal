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

package org.jasig.portal.rendering.xslt;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;

/**
 * Returns theme {@link IStylesheetUserPreferences}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @see IStylesheetUserPreferencesService#getThemeStylesheetUserPreferences(HttpServletRequest)
 */
public class ThemeStylesheetUserPreferencesTransformerConfigurationSource extends StylesheetUserPreferencesTransformerConfigurationSource {
    
    @Override
    protected PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request) {
        return PreferencesScope.THEME;
    }

}
