/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.xml.stream;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XMLEventWriterBuffer extends BaseXMLEventReader implements XMLEventWriter, XMLEventReader {
    private final Queue<XMLEvent> events = new ConcurrentLinkedQueue<XMLEvent>();
    private final Map<String, String> prefixMap = new LinkedHashMap<String, String>();
    private NamespaceContext namespaceContext = null;
    private String defaultNamespace = null;
    
    private XMLEvent previousEvent = null;
    
    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }
    

    @Override
    protected XMLEvent getPreviousEvent() {
        return this.previousEvent;
    }

    
    //********* XMLEventWriter APIs **********//

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#add(javax.xml.stream.events.XMLEvent)
     */
    @Override
    public void add(final XMLEvent event) throws XMLStreamException {
        this.events.add(event);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#add(javax.xml.stream.XMLEventReader)
     */
    @Override
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            this.events.add(reader.nextEvent());
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#close()
     */
    @Override
    public void close() throws XMLStreamException {
        //Does nothing
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#flush()
     */
    @Override
    public void flush() throws XMLStreamException {
        //Does nothing
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#getNamespaceContext()
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#getPrefix(java.lang.String)
     */
    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return this.prefixMap.get(uri);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#setDefaultNamespace(java.lang.String)
     */
    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        this.defaultNamespace = uri;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#setNamespaceContext(javax.xml.namespace.NamespaceContext)
     */
    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        this.namespaceContext = context;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventWriter#setPrefix(java.lang.String, java.lang.String)
     */
    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        this.prefixMap.put(uri, prefix);
    }

    
    
    //********* XMLEventReader APIs **********//
    
    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        // no properties are supported, returning null
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventReader#hasNext()
     */
    @Override
    public boolean hasNext() {
        return !this.events.isEmpty();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventReader#nextEvent()
     */
    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        final XMLEvent event = this.events.poll();
        if (event == null) {
            throw new NoSuchElementException();
        }
        this.previousEvent = event;
        return event;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventReader#peek()
     */
    @Override
    public XMLEvent peek() throws XMLStreamException {
        final XMLEvent event = this.events.peek();
        if (event == null) {
            throw new NoSuchElementException();
        }
        return event;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("XMLEventWriterBuffer is Read Only");
    }
}
