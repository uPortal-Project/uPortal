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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.io.xml.permission.ExternalPermissionOwner;
import org.jasig.portal.io.xml.ssd.ExternalStylesheetDescriptor;
import org.jasig.portal.io.xml.user.UserType;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.jasig.portal.utils.ICounterStore;
import org.jasig.portal.utils.Tuple;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.w3c.dom.Element;

import com.google.common.base.Function;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/jasig/portal/io/xml/importExportTestContext.xml")
public class IdentityImportExportTest extends BaseJpaDaoTest {
    @Autowired private DataSource dataSource;
    
    @javax.annotation.Resource(name="stylesheetDescriptorImporterExporter")
    private IDataImporter<ExternalStylesheetDescriptor> stylesheetDescriptorImporter;
    @javax.annotation.Resource(name="stylesheetDescriptorImporterExporter")
    private IDataExporter<ExternalStylesheetDescriptor> stylesheetDescriptorExporter;
    
    @javax.annotation.Resource(name="userImporterExporter")
    private IDataImporter<UserType> userImporter;
    @javax.annotation.Resource(name="userImporterExporter")
    private IDataExporter<UserType> userExporter;
    
    @javax.annotation.Resource(name="permissionOwnerImporterExporter")
    private IDataImporter<ExternalPermissionOwner> permissionOwnerImporter;
    @javax.annotation.Resource(name="permissionOwnerImporterExporter")
    private IDataExporter<ExternalPermissionOwner> permissionOwnerExporter;
    
    @javax.annotation.Resource(name="fragmentDefinitionImporter")
    private IDataImporter<Tuple<String, Element>> fragmentDefinitionImporter;
    @javax.annotation.Resource(name="fragmentDefinitionExporter")
    private IDataExporter<Tuple<String, org.dom4j.Element>> fragmentDefinitionExporter;
    
    @Autowired private ICounterStore counterStore;
    private SimpleJdbcTemplate simpleJdbcTemplate;
    private int counter = 0;
    private TimeZone defaultTimeZone;
    
    @PersistenceContext(unitName = "uPortalPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }
    
    protected void runSql(final String sql) {
        if (simpleJdbcTemplate == null) {
            simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
        }
        SimpleJdbcTestUtils.executeSqlScript(simpleJdbcTemplate, new ByteArrayResource(sql.getBytes()), false);
    }
    
    @After
    public void cleanup() {
        if (defaultTimeZone != null) {
            TimeZone.setDefault(defaultTimeZone);
        }
        
        runSql("DROP TABLE UP_USER"); 
        runSql("DROP TABLE UP_LAYOUT_STRUCT");
    }

    @Before
    public void setup() {
        simpleJdbcTemplate = null;
        
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("EST"));
        
        counter = 0;
        reset(counterStore);
        when(counterStore.getNextId(anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return counter++;
            }
        });
        
        //Needed for user import/export test
        runSql("CREATE TABLE UP_USER\n" + 
                        "(\n" + 
                        "   USER_ID integer,\n" + 
                        "   USER_NAME varchar(1000),\n" + 
                        "   USER_DFLT_USR_ID integer,\n" + 
                        "   USER_DFLT_LAY_ID integer,\n" + 
                        "   NEXT_STRUCT_ID integer,\n" + 
                        "   LST_CHAN_UPDT_DT timestamp,\n" +  
                        "   CONSTRAINT SYS_IDX_01 PRIMARY KEY (USER_ID)\n" + 
                        ");");
        runSql("CREATE INDEX UPU_DFLT_ID_IDX ON UP_USER(USER_DFLT_USR_ID);");
        runSql("CREATE TABLE UP_LAYOUT_STRUCT\n" + 
        		"(\n" + 
        		"   USER_ID integer NOT NULL,\n" + 
        		"   LAYOUT_ID integer NOT NULL,\n" + 
        		"   STRUCT_ID integer NOT NULL,\n" + 
        		"   NEXT_STRUCT_ID integer,\n" + 
        		"   CHLD_STRUCT_ID integer,\n" + 
        		"   EXTERNAL_ID varchar(1000),\n" + 
        		"   CHAN_ID integer,\n" + 
        		"   NAME varchar(1000),\n" + 
        		"   TYPE varchar(1000),\n" + 
        		"   HIDDEN varchar(1000),\n" + 
        		"   IMMUTABLE varchar(1000),\n" + 
        		"   UNREMOVABLE varchar(1000),\n" + 
        		"   CONSTRAINT SYS_IDX_03 PRIMARY KEY (LAYOUT_ID,USER_ID,STRUCT_ID)\n" + 
        		");");
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
    public void testPermissionOwner40ImportExport() throws Exception {
        final ClassPathResource permissionOwnerResource = new ClassPathResource("/org/jasig/portal/io/xml/permission-owner/test_4-0.permission-owner.xml");
        
        this.testIdentityImportExport(
                this.permissionOwnerImporter, this.permissionOwnerExporter,
                permissionOwnerResource,
                new Function<ExternalPermissionOwner, String>() {

                    @Override
                    public String apply(ExternalPermissionOwner input) {
                        return input.getFname();
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
    
  
    @Test
    public void testFragmentDefinition31ImportExport() throws Exception {
        final ClassPathResource dataResource = new ClassPathResource("/org/jasig/portal/io/xml/fragment-definition/academic-tab_3-1.fragment-definition.xml");
        
        this.<Tuple<String, Element>>testIdentityImportExport(
                this.fragmentDefinitionImporter, this.fragmentDefinitionExporter,
                dataResource,
                new Function<Tuple<String, Element>, String>() {
                    @Override
                    public String apply(Tuple<String, Element> input) {
                        return "Academics Tab";
                    }
                });
    }
    
    private <T> void testIdentityImportExport(
            final IDataImporter<T> dataImporter, final IDataExporter<?> dataExporter, 
            Resource resource, Function<T, String> getName) throws Exception {
        
    	final String importData = toString(resource);
        
    	final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
    	final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(importData));
        
        //Unmarshall from XML
        final Unmarshaller unmarshaller = dataImporter.getUnmarshaller();
        final StAXSource source = new StAXSource(xmlEventReader);
		@SuppressWarnings("unchecked")
        final T dataImport = (T)unmarshaller.unmarshal(source);
        
        //Make sure the data was unmarshalled
        assertNotNull("Unmarshalled import data was null", dataImport);
        
        //Import the data
        dataImporter.importData(dataImport);
        
        //Export the data
        final String name = getName.apply(dataImport);
        final Object dataExport = transactionOperations.execute(new TransactionCallback<Object>() {
            /* (non-Javadoc)
             * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
             */
            @Override
            public Object doInTransaction(TransactionStatus status) {
                return dataExporter.exportData(name);
            }
        });
        
        //Make sure the data was exported
        assertNotNull("Exported data was null", dataExport);
        
        //Marshall to XML
        final Marshaller marshaller = dataExporter.getMarshaller();
        
        final StringWriter result = new StringWriter();
        marshaller.marshal(dataExport, new StreamResult(result));
        
        //Compare the exported XML data with the imported XML data, they should match
        final String resultString = result.toString();
        try {
	        XMLUnit.setIgnoreWhitespace(true);
			Diff d = new Diff(new StringReader(importData), new StringReader(resultString));
	        assertTrue("Export result differs from import" + d, d.similar());
        }
        catch (Exception e) {
            throw new XmlTestException("Failed to assert similar between import XML and export XML", resultString, e);
        }
        catch (Error e) {
        	throw new XmlTestException("Failed to assert similar between import XML and export XML", resultString, e);
        }
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
