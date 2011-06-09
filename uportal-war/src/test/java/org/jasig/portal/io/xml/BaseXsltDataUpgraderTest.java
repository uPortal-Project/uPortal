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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.xml.XmlUtilities;
import org.springframework.core.io.Resource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseXsltDataUpgraderTest {
    
    protected void testXsltUpgrade(
            final Resource xslResource, final PortalDataKey dataKey,
            final Resource inputResource, final Resource expectedResultResource) throws Exception {

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Templates templates = transformerFactory.newTemplates(new StreamSource(xslResource.getInputStream()));

        final XmlUtilities xmlUtilities = mock(XmlUtilities.class);
        when(xmlUtilities.getTemplates(xslResource)).thenReturn(templates);
        
        final XsltDataUpgrader xsltDataUpgrader = new XsltDataUpgrader();
        xsltDataUpgrader.setPortalDataKey(dataKey);
        xsltDataUpgrader.setXslResource(xslResource);
        xsltDataUpgrader.setXmlUtilities(xmlUtilities);
        xsltDataUpgrader.afterPropertiesSet();
        
        final StringWriter result = new StringWriter();
        xsltDataUpgrader.upgradeData(new StreamSource(inputResource.getInputStream()), new StreamResult(result));
        
        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(new InputStreamReader(expectedResultResource.getInputStream()), new StringReader(result.toString()));
        assertTrue("Upgraded data doesn't match expected data: " + d, d.similar());
    }
}
