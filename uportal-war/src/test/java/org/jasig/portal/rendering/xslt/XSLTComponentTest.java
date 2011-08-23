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

package org.jasig.portal.rendering.xslt;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.jasig.portal.io.xml.XmlTestException;
import org.jasig.portal.rendering.PipelineEventReader;
import org.jasig.portal.rendering.PipelineEventReaderImpl;
import org.jasig.portal.rendering.StAXPipelineComponent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test XSLT when going from DOM -> StAX -> XSLT -> StAX
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XSLTComponentTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testXSLTComponent() throws Exception {
        final MockHttpServletRequest mockReq = new MockHttpServletRequest();
        final MockHttpServletResponse mockRes = new MockHttpServletResponse();

        final XMLEventReader xmlEventReader = this.getXmlEventReader("juser.xml");
        final PipelineEventReaderImpl<XMLEventReader, XMLEvent> cacheableEventReader = new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(xmlEventReader);
        
        final Transformer transformer = this.getTransformer("columns.xsl");

        final StAXPipelineComponent targetComponent = EasyMock.createMock(StAXPipelineComponent.class);
        final TransformerSource transformerSource = EasyMock.createMock(TransformerSource.class);
        
        EasyMock.expect(targetComponent.getEventReader(mockReq, mockRes)).andReturn(cacheableEventReader);
        EasyMock.expect(transformerSource.getTransformer(mockReq, mockRes)).andReturn(transformer);
        
        EasyMock.replay(targetComponent, transformerSource);
        
        final XSLTComponent xsltComponent = new XSLTComponent();
        xsltComponent.setWrappedComponent(targetComponent);
        xsltComponent.setTransformerSource(transformerSource);
        
        final PipelineEventReader<XMLEventReader, XMLEvent> eventReader = xsltComponent.getEventReader(mockReq, mockRes);
        
        Assert.assertNotNull(eventReader);
        
        final String output = this.serializeXMLEventReader(eventReader.getEventReader());
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        final DocumentBuilder db = dbf.newDocumentBuilder();
        
        XMLUnit.setIgnoreWhitespace(true);
        try {
            final Document expected = db.parse(this.getClass().getResourceAsStream("/org/jasig/portal/rendering/xslt/expected.xml"), "/org/jasig/portal/rendering/xslt/expected.xml");
            final Document actual = db.parse(new InputSource(new StringReader(output)));
            
            Diff d = new Diff(expected, actual);
            assertTrue("Upgraded data doesn't match expected data: " + d, d.similar());
        }
        catch (Exception e) {
            throw new XmlTestException("Failed to assert similar between XSLT output and expected XML", output, e);
        }
        catch (Error e) {
            throw new XmlTestException("Failed to assert similar between XSLT output and expected XML", output, e);
        }
        
        EasyMock.verify(targetComponent, transformerSource);
    }
    
    protected String serializeXMLEventReader(XMLEventReader reader) {
        final StringWriter writer = new StringWriter();
        
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        final XMLEventWriter xmlEventWriter;
        try {
            xmlEventWriter = outputFactory.createXMLEventWriter(writer);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLEventWriter", e);
        }
        
        try {
            xmlEventWriter.setDefaultNamespace("http://www.w3.org/1999/xhtml");
            xmlEventWriter.add(reader);
            xmlEventWriter.flush();
            xmlEventWriter.close();
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write events to Writer", e);
        }
        
        return writer.toString();
    }
    
    protected XMLEventReader getXmlEventReader(String file) throws SAXException, IOException, ParserConfigurationException {
        final InputStream xmlStream = this.getClass().getResourceAsStream(file);
        
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        
        final Document document = documentBuilder.parse(xmlStream);
        
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        
        final XMLEventReader xmlEventReader;
        try {
            xmlEventReader = inputFactory.createXMLEventReader(new DOMSource(document, "foobar"));
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLStreamReader for file '" + file + "'", e);
        }
        
        return xmlEventReader;
    }
    
    protected Transformer getTransformer(String file) {
        final InputStream stylesheetStream = this.getClass().getResourceAsStream(file);;
        
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            return transformerFactory.newTransformer(new StreamSource(stylesheetStream));
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to create Transformer for stylesheet: " + file, e);
        }
    }
}
