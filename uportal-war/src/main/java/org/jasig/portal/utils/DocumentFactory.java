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
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Produces an empty Document implementation
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public final class DocumentFactory {

    private static final Log log = LogFactory.getLog(DocumentFactory.class);

    protected static final SingletonDoubleCheckedCreator<DocumentBuilderFactory> documentBuilderFactoryInstance = new DocumentBuilderFactoryCreator();
    
    protected static final ThreadLocal<DocumentBuilder> localDocumentBuilder = new DocumentBuilderLocal();
    

    /**
     * Returns a new copy of a Document implementation. This will
     * return an <code>IPortalDocument</code> implementation.
     * @return an empty org.w3c.dom.Document implementation
     */
    public static Document getThreadDocument() {
        return getThreadDocumentBuilder().newDocument();
    }

    public static Document getDocumentFromStream(InputStream stream, String publicId) throws IOException, SAXException {
        DocumentBuilder builder = getThreadDocumentBuilder();
        InputSource source = new InputSource(stream);
        source.setPublicId(publicId);
        Document doc = builder.parse(source);
        return doc;
    }

    public static Document getDocumentFromStream(InputStream stream, EntityResolver er, String publicId)
            throws IOException, SAXException {
        DocumentBuilder builder = getThreadDocumentBuilder();
        builder.setEntityResolver(er);
        InputSource source = new InputSource(stream);
        source.setPublicId(publicId);
        Document doc = builder.parse(source);
        return doc;
    }

    /**
     * @return The DocumentBuilder for the current thread. The returned references should NEVER be retained outside of the stack.
     */
    public static DocumentBuilder getThreadDocumentBuilder() {
        return localDocumentBuilder.get();
    }
    
    private static final class DocumentBuilderFactoryCreator extends
            SingletonDoubleCheckedCreator<DocumentBuilderFactory> {
        @Override
        protected DocumentBuilderFactory createSingleton(Object... args) {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setValidating(false);
            
            return dbFactory;
        }
    }

    private static final class DocumentBuilderLocal extends ThreadLocal<DocumentBuilder> {
        @Override
        protected DocumentBuilder initialValue() {
            final DocumentBuilderFactory documentBuilderFactory = documentBuilderFactoryInstance.get();
            try {
                return documentBuilderFactory.newDocumentBuilder();
            }
            catch (ParserConfigurationException e) {
                throw new IllegalStateException("Failed to create new DocumentBuilder for thread: " + Thread.currentThread().getName(), e);
            }
        }

        @Override
        public DocumentBuilder get() {
            DocumentBuilder documentBuilder = super.get();
            
            //Handle a DocumentBuilder not getting created correctly
            if (documentBuilder == null) {
                log.warn("No DocumentBuilder existed for this thread, an initialValue() call must have failed");
                documentBuilder = this.initialValue();
                this.set(documentBuilder);
            }
            
            return documentBuilder;
        }
    }
}
