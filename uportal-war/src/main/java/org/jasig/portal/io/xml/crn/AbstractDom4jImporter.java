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
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;

import org.codehaus.staxmate.dom.DOMConverter;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.jasig.portal.io.xml.IDataImporter;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.threading.NoopLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.xml.FixedXMLEventStreamReader;
import org.w3c.dom.Document;

/**
 * Generic dom4j import impl
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractDom4jImporter implements IDataImporter<Tuple<String, Element>>, Unmarshaller {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Set<PortalDataKey> dataKeys;
    private Lock lock = NoopLock.INSTANCE;
    
    public void setDataKeys(Set<PortalDataKey> dataKeys) {
        this.dataKeys = dataKeys;
    }

    /**
     * Set if the import operation is thread-safe, defaults to true
     */
    public void setThreadSafe(boolean threadSafe) {
        if (threadSafe) {
            this.lock = NoopLock.INSTANCE;
        }
        else {
            this.lock = new ReentrantLock();
        }
    }

    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return this.dataKeys;
    }

    @Override
    public void importData(Tuple<String, Element> data) {
        //Have to make local reference since importLock is NOT immutable
        final Lock lock = this.lock;
        lock.lock();
        try {
            this.importDataElement(data);
        }
        finally {
            lock.unlock();
        }
    }
    
    protected abstract void importDataElement(Tuple<String, Element> data);

    @Override
    public Unmarshaller getUnmarshaller() {
        return this;
    }

    
    //** Unmarshaller APIs **/

    @Override
    public boolean supports(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object unmarshal(Source source) throws IOException, XmlMappingException {
        //Convert the StAX XMLEventReader to a dom4j Element
        final Element node = convertToElement(source);
        return new Tuple<String, Element>(source.getSystemId(), node);
    }
    
    protected Element convertToElement(Source source) {
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
                throw new RuntimeException("Failed to parse StAX Reader into Dom4J Element", e);
            }
            final DOMReader domReader = new DOMReader();
            final org.dom4j.Document dom4JDocument = domReader.read(document);
            dom4JDocument.setName(source.getSystemId());
            return dom4JDocument.getRootElement();
        }
        
        throw new IllegalArgumentException("Source of type " + source.getClass() + " is not supported");
    }
}
