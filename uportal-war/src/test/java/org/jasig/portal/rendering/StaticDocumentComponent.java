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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Pipeline component that reads in a XML file into a Document then creates a XMLEventReader from that. This
 * is meant to simulate what currently happens in {@link DistributedLayoutManager#getUserLayoutReader()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaticDocumentComponent implements StAXPipelineComponent {
    private Resource document;
    
    public void setDocument(Resource document) {
        this.document = document;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return CacheKey.build(StaticDocumentComponent.class.getName(), this.document.getDescription());
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        
        final InputStream documentStream;
        try {
            documentStream = this.document.getInputStream();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        final Document document;
        try {
            document = documentBuilder.parse(documentStream);
        }
        catch (SAXException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(documentStream);
        }

        final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        
        final DOMSource source = new DOMSource(document);
        final XMLEventReader streamReader;
        try {
            streamReader = inputFactory.createXMLEventReader(source);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(streamReader);
    }
}
