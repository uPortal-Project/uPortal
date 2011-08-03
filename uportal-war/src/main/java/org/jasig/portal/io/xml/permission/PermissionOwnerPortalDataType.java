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

package org.jasig.portal.io.xml.permission;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.jasig.portal.io.xml.AbstractPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;


/**
 * Describes a set of permissions in the portal
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PermissionOwnerPortalDataType extends AbstractPortalDataType {
	public static final QName PERMISSION_OWNER_QNAME = new QName(
	        "https://source.jasig.org/schemas/uportal/io/permission-owner", 
	        "permission-owner");
	
    public static final PortalDataKey IMPORT_40_DATA_KEY = new PortalDataKey(
    		PERMISSION_OWNER_QNAME, 
            null,
            "4.0");
    private static final List<PortalDataKey> PORTAL_DATA_KEYS = Arrays.asList(IMPORT_40_DATA_KEY);
    
    public PermissionOwnerPortalDataType() {
        super(PERMISSION_OWNER_QNAME);
    }
    
    @Override
    public List<PortalDataKey> getDataKeyImportOrder() {
        return PORTAL_DATA_KEYS;
    }
    
    @Override
    public String getTitleCode() {
        return "Permission Owner";
    }

    @Override
    public String getDescriptionCode() {
        return "Permission Owner";
    }
}
