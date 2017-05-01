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
package org.apereo.portal.io.xml;

import java.util.EnumSet;
import java.util.Formatter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apereo.portal.logging.AppendableLogger;
import org.apereo.portal.logging.LogLevel;
import org.apereo.portal.utils.TableFormatter;
import org.apereo.portal.utils.TableFormatter.TableEntry;
import org.slf4j.Logger;

/**
 */
public final class PortalDataHandlerServiceUtils {
    private PortalDataHandlerServiceUtils() {}

    private enum Operations {
        EXPORT,
        DELETE
    }

    public static void format(IPortalDataHandlerService dataHandlerService, Logger l) {
        final Formatter f = new Formatter(new AppendableLogger(l, LogLevel.INFO));

        final Map<String, Set<Operations>> portalDataTypes = new TreeMap<String, Set<Operations>>();

        final Iterable<IPortalDataType> exportPortalDataTypes =
                dataHandlerService.getExportPortalDataTypes();
        addDataTypes(portalDataTypes, exportPortalDataTypes, Operations.EXPORT);

        final Iterable<IPortalDataType> deletePortalDataTypes =
                dataHandlerService.getDeletePortalDataTypes();
        addDataTypes(portalDataTypes, deletePortalDataTypes, Operations.DELETE);

        final TableFormatter tableFormatter =
                new TableFormatter(
                        new TableEntry<String>("Data Type", "-", "s"),
                        new TableEntry<String>("Export", "-", "s"),
                        new TableEntry<String>("Delete", "-", "s"));

        for (final Map.Entry<String, Set<Operations>> portalDataTypeEntry :
                portalDataTypes.entrySet()) {
            final String typeId = portalDataTypeEntry.getKey();
            final Set<Operations> ops = portalDataTypeEntry.getValue();
            tableFormatter.addRow(
                    new TableEntry<String>(typeId, "-", "s"),
                    new TableEntry<Boolean>(ops.contains(Operations.EXPORT), "-", "b"),
                    new TableEntry<Boolean>(ops.contains(Operations.DELETE), "-", "b"));
        }

        tableFormatter.format(f);
    }

    public static void format(Iterable<? extends IPortalData> data, Logger l) {
        final Formatter f = new Formatter(new AppendableLogger(l, LogLevel.INFO));

        final TableFormatter tableFormatter =
                new TableFormatter(
                        new TableEntry<String>("sysid", "-", "s"),
                        new TableEntry<String>("Description", "-", "s"));

        for (final IPortalData it : data) {
            final String dataId = it.getDataId();

            String dataTitle = it.getDataTitle();
            if (dataTitle == null || dataTitle.equals(dataId)) {
                dataTitle = "";
            }

            tableFormatter.addRow(
                    new TableEntry<String>(dataId, "-", "s"),
                    new TableEntry<String>(dataTitle, "-", "s"));
        }

        tableFormatter.format(f);
    }

    /**
     * @param portalDataTypes
     * @param exportPortalDataTypes
     * @param operation
     */
    private static void addDataTypes(
            final Map<String, Set<Operations>> portalDataTypes,
            final Iterable<IPortalDataType> exportPortalDataTypes,
            final Operations operation) {
        for (final IPortalDataType portalDataType : exportPortalDataTypes) {
            final String typeId = portalDataType.getTypeId();
            Set<Operations> supports = portalDataTypes.get(typeId);
            if (supports == null) {
                supports = EnumSet.of(operation);
                portalDataTypes.put(typeId, supports);
            } else {
                supports.add(operation);
            }
        }
    }
}
