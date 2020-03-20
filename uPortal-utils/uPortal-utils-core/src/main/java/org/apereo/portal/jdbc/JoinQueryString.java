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
package org.apereo.portal.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Partial implementation of {@link IJoinQueryString} which provides the implementation of storing
 * and retrieving join queries. It also requires a test query be specified.
 */
public abstract class JoinQueryString implements IJoinQueryString {

    private final Map<String, String> queryStrings = new HashMap<>();

    private final String testJoin;

    /**
     * Creates a new {@link JoinQueryString}.
     *
     * @param testQuery The query to use to test if joins in the class are supported.
     */
    /* package-private */ JoinQueryString(final String testQuery) {
        if (testQuery == null) throw new IllegalArgumentException("testQuery must not be null");

        testJoin = testQuery;
    }

    /**
     * Gets the query to use to test if joins in this class are supported.
     *
     * @return The query to use to test if joins in this class are supported.
     */
    /* package-private */ String getTestJoin() {
        return testJoin;
    }

    /** @see IJoinQueryString#getQuery(java.lang.String) */
    @Override
    public String getQuery(final String key) throws SQLException {
        final String query = queryStrings.get(key);

        if (query == null) {
            throw new SQLException("Missing query");
        }

        return query;
    }

    /** @see IJoinQueryString#addQuery(java.lang.String, java.lang.String) */
    @Override
    public void addQuery(final String key, final String value) {
        queryStrings.put(key, value);
    }
}
