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

package org.jasig.portal.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XmlUtilitiesImplTest {
    @Test
    public void testGetUniqueXPath() throws Exception {
        final Document testDoc = loadTestDocument();
        
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        final XPathExpression xPathExpression = xPath.compile("//*[@ID='11']");
        
        final Node node = (Node)xPathExpression.evaluate(testDoc, XPathConstants.NODE);
        
        final XmlUtilitiesImpl xmlUtilities = new XmlUtilitiesImpl();
        final String nodePath = xmlUtilities.getUniqueXPath(node);
        
        assertEquals("/layout/folder[2]/folder[3]", nodePath);
    }

    private Document loadTestDocument() throws IOException, SAXException {
        final InputStream testDocStream = this.getClass().getResourceAsStream("/org/jasig/portal/xml/xmlUtilitiesTest.xml");
        final Document testDoc = DocumentFactory.getDocumentFromStream(testDocStream, "xmlUtilitiesTest.xml");
        IOUtils.closeQuietly(testDocStream);
        return testDoc;
    }
}
