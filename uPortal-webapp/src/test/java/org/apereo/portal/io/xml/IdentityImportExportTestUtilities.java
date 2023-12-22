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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Function;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apereo.portal.io.xml.ssd.ExternalStylesheetDescriptor;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/** Utility for doing import/export round trip testing */
public final class IdentityImportExportTestUtilities {

    public static <T> void testIdentityImportExport(
            TransactionOperations transactionOperations,
            final IDataImporter<T> dataImporter,
            final IDataExporter<?> dataExporter,
            Resource resource,
            Function<T, String> getName)
            throws Exception {

        final String importData = toString(resource);

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        final XMLEventReader xmlEventReader =
                xmlInputFactory.createXMLEventReader(new StringReader(importData));

        // Unmarshall from XML
        final Unmarshaller unmarshaller = dataImporter.getUnmarshaller();
        final StAXSource source = new StAXSource(xmlEventReader);
        @SuppressWarnings("unchecked")
        final T dataImport = (T) unmarshaller.unmarshal(source);

        // Make sure the data was unmarshalled
        assertNotNull("Unmarshalled import data was null", dataImport);

        // Import the data
        dataImporter.importData(dataImport);

        // Export the data
        final String name = getName.apply(dataImport);
        final Object dataExport =
                transactionOperations.execute(
                        new TransactionCallback<Object>() {
                            /* (non-Javadoc)
                             * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
                             */
                            @Override
                            public Object doInTransaction(TransactionStatus status) {
                                return dataExporter.exportData(name);
                            }
                        });

        // Make sure the data was exported
        assertNotNull("Exported data was null", dataExport);
        if (resource.getFilename().equals("test_4-0.stylesheet-descriptor.xml")) {
            Collections.sort(
                    ((ExternalStylesheetDescriptor) dataExport)
                            .getLayoutAttributes()
                            .get(0)
                            .getTargetElements());
        }

        // Marshall to XML
        final Marshaller marshaller = dataExporter.getMarshaller();

        final StringWriter result = new StringWriter();
        marshaller.marshal(dataExport, new StreamResult(result));

        // Compare the exported XML data with the imported XML data, they should match
        final String resultString = result.toString();
        try {
            XMLUnit.setIgnoreWhitespace(true);
            Diff d = new Diff(new StringReader(importData), new StringReader(resultString));
            assertTrue("Export result differs from import" + d, d.similar());
        } catch (Exception e) {
            throw new XmlTestException(
                    "Failed to assert similar between import XML and export XML", resultString, e);
        } catch (Error e) {
            throw new XmlTestException(
                    "Failed to assert similar between import XML and export XML", resultString, e);
        }
    }

    private static String toString(Resource resource) throws IOException {
        final InputStream inputStream = resource.getInputStream();
        try {
            return IOUtils.toString(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
