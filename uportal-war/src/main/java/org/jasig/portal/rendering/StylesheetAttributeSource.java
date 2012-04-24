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

package org.jasig.portal.rendering;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.ILayoutAttributeDescriptor;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base implementation of layout attribute source that feeds off of {@link IStylesheetDescriptor} and {@link IStylesheetUserPreferences} data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class StylesheetAttributeSource implements AttributeSource, BeanNameAware {
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private String name;
    protected IUserInstanceManager userInstanceManager;
    protected IStylesheetDescriptorDao stylesheetDescriptorDao;
    protected IStylesheetUserPreferencesService stylesheetUserPreferencesService;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }

    @Autowired
    public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public final Iterator<Attribute> getAdditionalAttributes(HttpServletRequest request, HttpServletResponse response, StartElement event) {
        final IStylesheetDescriptor stylesheetDescriptor = this.getStylesheetDescriptor(request);
        
        final PreferencesScope stylesheetPreferencesScope = this.getStylesheetPreferencesScope(request);
        
        final Collection<Attribute> attributes = new LinkedList<Attribute>();
        
        for (final ILayoutAttributeDescriptor layoutAttributeDescriptor : stylesheetDescriptor.getLayoutAttributeDescriptors()) {
            final Set<String> targetElementNames = layoutAttributeDescriptor.getTargetElementNames();
            final QName eventName = event.getName();
            final String localEventName = eventName.getLocalPart();
            if (targetElementNames.contains(localEventName)) {
                final Attribute subscribeIdAttr = event.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME);
                final String subscribeId = subscribeIdAttr.getValue();
                final String name = layoutAttributeDescriptor.getName();
                

                String value = this.stylesheetUserPreferencesService.getLayoutAttribute(request, stylesheetPreferencesScope, subscribeId, name);
                if (value == null) {
                    value = layoutAttributeDescriptor.getDefaultValue();
                }
                
                if (value != null) {
                    final Attribute attribute = xmlEventFactory.createAttribute(name, value);
                    attributes.add(attribute);
                }
            }
        }
        
        return attributes.iterator();
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final PreferencesScope stylesheetPreferencesScope = this.getStylesheetPreferencesScope(request);
        
        final CacheKeyBuilder<String, String> cacheKeyBuilder = CacheKey.builder(this.name);
        
        final Iterable<String> layoutAttributeNodeIds = this.stylesheetUserPreferencesService.getAllLayoutAttributeNodeIds(request, stylesheetPreferencesScope);
        for (final String nodeId : layoutAttributeNodeIds) {
            cacheKeyBuilder.add(nodeId);
            this.stylesheetUserPreferencesService.populateLayoutAttributes(request, stylesheetPreferencesScope, nodeId, cacheKeyBuilder);
        }
        
        return cacheKeyBuilder.build();
    }

    public IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserProfile userProfile = preferencesManager.getUserProfile();
        
        final PreferencesScope stylesheetPreferencesScope = this.getStylesheetPreferencesScope(request);
        final int stylesheetId = stylesheetPreferencesScope.getStylesheetId(userProfile);
        
        return this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetId);
    }
    
    public abstract PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request);
}