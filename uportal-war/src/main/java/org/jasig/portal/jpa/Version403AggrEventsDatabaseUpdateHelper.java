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
package org.jasig.portal.jpa;

import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.utils.JdbcUtils;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.om.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

/**
 * Update the aggregate stats db from 4.0.3
 * 
 * @author Eric Dalquist
 */
@Component
public class Version403AggrEventsDatabaseUpdateHelper implements IVersionedDatabaseUpdateHelper {
    private final Version version = VersionUtils.parseVersion("4.0.3");
    private JdbcOperations jdbcOperations;
    
    @Autowired
    @Qualifier(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public String getDatabaseName() {
        return BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @AggrEventsTransactional
    @Override
    public void preUpdate() {
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_LOGIN_EVENT_AGGREGATE__UIDS");
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_LOGIN_EVENT_AGGREGATE");
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_EVENT_AGGR_CONF_INTRVL_EXC");
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_EVENT_AGGR_CONF_INTRVL_INC");
    }

    @Override
    public void postUpdate() {
    }
}
