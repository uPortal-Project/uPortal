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
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
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
import org.jasig.portal.io.xml.IDataExporter;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.utils.threading.NoopLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.xml.FixedXMLEventStreamReader;
import org.w3c.dom.Document;

/**
 * Generic import impl that support Cernunnous Tasks.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CernunnosDataExporter implements IDataExporter<Source>, Marshaller {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Task task;
    

    public void setTask(Task task) {
        this.task = task;
    }


    @Override
    public IPortalDataType getPortalDataType() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Source exportData(String id) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getFileName(Source data) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Marshaller getMarshaller() {
        // TODO Auto-generated method stub
        return null;
    }


    
    
    @Override
    public boolean supports(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void marshal(Object graph, Result result) throws IOException, XmlMappingException {
        // TODO Auto-generated method stub
        
    }

}
