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
package org.apereo.portal.utils;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Utilities for JDBC operations
 *
 */
public final class JdbcUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUtils.class);

    private JdbcUtils() {}

    /**
     * Check if the named table exists, if it does drop it, calling the preDropCallback first
     *
     * @param jdbcOperations {@link JdbcOperations} used to check if the table exists and execute
     *     the drop
     * @param table The name of the table to drop, case insensitive
     * @param preDropCallback The callback to execute immediately before the table is dropped
     * @return The result returned from the callback
     */
    public static final <T> T dropTableIfExists(
            JdbcOperations jdbcOperations,
            final String table,
            final Function<JdbcOperations, T> preDropCallback) {
        LOGGER.info("Dropping table: " + table);

        final boolean tableExists = doesTableExist(jdbcOperations, table);

        if (tableExists) {
            final T ret = preDropCallback.apply(jdbcOperations);
            jdbcOperations.execute("DROP TABLE " + table);
            return ret;
        }

        return null;
    }

    /** Check if the specified table exists */
    public static boolean doesTableExist(JdbcOperations jdbcOperations, final String table) {

        final boolean tableExists =
                jdbcOperations.execute(
                        new ConnectionCallback<Boolean>() {
                            @Override
                            public Boolean doInConnection(Connection con)
                                    throws SQLException, DataAccessException {

                                final DatabaseMetaData metaData = con.getMetaData();
                                final ResultSet tables =
                                        metaData.getTables(
                                                null, null, null, new String[] {"TABLE"});
                                while (tables.next()) {
                                    final String dbTableName = tables.getString("TABLE_NAME");
                                    if (table.equalsIgnoreCase(dbTableName)) {
                                        return true;
                                    }
                                }

                                return false;
                            }
                        });
        return tableExists;
    }

    /** @see #dropTableIfExists(JdbcOperations, String, Function) */
    public static final void dropTableIfExists(JdbcOperations jdbcOperations, final String table) {
        dropTableIfExists(jdbcOperations, table, Functions.<JdbcOperations>identity());
    }
}
