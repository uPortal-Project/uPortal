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
package org.apereo.portal.jgroups.protocols;

import org.apereo.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.IVersionedDatabaseUpdateHelper;
import org.apereo.portal.utils.JdbcUtils;
import org.apereo.portal.version.VersionUtils;
import org.apereo.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

/**
 * Handle changing column types in the JDBC_PING table when upgrading from 4.0.8
 *
 */
@Component
public class Version408JdbcPingDaoUpdateHelper implements IVersionedDatabaseUpdateHelper {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Version version = VersionUtils.parseVersion("4.0.8");
    private JdbcOperations jdbcOperations;

    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public String getDatabaseName() {
        return BasePortalJpaDao.PERSISTENCE_UNIT_NAME;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @AggrEventsTransactional
    @Override
    public void preUpdate() {
        //Drop the aggregate events database tables
        logger.info(
                "Dropping " + JdbcPingDao.Table.NAME + " table for upgrade from " + getVersion());
        JdbcUtils.dropTableIfExists(jdbcOperations, JdbcPingDao.Table.NAME);
    }

    @Override
    public void postUpdate() {}
}
