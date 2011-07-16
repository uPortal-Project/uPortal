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

/**
 * 
 */
package org.jasig.portal.io.xml.portlettype;

import java.util.Collections;
import java.util.Set;

import org.jasig.portal.io.xml.AbstractJaxbDataHandler;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.registry.IPortletTypeRegistry;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class PortletTypeImporterExporter extends
		AbstractJaxbDataHandler<ExternalPortletType> {

	private PortletTypePortalDataType typePortalDataType;
    private IPortletTypeRegistry portletTypeRegistry;
    
    @Autowired
    public void setTypePortalDataType(PortletTypePortalDataType typePortalDataType) {
        this.typePortalDataType = typePortalDataType;
    }

    @Autowired
	public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
		this.portletTypeRegistry = portletTypeRegistry;
	}

	@Override
	public Set<PortalDataKey> getImportDataKeys() {
		return Collections.singleton(PortletTypePortalDataType.IMPORT_40_DATA_KEY);
	}

	@Override
	public IPortalDataType getPortalDataType() {
		return this.typePortalDataType;
	}

	@Override
	public Iterable<? extends IPortalData> getPortalData() {
		return this.portletTypeRegistry.getPortletTypes();
	}

	@Transactional
	@Override
	public void importData(ExternalPortletType data) {
	    final String name = data.getName();
	    IPortletType portletType = this.portletTypeRegistry.getPortletType(name);
	    if (portletType == null) {
	        portletType = this.portletTypeRegistry.createPortletType(name, data.getUri());
	    }
	    else {
	        portletType.setCpdUri(data.getUri());
	    }
	    
	    portletType.setDescription(data.getDescription());
		this.portletTypeRegistry.savePortletType(portletType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporterExporter#exportData(java.lang.String)
	 */
	@Override
	public ExternalPortletType exportData(String id) {
		IPortletType portletType = this.portletTypeRegistry.getPortletType(Integer.parseInt(id));
		if(null == portletType) {
			return null;
		}
		
		return convert(portletType);
	}

	@Override
    public String getFileName(ExternalPortletType data) {
        return SafeFilenameUtils.makeSafeFilename(data.getName());
    }

    /*
	 * (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporterExporter#deleteData(java.lang.String)
	 */
	@Transactional
	@Override
	public ExternalPortletType deleteData(String id) {
		IPortletType portletType = this.portletTypeRegistry.getPortletType(Integer.parseInt(id));
		if(null == portletType) {
			return null;
		}
		
		ExternalPortletType result = convert(portletType);
		this.portletTypeRegistry.deleteChannelType(portletType);
		return result;
	}

	/**
	 * 
	 * @param portletType
	 * @return
	 */
	protected ExternalPortletType convert(IPortletType portletType) {
		ExternalPortletType result = new ExternalPortletType();
		result.setUri(portletType.getCpdUri());
		result.setDescription(portletType.getDescription());
		result.setName(portletType.getName());
		result.setVersion("4.0");
		return result;
	}
}
