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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.utils.cache.CacheKey;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaticAttributeSource implements AttributeSource {
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    
    private Map<String, Map<String, String>> attributes;
    
    public void setAttributes(Map<String, Map<String, String>> attributes) {
        this.attributes = attributes;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.AttributeSource#getAdditionalAttributes(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.xml.stream.events.StartElement)
     */
    @Override
    public Iterator<Attribute> getAdditionalAttributes(HttpServletRequest request, HttpServletResponse response, StartElement event) {
        if (this.attributes == null) {
            return null;
        }
        
        final QName eventName = event.getName();
        final String localPart = eventName.getLocalPart();
        final Map<String, String> attributes = this.attributes.get(localPart);
        if (attributes == null || attributes.size() == 0) {
            return null;
        }
        
        final List<Attribute> newAttributes = new ArrayList<Attribute>(attributes.size());
        for (final Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
            final String key = attributeEntry.getKey();
            final String value = attributeEntry.getValue();
            final Attribute attribute = xmlEventFactory.createAttribute(key, value);
            newAttributes.add(attribute);
        }
        
        return newAttributes.iterator();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.AttributeSource#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return CacheKey.build("StaticAttributeSource");
    }
}
