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

import java.io.File;
import java.util.Set;

import javax.xml.transform.Result;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Service that can import, export and delete portal data.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IDataImportExportService {
    /**
     * Options that control behavior of batch import operations
     */
    public interface BatchImportOptions {
        /**
         * @return defaults to true
         */
        public boolean isRecursive();
        /**
         * @return defaults to true
         */
        public boolean isFailOnError();
        /**
         * @return defaults to true
         */
        public boolean isIngoreNonDataFiles();
    }
    
    /**
     * Import a batch of files from a directory.
     * 
     * @param directory Base directory to import from
     * @param pattern Optional ant path matcher pattern used for matching files to import. If not specified the default pattern set is used
     * @param options Optional set of options to better control the import
     */
    public void importData(File directory, String pattern, BatchImportOptions options);
    
    /**
     * Import data from the specified resource, uses a {@link ResourceLoader} find the data file
     */
    public void importData(String resource);
    
    /**
     * Import data from the specified resource
     */
    public void importData(Resource resource);
    
    /**
     * @return All portal data types that can be exported or deleted from
     */
    public Set<IPortalDataType> getPortalDataTypes();
    
    /**
     * @return All portal data for a specific portal data type, some types may return an empty set even if they contain data due size constraints.
     */
    public Set<IPortalData> getPortalData(String typeId);
    
    /**
     * Export the portal data for the specified type and id writing it to the provided XML Transformer Result 
     */
    public void exportData(String typeId, String dataId, Result result);
    
    /**
     * Delete the portal data for the specified type and id.
     * 
     * @param typeId id of the portal type to delete data from
     * @param dataId the id of the data to delete
     */
    public void deleteData(String typeId, String dataId);
}