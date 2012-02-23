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

package org.jasig.portal.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.jasig.portal.io.xml.IPortalDataHandlerService.BatchImportOptions;
import org.jasig.portal.utils.ConcurrentMapUtils;
import org.jasig.portal.utils.ResourceUtils;
import org.jasig.portal.xml.StaxUtils;
import org.jasig.portal.xml.stream.BufferedXMLEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.ctc.wstx.api.WstxInputProperties;
import com.google.common.base.Function;

public final class PortalDataKeyFileProcessor implements Function<Resource, Object> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final AtomicLong count = new AtomicLong();
    private final ConcurrentMap<PortalDataKey, Queue<Resource>> dataToImport = new ConcurrentHashMap<PortalDataKey, Queue<Resource>>();
    private final Map<PortalDataKey, IPortalDataType> dataKeyTypes;
    private final XMLInputFactory xmlInputFactory;
    private final BatchImportOptions options;
    
    PortalDataKeyFileProcessor(Map<PortalDataKey, IPortalDataType> dataKeyTypes, BatchImportOptions options) {
        this.dataKeyTypes = dataKeyTypes;
        this.options = options;
        
        this.xmlInputFactory = XMLInputFactory.newFactory();
        
        //Set the input buffer to 2k bytes. This appears to work for reading just enough to get the start element event for
        //all of the data files in a single read operation.
        this.xmlInputFactory.setProperty(WstxInputProperties.P_INPUT_BUFFER_LENGTH, 2000);
        this.xmlInputFactory.setProperty(XMLInputFactory2.P_LAZY_PARSING, true); //Do as little parsing as possible, just want basic info
        this.xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false); //Don't do any validation here
        this.xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); //Don't load referenced DTDs here
    }
    
	/**
	 * @return total number of resources to import
	 */
	public long getResourceCount() {
		return count.get();
	}

	/**
	 * @return Map of the data to import
	 */
	public ConcurrentMap<PortalDataKey, Queue<Resource>> getDataToImport() {
		return dataToImport;
	}



	@Override
    public Object apply(Resource input) {
        final InputStream fis;
        try {
            fis = input.getInputStream();
        }
        catch (IOException e) {
            if (this.options == null || this.options.isFailOnError()) {
                throw new RuntimeException("Failed to create InputStream for: " + input, e);
            }

            logger.warn("Failed to create InputStream, resource will be ignored: {}", input);
            return null;
        }
        
        final PortalDataKey portalDataKey;
        final BufferedXMLEventReader xmlEventReader;
        try {
            xmlEventReader = new BufferedXMLEventReader(this.xmlInputFactory.createXMLEventReader(fis), -1);
            
            final StartElement rootElement = StaxUtils.getRootElement(xmlEventReader);
            portalDataKey = new PortalDataKey(rootElement);
        }
        catch (Exception e) {
            if (this.options != null && !this.options.isIngoreNonDataFiles()) {
                throw new RuntimeException("Failed to parse: " + input, e);
            }

            logger.warn("Failed to parse resource, it will be ignored: {}", input);
            return null;
        }
        finally {
            IOUtils.closeQuietly(fis);
        }
        xmlEventReader.reset();
        
        final IPortalDataType portalDataType = this.dataKeyTypes.get(portalDataKey);
        if (portalDataType == null) {
            logger.warn("No IPortalDataType configured for {}, the resource will be ignored: {}", portalDataKey, input);
            return null;
        }
        
        //Allow the PortalDataType to do any necessary post-processing of the input, needed as some types require extra work
        final String resourceUri = ResourceUtils.getResourceUri(input);
        final Set<PortalDataKey> processedPortalDataKeys = portalDataType.postProcessPortalDataKey(resourceUri, portalDataKey, xmlEventReader);
        xmlEventReader.reset();
        
        for (final PortalDataKey processedPortalDataKey : processedPortalDataKeys) {
            //Add the PortalDataKey and File into the map
            Queue<Resource> queue = this.dataToImport.get(processedPortalDataKey);
            if (queue == null) {
                queue = ConcurrentMapUtils.putIfAbsent(this.dataToImport, processedPortalDataKey, new ConcurrentLinkedQueue<Resource>());
            }
            queue.offer(input);
            count.incrementAndGet();
        }
        
        return null;
    }
}