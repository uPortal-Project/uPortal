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
import org.jasig.portal.tools.dbloader.ISchemaExport;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Update the aggregate stats db from 4.0.4
 * 
 * @author Eric Dalquist
 */
@Component
public class Version404AggrEventsDatabaseUpdateHelper implements IVersionedDatabaseUpdateHelper {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Version version = VersionUtils.parseVersion("4.0.4");
    private ISchemaExport schemaExport;

    @Autowired
    @Qualifier(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
    public void setSchemaExport(ISchemaExport schemaExport) {
        this.schemaExport = schemaExport;
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
        //Drop the aggregate events database tables
        logger.info("Dropping aggregate event tables for upgrade from " + getVersion());
        this.schemaExport.drop(true, null, true);
        
        //Create the aggregate events database tables
        logger.info("Creating aggregate event tables for upgrade from " + getVersion());
        this.schemaExport.create(true, null, true);
        
        logger.warn("IMPORTANT: You must import your event aggregation configuration again!\n\tex: ant data-import -Dfile=/path/to/uportal/uportal-war/src/main/data/default_entities/event-aggregation/default.event-aggregation.xml");
    }

    @Override
    public void postUpdate() {
    }
}
