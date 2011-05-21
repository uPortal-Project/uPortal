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
