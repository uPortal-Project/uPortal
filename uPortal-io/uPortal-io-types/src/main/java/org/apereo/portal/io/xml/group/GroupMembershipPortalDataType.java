/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.io.xml.group;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import org.apereo.portal.io.xml.AbstractPortalDataType;
import org.apereo.portal.io.xml.IExportAllPortalDataType;
import org.apereo.portal.io.xml.PortalDataKey;
import org.springframework.stereotype.Component;

/** Describes a group with members in the portal */
@Component("groupMembershipPortalDataType")
public class GroupMembershipPortalDataType extends AbstractPortalDataType implements IExportAllPortalDataType {

    public static final int ORDER = 60;

    public static final QName LEGACY_GROUP_QNAME = new QName("group");

    public static final String TYPE_ID = "group-membership";

    /** @deprecated used for importing old data files */
    @Deprecated
    public static final PortalDataKey IMPORT_30_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/jasig/portal/io/import-group_membership_v3-0.crn",
                    null);

    @Deprecated
    public static final PortalDataKey IMPORT_32_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/jasig/portal/io/import-group_membership_v3-2.crn",
                    null);

    public static final PortalDataKey IMPORT_50_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/apereo/portal/io/import-group_membership_v5-0.crn",
                    null);

    /** Pseudo type used to enforce the importing of all groups before any members */
    public static final PortalDataKey IMPORT_GROUP_50_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/apereo/portal/io/import-group_membership_v5-0.crn",
                    "GROUP");

    /** Pseudo type used to enforce the importing of all groups before any members */
    public static final PortalDataKey IMPORT_MEMBERS_50_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/apereo/portal/io/import-group_membership_v5-0.crn",
                    "MEMBERS");

    /** Pseudo type used to enforce the importing of all groups before any members */
    @Deprecated
    public static final PortalDataKey IMPORT_GROUP_32_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/jasig/portal/io/import-group_membership_v3-2.crn",
                    "GROUP");

    /** Pseudo type used to enforce the importing of all groups before any members */
    @Deprecated
    public static final PortalDataKey IMPORT_MEMBERS_32_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/jasig/portal/io/import-group_membership_v3-2.crn",
                    "MEMBERS");

    /** Pseudo type used to enforce the importing of all groups before any members */
    @Deprecated
    public static final PortalDataKey IMPORT_GROUP_30_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/jasig/portal/io/import-group_membership_v3-0.crn",
                    "GROUP");

    /** Pseudo type used to enforce the importing of all groups before any members */
    @Deprecated
    public static final PortalDataKey IMPORT_MEMBERS_30_DATA_KEY =
            new PortalDataKey(
                    LEGACY_GROUP_QNAME,
                    "classpath://org/jasig/portal/io/import-group_membership_v3-0.crn",
                    "MEMBERS");

    private static final List<PortalDataKey> PORTAL_DATA_KEYS =
            Arrays.asList(
                    IMPORT_30_DATA_KEY,
                    IMPORT_32_DATA_KEY,
                    IMPORT_50_DATA_KEY,
                    IMPORT_GROUP_30_DATA_KEY,
                    IMPORT_GROUP_32_DATA_KEY,
                    IMPORT_GROUP_50_DATA_KEY,
                    IMPORT_MEMBERS_30_DATA_KEY,
                    IMPORT_MEMBERS_32_DATA_KEY,
                    IMPORT_MEMBERS_50_DATA_KEY);

    private static final Set<PortalDataKey> GROUP_MEMBERS_30_KEYS =
            ImmutableSet.of(IMPORT_GROUP_30_DATA_KEY, IMPORT_MEMBERS_30_DATA_KEY);
    private static final Set<PortalDataKey> GROUP_MEMBERS_32_KEYS =
            ImmutableSet.of(IMPORT_GROUP_32_DATA_KEY, IMPORT_MEMBERS_32_DATA_KEY);
    private static final Set<PortalDataKey> GROUP_MEMBERS_50_KEYS =
            ImmutableSet.of(IMPORT_GROUP_50_DATA_KEY, IMPORT_MEMBERS_50_DATA_KEY);

    public GroupMembershipPortalDataType() {
        super(ORDER, LEGACY_GROUP_QNAME);
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public List<PortalDataKey> getDataKeyImportOrder() {
        return PORTAL_DATA_KEYS;
    }

    @Override
    public String getTitleCode() {
        return "Group with Members";
    }

    @Override
    public String getDescriptionCode() {
        return "Group with Members";
    }

    @Override
    public Set<PortalDataKey> postProcessPortalDataKey(
            String systemId, PortalDataKey portalDataKey, XMLEventReader reader) {
        if (IMPORT_30_DATA_KEY.equals(portalDataKey)) {
            // Split the import into two phases
            return GROUP_MEMBERS_30_KEYS;
        }
        if (IMPORT_32_DATA_KEY.equals(portalDataKey)) {
            // Split the import into two phases
            return GROUP_MEMBERS_32_KEYS;
        }
        if (IMPORT_50_DATA_KEY.equals(portalDataKey)) {
            // Split the import into two phases
            return GROUP_MEMBERS_50_KEYS;
        }

        return Collections.singleton(portalDataKey);
    }
}
