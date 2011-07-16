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


/**
 * Defines a class that can export a specific type of portal data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDataDeleter<T> {
    /**
     * @return The type descriptor for the 
     */
    public IPortalDataType getPortalDataType();
    
    /**
     * @return All of the available data for this type
     */
    public Iterable<? extends IPortalData> getPortalData();
    
    /**
     * Deletes the data specified by the id. If data existed it will be returned.
     */
    public T deleteData(String id);
}
