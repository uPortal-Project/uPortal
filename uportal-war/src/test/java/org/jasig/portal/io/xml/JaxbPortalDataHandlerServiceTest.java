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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.transform.Source;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    
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
        
        dataImportExportService.setDataTypeImportOrder(getPortalDataTypes());
    }

    protected List<IPortalDataType> getPortalDataTypes() {
        return ImmutableList.<IPortalDataType>of(
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
        );
    }
    
    private interface MockDataImporterSetup {
        void setup(IPortalDataType dataType, IDataImporter<? extends Object> dataImporter);
    }
    
    protected List<IDataImporter<? extends Object>> setupAllImporters(MockDataImporterSetup setupCallback) {
        final Builder<IDataImporter<? extends Object>> importersBuilder = ImmutableList.<IDataImporter<? extends Object>>builder();

        for (final IPortalDataType portalDataType : getPortalDataTypes()) {

            final IDataImporter importer = mock(IDataImporter.class);
            when(importer.getImportDataKeys()).thenReturn(new HashSet<PortalDataKey>(portalDataType.getDataKeyImportOrder()));
            if (setupCallback != null) {
                setupCallback.setup(portalDataType, importer);
            }
            importersBuilder.add(importer);
        }
        
        return importersBuilder.build();
    }
    
    @After
    public void teardown() {
        threadPoolExecutor.shutdown();
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
    
    @Test
    public void testImportJarArchive() throws Exception {
        final Unmarshaller unmarshaller = mock(Unmarshaller.class);
        
        final List<IDataImporter<? extends Object>> importers = setupAllImporters(new MockDataImporterSetup() {
            @Override
            public void setup(IPortalDataType dataType, IDataImporter<? extends Object> dataImporter) {
                when(dataImporter.getUnmarshaller()).thenReturn(unmarshaller);
            }
        });
        
        this.dataImportExportService.setDataImporters(importers);
        
        final Resource archiveResource = new ClassPathResource("/org/jasig/portal/io/xml/import_archive.jar");
        
        final IPortalDataHandlerService.BatchImportOptions options = new IPortalDataHandlerService.BatchImportOptions();
        options.setLogDirectoryParent(tempFolder.newFolder("jarArchiveImport"));
        
        this.dataImportExportService.importDataArchive(archiveResource, options);
        
        verify(unmarshaller, times(16)).unmarshal(any(Source.class));
    }
    
    @Test
    public void testImportZipArchive() throws Exception {
        final Unmarshaller unmarshaller = mock(Unmarshaller.class);
        
        final List<IDataImporter<? extends Object>> importers = setupAllImporters(new MockDataImporterSetup() {
            @Override
            public void setup(IPortalDataType dataType, IDataImporter<? extends Object> dataImporter) {
                when(dataImporter.getUnmarshaller()).thenReturn(unmarshaller);
            }
        });
        
        this.dataImportExportService.setDataImporters(importers);
        
        final Resource archiveResource = new ClassPathResource("/org/jasig/portal/io/xml/import_archive.zip");
        
        final IPortalDataHandlerService.BatchImportOptions options = new IPortalDataHandlerService.BatchImportOptions();
        options.setLogDirectoryParent(tempFolder.newFolder("zipArchiveImport"));
        
        this.dataImportExportService.importDataArchive(archiveResource, options);
        
        verify(unmarshaller, times(16)).unmarshal(any(Source.class));
    }
    
    @Test
    public void testImportTarGzipArchive() throws Exception {
        final Unmarshaller unmarshaller = mock(Unmarshaller.class);
        
        final List<IDataImporter<? extends Object>> importers = setupAllImporters(new MockDataImporterSetup() {
            @Override
            public void setup(IPortalDataType dataType, IDataImporter<? extends Object> dataImporter) {
                when(dataImporter.getUnmarshaller()).thenReturn(unmarshaller);
            }
        });
        
        this.dataImportExportService.setDataImporters(importers);
        
        final Resource archiveResource = new ClassPathResource("/org/jasig/portal/io/xml/import_archive.tar.gz");
        
        final IPortalDataHandlerService.BatchImportOptions options = new IPortalDataHandlerService.BatchImportOptions();
        options.setLogDirectoryParent(tempFolder.newFolder("targzipArchiveImport"));
        
        this.dataImportExportService.importDataArchive(archiveResource, options);
        
        verify(unmarshaller, times(16)).unmarshal(any(Source.class));
    }
    
    @Test
    public void testImportTGZArchive() throws Exception {
        final Unmarshaller unmarshaller = mock(Unmarshaller.class);
        
        final List<IDataImporter<? extends Object>> importers = setupAllImporters(new MockDataImporterSetup() {
            @Override
            public void setup(IPortalDataType dataType, IDataImporter<? extends Object> dataImporter) {
                when(dataImporter.getUnmarshaller()).thenReturn(unmarshaller);
            }
        });
        
        this.dataImportExportService.setDataImporters(importers);
        
        final Resource archiveResource = new ClassPathResource("/org/jasig/portal/io/xml/import_archive.tgz");
        
        final IPortalDataHandlerService.BatchImportOptions options = new IPortalDataHandlerService.BatchImportOptions();
        options.setLogDirectoryParent(tempFolder.newFolder("tgzArchiveImport"));
        
        this.dataImportExportService.importDataArchive(archiveResource, options);
        
        verify(unmarshaller, times(16)).unmarshal(any(Source.class));
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
