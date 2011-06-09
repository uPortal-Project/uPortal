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
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.dom4j.Node;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.stream.BufferedXMLEventReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;

/**
 * Uses Spring OXM {@link Marshaller} and {@link Unmarshaller} objects to convert portal data to/from XML.
 * Uses {@link IDataImporterExporter} objects to list available data and read and write data from the portal
 * data stores.
 * 
 * TODO better error handling, try to figure out what went wrong and provide a solution in the exception message
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("dataImportExportService")
public class JaxbDataImportExportService implements IDataImportExportService, ResourceLoaderAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Map<PortalDataKey, IDataImporterExporter<Object>> portalDataImporters = Collections.emptyMap();
    private Map<String, IDataImporterExporter<Object>> portalDataExporters = Collections.emptyMap();
    private Map<PortalDataKey, IDataUpgrader> portalDataUpgraders = Collections.emptyMap();
    private Set<IPortalDataType> portalDataTypes = Collections.emptySet();
    private XmlUtilities xmlUtilities;
    private ResourceLoader resourceLoader;

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }
    
    @SuppressWarnings("unchecked")
    @Autowired(required=false)
    public void setDataImporters(Collection<IDataImporterExporter<? extends Object>> dataImporters) {
        final Map<PortalDataKey, IDataImporterExporter<Object>> dataImportersMap = new LinkedHashMap<PortalDataKey, IDataImporterExporter<Object>>();
        final Map<String, IDataImporterExporter<Object>> dataExportersMap = new LinkedHashMap<String, IDataImporterExporter<Object>>();
        
        final Set<IPortalDataType> portalDataTypes = new LinkedHashSet<IPortalDataType>();
        
        for (final IDataImporterExporter<?> dataImporter : dataImporters) {
            final IPortalDataType portalDataType = dataImporter.getPortalDataType();
            final String typeId = portalDataType.getTypeId();
            final PortalDataKey importDataKey = dataImporter.getImportDataKey();
            
            this.logger.debug("Registering IDataImporterExporter for '{}','{}' - {}", new Object[] {typeId, importDataKey, dataImporter});
            
            dataImportersMap.put(importDataKey, (IDataImporterExporter<Object>)dataImporter);
            dataExportersMap.put(typeId, (IDataImporterExporter<Object>)dataImporter);
            portalDataTypes.add(portalDataType);
        }
        
        this.portalDataImporters = Collections.unmodifiableMap(dataImportersMap);
        this.portalDataExporters = Collections.unmodifiableMap(dataExportersMap);
        this.portalDataTypes = Collections.unmodifiableSet(portalDataTypes);
    }
    
    @Autowired(required=false)
    public void setDataUpgraders(Collection<IDataUpgrader> dataUpgraders) {
        final Map<PortalDataKey, IDataUpgrader> dataUpgraderMap = new LinkedHashMap<PortalDataKey, IDataUpgrader>();
        
        for (final IDataUpgrader dataUpgrader : dataUpgraders) {
            final PortalDataKey sourceDataType = dataUpgrader.getSourceDataType();
            this.logger.debug("Registering IDataUpgrader for '{}' - {}", sourceDataType, dataUpgrader);
            dataUpgraderMap.put(sourceDataType, dataUpgrader);
        }
        
        this.portalDataUpgraders = Collections.unmodifiableMap(dataUpgraderMap);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IEntityImportService#importEntity(org.dom4j.Node)
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void importData(Node node) {
        final Source source = new DocumentSource(node);
        importData(source);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataImportExportService#importData(java.lang.String)
     */
    @Override
    public void importData(String resourceLocation) {
        final Resource resource = this.resourceLoader.getResource(resourceLocation);
        final InputStream resourceStream;
        try {
            resourceStream = resource.getInputStream();
        }
        catch (IOException e) {
            throw new RuntimeException("Could not load InputStream for resource: " + resourceLocation, e);
        }
        
        try {
            this.importData(new StreamSource(resourceStream));
        }
        finally {
            IOUtils.closeQuietly(resourceStream);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IEntityImportService#importEntity(javax.xml.transform.Source)
     */
    @Override
    public void importData(final Source source) {
        //Get a StAX reader for the source to determine info about the data to import
        final BufferedXMLEventReader bufferedXmlEventReader = createSourceXmlEventReader(source);
        final StartElement rootElement = getRootElement(bufferedXmlEventReader);
        final PortalDataKey portalDataKey = new PortalDataKey(rootElement);
        
        //See if there is a registered importer for the data, if so import
        final IDataImporterExporter<Object> dataImporterExporter = this.portalDataImporters.get(portalDataKey);
        if (dataImporterExporter != null) {
            this.logger.debug("Importing: {}", portalDataKey);
            final Object data = unmarshallData(bufferedXmlEventReader, dataImporterExporter);
            dataImporterExporter.importData(data);
            return;
        }
        
        //No importer, see if there is an upgrader, if so upgrade
        final IDataUpgrader dataUpgrader = this.portalDataUpgraders.get(portalDataKey);
        if (dataUpgrader != null) {
            this.logger.debug("Upgrading: {}", portalDataKey);
            
            final StAXSource staxSource;
            try {
                staxSource = new StAXSource(bufferedXmlEventReader);
            }
            catch (XMLStreamException e) {
                throw new RuntimeException("Failed to create StAXSource from original XML reader", e);
            }
            
            final DOMResult result = new DOMResult();
            final boolean doImport = dataUpgrader.upgradeData(staxSource, result);
            if (doImport) {
                //If the upgrader didn't handle the import as well wrap the result DOM in a new Source and start the import process over again
                final org.w3c.dom.Node node = result.getNode();
                final DOMSource upgradedSource = new DOMSource(node);
                this.importData(upgradedSource);
            }
            return;
        }
        
        //No importer or upgrader found, fail
        throw new IllegalArgumentException("Provided data " + portalDataKey + " has no registered importer or upgrader support.");
    }

    protected Object unmarshallData(final BufferedXMLEventReader bufferedXmlEventReader, final IDataImporterExporter<Object> dataImporterExporter) {
        final Unmarshaller unmarshaller = dataImporterExporter.getUnmarshaller();
        
        try {
            final StAXSource source = new StAXSource(bufferedXmlEventReader);
            return unmarshaller.unmarshal(source);
        }
        catch (XmlMappingException e) {
            throw new RuntimeException("Failed to map provided XML to portal data", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read the provided XML data", e);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create StAX Source to read XML data", e);
        }
    }

    protected BufferedXMLEventReader createSourceXmlEventReader(final Source source) {
        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();
        final XMLEventReader xmlEventReader;
        try {
            xmlEventReader = xmlInputFactory.createXMLEventReader(source);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XML Event Reader for data Source", e);
        }
        return new BufferedXMLEventReader(xmlEventReader, -1);
    }

    protected StartElement getRootElement(final BufferedXMLEventReader bufferedXmlEventReader) {
        XMLEvent rootElement;
        try {
            rootElement = bufferedXmlEventReader.nextTag();
            while (rootElement.getEventType() != XMLEvent.START_ELEMENT && bufferedXmlEventReader.hasNext()) {
                rootElement = bufferedXmlEventReader.nextTag();
            }
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to read root element from XML", e);
        }
        
        if (XMLEvent.START_ELEMENT != rootElement.getEventType()) {
            throw new IllegalArgumentException("Bad XML document for import, no root element could be found");
        }
        bufferedXmlEventReader.reset();
        return rootElement.asStartElement();
    }

    @Override
    public Set<IPortalDataType> getPortalDataTypes() {
        return this.portalDataTypes;
    }

    @Override
    public Set<IPortalData> getPortalData(String typeId) {
        final IDataImporterExporter<Object> dataImporterExporter = getPortalDataExporter(typeId);
        return dataImporterExporter.getPortalData();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public Node exportData(String typeId, String dataId) {
        final DocumentResult result = new DocumentResult();
        this.exportData(typeId, dataId, result);
        return result.getDocument();
    }

    @Override
    public void exportData(String typeId, String dataId, Result result) {
        final IDataImporterExporter<Object> portalDataExporter = this.getPortalDataExporter(typeId);
        final Object data = portalDataExporter.exportData(dataId);
        if (data == null) {
            return;
        }
        
        final Marshaller marshaller = portalDataExporter.getMarshaller();
        try {
            marshaller.marshal(data, result);
        }
        catch (XmlMappingException e) {
            throw new RuntimeException("Failed to map provided portal data to XML", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write the provided XML data", e);
        }
    }

    protected IDataImporterExporter<Object> getPortalDataExporter(String typeId) {
        final IDataImporterExporter<Object> dataImporterExporter = this.portalDataExporters.get(typeId);
        if (dataImporterExporter == null) {
            throw new IllegalArgumentException("No IDataImporterExporter exists for: " + typeId);   
        }
        return dataImporterExporter;
    }

	@Override
	public void deleteData(String typeId, String dataId) {
		final IDataImporterExporter<Object> portalDataExporter = this.getPortalDataExporter(typeId);
		final Object data = portalDataExporter.deleteData(dataId);
		if(data == null) {
			logger.info("portalDataExporter#deleteData returned null for typeId " + typeId + " and dataId " + dataId );
		}
	}
    
    
}
