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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles converting the data stored in {@link StructureStylesheetUserPreferences} into additional attributes
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StructureAttributeSource implements AttributeSource {
    private IUserInstanceManager userInstanceManager;
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.AttributeSource#getAdditionalAttributes(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.xml.stream.events.StartElement)
     */
    @Override
    public Iterator<Attribute> getAdditionalAttributes(HttpServletRequest request, HttpServletResponse response, StartElement event) {
        final StructureStylesheetUserPreferences structureStylesheetUserPreferences = this.getStructureStylesheetUserPreferences(request);
        
        final QName name = event.getName();
        if (XMLPipelineConstants.CHANNEL.equals(name)) {
            final Enumeration<String> channelAttributeNames = structureStylesheetUserPreferences.getChannelAttributeNames();
            if (!channelAttributeNames.hasMoreElements()) {
                return null;
            }
            
            final Collection<Attribute> attributes = new LinkedList<Attribute>();

            final Attribute subscribeIdAttr = event.getAttributeByName(XMLPipelineConstants.ID_ATTR_NAME);
            final String subscribeId = subscribeIdAttr.getValue();
            while (channelAttributeNames.hasMoreElements()) {
                final String channelAttributeName = channelAttributeNames.nextElement();
                
                final String value = structureStylesheetUserPreferences.getChannelAttributeValue(subscribeId, channelAttributeName);
                
                final Attribute attribute = XMLPipelineConstants.XML_EVENT_FACTORY.createAttribute(channelAttributeName, value);
                attributes.add(attribute);
            }
            
            return attributes.iterator();
        }
        
        if (XMLPipelineConstants.FOLDER.equals(name)) {
            final Enumeration<String> folderAttributeNames = structureStylesheetUserPreferences.getFolderAttributeNames();
            if (!folderAttributeNames.hasMoreElements()) {
                return null;
            }
            
            final Collection<Attribute> attributes = new LinkedList<Attribute>();

            final Attribute folderIdAttr = event.getAttributeByName(XMLPipelineConstants.ID_ATTR_NAME);
            final String folderId = folderIdAttr.getValue();
            while (folderAttributeNames.hasMoreElements()) {
                final String folderAttributeName = folderAttributeNames.nextElement();
                final String value = structureStylesheetUserPreferences.getFolderAttributeValue(folderId, folderAttributeName);
                
                final Attribute attribute = XMLPipelineConstants.XML_EVENT_FACTORY.createAttribute(folderAttributeName, value);
                attributes.add(attribute);
            }
            
            return attributes.iterator();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.AttributeSource#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final StructureStylesheetUserPreferences structureStylesheetUserPreferences = this.getStructureStylesheetUserPreferences(request);
        final String cacheKey = structureStylesheetUserPreferences.getCacheKey();
        return new CacheKey(cacheKey);
    }

    private StructureStylesheetUserPreferences getStructureStylesheetUserPreferences(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final UserPreferences userPreferences = preferencesManager.getUserPreferences();
        final StructureStylesheetUserPreferences structureStylesheetUserPreferences = userPreferences.getStructureStylesheetUserPreferences();
        return structureStylesheetUserPreferences;
    }
}
