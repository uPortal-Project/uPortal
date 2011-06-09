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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.jasig.portal.io.xml.user.ExternalUser;
import org.jasig.portal.io.xml.user.UserPortalDataType;
import org.jasig.portal.xml.XmlUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class JaxbDataImportExportServiceTest {
    @InjectMocks private JaxbDataImportExportService dataImportExportService = new JaxbDataImportExportService();
    @Mock private XmlUtilities xmlUtilities;
    @Mock private ResourceLoader resourceLoader;
    
    @Test
    public void testUpgradeThenImport() throws Exception {
        final ClassPathResource importDataResource = new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-2.user.xml");
        when(resourceLoader.getResource("classpath:/org/jasig/portal/io/xml/user/test_3-2.user.xml")).thenReturn(importDataResource);
        
        when(xmlUtilities.getXmlInputFactory()).thenReturn(XMLInputFactory.newFactory());
        
        final ClassPathResource xslResource = new ClassPathResource("/org/jasig/portal/io/xml/user/upgrade-user_3-2.xsl");
        final IDataUpgrader xsltDataUpgrader = createXsltDataUpgrader(xslResource, UserPortalDataType.IMPORT_32_DATA_KEY);
        dataImportExportService.setDataUpgraders(Arrays.asList(xsltDataUpgrader));
        
        final Jaxb2Marshaller userJaxb2Marshaller = new Jaxb2Marshaller();
        userJaxb2Marshaller.setContextPath("org.jasig.portal.io.xml.user");
        userJaxb2Marshaller.afterPropertiesSet();
        
        final IDataImporterExporter<ExternalUser> userDataImporterExporter = mock(IDataImporterExporter.class);
        when(userDataImporterExporter.getImportDataKey()).thenReturn(UserPortalDataType.IMPORT_40_DATA_KEY);
        when(userDataImporterExporter.getPortalDataType()).thenReturn(UserPortalDataType.INSTANCE);
        when(userDataImporterExporter.getUnmarshaller()).thenReturn(userJaxb2Marshaller);
        
        Collection<IDataImporterExporter<?>> dataImporters = new LinkedList<IDataImporterExporter<?>>();
        dataImporters.add(userDataImporterExporter);
        dataImportExportService.setDataImporters(dataImporters);
        
        dataImportExportService.importData("classpath:/org/jasig/portal/io/xml/user/test_3-2.user.xml");
        
        final ArgumentCaptor<ExternalUser> userArgumentCaptor = ArgumentCaptor.forClass(ExternalUser.class);
        verify(userDataImporterExporter).importData(userArgumentCaptor.capture());
        final ExternalUser externalUser = userArgumentCaptor.getValue();
        assertNotNull(externalUser);
        assertEquals("student", externalUser.getUsername());
        assertEquals("defaultTemplateUser", externalUser.getDefaultUser());
        assertEquals("(MD5)mhmjKvf2F3gPizS9DrA+CsFmqj74oTSb", externalUser.getPassword());
        assertNull(externalUser.getLastPasswordChange());
    }

    protected IDataUpgrader createXsltDataUpgrader(final ClassPathResource xslResource, final PortalDataKey dataKey) throws Exception {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Templates templates = transformerFactory.newTemplates(new StreamSource(xslResource.getInputStream()));

        when(xmlUtilities.getTemplates(xslResource)).thenReturn(templates);
        
        final XsltDataUpgrader xsltDataUpgrader = new XsltDataUpgrader();
        xsltDataUpgrader.setPortalDataKey(dataKey);
        xsltDataUpgrader.setXslResource(xslResource);
        xsltDataUpgrader.setXmlUtilities(xmlUtilities);
        xsltDataUpgrader.afterPropertiesSet();
        
        return xsltDataUpgrader;
    }
}
