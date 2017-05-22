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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServer2005Dialect;
import org.hibernate.service.jdbc.dialect.internal.AbstractDialectResolver;

/**
 */
public class PortalDialectResolver extends AbstractDialectResolver {
    private static final long serialVersionUID = 1L;

    protected final Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
        final String databaseName = metaData.getDatabaseProductName();
        final int databaseMajorVersion = metaData.getDatabaseMajorVersion();
        final int databaseMinorVersion = metaData.getDatabaseMinorVersion();

        if ("MySQL".equals(databaseName) && 5 == databaseMajorVersion) {
            return new MySQL5InnoDBCompressedDialect();
        }

        if ("PostgreSQL".equals(databaseName)
                && 8 == databaseMajorVersion
                && databaseMinorVersion <= 1) {
            return new PostgreSQL81Dialect();
        }

        // This is due to a jTDS not supporting SQL Server 2008+, hence does not support some new types like TIME.
        if ("Microsoft SQL Server".equals(databaseName) && databaseMajorVersion > 9) {
            return new SQLServer2005Dialect();
        }

        return null;
    }
}
