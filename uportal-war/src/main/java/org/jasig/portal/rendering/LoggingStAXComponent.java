/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;

import org.jasig.portal.utils.cache.CacheKey;

/**
 * Logs the StAX events
 * 
 * TODO log as a complete XML document instead of just the events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LoggingStAXComponent implements StAXPipelineComponent {
    private String logPrefix;
    private StAXPipelineComponent parentComponent;
    
    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }
    public void setParentComponent(StAXPipelineComponent parentComponent) {
        this.parentComponent = parentComponent;
    }
    
    
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.parentComponent.getCacheKey(request, response);
    }
    
    
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.parentComponent.getEventReader(request, response);
        
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(new EventReaderDelegate(pipelineEventReader.getEventReader()) {

            @Override
            public Object getProperty(String name) throws IllegalArgumentException {
                final Object property = super.getProperty(name);
                System.out.println(logPrefix + "\t getProperty(" + name +") returns '" + property + "'");
                return property;
            }

            @Override
            public Object next() {
                final XMLEvent event = (XMLEvent)super.next();
                System.out.println(logPrefix + "\t next() returns '" + event + "'");
                return event;
            }

            @Override
            public XMLEvent nextEvent() throws XMLStreamException {
                final XMLEvent event = super.nextEvent();
                System.out.println(logPrefix + "\t nextEvent() returns '" + event + "'");
                return event;
            }

            @Override
            public XMLEvent nextTag() throws XMLStreamException {
                final XMLEvent event = super.nextTag();
                System.out.println(logPrefix + "\t nextTag() returns '" + event + "'");
                return event;
            }
        });
    }
}
