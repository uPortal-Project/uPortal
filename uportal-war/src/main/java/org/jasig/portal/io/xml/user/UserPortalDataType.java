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

package org.jasig.portal.io.xml.user;

import javax.xml.namespace.QName;

import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;


/**
 * Describes a User data type in the portal
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UserPortalDataType implements IPortalDataType {
    public static final UserPortalDataType INSTANCE = new UserPortalDataType();
    public static final QName USER_NAME = new QName("https://source.jasig.org/schemas/uportal/io/user", "user");
    public static final PortalDataKey IMPORT_40_DATA_KEY = new PortalDataKey(
            USER_NAME, 
            null,
            "4.0");
    public static final PortalDataKey IMPORT_32_DATA_KEY = new PortalDataKey(
            new QName("user"), 
            "classpath://org/jasig/portal/io/import-user_v3-2.crn",
            null);
    
    @Override
    public String getTypeId() {
        return USER_NAME.getLocalPart();
    }

    @Override
    public String getTitle() {
        return "User";
    }

    @Override
    public String getDescription() {
        return "Portal Users";
    }
}
