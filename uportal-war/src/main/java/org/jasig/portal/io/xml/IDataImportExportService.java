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

package org.jasig.portal.io.xml;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.dom4j.Node;

/**
 * Service that can import and export portal data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDataImportExportService {
    /**
     * Import data from the XML Node
     */
    public void importData(Node node);

    /**
     * Import data from the XML Transformer Source
     */
    public void importData(Source source);
    
    /**
     * @return All portal data types that can be exported
     */
    public Set<IPortalDataType> getPortalDataTypes();
    
    /**
     * @return All portal data for a specific portal data type
     */
    public Set<IPortalData> getPortalData(QName portalDataTypeName);
    
    /**
     * Export the portal data for the specified type and id as an XML Node
     */
    public Node exportData(QName portalDataTypeName, String id);
    
    /**
     * Export the portal data for the specified type and id writing it to the provided XML Transformer Result 
     */
    public void exportData(QName portalDataTypeName, String id, Result result);
}