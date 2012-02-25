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

package org.jasig.portal.io.xml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.springframework.core.io.Resource;
import org.springframework.oxm.support.SaxResourceUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseXsltDataUpgraderTest {
    
    protected void testXsltUpgrade(
            final Resource xslResource, final PortalDataKey dataKey,
            final Resource inputResource, final Resource expectedResultResource) throws Exception {
        this.testXsltUpgrade(xslResource, dataKey, inputResource, expectedResultResource, null);
    }
    
    protected void testXsltUpgrade(
            final Resource xslResource, final PortalDataKey dataKey,
            final Resource inputResource, final Resource expectedResultResource,
            final Resource xsdResource) throws Exception {

        final XmlUtilities xmlUtilities = new XmlUtilitiesImpl() {
            @Override
            public Templates getTemplates(Resource stylesheet) throws TransformerConfigurationException, IOException {
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                return transformerFactory.newTemplates(new StreamSource(stylesheet.getInputStream()));
            }
        };
        
        final XsltDataUpgrader xsltDataUpgrader = new XsltDataUpgrader();
        xsltDataUpgrader.setPortalDataKey(dataKey);
        xsltDataUpgrader.setXslResource(xslResource);
        xsltDataUpgrader.setXmlUtilities(xmlUtilities);
        xsltDataUpgrader.afterPropertiesSet();
        
        
        //Create XmlEventReader (what the JaxbPortalDataHandlerService has)
        final XMLInputFactory xmlInputFactory = xmlUtilities.getXmlInputFactory();
        final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(inputResource.getInputStream());
        final Node sourceNode = xmlUtilities.convertToDom(xmlEventReader);
        final DOMSource source = new DOMSource(sourceNode);
        
        final DOMResult result = new DOMResult();
        xsltDataUpgrader.upgradeData(source, result);

        //XSD Validation
        final String resultString = XmlUtilitiesImpl.toString(result.getNode());
        if (xsdResource != null) {
            final Schema schema = this.loadSchema(new Resource[] { xsdResource }, XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Validator validator = schema.newValidator();
            try {
                validator.validate(new StreamSource(new StringReader(resultString)));
            }
            catch (Exception e) {
                throw new XmlTestException("Failed to validate XSLT output against provided XSD", resultString, e);
            }
        }

        XMLUnit.setIgnoreWhitespace(true);
        try {
            Diff d = new Diff(new InputStreamReader(expectedResultResource.getInputStream()), new StringReader(resultString));
            assertTrue("Upgraded data doesn't match expected data: " + d, d.similar());
        }
        catch (Exception e) {
            throw new XmlTestException("Failed to assert similar between XSLT output and expected XML", resultString, e);
        }
        catch (Error e) {
            throw new XmlTestException("Failed to assert similar between XSLT output and expected XML", resultString, e);
        }
    }
    
    private Schema loadSchema(Resource[] resources, String schemaLanguage) throws IOException, SAXException {
        Assert.notEmpty(resources, "No resources given");
        Assert.hasLength(schemaLanguage, "No schema language provided");
        Source[] schemaSources = new Source[resources.length];
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        for (int i = 0; i < resources.length; i++) {
            Assert.notNull(resources[i], "Resource is null");
            Assert.isTrue(resources[i].exists(), "Resource " + resources[i] + " does not exist");
            InputSource inputSource = SaxResourceUtils.createInputSource(resources[i]);
            schemaSources[i] = new SAXSource(xmlReader, inputSource);
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
        return schemaFactory.newSchema(schemaSources);
    }
}
