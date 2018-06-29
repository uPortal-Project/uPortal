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

import com.google.common.base.Function;
import org.apereo.portal.io.xml.eventaggr.ExternalEventAggregationConfiguration;
import org.apereo.portal.test.BaseAggrEventsJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = "classpath:/org/apereo/portal/io/xml/aggrImportExportTestContext.xml")
public class AggrIdentityImportExportTest extends BaseAggrEventsJpaDaoTest {
    @javax.annotation.Resource(name = "eventAggregationConfigurationImporterExporter")
    private IDataImporter<ExternalEventAggregationConfiguration>
            eventAggregationConfigurationImporter;

    @javax.annotation.Resource(name = "eventAggregationConfigurationImporterExporter")
    private IDataExporter<ExternalEventAggregationConfiguration>
            eventAggregationConfigurationExporter;

    @Test
    public void testEventAggregationConfiguration40ImportExport() throws Exception {
        final ClassPathResource stylesheetDescriptorResource =
                new ClassPathResource(
                        "/org/apereo/portal/io/xml/eventaggr/test_5-0.event-aggregation.xml");

        IdentityImportExportTestUtilities.testIdentityImportExport(
                this.transactionOperations,
                this.eventAggregationConfigurationImporter,
                this.eventAggregationConfigurationExporter,
                stylesheetDescriptorResource,
                new Function<ExternalEventAggregationConfiguration, String>() {

                    @Override
                    public String apply(ExternalEventAggregationConfiguration input) {
                        return "DEFAULT";
                    }
                });
    }
}
