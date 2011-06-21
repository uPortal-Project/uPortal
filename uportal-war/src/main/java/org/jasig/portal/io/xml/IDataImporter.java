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

import org.springframework.oxm.Unmarshaller;

/**
 * Defines a class that can import a specific type of portal data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDataImporter<T> {
    /**
     * @return The {@link PortalDataKey}s that this importer can import
     */
    public Set<PortalDataKey> getImportDataKeys();
    
    /**
     * @param data The data to import
     */
    public void importData(T data);
    
    /**
     * @return The Unmarshaller to use to convert the data from XML to the required type T
     */
    public Unmarshaller getUnmarshaller();
}
