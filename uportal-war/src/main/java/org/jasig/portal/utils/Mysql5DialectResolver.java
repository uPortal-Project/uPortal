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

package org.jasig.portal.utils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.resolver.AbstractDialectResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Mysql5DialectResolver extends AbstractDialectResolver {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
        String databaseName = metaData.getDatabaseProductName();
        int databaseMajorVersion = metaData.getDatabaseMajorVersion();

        if ( "MySQL".equals( databaseName ) && databaseMajorVersion == 5) {
            return new MySQL5InnoDBDialect();
        }
        
        return null;
    }
}
