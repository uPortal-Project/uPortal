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
import java.util.Hashtable;
import java.util.Map;

/**
 * Partial implemenation of {@link IJoinQueryString} which provides
 * the implemention of storing and retrieving join queries. It also
 * requires a test query be specified.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public abstract class JoinQueryString implements IJoinQueryString {

    final private Map queryStrings = new Hashtable();

    final private String testJoin;

    /**
     * Creates a new {@link JoinQueryString}.
     * 
     * @param testQuery The query to use to test if joins in the class are supported.
     */
    protected JoinQueryString(final String testQuery) {
        if (testQuery == null)
            throw new IllegalArgumentException("testQuery must not be null");
        
        testJoin = testQuery;
    }

    /**
     * Gets the query to use to test if joins in this class are supported.
     * 
     * @return The query to use to test if joins in this class are supported.
     */
    protected String getTestJoin() {
        return testJoin;
    }

    /**
     * @see org.jasig.portal.rdbm.IJoinQueryString#getQuery(java.lang.String)
     */
    public String getQuery(final String key) throws SQLException {
        final String query = (String)queryStrings.get(key);
        
        if (query == null) {
            throw new SQLException("Missing query");
        }
        
        return query;
    }

    /**
     * @see org.jasig.portal.rdbm.IJoinQueryString#addQuery(java.lang.String, java.lang.String)
     */
    public void addQuery(final String key, final String value) {
        queryStrings.put(key, value);
    }
}