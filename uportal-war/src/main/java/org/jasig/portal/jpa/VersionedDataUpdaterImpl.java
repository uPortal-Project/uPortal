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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.jasig.portal.jpa.BasePortalJpaDao.PortalTransactional;
import org.jasig.portal.version.VersionUtils;
import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

/**
 * Runs steps needed to init/upgrade/update the database based on the version information
 * retrieved from {@link VersionDao}
 * 
 * @author Eric Dalquist
 */
@Component("versionedDataUpdater")
public class VersionedDataUpdaterImpl {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private Map<String, Version> requiredProductVersions;
    private VersionDao versionDao;
    
    private Map<String, SortedSet<IVersionedDatabaseUpdateHelper>> databaseUpdateHelpers;
    
    @Resource(name = "productVersions")
    public void setRequiredProductVersions(Map<String, Version> requiredProductVersions) {
        this.requiredProductVersions = ImmutableMap.copyOf(requiredProductVersions);
    }
    
    @Autowired
    public void setVersionedDatabaseUpdateHelpers(Collection<IVersionedDatabaseUpdateHelper> helpers) {
        this.databaseUpdateHelpers = new HashMap<String, SortedSet<IVersionedDatabaseUpdateHelper>>();
        
        for (final IVersionedDatabaseUpdateHelper helper : helpers) {
            final String databaseName = helper.getDatabaseName();
            
            SortedSet<IVersionedDatabaseUpdateHelper> updaters = this.databaseUpdateHelpers.get(databaseName);
            if (updaters == null) {
                updaters = new TreeSet<IVersionedDatabaseUpdateHelper>(VersionedDatabaseUpdateHelperComparator.INSTANCE);
                this.databaseUpdateHelpers.put(databaseName, updaters);
            }
            
            updaters.add(helper);
        }
    }
    
    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

	/**
	 * Initialize code-configured versions in the database. Nothing else is currently needed for init 
	 */
    @PortalTransactional
	public void postInitDatabase(String product) {
        final Version version = this.requiredProductVersions.get(product);
        if (version == null) {
            throw new IllegalArgumentException("No Version is configured for: " + product);
        }
        this.versionDao.setVersion(product, version);
	}
    
    public void preUpdateDatabase(String product) {
        final SortedSet<IVersionedDatabaseUpdateHelper> updateHelpers = this.databaseUpdateHelpers.get(product);
        if (updateHelpers == null || updateHelpers.isEmpty()) {
            logger.info("No IVersionedDatabaseUpdateHelpers configured for database {}, nothing will be done in preUpdate", product);
            return;
        }
        
        final Version dbVersion = getAndVerifyDatabaseVersionForUpdate(product);
        
        //updateHelpers is sorted oldest to newest by version so iterate through and run the updaters that apply 
        for (final IVersionedDatabaseUpdateHelper updateHelper : updateHelpers) {
            if (dbVersion.isBefore(updateHelper.getVersion())) {
                updateHelper.preUpdate();
            }
        }
    }
    
    public void postUpdateDatabase(String product) {
        final SortedSet<IVersionedDatabaseUpdateHelper> updateHelpers = this.databaseUpdateHelpers.get(product);
        if (updateHelpers == null || updateHelpers.isEmpty()) {
            logger.info("No IVersionedDatabaseUpdateHelpers configured for database {}, nothing will be done in postUpdate", product);
            this.versionDao.setVersion(product, codeVersion);
            return;
        }
        
        final Version dbVersion = getAndVerifyDatabaseVersionForUpdate(product);
        
        //updateHelpers is sorted oldest to newest by version so iterate through and run the updaters that apply 
        for (final IVersionedDatabaseUpdateHelper updateHelper : updateHelpers) {
            if (dbVersion.isBefore(updateHelper.getVersion())) {
                updateHelper.postUpdate();
            }
        }
        
        this.versionDao.setVersion(product, codeVersion);
    }
    
    protected Version getAndVerifyDatabaseVersionForUpdate(String product) {
        final Version codeVersion = this.requiredProductVersions.get(product);
        Version dbVersion = this.versionDao.getVersion(product);
        if (dbVersion == null) {
            //If null assume version 4.0.0
            dbVersion = VersionUtils.parseVersion("4.0.0");
        }
        
        if (!VersionUtils.canUpdate(dbVersion, codeVersion)) {
            throw new IllegalStateException("Cannot update " + product + " db from " + dbVersion + " to " + codeVersion);
        }
        
        if (codeVersion.equals(dbVersion)) {
            logger.info("{} code version matches database version. No update needed.", product);
        }
        
        return dbVersion;
    }
    
    private static final class VersionedDatabaseUpdateHelperComparator implements Comparator<IVersionedDatabaseUpdateHelper> {
        public static final VersionedDatabaseUpdateHelperComparator INSTANCE = new VersionedDatabaseUpdateHelperComparator();
        
        @Override
        public int compare(IVersionedDatabaseUpdateHelper o1, IVersionedDatabaseUpdateHelper o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.getVersion().compareTo(o2.getVersion());
        }
    }
//		switch (dbVersion.getPatch()) {
//		    default: {
//		        throw new IllegalStateException("Someone forgot to add handling for upgrading from database version " + dbVersion + " please report this as a bug");
//		    }
//			//Runs if updating from 4.0.0 or earlier
//			case 0: {
//				//Nothing special needed for 4.0.0 -> 4.0.1
//			}
//			//Runs if updating from 4.0.1 or earlier
//			case 1: {
//				//Nothing special needed for 4.0.1 -> 4.0.2
//			}
//			//Runs if updating from 4.0.2 or earlier
//			case 2: {
//				//Drop the UP_MUTEX table
////				dropTable(this.portalJdbcOperations, "UP_MUTEX");
//			}
//			//Runs if updating from 4.0.3 or earlier
//			case 3: {
//				//Nothing special needed for 4.0.3 -> 4.0.4
//			}
//			//Runs if updating from 4.0.4 or earlier
//			case 4: {
//                //Nothing special needed for 4.0.4 -> 4.0.5
//			}
//			/** ADD NEW CASE HERE FOR EACH NEW PATCH RELEASE **/
//		}
}
