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

package org.jasig.portal.io.xml.profile;

import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;


/**
 * Utility class to simplify logic needed in CRN script when importing user profile data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ProfileImportHelper {
    private final IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;
    private final IStylesheetDescriptor structureStylesheetDescriptor;
    private final IStylesheetDescriptor themeStylesheetDescriptor;
    private final int personId;
    private final int profileId;
    
    private IStylesheetUserPreferences structureStylesheetUserPreferences;
    private IStylesheetUserPreferences themeStylesheetUserPreferences;

    public ProfileImportHelper(
            IStylesheetDescriptorDao stylesheetDescriptorDao, IStylesheetUserPreferencesDao stylesheetUserPreferencesDao, 
            long structureStylesheetId, long themeStylesheetId, 
            int personId, int profileId) {
        
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
        this.structureStylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptor(structureStylesheetId);
        this.themeStylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptor(themeStylesheetId);
        this.personId = personId;
        this.profileId = profileId;
    }
    
    public void addStructureParameter(String name, String value) {
        if (this.structureStylesheetUserPreferences == null) {
            this.structureStylesheetUserPreferences = this.getCreateStylesheetUserPreferences(this.structureStylesheetDescriptor);
        }
        
        this.structureStylesheetUserPreferences.setStylesheetParameter(name, value);
    }
    
    public void addThemeParameter(String name, String value) {
        if (this.themeStylesheetUserPreferences == null) {
            this.themeStylesheetUserPreferences = this.getCreateStylesheetUserPreferences(this.themeStylesheetDescriptor);
        }
        
        this.themeStylesheetUserPreferences.setStylesheetParameter(name, value);
    }
    
    public void save() {
        if (this.structureStylesheetUserPreferences != null) {
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(this.structureStylesheetUserPreferences);
        }

        if (this.themeStylesheetUserPreferences != null) {
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(this.themeStylesheetUserPreferences);
        }
    }
    
    protected IStylesheetUserPreferences getCreateStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor) {
        final IStylesheetUserPreferences stylesheetUserPreferences = 
                this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, personId, profileId);
        if (stylesheetUserPreferences != null) {
            return stylesheetUserPreferences;
        }
        
        return this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, personId, profileId);
    }
}
