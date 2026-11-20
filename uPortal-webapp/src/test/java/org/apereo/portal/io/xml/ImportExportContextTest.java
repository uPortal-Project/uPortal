/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.io.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.apereo.portal.io.xml.crn.CernunnosDataExporter;
import org.apereo.portal.io.xml.dlm.FragmentDefinitionExporter;
import org.apereo.portal.io.xml.dlm.FragmentDefinitionPortalDataType;
import org.apereo.portal.io.xml.entitytype.EntityTypePortalDataType;
import org.apereo.portal.io.xml.eventaggr.EventAggregationConfigurationPortalDataType;
import org.apereo.portal.io.xml.group.GroupMembershipPortalDataType;
import org.apereo.portal.io.xml.layout.FragmentLayoutPortalDataType;
import org.apereo.portal.io.xml.layout.LayoutPortalDataType;
import org.apereo.portal.io.xml.layout.ProfilePortalDataType;
import org.apereo.portal.io.xml.pags.PersonAttributesGroupStoreExporter;
import org.apereo.portal.io.xml.pags.PersonAttributesGroupStorePortalDataType;
import org.apereo.portal.io.xml.permission.PermissionOwnerPortalDataType;
import org.apereo.portal.io.xml.permission.PermissionSetPortalDataType;
import org.apereo.portal.io.xml.portlet.PortletPortalDataType;
import org.apereo.portal.io.xml.portlettype.PortletTypePortalDataType;
import org.apereo.portal.io.xml.ssd.StylesheetDescriptorPortalDataType;
import org.apereo.portal.io.xml.user.UserPortalDataType;
import org.apereo.portal.layout.dlm.FragmentDefinition;
import org.apereo.portal.layout.dlm.IFragmentDefinitionDao;
import org.apereo.portal.utils.Tuple;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Checks that the portal can import what it exports: every data key that an exporter in {@code
 * importExportContext.xml} writes has an importer or an upgrader there.
 */
public class ImportExportContextTest {
    private static final String IMPORT_EXPORT_CONTEXT =
            "classpath:/properties/contexts/importExportContext.xml";

    /**
     * The data key that each exporter in importExportContext.xml writes. The Cernunnos exporters
     * take it from the {@code script} attribute of their export script, the PAGS and fragment
     * definition exporters from their code, the layout exporters from {@code
     * RDBMDistributedLayoutStore}, and the JAXB exporters from the fixed {@code version} of their
     * schema.
     */
    private static final Map<String, PortalDataKey> EXPORTED_DATA_KEYS = new LinkedHashMap<>();

    static {
        EXPORTED_DATA_KEYS.put(
                "portletDefinitionImporterExporter", PortletPortalDataType.IMPORT_50_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "portletTypeImporterExporter", PortletTypePortalDataType.IMPORT_40_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "stylesheetDescriptorImporterExporter",
                StylesheetDescriptorPortalDataType.IMPORT_40_DATA_KEY);
        EXPORTED_DATA_KEYS.put("userImporterExporter", UserPortalDataType.IMPORT_40_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "permissionOwnerImporterExporter",
                PermissionOwnerPortalDataType.IMPORT_40_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "eventAggregationConfigurationImporterExporter",
                EventAggregationConfigurationPortalDataType.IMPORT_40_DATA_KEY);
        EXPORTED_DATA_KEYS.put("entityTypeExporter", EntityTypePortalDataType.IMPORT_32_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "groupMembershipExporter", GroupMembershipPortalDataType.IMPORT_50_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "personAttributesGroupStoreExporter",
                PersonAttributesGroupStorePortalDataType.IMPORT_PAGS_41_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "permissionSetExporter", PermissionSetPortalDataType.IMPORT_50_DATA_KEY);
        EXPORTED_DATA_KEYS.put("profileExporter", ProfilePortalDataType.IMPORT_32_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "fragmentLayoutExporter", FragmentLayoutPortalDataType.IMPORT_32_DATA_KEY);
        EXPORTED_DATA_KEYS.put("layoutExporter", LayoutPortalDataType.IMPORT_32_DATA_KEY);
        EXPORTED_DATA_KEYS.put(
                "fragmentDefinitionExporter", FragmentDefinitionPortalDataType.IMPORT_31_DATA_KEY);
    }

