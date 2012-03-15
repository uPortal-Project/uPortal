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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IStylesheetData;
import org.jasig.portal.layout.om.IStylesheetData.Scope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles retrieving and storing the various scopes of stylesheet user preference data.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class StylesheetUserPreferencesServiceImpl implements IStylesheetUserPreferencesService {
    private static final String STYLESHEET_USER_PREFERENCES_KEY = StylesheetUserPreferencesServiceImpl.class.getName() + ".STYLESHEET_USER_PREFERENCES";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IUserInstanceManager userInstanceManager;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    @Autowired 
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }
    
    @Autowired
    public void setStylesheetUserPreferencesDao(IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IStylesheetUserPreferencesService#getThemeStylesheetUserPreferences(javax.servlet.http.HttpServletRequest)
     */
    @RequestCache
    @Override
    public IStylesheetUserPreferences getThemeStylesheetUserPreferences(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IPerson person = userInstance.getPerson();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        final int themeStylesheetId = userProfile.getThemeStylesheetId();
        
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        final IStylesheetUserPreferences distributedThemeStylesheetUserPreferences = userLayout.getDistributedThemeStylesheetUserPreferences();

        return createCompositeStylesheetUserPreferences(request, person, themeStylesheetId, distributedThemeStylesheetUserPreferences);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IStylesheetUserPreferencesService#getStructureStylesheetUserPreferences(javax.servlet.http.HttpServletRequest)
     */
    @RequestCache
    @Override
    public IStylesheetUserPreferences getStructureStylesheetUserPreferences(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IPerson person = userInstance.getPerson();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        final int structureStylesheetId = userProfile.getStructureStylesheetId();
        
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        final IStylesheetUserPreferences distributedStructureStylesheetUserPreferences = userLayout.getDistributedStructureStylesheetUserPreferences();
        
        return createCompositeStylesheetUserPreferences(request, person, structureStylesheetId, distributedStructureStylesheetUserPreferences);
    }

    protected IStylesheetUserPreferences createCompositeStylesheetUserPreferences(
            HttpServletRequest request,
            IPerson person,
            int stylesheetId,
            IStylesheetUserPreferences distributedStylesheetUserPreferences) {
        
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetId);
        if (stylesheetDescriptor == null) {
            throw new IllegalArgumentException("Could not find IStylesheetDescriptor for id: " + stylesheetId);
        }
        
        final Map<Scope, IStylesheetUserPreferences> stylesheetUserPreferences = new LinkedHashMap<IStylesheetData.Scope, IStylesheetUserPreferences>();
        
        //Load persistent preferences
        final IStylesheetUserPreferences persistentPreferences = this.getPersistentStylesheetUserPreferences(request, stylesheetDescriptor, false);
        if (persistentPreferences != null) {
            stylesheetUserPreferences.put(Scope.PERSISTENT, persistentPreferences);
        }
        
        //Load session preferences
        final IStylesheetUserPreferences sessionPreferences = this.getSessionStylesheetUserPreferences(request, stylesheetId, false);
        if (sessionPreferences != null) {
            stylesheetUserPreferences.put(Scope.SESSION, sessionPreferences);
        }
        
        //Load request preferences
        final IStylesheetUserPreferences requestPreferences = this.getRequestStylesheetUserPreferences(request, stylesheetId, false);
        if (requestPreferences != null) {
            stylesheetUserPreferences.put(Scope.REQUEST, requestPreferences);
        }
        
        final boolean readOnlyPersistent = person.isGuest();
        return new CompositeStylesheetUserPreferences(stylesheetDescriptor, stylesheetUserPreferences, distributedStylesheetUserPreferences, readOnlyPersistent);
    }

    @Override
    public void updateStylesheetUserPreferences(HttpServletRequest request, IStylesheetUserPreferences stylesheetUserPreferences) {
        if (!(stylesheetUserPreferences instanceof CompositeStylesheetUserPreferences)) {
            throw new IllegalArgumentException("The IStylesheetUserPreferences is not the same as returned by getStructureStylesheetUserPreferences or getThemeStylesheetUserPreferences");
        }
        
        final CompositeStylesheetUserPreferences compositeStylesheetUserPreferences = (CompositeStylesheetUserPreferences)stylesheetUserPreferences;
        final IStylesheetDescriptor stylesheetDescriptor = compositeStylesheetUserPreferences.getStylesheetDescriptor();
        
        final Map<Scope, IStylesheetUserPreferences> componentPreferences = compositeStylesheetUserPreferences.getComponentPreferences();
        
        final IStylesheetUserPreferences requestComponentPreferences = componentPreferences.get(Scope.REQUEST);
        if (requestComponentPreferences != null) {
            final IStylesheetUserPreferences requestPreferences = this.getRequestStylesheetUserPreferences(request, stylesheetDescriptor.getId(), true);
            if (requestComponentPreferences != requestPreferences) {
                //Assume that the requestPreferences are new since it is a different object
                requestPreferences.setStylesheetUserPreferences(requestComponentPreferences);
            }
        }
        
        final IStylesheetUserPreferences sessionComponentPreferences = componentPreferences.get(Scope.SESSION);
        if (sessionComponentPreferences != null) {
            final IStylesheetUserPreferences sessionPreferences = this.getSessionStylesheetUserPreferences(request, stylesheetDescriptor.getId(), true);
            if (sessionComponentPreferences != sessionPreferences) {
                //Assume that the sessionPreferences are new since it is a different object
                sessionPreferences.setStylesheetUserPreferences(sessionComponentPreferences);
            }
        }
        
        final IStylesheetUserPreferences persistentComponentPreferences = componentPreferences.get(Scope.PERSISTENT);
        if (persistentComponentPreferences != null) {
            final IStylesheetUserPreferences persistentPreferences = this.getPersistentStylesheetUserPreferences(request, stylesheetDescriptor, true);
            if (persistentComponentPreferences != persistentPreferences) {
                persistentPreferences.setStylesheetUserPreferences(persistentComponentPreferences);
            }
            
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(persistentPreferences);
        }
    }
    
    protected IStylesheetUserPreferences getPersistentStylesheetUserPreferences(HttpServletRequest request, IStylesheetDescriptor stylesheetDescriptor, boolean create) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        
        final IPerson person = userInstance.getPerson();
        if (person.isGuest()) {
            //Guests never have persistent preferences
            return this.getSessionStylesheetUserPreferences(request, stylesheetDescriptor.getId(), false);
        }
            
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        
        final IStylesheetUserPreferences persistentPreferences = this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, person, userProfile);
        if (persistentPreferences != null) {
            return persistentPreferences;
        }
        
        if (!create) {
            return null;
        }
        
        return this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, person, userProfile);
    }
    
    protected IStylesheetUserPreferences getRequestStylesheetUserPreferences(HttpServletRequest request, long stylesheetDescriptorId, boolean create) {
        final Map<Long, IStylesheetUserPreferences> preferencesMap = PortalWebUtils.getMapRequestAttribute(request, STYLESHEET_USER_PREFERENCES_KEY, create);
        
        return getStylesheetUserPreferences(preferencesMap, stylesheetDescriptorId, create);
    }
    
    protected IStylesheetUserPreferences getSessionStylesheetUserPreferences(HttpServletRequest request, long stylesheetDescriptorId, boolean create) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("An existing HttpSession is required");
        }

        final Map<Long, IStylesheetUserPreferences> preferencesMap = PortalWebUtils.getMapSessionAttribute(session, STYLESHEET_USER_PREFERENCES_KEY, create);
        
        return getStylesheetUserPreferences(preferencesMap, stylesheetDescriptorId, create);
    }

    protected IStylesheetUserPreferences getStylesheetUserPreferences(Map<Long, IStylesheetUserPreferences> preferencesMap, long stylesheetDescriptorId, boolean create) {
        //Should never be null if create is true
        if (preferencesMap == null) {
            return null;
        }
        
        IStylesheetUserPreferences stylesheetUserPreferences = preferencesMap.get(stylesheetDescriptorId);
        if (stylesheetUserPreferences != null) {
            return stylesheetUserPreferences;
        }
        if (!create) {
            return null;
        }
        
        stylesheetUserPreferences = new StylesheetUserPreferencesImpl(stylesheetDescriptorId);
        preferencesMap.put(stylesheetDescriptorId, stylesheetUserPreferences);
        return stylesheetUserPreferences;
    }
}
