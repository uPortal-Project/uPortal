/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class DebugCachingSerializer implements CachingSerializer {
    
    private StringBuffer cache = new StringBuffer();

    public void flush() throws IOException {
    }

    public String getCache() throws UnsupportedEncodingException, IOException {
        return cache.toString();
    }

    public void printRawCharacters(String text) throws IOException {
        this.cache.append(text);
    }

    public void setDocumentStarted(boolean setting) {
    }

    public boolean startCaching() throws IOException {
        return false;
    }

    public boolean stopCaching() throws IOException {
        return false;
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void endElement(String arg0, String arg1, String arg2)
        throws SAXException {
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
        throws SAXException {
    }

    public void processingInstruction(String arg0, String arg1)
        throws SAXException {
    }

    public void setDocumentLocator(Locator arg0) {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String arg0, String arg1, String arg2,
        Attributes arg3) throws SAXException {
    }

    public void startPrefixMapping(String arg0, String arg1)
        throws SAXException {
    }

    public void endElement(String arg0) throws SAXException {
    }

    public void startElement(String arg0, AttributeList arg1)
        throws SAXException {
    }

    public void comment(char[] arg0, int arg1, int arg2) throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String arg0) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String arg0, String arg1, String arg2)
        throws SAXException {
    }

    public void startEntity(String arg0) throws SAXException {
    }

    public void notationDecl(String arg0, String arg1, String arg2)
        throws SAXException {
    }

    public void unparsedEntityDecl(String arg0, String arg1, String arg2,
        String arg3) throws SAXException {
    }

    public void attributeDecl(String arg0, String arg1, String arg2,
        String arg3, String arg4) throws SAXException {
    }

    public void elementDecl(String arg0, String arg1) throws SAXException {
    }

    public void externalEntityDecl(String arg0, String arg1, String arg2)
        throws SAXException {
    }

    public void internalEntityDecl(String arg0, String arg1)
        throws SAXException {
    }

    public void serialize(Element elem) throws IOException {
    }

    public void serialize(Document doc) throws IOException {
    }

    public void serialize(DocumentFragment frag) throws IOException {
    }

    public ContentHandler asContentHandler() throws IOException {
        return null;
    }

    public DOMSerializer asDOMSerializer() throws IOException {
        return null;
    }

    public DocumentHandler asDocumentHandler() throws IOException {
        return null;
    }

    public void setOutputByteStream(OutputStream output) {
    }

    public void setOutputCharStream(Writer output) {
    }

    public void setOutputFormat(OutputFormat format) {
    }

    public void startAnchoring(String anchorId) {
    }

    public void stopAnchoring() {
    }

}