    /** Beans that importExportContext.xml passes to Cernunnos scripts this test doesn't run. */
    private static final List<String> UNUSED_SCRIPT_ATTRIBUTES =
            Arrays.asList(
                    "entityTypes",
                    "fragmentDefinitionDao",
                    "personAttributesGroupDefinitionDao",
                    "personAttributesGroupTestDefinitionDao",
                    "personAttributesGroupTestGroupDefinitionDao",
                    "stylesheetDescriptorDao",
                    "stylesheetUserPreferencesDao");

    private static final Pattern SCRIPT_ATTRIBUTE = Pattern.compile("script=([^)\\s]+)\\)");

    private EmbeddedDatabase dataSource;
    private DefaultListableBeanFactory beanFactory;
    private List<IPortalDataType> portalDataTypes;

    @Before
    public void setUp() {
        dataSource =
                new EmbeddedDatabaseBuilder()
                        .generateUniqueName(true)
                        .setType(EmbeddedDatabaseType.HSQL)
                        .build();
        beanFactory = new DefaultListableBeanFactory();
        beanFactory.addPropertyEditorRegistrar(
                new ResourceEditorRegistrar(
                        new DefaultResourceLoader(), new StandardEnvironment()));
        // UserImporterExporter gets PortalDb through @Resource
        final CommonAnnotationBeanPostProcessor resourceInjection =
                new CommonAnnotationBeanPostProcessor();
        resourceInjection.setBeanFactory(beanFactory);
        beanFactory.addBeanPostProcessor(resourceInjection);

        beanFactory.registerSingleton("PortalDb", dataSource);
        beanFactory.registerSingleton(
                "transactionManager", new DataSourceTransactionManager(dataSource));
        for (final String name : UNUSED_SCRIPT_ATTRIBUTES) {
            beanFactory.registerSingleton(name, new Object());
        }

        // The data types, found the way the portal's component scan finds them
        final ClassPathBeanDefinitionScanner scanner =
                new ClassPathBeanDefinitionScanner(beanFactory, false);
        scanner.setIncludeAnnotationConfig(false);
        scanner.addIncludeFilter(
                (reader, factory) ->
                        new AnnotationTypeFilter(Component.class).match(reader, factory)
                                && new AssignableTypeFilter(IPortalDataType.class)
                                        .match(reader, factory));
        scanner.scan("org.apereo.portal");

        new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(IMPORT_EXPORT_CONTEXT);

        portalDataTypes = beansOfType(IPortalDataType.class);
        AnnotationAwareOrderComparator.sort(portalDataTypes);
    }

    @After
    public void tearDown() {
        beanFactory.destroySingletons();
        dataSource.shutdown();
    }

    /**
     * For each exporter in importExportContext.xml: the data key it writes, and an importer or an
     * upgrader for that key.
     */
    @Test
    public void testEveryExportedDataKeyHasAnImporterOrUpgrader() throws Exception {
        assertEquals(
                "EXPORTED_DATA_KEYS lists every exporter in importExportContext.xml",
                new TreeSet<>(EXPORTED_DATA_KEYS.keySet()),
                new TreeSet<>(
                        Arrays.asList(
                                beanFactory.getBeanNamesForType(
                                        IDataExporter.class, false, false))));

        final Set<PortalDataKey> importerKeys = new LinkedHashSet<>();
        for (final IDataImporter<?> importer : beansOfType(IDataImporter.class)) {
            importerKeys.addAll(importer.getImportDataKeys());
        }
        final Set<PortalDataKey> upgraderKeys = new LinkedHashSet<>();
        for (final IDataUpgrader upgrader : beansOfType(IDataUpgrader.class)) {
            upgraderKeys.addAll(upgrader.getSourceDataTypes());
        }
        final Map<PortalDataKey, IPortalDataType> dataKeyTypes = new LinkedHashMap<>();
        for (final IPortalDataType portalDataType : portalDataTypes) {
            for (final PortalDataKey key : portalDataType.getDataKeyImportOrder()) {
                dataKeyTypes.put(key, portalDataType);
            }
        }

        final List<String> problems = new ArrayList<>();
        for (final Map.Entry<String, PortalDataKey> exported : EXPORTED_DATA_KEYS.entrySet()) {
            final String exporter = exported.getKey();
            final PortalDataKey key = exported.getValue();

            final PortalDataKey written = writtenDataKey(exporter);
            if (written != null && !written.equals(key)) {
                problems.add(exporter + " writes " + written + ", the table says " + key);
            }

            final IPortalDataType portalDataType = dataKeyTypes.get(key);
            if (portalDataType == null) {
                problems.add(exporter + " writes " + key + ", which no IPortalDataType lists");
                continue;
            }
            for (final PortalDataKey step :
                    portalDataType.postProcessPortalDataKey(exporter, key, null)) {
                if (!importerKeys.contains(step) && !upgraderKeys.contains(step)) {
                    problems.add(
                            exporter
                                    + " writes "
                                    + key
                                    + ", and importExportContext.xml has no importer or upgrader for "
                                    + step);
                }
            }
        }
        assertTrue(String.join("\n", problems), problems.isEmpty());
    }

