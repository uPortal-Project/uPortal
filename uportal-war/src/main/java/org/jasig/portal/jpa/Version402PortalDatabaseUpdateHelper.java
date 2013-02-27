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

import org.jasig.portal.jpa.BasePortalJpaDao.PortalTransactional;
import org.jasig.portal.utils.JdbcUtils;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.om.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

/**
 * Update the portal db from 4.0.2
 * 
 * @author Eric Dalquist
 */
@Component
public class Version402PortalDatabaseUpdateHelper implements IVersionedDatabaseUpdateHelper {
    private final Version version = VersionUtils.parseVersion("4.0.2");
    
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

    @PortalTransactional
    @Override
    public void preUpdate() {
        JdbcUtils.dropTableIfExists(this.jdbcOperations, "UP_MUTEX");
    }

    @Override
    public void postUpdate() {
    }
}
