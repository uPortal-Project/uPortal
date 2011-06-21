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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.dom.DOMConverter;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.xml.FixedXMLEventStreamReader;
import org.w3c.dom.Document;

/**
 * Generic import/export/delete impl that support Cernunnous Tasks.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CernunnosImportExportHandler implements IDataImporter<Source>, Unmarshaller {//, IDataExporter<Object>, IDataDeleter<Object> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Set<PortalDataKey> importDataKeys;
    private Task importTask;
    
    public void setImportDataKeys(Set<PortalDataKey> importDataKeys) {
        this.importDataKeys = importDataKeys;
    }

    public void setImportTask(Task importTask) {
        this.importTask = importTask;
    }

    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return this.importDataKeys;
    }

    @Override
    public void importData(Source data) {
      //Convert the StAX XMLEventReader to a dom4j Node
        final Node node = convertToNode(data);
        
        final RuntimeRequestResponse request = new RuntimeRequestResponse();
        request.setAttribute(Attributes.NODE, node);
        request.setAttribute(Attributes.LOCATION, StringUtils.trimToEmpty(data.getSystemId()));

        final ReturnValueImpl result = new ReturnValueImpl();
        final TaskResponse response = new RuntimeRequestResponse(
                Collections.<String, Object> singletonMap("Attributes.RETURN_VALUE", result));

        this.importTask.perform(request, response);
    }

    @Override
    public Unmarshaller getUnmarshaller() {
        return this;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object unmarshal(Source source) throws IOException, XmlMappingException {
        return source;
    }

    protected Node convertToNode(Source source) {
        if (source instanceof StAXSource) {
            final StAXSource staxSource = (StAXSource)source;
            
            final DOMConverter domConverter = new DOMConverter();
            final Document document;
            try {
                XMLStreamReader xmlStreamReader = staxSource.getXMLStreamReader();
                if (xmlStreamReader == null) {
                    final XMLEventReader xmlEventReader = staxSource.getXMLEventReader();
                    xmlStreamReader = new FixedXMLEventStreamReader(xmlEventReader);
                }
                
                document = domConverter.buildDocument(xmlStreamReader);
            }
            catch (XMLStreamException e) {
                throw new RuntimeException("Failed to parse StAX Reader into Dom4J Node", e);
            }
            final DOMReader domReader = new DOMReader();
            final org.dom4j.Document dom4JDocument = domReader.read(document);
            return dom4JDocument.getRootElement();
        }
        
        throw new IllegalArgumentException("Source of type " + source.getClass() + " is not supported");
    }
}
