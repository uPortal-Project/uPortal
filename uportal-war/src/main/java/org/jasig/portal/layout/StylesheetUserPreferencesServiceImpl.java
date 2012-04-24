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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.ILayoutAttributeDescriptor;
import org.jasig.portal.layout.om.IOutputPropertyDescriptor;
import org.jasig.portal.layout.om.IStylesheetData;
import org.jasig.portal.layout.om.IStylesheetData.Scope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Populator;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Handles retrieving and storing the various scopes of stylesheet user preference data.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class StylesheetUserPreferencesServiceImpl implements IStylesheetUserPreferencesService {
    private static final String OUTPUT_PROPERTIES_KEY = StylesheetUserPreferencesServiceImpl.class.getName() + ".OUTPUT_PROPERTIES";
    private static final String STYLESHEET_PARAMETERS_KEY = StylesheetUserPreferencesServiceImpl.class.getName() + ".STYLESHEET_PARAMETERS";
    private static final String LAYOUT_ATTRIBUTES_KEY = StylesheetUserPreferencesServiceImpl.class.getName() + ".LAYOUT_ATTRIBUTES";
    
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
    
    
    protected static final class StylesheetPreferencesKey {
        private final IPerson person;
        private final IUserProfile userProfile;
        private final IStylesheetDescriptor stylesheetDescriptor;
        private final String str;
        
        private StylesheetPreferencesKey(IPerson person, IUserProfile userProfile,
                IStylesheetDescriptor stylesheetDescriptor) {
            this.person = person;
            this.userProfile = userProfile;
            this.stylesheetDescriptor = stylesheetDescriptor;
            
            this.str = "[" + stylesheetDescriptor.getId() + "," + person.getUserName() + "," + userProfile.getProfileId() + "]";
        }

        public String toString() {
            return this.str;
        }
    }

    protected final StylesheetPreferencesKey getStylesheetPreferencesKey(HttpServletRequest request, PreferencesScope prefScope) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        
        final IPerson person = userInstance.getPerson();

        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        
        final int stylesheetId = prefScope.getStylesheetId(userProfile);
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetId);
        
        return new StylesheetPreferencesKey(person, userProfile, stylesheetDescriptor);
    }
    
    protected final IStylesheetUserPreferences getDistributedStylesheetUserPreferences(HttpServletRequest request, PreferencesScope prefScope) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        
        return prefScope.getDistributedIStylesheetUserPreferences(userLayout);
    }
    
    protected final Scope getWriteScope(HttpServletRequest request, PreferencesScope prefScope, StylesheetPreferencesKey stylesheetPreferencesKey, IStylesheetData descriptor) {
        final Scope scope = descriptor.getScope();
        final boolean persistentScopeReadOnly = this.isPersistentScopeReadOnly(request, prefScope, stylesheetPreferencesKey);
        if (persistentScopeReadOnly && Scope.PERSISTENT == scope) {
            return Scope.SESSION;
        }
        return scope;
    }
    
    protected boolean isPersistentScopeReadOnly(HttpServletRequest request, PreferencesScope prefScope, StylesheetPreferencesKey stylesheetPreferencesKey) {
        return stylesheetPreferencesKey.person.isGuest();
    }
    
    protected final boolean compareValues(String value, String defaultValue) {
        return value == defaultValue || (value != null && value.equals(defaultValue));
    }
    
    protected final <T> T getDataValue(HttpServletRequest request, StylesheetPreferencesKey stylesheetPreferencesKey, Scope scope, String mapKey, String name) {
        switch (scope) {
            case SESSION: {
                final HttpSession session = request.getSession(false);
                if (session == null) {
                    return null;
                }
                
                final Map<String, T> outputProperties = PortalWebUtils.getMapSessionAttribute(session, mapKey + stylesheetPreferencesKey.toString(), false);
                if (outputProperties == null) {
                    return null;
                }

                return outputProperties.get(name);
            }
            case REQUEST: {
                final Map<String, T> outputProperties = PortalWebUtils.getMapRequestAttribute(request, mapKey + stylesheetPreferencesKey.toString(), false);
                if (outputProperties == null) {
                    return null;
                }
                
                return outputProperties.get(name);
            }
            default: {
                return null;
            }
        }
    }
    
    protected final <T> T putDataValue(HttpServletRequest request, StylesheetPreferencesKey stylesheetPreferencesKey, Scope scope, String mapKey, String name, T value) {
        switch (scope) {
            case SESSION: {
                final HttpSession session = request.getSession();
                final Map<String, T> outputProperties = PortalWebUtils.getMapSessionAttribute(session, mapKey + stylesheetPreferencesKey.toString());

                return outputProperties.put(name, value);
            }
            case REQUEST: {
                final Map<String, T> outputProperties = PortalWebUtils.getMapRequestAttribute(request, mapKey + stylesheetPreferencesKey.toString());
                
                return outputProperties.put(name, value);
            }
            default: {
                return null;
            }
        }
    }
    
    protected final <T> T removeDataValue(HttpServletRequest request, StylesheetPreferencesKey stylesheetPreferencesKey, Scope scope, String mapKey, String name) {
        switch (scope) {
            case SESSION: {
                final HttpSession session = request.getSession(false);
                if (session == null) {
                    return null;
                }
                
                final Map<String, T> outputProperties = PortalWebUtils.getMapSessionAttribute(session, mapKey + stylesheetPreferencesKey.toString(), false);
                if (outputProperties == null) {
                    return null;
                }

                return outputProperties.remove(name);
            }
            case REQUEST: {
                final Map<String, T> outputProperties = PortalWebUtils.getMapRequestAttribute(request, mapKey + stylesheetPreferencesKey.toString(), false);
                if (outputProperties == null) {
                    return null;
                }
                
                return outputProperties.remove(name);
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    public IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request, PreferencesScope prefScope) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        
        final int stylesheetId = prefScope.getStylesheetId(userProfile);
        return this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetId);
    }

    @Override
    public String getOutputProperty(HttpServletRequest request, PreferencesScope prefScope, String name) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final IOutputPropertyDescriptor outputPropertyDescriptor = stylesheetDescriptor.getOutputPropertyDescriptor(name);
        if (outputPropertyDescriptor == null) {
            logger.warn("Attempted to get output property {} but no such output property is defined in stylesheet descriptor {}. null will be returned", new Object[] {name, stylesheetDescriptor.getName()});
            return null;
        }


        final String value;
        final Scope scope = outputPropertyDescriptor.getScope();
        switch (scope) {
            case PERSISTENT: {
                final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    return null;
                }
                
                value = stylesheetUserPreferences.getOutputProperty(name);
                break;
            }
            default: {
                value = this.getDataValue(request, stylesheetPreferencesKey, scope, OUTPUT_PROPERTIES_KEY, name);
                break;
            }
        }
        
        if (value == null) {
            return null;
        }
        
        //If the value is equal to the default value remove the property and return null
        if (this.compareValues(value, outputPropertyDescriptor.getDefaultValue())) {
            this.removeOutputProperty(request, prefScope, name);
            return null;
        }
        
        return value;
    }

    @Transactional
    @Override
    public String setOutputProperty(HttpServletRequest request, PreferencesScope prefScope, String name, String value) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final IOutputPropertyDescriptor outputPropertyDescriptor = stylesheetDescriptor.getOutputPropertyDescriptor(name);
        if (outputPropertyDescriptor == null) {
            logger.warn("Attempted to set output property {}={} but no such output property is defined in stylesheet descriptor {}. It will be ignored", new Object[] {name, value, stylesheetDescriptor.getName()});
            return null;
        }
        
        if (this.compareValues(value, outputPropertyDescriptor.getDefaultValue())) {
            return this.removeOutputProperty(request, prefScope, name);
        }
        
        final Scope scope = this.getWriteScope(request, prefScope, stylesheetPreferencesKey, outputPropertyDescriptor);
        switch (scope) {
            case PERSISTENT: {
                IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    stylesheetUserPreferences = this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, stylesheetPreferencesKey.person, stylesheetPreferencesKey.userProfile);
                    this.clearStylesheetUserPreferencesCache(request, stylesheetPreferencesKey);
                }
                
                final String oldValue = stylesheetUserPreferences.setOutputProperty(name, value);
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                return oldValue;
            }
            default: {
                return this.putDataValue(request, stylesheetPreferencesKey, scope, OUTPUT_PROPERTIES_KEY, name, value);
            }
        }
    }

    @Transactional
    @Override
    public String removeOutputProperty(HttpServletRequest request, PreferencesScope prefScope, String name) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final IOutputPropertyDescriptor outputPropertyDescriptor = stylesheetDescriptor.getOutputPropertyDescriptor(name);
        if (outputPropertyDescriptor == null) {
            logger.warn("Attempted to remove output property {} but no such output property is defined in stylesheet descriptor {}. It will be ignored", new Object[] {name, stylesheetDescriptor.getName()});
            return null;
        }
        
        final Scope scope = this.getWriteScope(request, prefScope, stylesheetPreferencesKey, outputPropertyDescriptor);
        switch (scope) {
            case PERSISTENT: {
                final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    return null;
                }
                
                final String oldValue = stylesheetUserPreferences.removeOutputProperty(name);
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                this.clearStylesheetUserPreferencesCache(request, stylesheetPreferencesKey);
                return oldValue;
            }
            default: {
                return removeDataValue(request, stylesheetPreferencesKey, scope, OUTPUT_PROPERTIES_KEY, name);
            }
        }
    }
    
    @Override
    public <P extends Populator<String, String>> P populateOutputProperties(HttpServletRequest request,
            PreferencesScope prefScope, P properties) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        
        //Get the scoped sources once
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        
        final Map<String, String> sessionOutputProperties;
        final HttpSession session = request.getSession(false);
        if (session == null) {
            sessionOutputProperties = null;
        }
        else {
            sessionOutputProperties = PortalWebUtils.getMapSessionAttribute(session, OUTPUT_PROPERTIES_KEY + stylesheetPreferencesKey.toString(), false);
        }
        
        final Map<String, String> requestOutputProperties = PortalWebUtils.getMapRequestAttribute(request, OUTPUT_PROPERTIES_KEY + stylesheetPreferencesKey.toString(), false);
        
        //Try getting each output property to populate the Properties
        for (final IOutputPropertyDescriptor outputPropertyDescriptor : stylesheetDescriptor.getOutputPropertyDescriptors()) {
            final String name = outputPropertyDescriptor.getName();

            final String value;
            final Scope scope = outputPropertyDescriptor.getScope();
            switch (scope) {
                case PERSISTENT: {
                    if (stylesheetUserPreferences == null) {
                        value = null;
                        break;
                    }
                    
                    value = stylesheetUserPreferences.getOutputProperty(name);
                    break;
                }
                case SESSION: {
                    if (sessionOutputProperties == null) {
                        value = null;
                        break;
                    }

                    value = sessionOutputProperties.get(name);
                    break;
                }
                case REQUEST: {
                    if (requestOutputProperties == null) {
                        value = null;
                        break;
                    }
                    
                    value = requestOutputProperties.get(name);
                    break;
                }
                default: {
                    value = null;
                    break;
                }
            }
            
            //Don't add unset properties
            if (value == null) {
                continue;
            }
            
            //If the value is equal to the default value remove the property and return null
            if (this.compareValues(value, outputPropertyDescriptor.getDefaultValue())) {
                this.removeOutputProperty(request, prefScope, name);
                continue;
            }
            
            properties.put(name, value);
        }
        
        return properties;
    }

    @Transactional
    @Override
    public void clearOutputProperties(HttpServletRequest request, PreferencesScope prefScope) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        if (stylesheetUserPreferences != null) {
            stylesheetUserPreferences.clearOutputProperties();
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(OUTPUT_PROPERTIES_KEY + stylesheetPreferencesKey.toString());
        }
        
        request.removeAttribute(OUTPUT_PROPERTIES_KEY + stylesheetPreferencesKey.toString());
    }
    

    

    @Override
    public String getStylesheetParameter(HttpServletRequest request, PreferencesScope prefScope, String name) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final IStylesheetParameterDescriptor stylesheetParameterDescriptor = stylesheetDescriptor.getStylesheetParameterDescriptor(name);
        if (stylesheetParameterDescriptor == null) {
            logger.warn("Attempted to get stylesheet parameter {} but no such stylesheet parameter is defined in stylesheet descriptor {}. null will be returned", new Object[] {name, stylesheetDescriptor.getName()});
            return null;
        }


        final String value;
        final Scope scope = stylesheetParameterDescriptor.getScope();
        switch (scope) {
            case PERSISTENT: {
                final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    return null;
                }
                
                value = stylesheetUserPreferences.getStylesheetParameter(name);
                break;
            }
            default: {
                value = this.getDataValue(request, stylesheetPreferencesKey, scope, STYLESHEET_PARAMETERS_KEY, name);
                break;
            }
        }
        
        if (value == null) {
            return null;
        }
        
        //If the value is equal to the default value remove the property and return null
        if (this.compareValues(value, stylesheetParameterDescriptor.getDefaultValue())) {
            this.removeStylesheetParameter(request, prefScope, name);
            return null;
        }
        
        return value;
    }

    @Transactional
    @Override
    public String setStylesheetParameter(HttpServletRequest request, PreferencesScope prefScope, String name, String value) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final IStylesheetParameterDescriptor stylesheetParameterDescriptor = stylesheetDescriptor.getStylesheetParameterDescriptor(name);
        if (stylesheetParameterDescriptor == null) {
            logger.warn("Attempted to set stylesheet parameter {}={} but no such stylesheet parameter is defined in stylesheet descriptor {}. It will be ignored", new Object[] {name, value, stylesheetDescriptor.getName()});
            return null;
        }
        
        if (this.compareValues(value, stylesheetParameterDescriptor.getDefaultValue())) {
            return this.removeStylesheetParameter(request, prefScope, name);
        }
        
        final Scope scope = this.getWriteScope(request, prefScope, stylesheetPreferencesKey, stylesheetParameterDescriptor);
        switch (scope) {
            case PERSISTENT: {
                IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    stylesheetUserPreferences = this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, stylesheetPreferencesKey.person, stylesheetPreferencesKey.userProfile);
                    this.clearStylesheetUserPreferencesCache(request, stylesheetPreferencesKey);
                }
                
                final String oldValue = stylesheetUserPreferences.setStylesheetParameter(name, value);
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                return oldValue;
            }
            default: {
                return this.putDataValue(request, stylesheetPreferencesKey, scope, STYLESHEET_PARAMETERS_KEY, name, value);
            }
        }
    }

    @Transactional
    @Override
    public String removeStylesheetParameter(HttpServletRequest request, PreferencesScope prefScope, String name) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final IStylesheetParameterDescriptor stylesheetParameterDescriptor = stylesheetDescriptor.getStylesheetParameterDescriptor(name);
        if (stylesheetParameterDescriptor == null) {
            logger.warn("Attempted to remove stylesheet parameter {} but no such stylesheet parameter is defined in stylesheet descriptor {}. It will be ignored", new Object[] {name, stylesheetDescriptor.getName()});
            return null;
        }
        
        final Scope scope = this.getWriteScope(request, prefScope, stylesheetPreferencesKey, stylesheetParameterDescriptor);
        switch (scope) {
            case PERSISTENT: {
                final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    return null;
                }
                
                final String oldValue = stylesheetUserPreferences.removeStylesheetParameter(name);
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                return oldValue;
            }
            default: {
                return removeDataValue(request, stylesheetPreferencesKey, scope, STYLESHEET_PARAMETERS_KEY, name);
            }
        }
    }

    @Override
    public Iterable<String> getStylesheetParameterNames(HttpServletRequest request, PreferencesScope prefScope) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final Collection<IStylesheetParameterDescriptor> stylesheetParameterDescriptors = stylesheetDescriptor.getStylesheetParameterDescriptors();
        
        return Collections2.transform(stylesheetParameterDescriptors, new Function<IStylesheetParameterDescriptor, String>() {
            @Override
            public String apply(IStylesheetParameterDescriptor input) {
                return input.getName();
            }
        });
    }

    @Override
    public <P extends Populator<String, String>> P populateStylesheetParameters(HttpServletRequest request,
            PreferencesScope prefScope, P stylesheetParameters) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        
        //Get the scoped sources once
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        final Map<String, String> sessionStylesheetParameters;
        final HttpSession session = request.getSession(false);
        if (session == null) {
            sessionStylesheetParameters = null;
        }
        else {
            sessionStylesheetParameters = PortalWebUtils.getMapSessionAttribute(session, STYLESHEET_PARAMETERS_KEY + stylesheetPreferencesKey.toString(), false);
        }
        final Map<String, String> requestStylesheetParameters = PortalWebUtils.getMapRequestAttribute(request, STYLESHEET_PARAMETERS_KEY + stylesheetPreferencesKey.toString(), false);
        
        //Try getting each stylesheet parameter to populate the Map
        for (final IStylesheetParameterDescriptor stylesheetParameterDescriptor : stylesheetDescriptor.getStylesheetParameterDescriptors()) {
            final String name = stylesheetParameterDescriptor.getName();

            final String value;
            final Scope scope = stylesheetParameterDescriptor.getScope();
            switch (scope) {
                case PERSISTENT: {
                    if (stylesheetUserPreferences == null) {
                        value = null;
                        break;
                    }
                    
                    value = stylesheetUserPreferences.getStylesheetParameter(name);
                    break;
                }
                case SESSION: {
                    if (sessionStylesheetParameters == null) {
                        value = null;
                        break;
                    }

                    value = sessionStylesheetParameters.get(name);
                    break;
                }
                case REQUEST: {
                    if (requestStylesheetParameters == null) {
                        value = null;
                        break;
                    }
                    
                    value = requestStylesheetParameters.get(name);
                    break;
                }
                default: {
                    value = null;
                    break;
                }
            }
            
            //Don't add unset properties
            if (value == null) {
                continue;
            }
            
            //If the value is equal to the default value remove the property and return null
            if (this.compareValues(value, stylesheetParameterDescriptor.getDefaultValue())) {
                this.removeStylesheetParameter(request, prefScope, name);
                continue;
            }
            
            stylesheetParameters.put(name, value);
        }
        
        return stylesheetParameters;
    }

    @Transactional
    @Override
    public void clearStylesheetParameters(HttpServletRequest request, PreferencesScope prefScope) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        if (stylesheetUserPreferences != null) {
            stylesheetUserPreferences.clearStylesheetParameters();
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(STYLESHEET_PARAMETERS_KEY + stylesheetPreferencesKey.toString());
        }
        
        request.removeAttribute(STYLESHEET_PARAMETERS_KEY + stylesheetPreferencesKey.toString());
    }

    @Override
    public String getLayoutAttribute(HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final ILayoutAttributeDescriptor layoutAttributeDescriptor = stylesheetDescriptor.getLayoutAttributeDescriptor(name);
        if (layoutAttributeDescriptor == null) {
            logger.warn("Attempted to get layout attribute {} for ID=\"{}\" but no such stylesheet parameter is defined in stylesheet descriptor {}. Null will be returned", new Object[] {name, nodeId, stylesheetDescriptor.getName()});
            return null;
        }


        
        //Load the default value
        String defaultValue = null;
        final IStylesheetUserPreferences distributedStylesheetUserPreferences = this.getDistributedStylesheetUserPreferences(request, prefScope);
        if (distributedStylesheetUserPreferences != null) {
            defaultValue = distributedStylesheetUserPreferences.getLayoutAttribute(nodeId, name);
            
            if (this.compareValues(defaultValue, layoutAttributeDescriptor.getDefaultValue())) {
                //DLM attribute value matches the stylesheet descriptor default, remove the DLM value
                distributedStylesheetUserPreferences.removeLayoutAttribute(nodeId, name);
                defaultValue = null;
            }
        }
        
        final String value;
        final Scope scope = layoutAttributeDescriptor.getScope();
        switch (scope) {
            case PERSISTENT: {
                final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    value = null;
                    break;
                }
                
                value = stylesheetUserPreferences.getLayoutAttribute(nodeId, name);
                break;
            }
            default: {
                final Map<String, String> nodeAttributes = this.getDataValue(request, stylesheetPreferencesKey, scope, LAYOUT_ATTRIBUTES_KEY, nodeId);
                if (nodeAttributes == null) {
                    value = null;
                    break; 
                }
                
                value = nodeAttributes.get(name);
                break;
            }
        }
        
        if (value == null) {
            return defaultValue;
        }
        
        if (this.compareValues(value, layoutAttributeDescriptor.getDefaultValue()) || //Value is equal to stylesheet descriptor default
            (defaultValue != null && this.compareValues(value, defaultValue))) { //Value is equal to DLM stylesheet preferences default
            
            //Remove the user's customized value
            this.removeLayoutAttribute(request, prefScope, nodeId, name);
            return null;
        }
        
        return value;
    }
    
    private static final String NO_PERSISTENT_IStylesheetUserPreferences = StylesheetUserPreferencesServiceImpl.class.getName() + ".NO_PERSISTENT_IStylesheetUserPreferences";

    private String getCacheKey(StylesheetPreferencesKey stylesheetPreferencesKey) {
        return NO_PERSISTENT_IStylesheetUserPreferences + "." + stylesheetPreferencesKey.toString();
    }
    
    private IStylesheetUserPreferences getStylesheetUserPreferences(HttpServletRequest request, StylesheetPreferencesKey stylesheetPreferencesKey) {
        final String key = getCacheKey(stylesheetPreferencesKey);
        
        if (request.getAttribute(key) != null) {
            return null;
        }
        
        final HttpSession session = request.getSession();
        if (session.getAttribute(key) != null) {
            request.setAttribute(key, Boolean.TRUE);
            return null;
        }
        
        final IStylesheetUserPreferences stylesheetUserPreferences = this.stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetPreferencesKey.stylesheetDescriptor, stylesheetPreferencesKey.person, stylesheetPreferencesKey.userProfile);
        
        if (stylesheetUserPreferences == null) {
            session.setAttribute(key, Boolean.TRUE);
            request.setAttribute(key, Boolean.TRUE);
        }

        return stylesheetUserPreferences;
    }
    
    private void clearStylesheetUserPreferencesCache(HttpServletRequest request, StylesheetPreferencesKey stylesheetPreferencesKey) {
        final String key = getCacheKey(stylesheetPreferencesKey);
        
        request.removeAttribute(key);

        final HttpSession session = request.getSession();
        session.removeAttribute(key);
    }
    
    @Transactional
    @Override
    public String setLayoutAttribute(HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name, String value) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final ILayoutAttributeDescriptor layoutAttributeDescriptor = stylesheetDescriptor.getLayoutAttributeDescriptor(name);
        if (layoutAttributeDescriptor == null) {
            logger.warn("Attempted to set layout attribute {}={} on node with ID=\"{}\" but no such stylesheet parameter is defined in stylesheet descriptor {}. It will be ignored.", new Object[] {name, value, nodeId, stylesheetDescriptor.getName()});
            return null;
        }
        
        if (this.compareValues(value, layoutAttributeDescriptor.getDefaultValue())) {
            //Value matches the default value, remove the attribute
            return this.removeLayoutAttribute(request, prefScope, nodeId, name);
        }
        
        final IStylesheetUserPreferences distributedStylesheetUserPreferences = this.getDistributedStylesheetUserPreferences(request, prefScope);
        if (distributedStylesheetUserPreferences != null) {
            final String defaultValue = distributedStylesheetUserPreferences.getLayoutAttribute(nodeId, name);
            if (this.compareValues(value, defaultValue)) {
                //Value matches the DLM preferences value, remove the value
                return this.removeLayoutAttribute(request, prefScope, nodeId, name);
            }
        }
        
        final Scope scope = this.getWriteScope(request, prefScope, stylesheetPreferencesKey, layoutAttributeDescriptor);
        switch (scope) {
            case PERSISTENT: {
                IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    stylesheetUserPreferences = this.stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, stylesheetPreferencesKey.person, stylesheetPreferencesKey.userProfile);
                    this.clearStylesheetUserPreferencesCache(request, stylesheetPreferencesKey);
                }
                
                final String oldValue = stylesheetUserPreferences.setLayoutAttribute(nodeId, name, value);
                this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                return oldValue;
            }
            default: {
                
                //Determine the mutex to use for accessing the nodeAttributes map
                final Object mutex;
                switch (scope) {
                    case REQUEST: {
                        mutex = PortalWebUtils.getRequestAttributeMutex(request);
                        break;
                    }
                    case SESSION: {
                        final HttpSession session = request.getSession();
                        mutex = WebUtils.getSessionMutex(session);
                        break;
                    }
                    default: {
                        mutex = new Object();
                        break;
                    }
                }
                
                //Get/Create the nodeAttributes map
                Map<String, String> nodeAttributes;
                synchronized (mutex) {
                    nodeAttributes = this.getDataValue(request, stylesheetPreferencesKey, scope, LAYOUT_ATTRIBUTES_KEY, nodeId);
                    if (nodeAttributes == null) {
                        nodeAttributes = new ConcurrentHashMap<String, String>();
                        this.putDataValue(request, stylesheetPreferencesKey, scope, LAYOUT_ATTRIBUTES_KEY, nodeId, nodeAttributes);
                    }
                }
                
                return nodeAttributes.put(name, value);
            }
        }
    }

    @Transactional
    @Override
    public String removeLayoutAttribute(HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        final ILayoutAttributeDescriptor layoutAttributeDescriptor = stylesheetDescriptor.getLayoutAttributeDescriptor(name);
        if (layoutAttributeDescriptor == null) {
            logger.warn("Attempted to remove layout attribute {} for ID=\"{}\" but no such stylesheet parameter is defined in stylesheet descriptor {}. It will be ignored.", new Object[] {name, nodeId, stylesheetDescriptor.getName()});
            return null;
        }
        
        final Scope scope = this.getWriteScope(request, prefScope, stylesheetPreferencesKey, layoutAttributeDescriptor);
        switch (scope) {
            case PERSISTENT: {
                final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
                if (stylesheetUserPreferences == null) {
                    break;
                }
                
                final String oldValue = stylesheetUserPreferences.removeLayoutAttribute(nodeId, name);
                if (oldValue != null) {
                    this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                    return oldValue;
                }
            }
            default: {
                final Map<String, String> nodeAttributes = this.getDataValue(request, stylesheetPreferencesKey, scope, LAYOUT_ATTRIBUTES_KEY, nodeId);
                if (nodeAttributes == null) {
                    break;
                }
                
                final String oldValue = nodeAttributes.remove(name);
                if (oldValue != null) {
                    return oldValue;
                }
            }
        }
        
        final IStylesheetUserPreferences distributedStylesheetUserPreferences = this.getDistributedStylesheetUserPreferences(request, prefScope);
        if (distributedStylesheetUserPreferences != null) {
            return distributedStylesheetUserPreferences.removeLayoutAttribute(nodeId, name);
        }
        
        return null;
    }

    @Override
    public Iterable<String> getAllLayoutAttributeNodeIds(HttpServletRequest request, PreferencesScope prefScope) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        
        final LinkedHashSet<String> allNodeIds = new LinkedHashSet<String>();
        
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        if (stylesheetUserPreferences != null) {
            allNodeIds.addAll(stylesheetUserPreferences.getAllLayoutAttributeNodeIds());
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final Map<String, Map<String, String>> sessionLayoutAttributes = PortalWebUtils.getMapSessionAttribute(session, LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString(), false);
            if (sessionLayoutAttributes != null) {
                allNodeIds.addAll(sessionLayoutAttributes.keySet());
            }
            
        }

        final Map<String, Map<String, String>> requestLayoutAttributes = PortalWebUtils.getMapRequestAttribute(request, LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString(), false);
        if (requestLayoutAttributes != null) {
            allNodeIds.addAll(requestLayoutAttributes.keySet());
        }
        
        final IStylesheetUserPreferences distributedStylesheetUserPreferences = this.getDistributedStylesheetUserPreferences(request, prefScope);
        if (distributedStylesheetUserPreferences != null) {
            allNodeIds.addAll(distributedStylesheetUserPreferences.getAllLayoutAttributeNodeIds());
        }
        
        return allNodeIds;
    }
    
    @Override
    public <P extends Populator<String, String>> P populateLayoutAttributes(HttpServletRequest request,
            PreferencesScope prefScope, String nodeId, P layoutAttributes) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetPreferencesKey.stylesheetDescriptor;
        
        //Get the scoped sources once
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        final Map<String, Map<String, String>> sessionLayoutAttributes;
        final HttpSession session = request.getSession(false);
        if (session == null) {
            sessionLayoutAttributes = null;
        }
        else {
            sessionLayoutAttributes = PortalWebUtils.getMapSessionAttribute(session, LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString(), false);
        }
        final Map<String, Map<String, String>> requestLayoutAttributes = PortalWebUtils.getMapRequestAttribute(request, LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString(), false);
        
        final IStylesheetUserPreferences distributedStylesheetUserPreferences = this.getDistributedStylesheetUserPreferences(request, prefScope);
        if (distributedStylesheetUserPreferences != null) {
            distributedStylesheetUserPreferences.populateLayoutAttributes(nodeId, layoutAttributes);
        }
        
        //Try getting each layout attribute to populate the Map
        for (final ILayoutAttributeDescriptor layoutAttributeDescriptor : stylesheetDescriptor.getLayoutAttributeDescriptors()) {
            final String name = layoutAttributeDescriptor.getName();

            String value;
            final Scope scope = layoutAttributeDescriptor.getScope();
            switch (scope) {
                case PERSISTENT: {
                    if (stylesheetUserPreferences == null) {
                        value = null;
                        break;
                    }
                    
                    value = stylesheetUserPreferences.getLayoutAttribute(nodeId, name);
                    break;
                }
                case SESSION: {
                    if (sessionLayoutAttributes == null) {
                        value = null;
                        break;
                    }

                    final Map<String, String> nodeAttributes = sessionLayoutAttributes.get(nodeId);
                    if (nodeAttributes == null) {
                        value = null;
                        break;
                    }
                    
                    value = nodeAttributes.get(name);
                    break;
                }
                case REQUEST: {
                    if (requestLayoutAttributes == null) {
                        value = null;
                        break;
                    }
                    
                    final Map<String, String> nodeAttributes = requestLayoutAttributes.get(nodeId);
                    if (nodeAttributes == null) {
                        value = null;
                        break;
                    }
                    
                    value = nodeAttributes.get(name);
                    break;
                }
                default: {
                    value = null;
                    break;
                }
            }
            
            //Don't add unset properties
            if (value == null) {
                continue;
            }
            
            //If the value is equal to the default value remove the property and return null
            if (this.compareValues(value, layoutAttributeDescriptor.getDefaultValue())) {
                this.removeLayoutAttribute(request, prefScope, nodeId, name);
                continue;
            }
            
            layoutAttributes.put(name, value);
        }
        
        return layoutAttributes;
    }

    @Transactional
    @Override
    public void clearLayoutAttributes(HttpServletRequest request, PreferencesScope prefScope, String nodeId) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        if (stylesheetUserPreferences != null) {
            stylesheetUserPreferences.clearLayoutAttributes(nodeId);
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final Map<String, Map<String, String>> sessionLayoutAttributes = PortalWebUtils.getMapSessionAttribute(session, LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString(), false);
            if (sessionLayoutAttributes != null) {
                sessionLayoutAttributes.remove(nodeId);
            }
            
        }

        final Map<String, Map<String, String>> requestLayoutAttributes = PortalWebUtils.getMapRequestAttribute(request, LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString(), false);
        if (requestLayoutAttributes != null) {
            requestLayoutAttributes.remove(nodeId);
        }
    }

    @Transactional
    @Override
    public void clearAllLayoutAttributes(HttpServletRequest request, PreferencesScope prefScope) {
        final StylesheetPreferencesKey stylesheetPreferencesKey = this.getStylesheetPreferencesKey(request, prefScope);
        
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request, stylesheetPreferencesKey);
        if (stylesheetUserPreferences != null) {
            stylesheetUserPreferences.clearAllLayoutAttributes();
            this.stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
        }
        
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString());
        }
        
        request.removeAttribute(LAYOUT_ATTRIBUTES_KEY + stylesheetPreferencesKey.toString());
    }
}
