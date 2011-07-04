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

package org.jasig.portal.io.xml.crn;

import java.io.IOException;
import java.util.Collections;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.dom4j.Node;
import org.dom4j.io.DocumentSource;
import org.jasig.portal.io.xml.IDataExporter;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.xml.XmlUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;

import com.google.common.base.Function;

/**
 * Generic export impl that support Cernunnous Tasks.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CernunnosDataExporter implements IDataExporter<Tuple<String, Node>>, Marshaller {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private XmlUtilities xmlUtilities;
    private Task task;
    private String idAttributeName;
    private IPortalDataType portalDataType;
    private Function<IPortalDataType, Iterable<? extends IPortalData>> portalDataRetriever;
    private Function<Tuple<String, Node>, String> fileNameFunction;
    
    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Required
    public void setIdAttributeName(String idAttributeName) {
        this.idAttributeName = idAttributeName;
    }

    @Required
    public void setTask(Task task) {
        this.task = task;
    }
    
    @Required
    public void setPortalDataType(IPortalDataType portalDataType) {
        this.portalDataType = portalDataType;
    }
    
    @Required
    public void setPortalDataRetriever(Function<IPortalDataType, Iterable<? extends IPortalData>> portalDataRetriever) {
        this.portalDataRetriever = portalDataRetriever;
    }
    
    public void setFileNameFunction(Function<Tuple<String, Node>, String> fileNameFunction) {
        this.fileNameFunction = fileNameFunction;
    }

    @Override
    public IPortalDataType getPortalDataType() {
        return this.portalDataType;
    }

    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        return this.portalDataRetriever.apply(this.portalDataType);
    }


    @Override
    public Tuple<String, Node> exportData(String id) {
        
        final RuntimeRequestResponse request = new RuntimeRequestResponse();
        request.setAttribute(this.idAttributeName, id);

        final ReturnValueImpl result = new ReturnValueImpl();
        final TaskResponse response = new RuntimeRequestResponse(
                Collections.<String, Object> singletonMap("Attributes.RETURN_VALUE", result));
        
        task.perform(request, response);
        
        final Node node = (Node)result.getValue();
        if (node == null) {
            return null;
        }
        
        return new Tuple<String, Node>(id, node);
    }


    @Override
    public String getFileName(Tuple<String, Node> data) {
        final String fileName;
        if (this.fileNameFunction == null) {
            fileName = data.first;
        }
        else {
            fileName = this.fileNameFunction.apply(data);
        }
        
        return SafeFilenameUtils.makeSafeFilename(fileName);
    }


    @Override
    public Marshaller getMarshaller() {
        return this;
    }
    
    
    @Override
    public boolean supports(Class<?> clazz) {
        System.err.println(clazz);
        return true;
    }


    @Override
    public void marshal(Object graph, Result result) throws IOException, XmlMappingException {
        @SuppressWarnings("unchecked")
        final Tuple<String, Node> data = (Tuple<String, Node>)graph;
        
        final Transformer transformer;
        try {
            transformer = this.xmlUtilities.getIdentityTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to load identity Transformer", e);
        }

        //Setup the transformer to pretty-print the output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        try {
            transformer.transform(new DocumentSource(data.second), result);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Failed to write Node to Result for: " + data.first, e);
        }
    }
}
