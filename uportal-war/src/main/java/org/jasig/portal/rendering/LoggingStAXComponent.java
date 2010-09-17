/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.jasig.portal.xml.stream.FilteringXMLEventReader;
import org.jasig.portal.xml.stream.XMLEventBufferReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the StAX events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LoggingStAXComponent implements StAXPipelineComponent {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private StAXPipelineComponent parentComponent;
    private boolean logFullDocument = true;
    private boolean logEvents = true;

    public void setLoggerName(String loggerName) {
        logger = LoggerFactory.getLogger(loggerName);
    }
    public void setParentComponent(StAXPipelineComponent parentComponent) {
        this.parentComponent = parentComponent;
    }
    public void setLogFullDocument(boolean logFullDocument) {
        this.logFullDocument = logFullDocument;
    }
    public void setLogEvents(boolean logEvents) {
        this.logEvents = logEvents;
    }
    
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.parentComponent.getCacheKey(request, response);
    }
    
    
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.parentComponent.getEventReader(request, response);
        
        final XMLEventReader eventReader = pipelineEventReader.getEventReader();
        final LoggingXMLEventReader loggingEventReader = new LoggingXMLEventReader(eventReader);
        
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(loggingEventReader);
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
            
            if (!peek && logFullDocument && logger.isDebugEnabled()) {
                eventBuffer.add(event);
                
                if (event.isEndDocument()) {
                    
                    //TODO move this into XmlUtilities
                    final StringWriter writer = new StringWriter();
                    
                    final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
                    final XMLEventWriter xmlEventWriter;
                    try {
                        xmlEventWriter = outputFactory.createXMLEventWriter(writer);
                    }
                    catch (XMLStreamException e) {
                        throw new RuntimeException("Failed to create XMLEventWriter", e);
                    }
                    
                    try {
                        xmlEventWriter.add(new XMLEventBufferReader(this.eventBuffer.listIterator()));
                        xmlEventWriter.flush();
                        xmlEventWriter.close();
                    }
                    catch (XMLStreamException e) {
                        throw new RuntimeException("Failed to write events to Writer", e);
                    }
                    
                    logger.debug(writer.toString());
                }
            }
            
            
            return event;
        }
    }
}
