/**
 * 
 */
package org.jasig.portal.portlets.importexport;

import org.jasig.portal.io.xml.IPortalDataType;

/**
 * Interim {@link IPortalDataType} implementation used for Cernunnos-only supported
 * portal data types.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class CernnunosOnlyPortalDataType implements IPortalDataType {

	private final String legacyTypeName;
	
	/**
	 * 
	 * @param legacyTypeName
	 */
	public CernnunosOnlyPortalDataType(String legacyTypeName) {
		this.legacyTypeName = legacyTypeName;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return legacyTypeName;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Cernnunos Only - " + legacyTypeName;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Cernnunos Only - " + legacyTypeName;
	}

}
