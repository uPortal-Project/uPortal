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

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.TimeZone;

import javax.sql.DataSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.io.xml.ssd.ExternalStylesheetDescriptor;
import org.jasig.portal.io.xml.user.UserType;
import org.jasig.portal.utils.ICounterStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/jasig/portal/io/xml/importExportTestContext.xml")
public class IdentityImportExportTest {
    @Autowired private DataSource dataSource;
    
    @javax.annotation.Resource(name="stylesheetDescriptorImporterExporter")
    private IDataImporter<ExternalStylesheetDescriptor> stylesheetDescriptorImporter;
    @javax.annotation.Resource(name="stylesheetDescriptorImporterExporter")
    private IDataExporter<ExternalStylesheetDescriptor> stylesheetDescriptorExporter;
    
    @javax.annotation.Resource(name="userImporterExporter")
    private IDataImporter<UserType> userImporter;
    @javax.annotation.Resource(name="userImporterExporter")
    private IDataExporter<UserType> userExporter;
    
    @Autowired private ICounterStore counterStore;
    private int counter = 0;
    private TimeZone defaultTimeZone;
    
    @After
    public void cleanup() {
        if (defaultTimeZone != null) {
            TimeZone.setDefault(defaultTimeZone);
        }
    }
    
    @Before
    public void setup() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("EST"));
        
        counter = 0;
        reset(counterStore);
        when(counterStore.getIncrementIntegerId(anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return counter++;
            }
        });
        
        final SimpleJdbcTemplate simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
        
        final StringBuilder dbSetupScript = new StringBuilder();

        //Needed for user import/export test
        dbSetupScript.append(
                "CREATE TABLE UP_USER\n" + 
        		"(\n" + 
        		"   USER_ID integer PRIMARY KEY,\n" + 
        		"   USER_NAME varchar,\n" + 
        		"   USER_DFLT_USR_ID integer,\n" + 
        		"   USER_DFLT_LAY_ID integer,\n" + 
        		"   NEXT_STRUCT_ID integer,\n" + 
        		"   LST_CHAN_UPDT_DT timestamp,\n" +  
        		"   CONSTRAINT SYS_IDX_01 PRIMARY KEY (USER_ID)\n" + 
        		");\n");
        dbSetupScript.append("CREATE INDEX UPU_DFLT_ID_IDX ON UP_USER(USER_DFLT_USR_ID);\n"); 
        dbSetupScript.append("CREATE UNIQUE INDEX SYS_IDX_02 ON UP_USER(USER_ID);\n");
        dbSetupScript.append(
                "CREATE TABLE UP_LAYOUT_STRUCT\n" + 
        		"(\n" + 
        		"   USER_ID integer NOT NULL,\n" + 
        		"   LAYOUT_ID integer NOT NULL,\n" + 
        		"   STRUCT_ID integer NOT NULL,\n" + 
        		"   NEXT_STRUCT_ID integer,\n" + 
        		"   CHLD_STRUCT_ID integer,\n" + 
        		"   EXTERNAL_ID varchar,\n" + 
        		"   CHAN_ID integer,\n" + 
        		"   NAME varchar,\n" + 
        		"   TYPE varchar,\n" + 
        		"   HIDDEN varchar,\n" + 
        		"   IMMUTABLE varchar,\n" + 
        		"   UNREMOVABLE varchar,\n" + 
        		"   CONSTRAINT SYS_IDX_03 PRIMARY KEY (LAYOUT_ID,USER_ID,STRUCT_ID)\n" + 
        		");\n");
        dbSetupScript.append(
                "CREATE UNIQUE INDEX SYS_IDX_04 ON UP_LAYOUT_STRUCT\n" + 
        		"(\n" + 
        		"  LAYOUT_ID,\n" + 
        		"  USER_ID,\n" + 
        		"  STRUCT_ID\n" + 
        		");\n");
        
        SimpleJdbcTestUtils.executeSqlScript(simpleJdbcTemplate, new ByteArrayResource(dbSetupScript.toString().getBytes()), true);
    }

    
    @Test
    public void testStylesheetDescriptor40ImportExport() throws Exception {
        final ClassPathResource stylesheetDescriptorResource = new ClassPathResource("/org/jasig/portal/io/xml/ssd/test_4-0.stylesheet-descriptor.xml");
        
        this.testIdentityImportExport(
                this.stylesheetDescriptorImporter, this.stylesheetDescriptorExporter,
                stylesheetDescriptorResource,
                new Function<ExternalStylesheetDescriptor, String>() {

                    @Override
                    public String apply(ExternalStylesheetDescriptor input) {
                        return input.getName();
                    }
                });
    }
    
    @Test
    public void testUser40ImportExport() throws Exception {
        final ClassPathResource dataResource = new ClassPathResource("/org/jasig/portal/io/xml/user/test_4-0.user.xml");
        
        this.<UserType>testIdentityImportExport(
                this.userImporter, this.userExporter,
                dataResource,
                new Function<UserType, String>() {
                    @Override
                    public String apply(UserType input) {
                        return input.getUsername();
                    }
                });
    }
    
    
    private <T> void testIdentityImportExport(
            IDataImporter<T> dataImporter, IDataExporter<T> dataExporter, 
            Resource resource, Function<T, String> getName) throws Exception {
        final String importData = toString(resource);
        
        //Unmarshall from XML
        final Unmarshaller unmarshaller = dataImporter.getUnmarshaller();
        @SuppressWarnings("unchecked")
        final T dataImport = (T)unmarshaller.unmarshal(new StreamSource(new StringReader(importData)));
        
        //Make sure the data was unmarshalled
        assertNotNull(dataImport);
        
        //Import the data
        dataImporter.importData(dataImport);
        
        //Export the data
        final String name = getName.apply(dataImport);
        final T dataExport = dataExporter.exportData(name);
        
        //Marshall to XML
        final Marshaller marshaller = dataExporter.getMarshaller();
        
        final StringWriter result = new StringWriter();
        marshaller.marshal(dataExport, new StreamResult(result));
        
//        System.out.println(result.toString());
//        System.out.println("");

        //Compare the exported XML data with the imported XML data, they should match
        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(new StringReader(importData), new StringReader(result.toString()));
        assertTrue("Export result differs from import" + d, d.similar());
    }

    protected String toString(Resource resource) throws IOException {
        final InputStream inputStream = resource.getInputStream();
        try {
            return IOUtils.toString(inputStream);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
