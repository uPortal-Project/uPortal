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
package org.apereo.portal.io.xml.layout;

import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import org.apereo.portal.io.xml.AbstractPortalDataType;
import org.apereo.portal.io.xml.IExportAllPortalDataType;
import org.apereo.portal.io.xml.PortalDataKey;
import org.springframework.stereotype.Component;

/** Describes a fragment owner's layout in the portal */
@Component("fragmentLayoutPortalDataType")
public class FragmentLayoutPortalDataType extends AbstractPortalDataType implements IExportAllPortalDataType {

    public static final int ORDER = 150;

    public static final QName LEGACY_LAYOUT_QNAME = new QName("fragment-layout");

    public static final PortalDataKey IMPORT_32_DATA_KEY =
            new PortalDataKey(
                    LEGACY_LAYOUT_QNAME,
                    "classpath://org/jasig/portal/io/import-layout_v3-2.crn",
                    null);

    /** @deprecated used for importing old data files */
    @Deprecated
    public static final PortalDataKey IMPORT_30_DATA_KEY =
            new PortalDataKey(
                    LEGACY_LAYOUT_QNAME,
                    "classpath://org/jasig/portal/io/import-layout_v3-0.crn",
                    null);

    /** @deprecated used for importing old data files */
    @Deprecated
    public static final PortalDataKey IMPORT_26_DATA_KEY =
            new PortalDataKey(
                    LEGACY_LAYOUT_QNAME,
                    "classpath://org/jasig/portal/io/import-layout_v2-6.crn",
                    null);

    private static final List<PortalDataKey> PORTAL_DATA_KEYS =
            Arrays.asList(IMPORT_26_DATA_KEY, IMPORT_30_DATA_KEY, IMPORT_32_DATA_KEY);

    public FragmentLayoutPortalDataType() {
        super(ORDER, LEGACY_LAYOUT_QNAME);
    }

    @Override
    public List<PortalDataKey> getDataKeyImportOrder() {
        return PORTAL_DATA_KEYS;
    }

    @Override
    public String getTitleCode() {
        return "Fragment Layout";
    }

    @Override
    public String getDescriptionCode() {
        return "Fragment Owner Layout";
    }
}
