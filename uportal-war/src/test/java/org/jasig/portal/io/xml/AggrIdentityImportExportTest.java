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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.io.xml.eventaggr.ExternalEventAggregationConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/jasig/portal/io/xml/aggrImportExportTestContext.xml")
public class AggrIdentityImportExportTest extends AbstractIdentityImportExportTest {
    @javax.annotation.Resource(name="eventAggregationConfigurationImporterExporter")
    private IDataImporter<ExternalEventAggregationConfiguration> eventAggregationConfigurationImporter;
    @javax.annotation.Resource(name="eventAggregationConfigurationImporterExporter")
    private IDataExporter<ExternalEventAggregationConfiguration> eventAggregationConfigurationExporter;
    

    @PersistenceContext(unitName = "uPortalAggrEventsPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    @Test
    public void testEventAggregationConfiguration40ImportExport() throws Exception {
        final ClassPathResource stylesheetDescriptorResource = new ClassPathResource("/org/jasig/portal/io/xml/eventaggr/test_4-0.event-aggregation.xml");
        
        this.testIdentityImportExport(
                this.eventAggregationConfigurationImporter, this.eventAggregationConfigurationExporter,
                stylesheetDescriptorResource,
                new Function<ExternalEventAggregationConfiguration, String>() {

                    @Override
                    public String apply(ExternalEventAggregationConfiguration input) {
                        return "DEFAULT";
                    }
                });
    }
}
