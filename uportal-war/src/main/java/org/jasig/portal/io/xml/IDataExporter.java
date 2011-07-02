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

import org.springframework.oxm.Marshaller;

/**
 * Defines a class that can export a specific type of portal data
 * 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDataExporter<T> {
    /**
     * @return The type descriptor for the 
     */
    public IPortalDataType getPortalDataType();
    
    /**
     * @return All of the available data for this type
     */
    public Iterable<? extends IPortalData> getPortalData();
    
    /**
     * Return portal data to export, if no data exists for the id null should be returned.
     */
    public T exportData(String id);
    
    /**
     * Return a safe file name. Safe is defined as not containing characters that are invalid or difficult to
     * type on common file systems or names that are reserved on common file systems.
     */
    public String getFileName(T data);
    
    /**
     * @return The Marshaller to use the convert the data to XML
     */
    public Marshaller getMarshaller();
}
