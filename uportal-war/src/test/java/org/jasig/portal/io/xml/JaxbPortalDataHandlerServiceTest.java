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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.jasig.portal.io.xml.user.ExternalUser;
import org.jasig.portal.io.xml.user.UserPortalDataType;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class JaxbPortalDataHandlerServiceTest {
    @InjectMocks private JaxbPortalDataHandlerService dataImportExportService = new JaxbPortalDataHandlerService();
    private XmlUtilities xmlUtilities;
    @Mock private ResourceLoader resourceLoader;
    private ExecutorService threadPoolExecutor;
    
    @Before
    public void setup() throws Exception {
        xmlUtilities = new XmlUtilitiesImpl() {
            @Override
            public Templates getTemplates(Resource stylesheet) throws TransformerConfigurationException, IOException {
                final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                return transformerFactory.newTemplates(new StreamSource(stylesheet.getInputStream()));
            }
        };
        
        dataImportExportService.setXmlUtilities(xmlUtilities);
        
        final ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean = new ThreadPoolExecutorFactoryBean();
        threadPoolExecutorFactoryBean.setCorePoolSize(0);
        threadPoolExecutorFactoryBean.setMaxPoolSize(20);
        threadPoolExecutorFactoryBean.setQueueCapacity(20);
        threadPoolExecutorFactoryBean.setThreadGroupName("uPortal-ImportExportThreadGroup");
        threadPoolExecutorFactoryBean.setThreadNamePrefix("uPortal-ImportExport-");
        threadPoolExecutorFactoryBean.setThreadPriority(5);
        threadPoolExecutorFactoryBean.setKeepAliveSeconds(30);
        threadPoolExecutorFactoryBean.setDaemon(true);
        threadPoolExecutorFactoryBean.setAllowCoreThreadTimeOut(true);
        threadPoolExecutorFactoryBean.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolExecutorFactoryBean.afterPropertiesSet();
        
        threadPoolExecutor = threadPoolExecutorFactoryBean.getObject();
        this.dataImportExportService.setImportExportThreadPool(threadPoolExecutor);
        
        dataImportExportService.setDataFileIncludes(ImmutableSet.of(
                "**/*.xml",
                "**/*.entity-type",
                "**/*.template-user",
                "**/*.user",
                "**/*.group",
                "**/*.group_membership",
                "**/*.membership",
                "**/*.portlet-type",
                "**/*.channel-type",
                "**/*.portlet",
                "**/*.channel",
                "**/*.permission",
                "**/*.permission_set",
                "**/*.permission_owner",
                "**/*.profile",
                "**/*.fragment-layout",
                "**/*.layout",
                "**/*.fragment-definition"
        ));
        
        dataImportExportService.setDataTypeImportOrder(Arrays.<IPortalDataType>asList(
            new org.jasig.portal.io.xml.entitytype.EntityTypePortalDataType(),
            new org.jasig.portal.io.xml.ssd.StylesheetDescriptorPortalDataType(),
            new org.jasig.portal.io.xml.user.UserPortalDataType(),
            new org.jasig.portal.io.xml.group.GroupPortalDataType(),
            new org.jasig.portal.io.xml.group.GroupMembershipPortalDataType(),
            new org.jasig.portal.io.xml.group.MembershipPortalDataType(),
            new org.jasig.portal.io.xml.portlettype.PortletTypePortalDataType(),
            new org.jasig.portal.io.xml.portlet.PortletPortalDataType(),
            new org.jasig.portal.io.xml.permission.PermissionPortalDataType(),
            new org.jasig.portal.io.xml.permission.PermissionSetPortalDataType(),
            new org.jasig.portal.io.xml.permission.PermissionOwnerPortalDataType(),
            new org.jasig.portal.io.xml.layout.ProfilePortalDataType(),
            new org.jasig.portal.io.xml.layout.LayoutPortalDataType(),
            new org.jasig.portal.io.xml.dlm.FragmentDefinitionPortalDataType()
        ));
    }
    
    @After
    public void teardown() {
        threadPoolExecutor.shutdown();
    }
    
    @Test
    @Ignore
    public void testBatchImport() throws Exception {
        when(xmlUtilities.getXmlInputFactory()).thenReturn(XMLInputFactory.newFactory());
        
        final File dataDir = new File("/Users/edalquist/java/workspace/uPortal_trunk/uportal-war/src/main/data");
        dataImportExportService.importData(dataDir, null, null);
        
        //TODO how to test this since an importer needs to be registered?
    }
    
    
    @Test
    public void testUpgradeThenImport() throws Exception {
        final ClassPathResource importDataResource = new ClassPathResource("/org/jasig/portal/io/xml/user/test_3-2.user.xml");
        when(resourceLoader.getResource("classpath:/org/jasig/portal/io/xml/user/test_3-2.user.xml")).thenReturn(importDataResource);
        
        final ClassPathResource xslResource = new ClassPathResource("/org/jasig/portal/io/xml/user/upgrade-user_3-2.xsl");
        final IDataUpgrader xsltDataUpgrader = createXsltDataUpgrader(xslResource, UserPortalDataType.IMPORT_32_DATA_KEY);
        dataImportExportService.setDataUpgraders(Arrays.asList(xsltDataUpgrader));
        
        final Jaxb2Marshaller userJaxb2Marshaller = new Jaxb2Marshaller();
        userJaxb2Marshaller.setContextPath("org.jasig.portal.io.xml.user");
        userJaxb2Marshaller.afterPropertiesSet();
        
        final IDataImporter<ExternalUser> userDataImporter = mock(IDataImporter.class);
        when(userDataImporter.getImportDataKeys()).thenReturn(Collections.singleton(UserPortalDataType.IMPORT_40_DATA_KEY));
        when(userDataImporter.getUnmarshaller()).thenReturn(userJaxb2Marshaller);
        
        Collection<IDataImporter<?>> dataImporters = new LinkedList<IDataImporter<?>>();
        dataImporters.add(userDataImporter);
        dataImportExportService.setDataImporters(dataImporters);
        
        dataImportExportService.importData("classpath:/org/jasig/portal/io/xml/user/test_3-2.user.xml");
        
        final ArgumentCaptor<ExternalUser> userArgumentCaptor = ArgumentCaptor.forClass(ExternalUser.class);
        verify(userDataImporter).importData(userArgumentCaptor.capture());
        final ExternalUser externalUser = userArgumentCaptor.getValue();
        assertNotNull(externalUser);
        assertEquals("student", externalUser.getUsername());
        assertEquals("defaultTemplateUser", externalUser.getDefaultUser());
        assertEquals("(MD5)mhmjKvf2F3gPizS9DrA+CsFmqj74oTSb", externalUser.getPassword());
        assertNull(externalUser.getLastPasswordChange());
    }

    protected IDataUpgrader createXsltDataUpgrader(final ClassPathResource xslResource, final PortalDataKey dataKey) throws Exception {
        final XsltDataUpgrader xsltDataUpgrader = new XsltDataUpgrader();
        xsltDataUpgrader.setPortalDataKey(dataKey);
        xsltDataUpgrader.setXslResource(xslResource);
        xsltDataUpgrader.setXmlUtilities(xmlUtilities);
        xsltDataUpgrader.afterPropertiesSet();
        
        return xsltDataUpgrader;
    }
}
