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
