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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.cache.CacheKey;
import org.jasig.portal.xml.stream.FilteringXMLEventReader;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StAXAttributeIncorporationComponent implements StAXPipelineComponent {
    private StAXPipelineComponent parentComponent;
    private AttributeSource attributeSource;
    
    public void setParentComponent(StAXPipelineComponent parentComponent) {
        this.parentComponent = parentComponent;
    }

    public void setAttributeSource(AttributeSource attributeSource) {
        this.attributeSource = attributeSource;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKey parentKey = this.parentComponent.getCacheKey(request, response);
        final CacheKey attributeKey = this.attributeSource.getCacheKey(request, response);
        return new CacheKey(parentKey, attributeKey);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheableEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        //Get the reader from the parent and add the attribute incorporating wrapper
        final CacheableEventReader<XMLEventReader, XMLEvent> cachingEventReader = this.parentComponent.getEventReader(request, response);
        final XMLEventReader eventReader = new AttributeIncorporatingXMLEventReader(request, response, this.attributeSource, cachingEventReader.getEventReader());
        
        //Create the cache key from the parent and the attribute source
        final CacheKey parentKey = cachingEventReader.getCacheKey();
        final CacheKey attributeKey = this.attributeSource.getCacheKey(request, response);
        final CacheKey cacheKey = new CacheKey(parentKey, attributeKey);
        
        return new CacheableEventReaderImpl<XMLEventReader, XMLEvent>(cacheKey, eventReader);
    }
    
    private static final class AttributeIncorporatingXMLEventReader extends FilteringXMLEventReader {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final AttributeSource attributeSource;

        
        public AttributeIncorporatingXMLEventReader(HttpServletRequest request, HttpServletResponse response,
                AttributeSource attributeSource, XMLEventReader reader) {
            super(reader);
            this.request = request;
            this.response = response;
            this.attributeSource = attributeSource;
        }
        
        @Override
        protected XMLEvent filterEvent(XMLEvent event, boolean peek) {
            //Only filtering start elements to add attributes
            if (!event.isStartElement()) {
                return event;
            }

            //Get the additional attributes, return if there are no attributes for this element
            final StartElement startElement = event.asStartElement();
            final Iterator<Attribute> additionalAttributes = attributeSource.getAdditionalAttributes(request, response, startElement);
            if (additionalAttributes == null || !additionalAttributes.hasNext()) {
                return event;
            }
            
            //Merge the additional attributes with the existing attributes
            //additional attributes overwrite
            final Map<QName, Attribute> mergedAttributes = new LinkedHashMap<QName, Attribute>();
            

            for (final Iterator<Attribute> attributes = startElement.getAttributes(); attributes.hasNext(); ) {
                final Attribute attribute = attributes.next();
                mergedAttributes.put(attribute.getName(), attribute);
            }
            
            while (additionalAttributes.hasNext()) {
                final Attribute attribute = additionalAttributes.next();
                mergedAttributes.put(attribute.getName(), attribute);
            }
            
            //Create the modified StartElement with the additional attribute data
            final StartElement modifiedStartElement = XMLPipelineConstants.XML_EVENT_FACTORY.createStartElement(
                    startElement.getName(), 
                    mergedAttributes.values().iterator(), 
                    startElement.getNamespaces());
            
            
            return modifiedStartElement;
        }
    }
}
