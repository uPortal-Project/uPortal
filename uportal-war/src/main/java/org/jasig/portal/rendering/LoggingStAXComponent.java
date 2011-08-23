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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.jasig.portal.xml.stream.FilteringXMLEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Logs the StAX events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LoggingStAXComponent extends StAXPipelineComponentWrapper {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private XmlUtilities xmlUtilities;
    private boolean logFullDocument = true;
    private boolean logEvents = true;
    private boolean logFullDocumentAsHtml = false;

    public void setLoggerName(String loggerName) {
        logger = LoggerFactory.getLogger(loggerName);
    }
    public void setLogFullDocument(boolean logFullDocument) {
        this.logFullDocument = logFullDocument;
    }
    public void setLogFullDocumentAsHtml(boolean logFullDocumentAsHtml) {
        this.logFullDocumentAsHtml = logFullDocumentAsHtml;
    }
    public void setLogEvents(boolean logEvents) {
        this.logEvents = logEvents;
    }
    
    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }
    
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.wrappedComponent.getCacheKey(request, response);
    }
    
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);
        
        final XMLEventReader eventReader = pipelineEventReader.getEventReader();
        final LoggingXMLEventReader loggingEventReader = new LoggingXMLEventReader(eventReader);
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(loggingEventReader, outputProperties);
    }
    
    private class LoggingXMLEventReader extends FilteringXMLEventReader {
        private final List<XMLEvent> eventBuffer = new LinkedList<XMLEvent>();
        
        public LoggingXMLEventReader(XMLEventReader reader) {
            super(reader);
        }

        @Override
        protected XMLEvent filterEvent(XMLEvent event, boolean peek) {
            if (logEvents && logger.isDebugEnabled()) {
                if (peek) {
                    logger.debug("Peek: " + XmlUtilitiesImpl.toString(event));
                }
                else {
                    logger.debug("Read: " + XmlUtilitiesImpl.toString(event));
                }
            }
            
            if (logFullDocument && logger.isDebugEnabled()) {
                eventBuffer.add(event);
                
                if (event.isEndDocument()) {
                    final String xmlOutput = xmlUtilities.serializeXMLEvents(eventBuffer, logFullDocumentAsHtml);
                    logger.debug("\n" + xmlOutput);
                }
            }
            
            return event;
        }

        @Override
        public void close() throws XMLStreamException {
            this.eventBuffer.clear();
        }
    }
}
