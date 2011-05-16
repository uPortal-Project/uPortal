/**
 * 
 */
package org.jasig.portal.io.xml.portlettype;

import javax.xml.namespace.QName;

import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class PortletTypePortalDataType implements IPortalDataType {

	public static final PortletTypePortalDataType INSTANCE = new PortletTypePortalDataType();
	public static final QName PORTLET_DEFINITION_NAME = new QName("https://source.jasig.org/schemas/uportal/io/portlet-type", "portlet-type");
    public static final PortalDataKey IMPORT_DATA_KEY = new PortalDataKey(
            PORTLET_DEFINITION_NAME, 
            null,
            "4.0");
	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return PORTLET_DEFINITION_NAME.getLocalPart();
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Portlet Type";
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IPortalDataType#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Types of portlets published in the portal";
	}

}
