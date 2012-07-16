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
package org.jasig.portal.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.jasig.portal.jpa.BaseAggrEventsJpaDao;
import org.jasig.portal.jpa.BaseAggrEventsJpaDao.AggrEventsTransactional;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.BasePortalJpaDao.PortalTransactional;
import org.jasig.portal.shell.PortalShellBuildHelper;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

/**
 * Runs steps needed to init/upgrade/update the database based on the version information
 * retrieved from {@link VersionDao}
 * 
 * @author Eric Dalquist
 */
@Component
public class VersionedDataUpdater {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private Map<String, Version> requiredProductVersions;
    private VersionDao versionDao;
    private JdbcOperations portalJdbcOperations;
    private JdbcOperations aggrEventsJdbcOperations;
    private PortalShellBuildHelper portalShellBuildHelper;
    
    @Resource(name = "productVersions")
    public void setRequiredProductVersions(Map<String, Version> requiredProductVersions) {
        this.requiredProductVersions = ImmutableMap.copyOf(requiredProductVersions);
    }
    
    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
	public void setPortalJdbcOperations(JdbcOperations portalJdbcOperations) {
		this.portalJdbcOperations = portalJdbcOperations;
	}

    @Autowired
    @Qualifier(BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME)
	public void setAggrEventsJdbcOperations(JdbcOperations aggrEventsJdbcOperations) {
		this.aggrEventsJdbcOperations = aggrEventsJdbcOperations;
	}

    @Autowired
	public void setPortalShellBuildHelper(
			PortalShellBuildHelper portalShellBuildHelper) {
		this.portalShellBuildHelper = portalShellBuildHelper;
	}

	/**
	 * Initialize code-configured versions in the database. Nothing else is currently needed for init 
	 */
    @PortalTransactional
	public void initDatabase() {
		for (final Map.Entry<String, Version> productVersionEntry : this.requiredProductVersions.entrySet()) {
            final String product = productVersionEntry.getKey();
            final Version version = productVersionEntry.getValue();
            this.versionDao.setVersion(product, version.getMajor(), version.getMinor(), version.getPatch());
		}
	}
	
    @PortalTransactional
    @AggrEventsTransactional
	public void prepareDatabaseForUpdate() {
		final Version codeVersion = this.requiredProductVersions.get("uPortal");
		Version dbVersion = this.versionDao.getVersion("uPortal");
		if (dbVersion == null) {
			//If null assume version 4.0.0
			dbVersion = VersionUtils.parseVersion("4.0.0");
		}
		
		if (codeVersion.equals(dbVersion)) {
			logger.info("uPortal" + " code version matches db version. No update needed.");
			return;
		}
		
		if (!VersionUtils.canUpdate(dbVersion, codeVersion)) {
			throw new IllegalStateException("Cannot update uPortal db from " + dbVersion + " to " + codeVersion);
		}
		
		switch (dbVersion.getPatch()) {
			//Runs if updating from 4.0.0 or earlier
			case 0: {
				//Nothing special needed for 4.0.0 -> 4.0.1
			}
			//Runs if updating from 4.0.1 or earlier
			case 1: {
				//Nothing special needed for 4.0.1 -> 4.0.2
			}
			//Runs if updating from 4.0.2 or earlier
			case 2: {
				//Drop the UP_MUTEX table
				dropTable(this.portalJdbcOperations, "UP_MUTEX");
			}
			//Runs if updating from 4.0.3 or earlier
			case 3: {
				//Drop aggregation config tables
				dropTable(this.aggrEventsJdbcOperations, "UP_EVENT_AGGR_CONF_INTRVL_EXC");
				dropTable(this.aggrEventsJdbcOperations, "UP_EVENT_AGGR_CONF_INTRVL_INC");
			}
			//Runs if updating from 4.0.4 or earlier
			case 4: {
				//Drop the aggregate events database tables
				portalShellBuildHelper.hibernateCreate("db-hibernate", "aggrEventsDbHibernateExport", true, false, true, " ", false);
				//Create the aggregate events database tables
				portalShellBuildHelper.hibernateCreate("db-hibernate", "aggrEventsDbHibernateExport", true, true, false, " ", true);
				
				logger.warn("IMPORTANT: You must import your event aggregation configuration again!\n\tex: ant data-import -Dfile=/path/to/uportal/uportal-war/src/main/data/default_entities/event-aggregation/default.event-aggregation.xml");
			}
			/** ADD NEW CASE HERE FOR EACH NEW PATCH RELEASE **/
		}
	}
    
    protected final void dropTable(JdbcOperations jdbcOperations, final String table) {
    	final boolean tableExists = jdbcOperations.execute(new ConnectionCallback<Boolean>() {
			@Override
			public Boolean doInConnection(Connection con) throws SQLException,
					DataAccessException {
				
				final DatabaseMetaData metaData = con.getMetaData();
				final ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
				while (tables.next()) {
					final String dbTableName = tables.getString("TABLE_NAME");
					if (table.equalsIgnoreCase(dbTableName)) {
						return true;
					}
				}
				
				return false;
			}
		});
    	
    	if (tableExists) {
    		jdbcOperations.execute("DROP TABLE " + table);
		}
    }
}
