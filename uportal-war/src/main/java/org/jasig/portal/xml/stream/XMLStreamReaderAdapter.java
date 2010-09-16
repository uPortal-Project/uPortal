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

package org.jasig.portal.xml.stream;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XMLStreamReaderAdapter implements XMLStreamReader {
    private final XMLEventReader eventReader;
    
    private final Deque<NamespaceContext> namespaceContextStack = new LinkedList<NamespaceContext>();
    private StartDocument startDocument;
    
    //State fields
    private XMLEvent currentEvent;
    private int eventType;
    private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private final ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
    private String text;
    private char[] chars;
    
    public XMLStreamReaderAdapter(XMLEventReader eventReader) throws XMLStreamException {
        this.eventReader = eventReader;
        this.next();
    }
    
    /**
     * Clear out cached data from the previous element
     */
    private void resetFields() {
        this.currentEvent = null;
        this.eventType = 0;
        this.attributes.clear();
        this.namespaces.clear();
        this.text = null;
        this.chars = null;
    }
    
    private void cacheNamespaces(Iterator<Namespace> namespaces) {
        while (namespaces.hasNext()) {
            final Namespace namespace = namespaces.next();
            this.namespaces.add(namespace);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return this.eventReader.getProperty(name);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#next()
     */
    @Override
    @SuppressWarnings("unchecked")
    public int next() throws XMLStreamException {
        this.resetFields();
        
        this.currentEvent = this.eventReader.nextEvent();
        this.eventType = this.currentEvent.getEventType();
        
        switch (this.eventType) {
            case START_DOCUMENT: {
                this.startDocument = (StartDocument)this.currentEvent;
                break;
            }
            case START_ELEMENT: {
                final StartElement startElement = this.currentEvent.asStartElement();
                final NamespaceContext namespaceContext = startElement.getNamespaceContext();
                this.namespaceContextStack.push(namespaceContext);
                
                for (final Iterator<Attribute> attributes = startElement.getAttributes(); attributes.hasNext(); ) {
                    final Attribute attribute = attributes.next();
                    this.attributes.add(attribute);
                }
                
                final Iterator<Namespace> namespaces = startElement.getNamespaces();
                this.cacheNamespaces(namespaces);
                
                break;
            }
            case END_ELEMENT: {
                final EndElement endElement = this.currentEvent.asEndElement();
                this.namespaceContextStack.pop();
                
                final Iterator<Namespace> namespaces = endElement.getNamespaces();
                this.cacheNamespaces(namespaces);
                
                break;
            }
        }
        
        return this.eventType;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#require(int, java.lang.String, java.lang.String)
     */
    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (this.currentEvent == null) {
            throw new XMLStreamException("next() must be called first");
        }

        if (type != this.eventType) {
            throw new XMLStreamException("Expected event of type: " + 
                    XMLStreamConstantsUtils.getEventName(type) + ", current event type: " + 
                    XMLStreamConstantsUtils.getEventName(this.eventType));
        }

        if (namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
            throw new XMLStreamException("Namespace URI " + namespaceURI + " specified did not match "
                    + "with current namespace URI");
        }

        if (localName != null && !localName.equals(getLocalName())) {
            throw new XMLStreamException("LocalName " + localName + " specified did not match with "
                    + "current local name");
        }
        
        return;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getElementText()
     */
    @Override
    public String getElementText() throws XMLStreamException {
        if (this.eventType != START_ELEMENT) {
            throw new XMLStreamException("parser must be on " + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + " to read next text", getLocation());
        }
        
        int eventType = this.next();
        final StringBuilder content = new StringBuilder();
        while (eventType != XMLStreamConstants.END_ELEMENT) {
            switch (eventType) {
                case CHARACTERS:
                case CDATA:
                case SPACE:
                case ENTITY_REFERENCE: {
                    final String text = this.getText();
                    content.append(text);
                    break;
                }
                case PROCESSING_INSTRUCTION:
                case COMMENT: {
                    // skip
                    break;
                }
                case END_DOCUMENT: {
                    throw new XMLStreamException("unexpected end of document when reading element text content");
                }
                case START_ELEMENT: {
                    throw new XMLStreamException("elementGetText() function expects text only elment but " + 
                            XMLStreamConstantsUtils.getEventName(START_ELEMENT) + " was encountered.", getLocation());
                }
                default: {
                    throw new XMLStreamException("Unexpected event type " + XMLStreamConstantsUtils.getEventName(eventType), getLocation());
                }
                
            }

            eventType = this.next();
        }
        return content.toString();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#nextTag()
     */
    @Override
    public int nextTag() throws XMLStreamException {
        int eventType = this.next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
            || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
            || eventType == XMLStreamConstants.SPACE
            || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
            || eventType == XMLStreamConstants.COMMENT) {
            
            eventType = this.next();
        }

        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("found: " + XMLStreamConstantsUtils.getEventName(eventType) + ", expected "
                    + XMLStreamConstantsUtils.getEventName(XMLStreamConstants.START_ELEMENT) + " or "
                    + XMLStreamConstantsUtils.getEventName(XMLStreamConstants.END_ELEMENT), getLocation());
        }

        return eventType;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#hasNext()
     */
    @Override
    public boolean hasNext() throws XMLStreamException {
        return this.eventReader.hasNext();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#close()
     */
    @Override
    public void close() throws XMLStreamException {
        this.eventReader.close();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI(java.lang.String)
     */
    @Override
    public String getNamespaceURI(String prefix) {
        final NamespaceContext namespaceContext = this.namespaceContextStack.peek();
        if (namespaceContext == null) {
            return null;
        }
        return namespaceContext.getNamespaceURI(prefix);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#isStartElement()
     */
    @Override
    public boolean isStartElement() {
        return this.eventType == START_ELEMENT;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#isEndElement()
     */
    @Override
    public boolean isEndElement() {
        return this.eventType == END_ELEMENT;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#isCharacters()
     */
    @Override
    public boolean isCharacters() {
        return this.eventType == CHARACTERS;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#isWhiteSpace()
     */
    @Override
    public boolean isWhiteSpace() {
        return (this.eventType == CHARACTERS || this.eventType == CDATA) &&
                this.currentEvent.asCharacters().isWhiteSpace();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        switch (this.eventType) {
            case START_ELEMENT: {
                final StartElement startElement = this.currentEvent.asStartElement();
                final QName name = new QName(namespaceURI, localName);
                final Attribute attribute = startElement.getAttributeByName(name);
                return attribute != null ? attribute.getValue() : null;
            }
            case ATTRIBUTE: {
                final Attribute attribute = (Attribute)this.currentEvent;
                final QName name = new QName(namespaceURI, localName);
                if (name.equals(attribute.getName())) {
                    return attribute.getValue();
                }
                
                return null;
            }
        }

        throw new IllegalStateException("Current state is not among the states "
                + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + " , "
                + XMLStreamConstantsUtils.getEventName(ATTRIBUTE) + "valid for getAttributeValue()");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeCount()
     */
    @Override
    public int getAttributeCount() {
        switch (this.eventType) {
            case START_ELEMENT: {
                return this.attributes.size();
            }
            case ATTRIBUTE: {
                throw new UnsupportedOperationException();
            }
        }

        throw new IllegalStateException("Current state is not among the states "
                + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + " , "
                + XMLStreamConstantsUtils.getEventName(ATTRIBUTE) + "valid for getAttributeCount()");
    }
    
    private Attribute getAttribute(int index) {
        switch (this.eventType) {
            case START_ELEMENT: {
                if (index < 0 || index > this.attributes.size()) {
                    return null;
                }
                
                return this.attributes.get(index);
            }
            case ATTRIBUTE: {
                throw new UnsupportedOperationException();
            }
        }

        throw new IllegalStateException("Current state is not among the states "
                + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + " , "
                + XMLStreamConstantsUtils.getEventName(ATTRIBUTE) + "valid for getAttributeXXX()");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeName(int)
     */
    @Override
    public QName getAttributeName(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.getName() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeNamespace(int)
     */
    @Override
    public String getAttributeNamespace(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.getName().getNamespaceURI() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeLocalName(int)
     */
    @Override
    public String getAttributeLocalName(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.getName().getLocalPart() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributePrefix(int)
     */
    @Override
    public String getAttributePrefix(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.getName().getPrefix() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeType(int)
     */
    @Override
    public String getAttributeType(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.getDTDType() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getAttributeValue(int)
     */
    @Override
    public String getAttributeValue(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.getValue() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#isAttributeSpecified(int)
     */
    @Override
    public boolean isAttributeSpecified(int index) {
        final Attribute attribute = this.getAttribute(index);
        return attribute != null ? attribute.isSpecified() : false;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getNamespaceCount()
     */
    @Override
    public int getNamespaceCount() {
        switch (this.eventType) {
            case START_ELEMENT:
            case END_ELEMENT: {
                return this.namespaces.size();
            }
            case NAMESPACE: {
                throw new UnsupportedOperationException();
            }
        }
        
        throw new IllegalStateException("Current event state is "
                + XMLStreamConstantsUtils.getEventName(this.eventType) + " is not among the states "
                + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + ", "
                + XMLStreamConstantsUtils.getEventName(END_ELEMENT) + ", "
                + XMLStreamConstantsUtils.getEventName(NAMESPACE) + " valid for getNamespaceCount().");
    }
    
    private Namespace getNamespace(int index) {
        switch (this.eventType) {
            case START_ELEMENT:
            case END_ELEMENT: {
                if (index < 0 || index > this.namespaces.size()) {
                    return null;
                }
                
                return this.namespaces.get(index);
            }
            case NAMESPACE: {
                throw new UnsupportedOperationException();
            }
        }

        throw new IllegalStateException("Current state is not among the states "
                + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + " , "
                + XMLStreamConstantsUtils.getEventName(ATTRIBUTE) + "valid for getNamespaceXXX()");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getNamespacePrefix(int)
     */
    @Override
    public String getNamespacePrefix(int index) {
        final Namespace namespace = this.getNamespace(index);
        return namespace != null ? namespace.getPrefix() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI(int)
     */
    @Override
    public String getNamespaceURI(int index) {
        final Namespace namespace = this.getNamespace(index);
        return namespace != null ? namespace.getNamespaceURI() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getNamespaceContext()
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        return this.namespaceContextStack.peek();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getEventType()
     */
    @Override
    public int getEventType() {
        return this.eventType;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getText()
     */
    @Override
    public String getText() {
        if (this.text != null) {
            return this.text;
        }
        
        switch (this.eventType) {
            case CHARACTERS:
            case CDATA: 
            case SPACE: {
                final Characters characters = this.currentEvent.asCharacters();
                this.text = characters.getData();
                break;
            }
            case COMMENT: {
                final Comment comment = (Comment)this.currentEvent;
                this.text = comment.getText();
                break;
            }
            case ENTITY_REFERENCE: {
                //TODO I have know idea if this is correct
                final EntityReference entityReference = (EntityReference)this.currentEvent;
                final EntityDeclaration declaration = entityReference.getDeclaration();
                this.text = declaration.getReplacementText();
                if (this.text == null) {
                    this.text = declaration.getSystemId();
                }
                break;
            }
            case DTD: {
                final javax.xml.stream.events.DTD dtd = (javax.xml.stream.events.DTD)this.currentEvent;
                this.text = dtd.getDocumentTypeDeclaration();
                break;
            }
            default: {
                throw new IllegalStateException("Current state " + XMLStreamConstantsUtils.getEventName(eventType)
                        + " is not among the states" + XMLStreamConstantsUtils.getEventName(CHARACTERS) + ", "
                        + XMLStreamConstantsUtils.getEventName(COMMENT) + ", "
                        + XMLStreamConstantsUtils.getEventName(CDATA) + ", "
                        + XMLStreamConstantsUtils.getEventName(SPACE) + ", "
                        + XMLStreamConstantsUtils.getEventName(ENTITY_REFERENCE) + ", "
                        + XMLStreamConstantsUtils.getEventName(DTD) + " valid for getText() ");
            }
        }
        
        return this.text;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters()
     */
    @Override
    public char[] getTextCharacters() {
        if (this.chars == null) {
            final String text = this.getText();
            this.chars = text != null ? text.toCharArray() : null;
        }
        
        return this.chars;
        
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)
     */
    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        if (target == null) {
            throw new NullPointerException("target char array can't be null");
        }

        if (targetStart < 0 || length < 0 || sourceStart < 0 || targetStart >= target.length || (targetStart + length) > target.length) {
            throw new IndexOutOfBoundsException();
        }
        
        final char[] textCharacters = this.getTextCharacters();
        
        length = Math.min(textCharacters.length - sourceStart, length);
        System.arraycopy(textCharacters, sourceStart, target, targetStart, length);
        
        return length;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getTextStart()
     */
    @Override
    public int getTextStart() {
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getTextLength()
     */
    @Override
    public int getTextLength() {
        final String text = this.getText();
        return text != null ? text.length() : 0;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getEncoding()
     */
    @Override
    public String getEncoding() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#hasText()
     */
    @Override
    public boolean hasText() {
        try {
            return this.getText() != null;
        }
        catch (IllegalStateException e) {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getLocation()
     */
    @Override
    public Location getLocation() {
        final Location location = this.currentEvent.getLocation();
        if (location == null) {
            return UnknownLocation.INSTANCE;
        }
        return location;
    }
    
    private QName getNameSafe() {
        switch (this.eventType) {
            case START_ELEMENT: {
                final StartElement startElement = this.currentEvent.asStartElement();
                return startElement.getName();
            }
            case END_ELEMENT: {
                final EndElement endElement = this.currentEvent.asEndElement();
                return endElement.getName();
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getName()
     */
    @Override
    public QName getName() {
        final QName name = this.getNameSafe();
        if (name != null) {
            return name;
        }
        
        throw new java.lang.IllegalArgumentException("Illegal to call getName() " + "when event type is "
                + XMLStreamConstantsUtils.getEventName(this.eventType) + "." + " Valid states are "
                + XMLStreamConstantsUtils.getEventName(START_ELEMENT) + ", "
                + XMLStreamConstantsUtils.getEventName(END_ELEMENT));
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getLocalName()
     */
    @Override
    public String getLocalName() {
        final QName name = this.getNameSafe();
        if (name != null) {
            return name.getLocalPart();
        }
        
        switch (this.eventType) {
            case PROCESSING_INSTRUCTION: {
                final ProcessingInstruction processingInstruction = (ProcessingInstruction) this.currentEvent;
                return processingInstruction.getTarget();
            }
            case ENTITY_REFERENCE: {
                final EntityReference entityReference = (EntityReference) this.currentEvent;
                return entityReference.getName();
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#hasName()
     */
    @Override
    public boolean hasName() {
        final int eventType = this.eventType;
        return eventType == START_ELEMENT || 
                eventType == END_ELEMENT || 
                eventType == ENTITY_REFERENCE || 
                eventType == PROCESSING_INSTRUCTION;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI()
     */
    @Override
    public String getNamespaceURI() {
        final QName name = this.getNameSafe();
        if (name != null) {
            return name.getNamespaceURI();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getPrefix()
     */
    @Override
    public String getPrefix() {
        final QName name = this.getNameSafe();
        if (name != null) {
            return name.getPrefix();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getVersion()
     */
    @Override
    public String getVersion() {
        return this.startDocument != null ? this.startDocument.getVersion() : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#isStandalone()
     */
    @Override
    public boolean isStandalone() {
        return this.startDocument != null ? this.startDocument.isStandalone() : false;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#standaloneSet()
     */
    @Override
    public boolean standaloneSet() {
        return this.startDocument != null;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getCharacterEncodingScheme()
     */
    @Override
    public String getCharacterEncodingScheme() {
        return this.startDocument != null ? this.startDocument.getCharacterEncodingScheme() : null;
    }
    
    private ProcessingInstruction getProcessingInstruction() {
        if (this.eventType == PROCESSING_INSTRUCTION) {
            return (ProcessingInstruction)this.currentEvent;
        }

        throw new IllegalStateException("Current state of the parser is "
                + XMLStreamConstantsUtils.getEventName(eventType) + " But Expected state is "
                + PROCESSING_INSTRUCTION);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getPITarget()
     */
    @Override
    public String getPITarget() {
        final ProcessingInstruction processingInstruction = this.getProcessingInstruction();
        return processingInstruction.getTarget();

    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamReader#getPIData()
     */
    @Override
    public String getPIData() {
        final ProcessingInstruction processingInstruction = this.getProcessingInstruction();
        return processingInstruction.getData();
    }
}
