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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.dom4j.Node;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;

/**
 * Uses Spring OXM {@link Marshaller} and {@link Unmarshaller} objects to convert portal data to/from XML.
 * Uses {@link IDataImporterExporter} objects to list available data and read and write data from the portal
 * data stores.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("dataImportExportService")
public class JaxbDataImportExportService implements IDataImportExportService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private Map<QName, IDataImporterExporter<Object>> portalDataHandlers = Collections.emptyMap();
    private Set<IPortalDataType> portalDataTypes = Collections.emptySet();

    @Autowired
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Autowired
    public void setUnmarshaller(@Qualifier("entityUnmarshaller") Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }
    
    @Autowired
    public void setEntityImporters(Collection<IDataImporterExporter<Object>> entityImporters) {
        final Map<QName, IDataImporterExporter<Object>> entityImportersMap = new LinkedHashMap<QName, IDataImporterExporter<Object>>();
        final Set<IPortalDataType> portalDataTypes = new LinkedHashSet<IPortalDataType>();
        
        for (final IDataImporterExporter<Object> entityImporter : entityImporters) {
            final IPortalDataType portalDataType = entityImporter.getPortalDataType();
            final QName name = portalDataType.getName();
            this.logger.debug("Registering IDataImporterExporter for '{}' - {}", name, entityImporter);
            entityImportersMap.put(name, entityImporter);
            portalDataTypes.add(portalDataType);
        }
        
        this.portalDataHandlers = Collections.unmodifiableMap(entityImportersMap);
        this.portalDataTypes = Collections.unmodifiableSet(portalDataTypes);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IEntityImportService#importEntity(org.dom4j.Node)
     */
    @Override
    public void importData(Node node) {
        final Source source = new DocumentSource(node);
        
        importData(source);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IEntityImportService#importEntity(javax.xml.transform.Source)
     */
    @Override
    public void importData(final Source source) {
        final JAXBElement<?> result;
        try {
            result = (JAXBElement<?>)this.unmarshaller.unmarshal(source);
        }
        catch (XmlMappingException e) {
            //TODO better error handling, try to figure out what went wrong and provide a solution in the exception message
            throw new RuntimeException("Failed to map provided XML to portal data", e);
        }
        catch (IOException e) {
            //TODO better error handling, try to figure out what went wrong and provide a solution in the exception message
            throw new RuntimeException("Failed to read the provided XML data", e);
        }
        
        final QName entityName = result.getName();
        this.logger.debug("Unmarshalled entity: {}", entityName);
        
        final IDataImporterExporter<Object> entityImporter = getDataImporterExporter(entityName);
        
        entityImporter.importData(result.getValue());
    }

    @Override
    public Set<IPortalDataType> getPortalDataTypes() {
        return this.portalDataTypes;
    }

    @Override
    public Set<IPortalData> getPortalData(QName portalDataTypeName) {
        final IDataImporterExporter<Object> dataImporterExporter = getDataImporterExporter(portalDataTypeName);

        return dataImporterExporter.getPortalData();
    }

    @Override
    public Node exportData(QName portalDataTypeName, String id) {
        final DocumentResult result = new DocumentResult();
        this.exportData(portalDataTypeName, id, result);
        return result.getDocument();
    }

    @Override
    public void exportData(QName portalDataTypeName, String id, Result result) {
        final IDataImporterExporter<Object> dataImporterExporter = getDataImporterExporter(portalDataTypeName);
        final Object data = dataImporterExporter.exportData(id);
        try {
            this.marshaller.marshal(data, result);
        }
        catch (XmlMappingException e) {
            //TODO better error handling, try to figure out what went wrong and provide a solution in the exception message
            throw new RuntimeException("Failed to map provided portal data to XML", e);
        }
        catch (IOException e) {
            //TODO better error handling, try to figure out what went wrong and provide a solution in the exception message
            throw new RuntimeException("Failed to write the provided XML data", e);
        }
    }

    /**
     * Get the IDataImporterExporter for the QName, throws an exception if there is no IDataImporterExporter
     * registered for the name. 
     */
    protected IDataImporterExporter<Object> getDataImporterExporter(QName portalDataTypeName) {
        final IDataImporterExporter<Object> dataImporterExporter = this.portalDataHandlers.get(portalDataTypeName);
        if (dataImporterExporter == null) {
            throw new IllegalArgumentException("No IDataImporterExporter exists for: " + portalDataTypeName);
        }
        return dataImporterExporter;
    }
}
