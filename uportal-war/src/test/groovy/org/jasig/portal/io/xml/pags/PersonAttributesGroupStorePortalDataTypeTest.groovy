/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.io.xml.pags;

import java.util.Collections;
import java.util.Set;

import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;

import static org.junit.Assert.*;
import org.junit.Test;

class PersonAttributesGroupStorePortalDataTypeTest {

    IPortalDataType portalDataType = new PersonAttributesGroupStorePortalDataType();

    @Test
    void testPostProcessPortalDataKey() {

        // v4.1 keys
        Set<PortalDataKey> keys41 = portalDataType.postProcessPortalDataKey(
            null,   // unused
            PersonAttributesGroupStorePortalDataType.IMPORT_PAGS_41_DATA_KEY,
            null);  // unused
        assertEquals('Incorrect output from postProcessPortalDataKey() -- ',
            PersonAttributesGroupStorePortalDataType.PAGS_GROUP_MEMBERS_41_KEYS,
            keys41);

        // Single, specific key
        Set<PortalDataKey> groupKey41 = portalDataType.postProcessPortalDataKey(
            null,   // unused
            PersonAttributesGroupStorePortalDataType.IMPORT_PAGS_GROUP_41_DATA_KEY,
            null);  // unused
        assertEquals('Incorrect output from postProcessPortalDataKey() -- ',
            Collections.singleton(PersonAttributesGroupStorePortalDataType.IMPORT_PAGS_GROUP_41_DATA_KEY),
            groupKey41);

    }

}
