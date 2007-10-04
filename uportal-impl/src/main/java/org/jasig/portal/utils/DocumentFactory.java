/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Produces an empty Document implementation
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class DocumentFactory {

    private static final Log log = LogFactory.getLog(DocumentFactory.class);

    protected static DocumentFactory _instance;
    protected static final LocalDocumentBuilder localDocBuilder = new LocalDocumentBuilder();
    protected DocumentBuilderFactory dbFactory = null;

    protected static synchronized DocumentFactory instance() {
        if (_instance == null) {
            _instance = new DocumentFactory();
        }
        return _instance;
    }

    protected DocumentFactory() {

        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setValidating(false);
        } catch (Exception e) {
            log.error( "DocumentFactory: unable to initialize DocumentBuilderFactory", e);
        }
    }

    /**
     * Returns a new copy of a Document implementation. This will
     * return an <code>IPortalDocument</code> implementation.
     * @return an empty org.w3c.dom.Document implementation
     */
    public static Document getNewDocument() {
        return newDocumentBuilder().newDocument();
    }

    /**
     * Returns a new copy of a Document implementation.
     * @return an empty org.w3c.dom.Document implementation
     */
    static Document __getNewDocument() {
        Document doc = newDocumentBuilder().newDocument();
        return doc;
    }

public static Document getDocumentFromStream(InputStream stream, String publicId) throws IOException, SAXException {
    DocumentBuilder builder = newDocumentBuilder();
    InputSource source = new InputSource(stream);
    source.setPublicId(publicId);
    Document doc = builder.parse(source);
    return doc;
}

public static Document getDocumentFromStream(InputStream stream, EntityResolver er, String publicId) throws IOException, SAXException {
    DocumentBuilder builder = newDocumentBuilder();
    builder.setEntityResolver(er);
    InputSource source = new InputSource(stream);
    source.setPublicId(publicId);
    Document doc = builder.parse(source);
    return doc;
}

    public static DocumentBuilder newDocumentBuilder() {
        DocumentBuilder builder = (DocumentBuilder)localDocBuilder.get();
        return builder;
    }

    protected static class LocalDocumentBuilder extends ThreadLocal {
        protected Object initialValue() {
            Object r = null;
            try {
                r = instance().dbFactory.newDocumentBuilder();
            } catch (Exception e) {
                log.error(e, e);
            }
            return r;
        }
    }
}
