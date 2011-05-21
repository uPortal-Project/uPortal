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

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.UserPreferencesManager;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
public class ThemeNameRequestPropertiesManager extends BaseRequestPropertiesManager {
    
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
    public Map<String, String[]> getRequestProperties(
            HttpServletRequest portletRequest, IPortletWindow portletWindow) {
        
        // get the current user profile
        IUserInstance ui = userInstanceManager.getUserInstance(portletRequest);
        UserPreferencesManager upm = (UserPreferencesManager) ui.getPreferencesManager();
        IUserProfile profile = upm.getUserProfile();
        
        // get the theme for this profile
        long themeId = profile.getThemeStylesheetId();
        IStylesheetDescriptor theme = stylesheetDao.getStylesheetDescriptor(themeId);

        // set the theme name as a portlet response property
        return Collections.singletonMap(THEME_NAME_PROPERTY, new String[]{ theme.getName() });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
