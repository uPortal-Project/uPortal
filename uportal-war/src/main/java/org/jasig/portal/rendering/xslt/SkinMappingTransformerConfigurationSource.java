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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Maps a user attribute to a skin. The user's attribute named by {@link #setSkinAttributeName(String)} is used to
 * look up a skin name via the {@link #setAttributeToSkinMap(Map)} map and the skin name is set to in the transformer
 * using the {@link #setSkinParameterName(String)} parameter.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class SkinMappingTransformerConfigurationSource extends TransformerConfigurationSourceAdapter implements BeanNameAware {
    private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    private ResourcesElementsProvider resourcesElementsProvider;
    private String skinNameAttribute;
    private String skinParameterName = "skin";
    private Set<String> stylesheetDescriptorNames;
    private boolean cacheSkinResolution = true;

    /**
     * The name of the transformer parameter used for the skin name, defaults to "skin"
     */
    public void setSkinParameterName(String skinParameterName) {
        this.skinParameterName = skinParameterName;
    }
    
    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    @Autowired
    public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    /**
     * If true the result of {@link #getSkinName(HttpServletRequest)} will be cached in the user's session and
     * re-used. If false {@link #getSkinName(HttpServletRequest)} will be called on every execution.
     */
    public void setCacheSkinResolution(boolean cacheSkinResolution) {
        this.cacheSkinResolution = cacheSkinResolution;
    }
    
    /**
     * Set of theme stylesheet descriptor names that the skin-default should be set for. If not set
     * the skin-default will be set for all stylesheets
     */
    public void setStylesheetDescriptorNames(Set<String> stylesheetDescriptorNames) {
        this.stylesheetDescriptorNames = ImmutableSet.copyOf(stylesheetDescriptorNames);
    }
    

    @Override
    public void setBeanName(String name) {
        this.skinNameAttribute = name + ".SKIN_NAME";
    }

    @Override
    public final Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final String skinName = this.getSkinNameInternal(request);
        if (skinName == null) {
            return Collections.emptyMap();
        }
        
        return ImmutableMap.<String, Object>of(this.skinParameterName, skinName);
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final String skinName = this.getSkinNameInternal(request);
        return CacheKey.build(this.getClass().getName(), skinName);
    }
    
    private String getSkinNameInternal(HttpServletRequest request) {
        if (!this.cacheSkinResolution || Included.PLAIN == this.resourcesElementsProvider.getDefaultIncludedType()) {
            if (!this.shouldOverrideSkin(request)) {
                return null;
            }
            
            return this.getSkinName(request);
        }
        
        final HttpSession session = request.getSession();
        SkinNameHolder skinNameHolder = (SkinNameHolder)session.getAttribute(skinNameAttribute);
        if (skinNameHolder == null) {
            final String skinName;
            if (this.shouldOverrideSkin(request)) {
                skinName = this.getSkinName(request);
            }
            else {
                skinName = null;
            }
                
            skinNameHolder = new SkinNameHolder(skinName);
            session.setAttribute(skinNameAttribute, skinNameHolder);
        }
        
        return skinNameHolder.skinName;
    }
    

    protected boolean shouldOverrideSkin(HttpServletRequest request) {
        if (this.stylesheetDescriptorNames == null || this.stylesheetDescriptorNames.isEmpty()) {
            return true;
        }
        
        final IStylesheetDescriptor stylesheetDescriptor = stylesheetUserPreferencesService.getStylesheetDescriptor(request, PreferencesScope.THEME);
        return this.stylesheetDescriptorNames.contains(stylesheetDescriptor.getName());
    }
    
    /**
     * @return The skin name to use for this request
     */
    protected abstract String getSkinName(HttpServletRequest request);
    
    private static final class SkinNameHolder implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String skinName;

        public SkinNameHolder(String skinName) {
            this.skinName = skinName;
        }
    }
}
