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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.jasig.portal.io.xml.AbstractPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;

import com.google.common.collect.ImmutableSet;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
public class PersonAttributesGroupStorePortalDataType extends AbstractPortalDataType {
    public static final QName PERSON_ATTRIBUTE_GROUP_STORE_TYPE_QNAME = new QName("pags-group");

    public static final PortalDataKey IMPORT_PAGS_41_DATA_KEY = new PortalDataKey(
            PERSON_ATTRIBUTE_GROUP_STORE_TYPE_QNAME, 
            "classpath://org/jasig/portal/io/import-pags-group_v4-1.crn",
            null);
    /**
     * Pseudo type used to enforce the importing of all groups before any members
     */
    public static final PortalDataKey IMPORT_PAGS_GROUP_41_DATA_KEY = new PortalDataKey(
            PERSON_ATTRIBUTE_GROUP_STORE_TYPE_QNAME, 
            "classpath://org/jasig/portal/io/import-pags-group_v4-1.crn",
            "GROUP");
    /**
     * Pseudo type used to enforce the importing of all groups before any members
     */
    public static final PortalDataKey IMPORT_PAGS_MEMBERS_41_DATA_KEY = new PortalDataKey(
            PERSON_ATTRIBUTE_GROUP_STORE_TYPE_QNAME, 
            "classpath://org/jasig/portal/io/import-pags-group_v4-1.crn",
            "MEMBERS");
    
    private static final List<PortalDataKey> PERSON_ATTRIBUTE_GROUP_STORE_DATA_KEYS = Arrays.asList(
            IMPORT_PAGS_41_DATA_KEY,
            IMPORT_PAGS_GROUP_41_DATA_KEY,
            IMPORT_PAGS_MEMBERS_41_DATA_KEY);

    private static final Set<PortalDataKey> PAGS_GROUP_MEMBERS_41_KEYS = ImmutableSet.of(
            IMPORT_PAGS_GROUP_41_DATA_KEY,
            IMPORT_PAGS_MEMBERS_41_DATA_KEY);
    
    public PersonAttributesGroupStorePortalDataType() {
        super(PERSON_ATTRIBUTE_GROUP_STORE_TYPE_QNAME);
    }

    @Override
    public List<PortalDataKey> getDataKeyImportOrder() {
        return PERSON_ATTRIBUTE_GROUP_STORE_DATA_KEYS;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalDataType#getTitle()
     */
    @Override
    public String getTitleCode() {
        return "Person Attribute Group Store Type";
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IPortalDataType#getDescription()
     */
    @Override
    public String getDescriptionCode() {
        return "Person Attribute Group Store";
    }
    @Override
    public String getTypeId() {
        return "pags-group";
    }
    @Override
    public Set<PortalDataKey> postProcessPortalDataKey(String systemId, PortalDataKey portalDataKey, XMLEventReader reader) {
        if (IMPORT_PAGS_41_DATA_KEY.equals(portalDataKey)) {
            //Split the import into two phases
            return PAGS_GROUP_MEMBERS_41_KEYS;
        }
        
        return Collections.singleton(portalDataKey);
    }
}
