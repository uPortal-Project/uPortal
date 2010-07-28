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

package org.jasig.portal.url.xml;

import org.apache.xalan.transformer.TransformerImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * {@link ContentHandler} that builds a single String from one or more calls to {@link #characters(char[], int, int)}.
 * All methods except {@link #startDocument()}, {@link #characters(char[], int, int)}, and {@link #endDocument()} throw
 * {@link UnsupportedOperationException}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StringBuildingContentHandler implements ContentHandler {
    private final TransformerImpl transformer;
    private StringBuilder builder = new StringBuilder();
    
    public StringBuildingContentHandler(TransformerImpl transformer) {
        this.transformer = transformer;
    }

    /**
     * Returns the buffered string from the calls to {@link #characters(char[], int, int)} since {@link #startDocument()}
     */
    @Override
    public String toString() {
        return this.builder.toString();
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        this.builder = new StringBuilder();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.builder.append(ch, start, length);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        //Ignore
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void endPrefixMapping(String prefix) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void processingInstruction(String target, String data) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void setDocumentLocator(Locator locator) {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void skippedEntity(String name) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        final UnsupportedOperationException e = new UnsupportedOperationException("Only startDocument, characters, and endDocument are supported");
        this.transformer.setExceptionThrown(e);
        throw e;
    }
}
