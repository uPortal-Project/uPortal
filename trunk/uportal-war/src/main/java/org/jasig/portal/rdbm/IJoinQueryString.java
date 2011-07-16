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

package org.jasig.portal.rdbm;

import java.sql.SQLException;


/**
 * The join query interface defines methods for storing and retrieving
 * queries fragments that will be used for performing joins in SQL.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public interface IJoinQueryString {
    /**
     * Gets a query by the specified key.
     * 
     * @param key The key to find the query for.
     * @return The query for the key.
     * @throws SQLException Will be thrown if no query exists for the key
     */
    public String getQuery(String key) throws SQLException;
    
    /**
     * Adds a query for the specified key, overwriting and query that was
     * already associated with the key.
     * 
     * @param key The key to store the query under.
     * @param value The query to store under the key.
     * @throws SQLException
     */
    public void addQuery(String key, String value);
}