    /**
     * The data key an exporter writes, read from its export script or from the element it builds.
     * Null for the exporters whose key this test can't read without a portal database.
     */
    private PortalDataKey writtenDataKey(String exporterName) throws IOException {
        final Object exporter = beanFactory.getBean(exporterName);
        if (exporter instanceof CernunnosDataExporter) {
            final RuntimeBeanReference task =
                    (RuntimeBeanReference)
                            beanFactory
                                    .getBeanDefinition(exporterName)
                                    .getPropertyValues()
                                    .get("task");
            final TypedStringValue location =
                    (TypedStringValue)
                            beanFactory
                                    .getBeanDefinition(task.getBeanName())
                                    .getPropertyValues()
                                    .get("location");
            final String script =
                    StreamUtils.copyToString(
                            new DefaultResourceLoader()
                                    .getResource(location.getValue())
                                    .getInputStream(),
                            StandardCharsets.UTF_8);
            final Matcher matcher = SCRIPT_ATTRIBUTE.matcher(script);
            assertTrue(location.getValue() + " sets a script attribute", matcher.find());
            final IPortalDataType portalDataType =
                    ((CernunnosDataExporter) exporter).getPortalDataType();
            return new PortalDataKey(
                    portalDataType.getDataKeyImportOrder().get(0).getName(),
                    matcher.group(1),
                    null);
        }
        if (exporter instanceof PersonAttributesGroupStoreExporter) {
            final IPersonAttributesGroupDefinitionDao dao =
                    mock(IPersonAttributesGroupDefinitionDao.class);
            when(dao.getPersonAttributesGroupDefinitionByName("group"))
                    .thenReturn(
                            Collections.singleton(mock(IPersonAttributesGroupDefinition.class)));
            ((PersonAttributesGroupStoreExporter) exporter)
                    .setpersonAttributesGroupDefinitionDao(dao);
            return dataKey(((PersonAttributesGroupStoreExporter) exporter).exportData("group"));
        }
        if (exporter instanceof FragmentDefinitionExporter) {
            final IFragmentDefinitionDao dao = mock(IFragmentDefinitionDao.class);
            when(dao.getFragmentDefinition("fragment")).thenReturn(mock(FragmentDefinition.class));
            ((FragmentDefinitionExporter) exporter).setFragmentDefinitionDao(dao);
            return dataKey(((FragmentDefinitionExporter) exporter).exportData("fragment"));
        }
        return null;
    }

    private static PortalDataKey dataKey(Tuple<String, Element> exported) {
        final Element root = exported.second;
        return new PortalDataKey(
                new QName(root.getNamespaceURI(), root.getName()),
                root.attributeValue("script"),
                root.attributeValue("version"));
    }

    /** The beans of a type that importExportContext.xml defines, without its factory beans. */
    private <T> List<T> beansOfType(Class<T> type) {
        final List<T> beans = new ArrayList<>();
        for (final String name : beanFactory.getBeanNamesForType(type, false, false)) {
            beans.add(beanFactory.getBean(name, type));
        }
        return beans;
    }
}
