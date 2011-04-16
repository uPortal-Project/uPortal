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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.io.xml.ssd.ExternalStylesheetDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/jasig/portal/io/xml/importExportTestContext.xml")
public class ImportExportTest {
    private IDataImporterExporter<ExternalStylesheetDescriptor> stylesheetDescriptorImporterExporter;

    @Autowired
    public void setStylesheetDescriptorImporterExporter(
            IDataImporterExporter<ExternalStylesheetDescriptor> stylesheetDescriptorImporterExporter) {
        this.stylesheetDescriptorImporterExporter = stylesheetDescriptorImporterExporter;
    }
    
    @Test
    public void testStylesheetDescriptor40ImportExpot() throws Exception {
        final ClassPathResource stylesheetDescriptorResource = new ClassPathResource("/org/jasig/portal/io/xml/ssd/test_4-0.stylesheet-descriptor.xml");
        
        this.testIdentityImportExport(
                this.stylesheetDescriptorImporterExporter,
                stylesheetDescriptorResource,
                new Function<ExternalStylesheetDescriptor, String>() {

                    @Override
                    public String apply(ExternalStylesheetDescriptor input) {
                        return input.getName();
                    }
                });
    }
    
    
    private <T> void testIdentityImportExport(IDataImporterExporter<T> dataImporterExporter, Resource resource, Function<T, String> getName) throws Exception {
        final ClassPathResource stylesheetDescriptorResource = new ClassPathResource("/org/jasig/portal/io/xml/ssd/test_4-0.stylesheet-descriptor.xml");
        
        //Unmarshall from XML
        final Unmarshaller unmarshaller = dataImporterExporter.getUnmarshaller();
        final T dataImport = (T)unmarshaller.unmarshal(new StreamSource(stylesheetDescriptorResource.getInputStream()));
        
        //Make sure the data was unmarshalled
        assertNotNull(dataImport);
        
        //Import the data
        dataImporterExporter.importData(dataImport);
        
        //Export the data
        final String name = getName.apply(dataImport);
        final T dataExport = dataImporterExporter.exportData(name);
        
        //Marshall to XML
        final Marshaller marshaller = dataImporterExporter.getMarshaller();
        
        final StringWriter result = new StringWriter();
        marshaller.marshal(dataExport, new StreamResult(result));

        //Compare the exported XML data with the imported XML data, they should match
        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(new InputStreamReader(stylesheetDescriptorResource.getInputStream()), new StringReader(result.toString()));
        assertTrue("Export result differs from import" + d, d.similar());
    }
}
