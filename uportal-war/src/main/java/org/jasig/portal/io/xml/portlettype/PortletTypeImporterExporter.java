/**
 * 
 */
package org.jasig.portal.io.xml.portlettype;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jasig.portal.io.xml.AbstractJaxbIDataImporterExporter;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.registry.IPortletTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class PortletTypeImporterExporter extends
		AbstractJaxbIDataImporterExporter<ExternalPortletType> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortletTypeRegistry portletTypeRegistry;
    
    @Autowired
	public void setPortletTypeRegistry(IPortletTypeRegistry portletTypeRegistry) {
		this.portletTypeRegistry = portletTypeRegistry;
	}

	@Override
	public PortalDataKey getImportDataKey() {
		return PortletTypePortalDataType.IMPORT_DATA_KEY;
	}

	@Override
	public IPortalDataType getPortalDataType() {
		return PortletTypePortalDataType.INSTANCE;
	}

	@Override
	public Set<IPortalData> getPortalData() {
		List<IPortletType> portletTypes = this.portletTypeRegistry.getPortletTypes();
		Set<IPortalData> results = new LinkedHashSet<IPortalData>(portletTypes);
		return Collections.unmodifiableSet(results);
	}

	@Transactional
	@Override
	public void importData(ExternalPortletType data) {
		IPortletType result = this.portletTypeRegistry.createPortletType(data.getName(), data.getCpdUri());
		result.setDescription(data.getDescription());
		this.portletTypeRegistry.savePortletType(result);
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
		} else {
			ExternalPortletType result = convert(portletType);
			this.portletTypeRegistry.deleteChannelType(portletType);
			return result;
		}
	}

	/**
	 * 
	 * @param portletType
	 * @return
	 */
	protected ExternalPortletType convert(IPortletType portletType) {
		ExternalPortletType result = new ExternalPortletType();
		result.setCpdUri(portletType.getCpdUri());
		result.setDescription(portletType.getDescription());
		result.setId(portletType.getId());
		result.setName(portletType.getName());
		result.setVersion("4.0");
		return result;
	}
}
