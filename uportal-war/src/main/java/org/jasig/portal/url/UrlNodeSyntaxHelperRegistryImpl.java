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

package org.jasig.portal.url;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simple autowired registry of {@link IUrlNodeSyntaxHelper} beans.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class UrlNodeSyntaxHelperRegistryImpl implements IUrlNodeSyntaxHelperRegistry {
    private IUserInstanceManager userInstanceManager;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    private Map<String, IUrlNodeSyntaxHelper> urlNodeSyntaxHelpers = Collections.emptyMap();
   
    @Autowired
    public void setUrlNodeSyntaxHelpers(Collection<IUrlNodeSyntaxHelper> urlNodeSyntaxHelpers) {
        final Map<String, IUrlNodeSyntaxHelper> urlNodeSyntaxHelperBuilder = new LinkedHashMap<String, IUrlNodeSyntaxHelper>();
        
        for (final IUrlNodeSyntaxHelper urlNodeSyntaxHelper : urlNodeSyntaxHelpers) {
            urlNodeSyntaxHelperBuilder.put(urlNodeSyntaxHelper.getName(), urlNodeSyntaxHelper);
        }
        
        this.urlNodeSyntaxHelpers = Collections.unmodifiableMap(urlNodeSyntaxHelperBuilder);
    }
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlNodeSyntaxHelperRegistry#getUrlNodeSyntaxHelper(java.lang.String)
     */
    @Override
    public IUrlNodeSyntaxHelper getUrlNodeSyntaxHelper(String name) {
        return this.urlNodeSyntaxHelpers.get(name);
    }

    @Override
    public IUrlNodeSyntaxHelper getCurrentUrlNodeSyntaxHelper(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        
        final int themeStylesheetId = userProfile.getThemeStylesheetId();
        final IUrlNodeSyntaxHelper themeUrlSyntaxHelper = getUrlNodeSyntaxHelperForStylesheet(themeStylesheetId);
        if (themeUrlSyntaxHelper != null) {
            return themeUrlSyntaxHelper;
        }
        
        final int structureStylesheetId = userProfile.getStructureStylesheetId();
        final IUrlNodeSyntaxHelper structureUrlSyntaxHelper = getUrlNodeSyntaxHelperForStylesheet(structureStylesheetId);
        if (structureUrlSyntaxHelper != null) {
            return structureUrlSyntaxHelper;
        }
        
        throw new IllegalStateException("No IUrlNodeSyntaxHelper could be found for the current request. Review the IStylesheetDescriptor configuration.");
    }

    @Override
    public IUrlNodeSyntaxHelper getUrlNodeSyntaxHelperForStylesheet(final int stylesheetDescriptorId) {
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetDescriptorId);
        if (stylesheetDescriptor == null) {
            throw new IllegalArgumentException("No IStylesheetDescriptor found for id: " + stylesheetDescriptorId);
        }
        
        final String themeUrlSyntaxHelperName = stylesheetDescriptor.getUrlNodeSyntaxHelperName();
        if (themeUrlSyntaxHelperName != null) {
            return this.getUrlNodeSyntaxHelper(themeUrlSyntaxHelperName);
        }
        
        return null;
    }
}
