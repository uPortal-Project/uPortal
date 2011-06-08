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

package org.jasig.portal.io.xml.user;

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
import org.jasig.portal.io.xml.XsltDataUpgrader;
import org.jasig.portal.xml.XmlUtilities;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UserDataUpgradeTest {
    @Test
    public void testUpgrade32to40() throws Exception {
        final ClassPathResource xslResource = new ClassPathResource("/org/jasig/portal/io/xml/user/upgrade-user_3-2.xsl");
        
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Templates templates = transformerFactory.newTemplates(new StreamSource(xslResource.getInputStream()));

        final XmlUtilities xmlUtilities = mock(XmlUtilities.class);
        when(xmlUtilities.getTemplates(xslResource)).thenReturn(templates);
        
        final XsltDataUpgrader xsltDataUpgrader = new XsltDataUpgrader();
        xsltDataUpgrader.setPortalDataKey(UserPortalDataType.IMPORT_32_DATA_KEY);
        xsltDataUpgrader.setXslResource(xslResource);
        xsltDataUpgrader.setXmlUtilities(xmlUtilities);
        xsltDataUpgrader.afterPropertiesSet();
        
        final ClassPathResource user32Resource = new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-2.user.xml");
        
        final StringWriter result = new StringWriter();
        xsltDataUpgrader.upgradeData(new StreamSource(user32Resource.getInputStream()), new StreamResult(result));
        
        final ClassPathResource expectedResultResource = new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-2_to_4-0_expected.user.xml");
        
        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(new InputStreamReader(expectedResultResource.getInputStream()), new StringReader(result.toString()));
        assertTrue("Upgraded user data doesn't match expected user data: " + d, d.similar());
    }
}
