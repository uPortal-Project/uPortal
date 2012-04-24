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

package org.jasig.portal.portlet.container.properties;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Populator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThemeNameRequestPropertiesManager extends BaseRequestPropertiesManager {
    
    /**
     * @deprecated Use {@link IPortletRenderer#THEME_NAME_PROPERTY}
     */
    @Deprecated
    public static final String THEME_NAME_PROPERTY = "themeName";

    private IUserInstanceManager userInstanceManager;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    private IStylesheetDescriptorDao stylesheetDao;
    
    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDao) {
        this.stylesheetDao = stylesheetDao;
    }
    
    @Override
    public <P extends Populator<String, String>> void populateRequestProperties(HttpServletRequest portletRequest,
            IPortletWindow portletWindow, P propertiesPopulator) {
        
        // get the current user profile
        IUserInstance ui = userInstanceManager.getUserInstance(portletRequest);
        IUserPreferencesManager upm = ui.getPreferencesManager();
        IUserProfile profile = upm.getUserProfile();
        
        // get the theme for this profile
        long themeId = profile.getThemeStylesheetId();
        IStylesheetDescriptor theme = stylesheetDao.getStylesheetDescriptor(themeId);

        // set the theme name as a portlet response property
        final String themeName = theme.getName();
        propertiesPopulator.put(IPortletRenderer.THEME_NAME_PROPERTY, themeName);
        propertiesPopulator.put(THEME_NAME_PROPERTY, themeName);
    }

}
