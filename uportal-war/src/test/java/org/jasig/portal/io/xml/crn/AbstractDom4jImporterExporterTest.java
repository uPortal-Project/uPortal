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
package org.jasig.portal.io.xml.crn;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.Element;
import org.jasig.portal.io.xml.XmlTestException;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.junit.Test;

public class AbstractDom4jImporterExporterTest {
    
    @Test
    public void testDom4jRoundTripWithComment() throws Exception {
        final TestDom4jImporter importer = new TestDom4jImporter();
        final TestDom4jExporter exporter = new TestDom4jExporter();
        exporter.setXmlUtilities(new XmlUtilitiesImpl());
        
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        final InputStream resource = this.getClass().getResourceAsStream("/org/jasig/portal/io/xml/crn/pilot-lo.fragment-layout.xml");
        final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(resource);
        final Tuple<String, Element> result = importer.unmarshal(new StAXSource(xmlEventReader));
        
        assertNotNull(result);
        
        final StringWriter writer = new StringWriter();
        exporter.marshal(result, new StreamResult(writer));
        
        final String marshalResult = writer.toString();
        
        assertNotNull(marshalResult);
        
        XMLUnit.setIgnoreWhitespace(true);
        try {
            Diff d = new Diff(new InputStreamReader(this.getClass().getResourceAsStream("/org/jasig/portal/io/xml/crn/pilot-lo.fragment-layout.xml")), new StringReader(marshalResult));
            assertTrue("Upgraded data doesn't match expected data: " + d, d.similar());
        }
        catch (Exception e) {
            throw new XmlTestException("Failed to assert similar between marshall output and expected XML", marshalResult, e);
        }
        catch (Error e) {
            throw new XmlTestException("Failed to assert similar between marshall output and expected XML", marshalResult, e);
        }
    }
    
    @Test
    public void testDom4jCommentFiltering() throws Exception {
        final TestDom4jImporter importer = new TestDom4jImporter();
        final TestDom4jExporter exporter = new TestDom4jExporter();
        exporter.setXmlUtilities(new XmlUtilitiesImpl());
        
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        final InputStream resource = this.getClass().getResourceAsStream("/org/jasig/portal/io/xml/crn/pilot-lo.fragment-layout.xml");
        final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(resource);
        final Tuple<String, Element> result = importer.unmarshal(new StAXSource(xmlEventReader));
        
        assertNotNull(result);
        
        final Element document = result.getSecond();
        final List<org.dom4j.Node> comments = document.selectNodes("//comment()");
        for (final org.dom4j.Node comment : comments) {
            comment.detach();
        }
        
        final StringWriter writer = new StringWriter();
        exporter.marshal(result, new StreamResult(writer));
        
        final String marshalResult = writer.toString();
        
        assertNotNull(marshalResult);
        
        XMLUnit.setIgnoreWhitespace(true);
        try {
            Diff d = new Diff(new InputStreamReader(this.getClass().getResourceAsStream("/org/jasig/portal/io/xml/crn/filtered-pilot-lo.fragment-layout.xml")), new StringReader(marshalResult));
            assertTrue("Upgraded data doesn't match expected data: " + d, d.similar());
        }
        catch (Exception e) {
            throw new XmlTestException("Failed to assert similar between marshall output and expected XML", marshalResult, e);
        }
        catch (Error e) {
            throw new XmlTestException("Failed to assert similar between marshall output and expected XML", marshalResult, e);
        }
    }

    private static class TestDom4jImporter extends AbstractDom4jImporter {
        @Override
        protected void importDataElement(Tuple<String, Element> data) {
        }
    }

    private static class TestDom4jExporter extends AbstractDom4jExporter {
        @Override
        protected Element exportDataElement(String id) {
            return null;
        }
    }
}
