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

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;
import org.jasig.portal.rdbm.IDatabaseMetadata;

/**
 * Gathers jdbc driver and database names and versions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DatabaseInfoCollector implements IPortalDataCollector {
    private IDatabaseMetadata databaseMetadata;
    
    /**
     * @param databaseMetadata the databaseMetadata to set
     */
    public void setDatabaseMetadata(IDatabaseMetadata databaseMetadata) {
        this.databaseMetadata = databaseMetadata;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public Map<String, String> getData() {
        final Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("jdbcDriverName", databaseMetadata.getJdbcDriver());
        data.put("jdbcDriverVersion", databaseMetadata.getJdbcDriverVersion());
        data.put("databaseName", databaseMetadata.getDatabaseProductName());
        data.put("databaseVersion", databaseMetadata.getDatabaseProductVersion());
        
        return data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "uPortalDatabase";
    }
}
