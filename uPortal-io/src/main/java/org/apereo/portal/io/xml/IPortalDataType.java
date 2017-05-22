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

import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLEventReader;

/**
 * Describes a type of portal data that can be imported, exported, or deleted via the {@link
 * IPortalDataHandlerService}
 *
 */
public interface IPortalDataType {
    /** @return The unique name of this portal data type, must be a valid XML element name. */
    public String getTypeId();
    /** @return Message code to use for displaying the user readable title of this data type */
    public String getTitleCode();
    /**
     * @return Message code to use for displaying the user readable description of this data type
     */
    public String getDescriptionCode();
    /**
     * @return The {@link PortalDataKey}s that can be imported for this data type and the order that
     *     the must be imported in. All data for each {@link PortalDataKey} will be imported in
     *     parallel.
     */
    public List<PortalDataKey> getDataKeyImportOrder();

    /**
     * Post processes the resolved {@link PortalDataKey}, allows for data that needs to be retyped
     * based on file name, returning multiple data keys for data that needs a multi-pass import, and
     * other operations.
     *
     * @param systemId The url, file name or other system identifier for the data
     * @param portalDataKey The already parsed key, must be contained in the set returned by {@link
     *     #getDataKeyImportOrder()}
     * @param reader The XMLEventReader (set to the start of the XML event stream) to use for
     *     processing the input
     * @return One or more PortalDataKeys that represent the data
     */
    public Set<PortalDataKey> postProcessPortalDataKey(
            String systemId, PortalDataKey portalDataKey, XMLEventReader reader);
}
