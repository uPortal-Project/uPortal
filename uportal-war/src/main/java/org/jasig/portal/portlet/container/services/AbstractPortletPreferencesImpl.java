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
package org.jasig.portal.portlet.container.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Base class for portlet preferences
 * 
 * @author Eric Dalquist
 */
public abstract class AbstractPortletPreferencesImpl<C> implements PortletPreferences {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final boolean render;
    
    private Map<String, IPortletPreference> targetPortletPreferences;
    private Map<String, IPortletPreference> basePortletPreferences;
    private Map<String, IPortletPreference> compositePreferences;
    private Map<String, String[]> compositePreferencesView;
    private boolean modified = false;
    
    public AbstractPortletPreferencesImpl(boolean render) {
        this.render = render;
    }
    
    protected C getInitializationContext() {
        return null;
    }
    
    protected abstract Object getLogDescription();

    protected abstract void loadTargetPortletPreferences(C initContext, Map<String, IPortletPreference> targetPortletPreferences);
    
    protected abstract void loadBasePortletPreferences(C initContext, Map<String, IPortletPreference> basePortletPreferences);
    
    protected abstract boolean storeInternal() throws IOException, ValidatorException;
    
    protected boolean isReadOnly(IPortletPreference portletPreference) {
        return portletPreference != null && portletPreference.isReadOnly();
    }
    
    private final void initPrefs() {
        if (this.compositePreferencesView != null) {
            return;
        }
        
        final C initContext = this.getInitializationContext();
        
        this.targetPortletPreferences = new LinkedHashMap<String, IPortletPreference>();
        this.loadTargetPortletPreferences(initContext, this.targetPortletPreferences);
        
        this.basePortletPreferences = new LinkedHashMap<String, IPortletPreference>();
        this.loadBasePortletPreferences(initContext, this.basePortletPreferences);
        
        this.compositePreferences = new LinkedHashMap<String, IPortletPreference>(this.basePortletPreferences);
        this.compositePreferences.putAll(this.targetPortletPreferences);
        
        this.compositePreferencesView = Maps.transformValues(this.compositePreferences, new Function<IPortletPreference, String[]>() {
            @Override
            public String[] apply(IPortletPreference input) {
                return input.getValues();
            }
        });
    }
    
    private final void clearPrefs() {
        this.targetPortletPreferences = null;
        this.basePortletPreferences = null;
        this.compositePreferences = null;
        this.compositePreferencesView = null;
    }
    
    protected final Map<String, IPortletPreference> getTargetPortletPreferences() {
        this.initPrefs();
        return this.targetPortletPreferences;
    }
    
    protected final Map<String, IPortletPreference> getBasePortletPreferences() {
        this.initPrefs();
        return this.basePortletPreferences;
    }
    
    protected final Map<String, IPortletPreference> getCompositePortletPreferences() {
        this.initPrefs();
        return this.compositePreferences;
    }
    
    protected final Map<String, String[]> getCompositePortletPreferencesView() {
        this.initPrefs();
        return this.compositePreferencesView;
    }
    
    protected final IPortletPreference getPortletPreference(String key) {
        Assert.notNull(key, "Preference Key cannot be null");
        
        final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
        final IPortletPreference portletPreference = targetPortletPreferences.get(key);
        if (portletPreference != null) {
            return portletPreference;
        }
        
        final Map<String, IPortletPreference> basePortletPreferences = this.getBasePortletPreferences();
        return basePortletPreferences.get(key);
    }

    @Override
    public final boolean isReadOnly(String key) {
        final IPortletPreference portletPreference = this.getPortletPreference(key);
        return portletPreference != null && portletPreference.isReadOnly();
    }

    @Override
    public final String getValue(String key, String def) {
        final IPortletPreference portletPreference = this.getPortletPreference(key);
        
        if (portletPreference != null) {
            final String[] values = portletPreference.getValues();
            if (values == null || values.length == 0) {
                return null;
            }
            
            return values[0];
        }
        
        return def;
    }

    @Override
    public final String[] getValues(String key, String[] def) {
        final IPortletPreference portletPreference = this.getPortletPreference(key);
        
        if (portletPreference != null) {
            return portletPreference.getValues();
        }
        
        return def;
    }

    @Override
    public final void setValue(String key, String value) throws ReadOnlyException {
        this.setValues(key, new String[] { value });
    }

    @Override
    public final void setValues(String key, String[] values) throws ReadOnlyException {
        Assert.notNull(key, "Preference Key cannot be null");
        
        final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
        
        //Check if there is a base preference for the key
        final Map<String, IPortletPreference> basePortletPreferences = this.getBasePortletPreferences();
        final IPortletPreference basePreference = basePortletPreferences.get(key);
        if (basePreference != null) {
            if (this.isReadOnly(basePreference)) {
                throw new ReadOnlyException("Preference '" + key + "' is read only");
            }

            //if the set value matches base value, delete any target pref
            if (Arrays.equals(values, basePreference.getValues())) {
                this.reset(key);
                return;
            }
        }
        
        IPortletPreference portletPreference = targetPortletPreferences.get(key);
        //No target preference exists yet, create it and then update the composite map
        if (portletPreference == null) {
            portletPreference = new PortletPreferenceImpl(key, false, values != null ? values.clone() : null);
            targetPortletPreferences.put(key, portletPreference);
            
            final Map<String, IPortletPreference> compositePortletPreferences = this.getCompositePortletPreferences();
            compositePortletPreferences.put(key, portletPreference);
            
            this.modified = true;
        }
        //Update the existing preference if the values array is different
        else if (!Arrays.equals(values, portletPreference.getValues())) {
            portletPreference.setValues(values != null ? values.clone() : null);
            
            this.modified = true;
        }
    }

    @Override
    public final Enumeration<String> getNames() {
        final Map<String, String[]> compositePortletPreferencesView = this.getCompositePortletPreferencesView();
        return Collections.enumeration(compositePortletPreferencesView.keySet());
    }

    @Override
    public final Map<String, String[]> getMap() {
        return this.getCompositePortletPreferencesView();
    }

    @Override
    public final void reset(String key) throws ReadOnlyException {
        final Map<String, IPortletPreference> basePortletPreferences = this.getBasePortletPreferences();
        final IPortletPreference basePreference = basePortletPreferences.get(key);
        if (this.isReadOnly(basePreference)) {
            throw new ReadOnlyException("Preference '" + key + "' is read only");
        }
        
        final Map<String, IPortletPreference> targetPortletPreferences = this.getTargetPortletPreferences();
        final IPortletPreference removed = targetPortletPreferences.remove(key);

        //There was a target preference with that key, update the composite preferences map
        if (removed != null) {
            final Map<String, IPortletPreference> compositePortletPreferences = this.getCompositePortletPreferences();
            if (basePreference != null) {
                compositePortletPreferences.put(key, basePreference);
            }
            else {
                compositePortletPreferences.remove(key);
            }
            
            this.modified = true;
        }
    }

    @Override
    public final void store() throws IOException, ValidatorException {
        if (this.render) {
            throw new IllegalStateException("store is not allowed during RENDER phase.");
        }
        
        if (!modified) {
            logger.debug("Skipping store of portlet preferences, nothing has changed: {}", getLogDescription());
            return;
        }
        
        final boolean stored = storeInternal();
        if (stored) {
            logger.debug("Store of portlet preferences resulted in a change, clear preferences cache: {}", getLogDescription());
            this.clearPrefs();
        }
        
        this.modified = false;
    }
}
