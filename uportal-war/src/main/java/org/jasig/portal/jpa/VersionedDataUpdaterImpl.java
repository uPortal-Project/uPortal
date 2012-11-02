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
import java.util.Collections;
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

@Component("versionedDataUpdater")
public class VersionedDataUpdaterImpl implements VersionedDataUpdater {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private Map<String, Version> requiredProductVersions = Collections.emptyMap();
    private VersionDao versionDao;
    
    private Map<String, SortedSet<IVersionedDatabaseUpdateHelper>> databaseUpdateHelpers = Collections.emptyMap();
    
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

    @Override
    @PortalTransactional
	public void postInitDatabase(String product) {
        final Version version = this.requiredProductVersions.get(product);
        if (version == null) {
            throw new IllegalArgumentException("No Version is configured for: " + product);
        }
        
        logger.info("PostInit - Set {} version to {}", product, version);
        this.versionDao.setVersion(product, version);
	}
    
    @Override
    @OpenEntityManager(unitName = BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void preUpdateDatabase(String product) {
        //This happens first because even if there are no updaters we need to make sure the attempted update is valid 
        final Version dbVersion = getAndVerifyDatabaseVersionForUpdate(product);
        
        final SortedSet<IVersionedDatabaseUpdateHelper> updateHelpers = this.databaseUpdateHelpers.get(product);
        if (updateHelpers == null || updateHelpers.isEmpty()) {
            logger.info("No IVersionedDatabaseUpdateHelpers configured for database {}, nothing will be done in preUpdate", product);
        }
        else {
            //updateHelpers is sorted oldest to newest by version so iterate through and run the updaters that apply 
            for (final IVersionedDatabaseUpdateHelper updateHelper : updateHelpers) {
                final Version updateVersion = updateHelper.getVersion();
                if (dbVersion.equals(updateVersion) || dbVersion.isBefore(updateVersion)) {
                    logger.info("PreUpdate {} from {} to {}", product, dbVersion, updateVersion);
                    updateHelper.preUpdate();
                    logger.info("PreUpdate {} from {} to {} complete", product, dbVersion, updateVersion);
                }
            }
        }
    }
    
    @Override
    @PortalTransactional
    public void postUpdateDatabase(String product) {
        //This happens first because even if there are no updaters we need to make sure the attempted update is valid 
        final Version dbVersion = getAndVerifyDatabaseVersionForUpdate(product);
        
        final SortedSet<IVersionedDatabaseUpdateHelper> updateHelpers = this.databaseUpdateHelpers.get(product);
        if (updateHelpers == null || updateHelpers.isEmpty()) {
            logger.info("No IVersionedDatabaseUpdateHelpers configured for database {}, nothing will be done in postUpdate", product);
        }
        else {
            //updateHelpers is sorted oldest to newest by version so iterate through and run the updaters that apply 
            for (final IVersionedDatabaseUpdateHelper updateHelper : updateHelpers) {
                final Version updateVersion = updateHelper.getVersion();
                if (dbVersion.equals(updateVersion) || dbVersion.isBefore(updateVersion)) {
                    logger.info("PostUpdate {} from {} to {}", product, dbVersion, updateVersion);
                    updateHelper.postUpdate();
                    logger.info("PostUpdate {} from {} to {} complete", product, dbVersion, updateVersion);
                }
            }
        }
        
        //Update the db version number
        final Version codeVersion = this.requiredProductVersions.get(product);
        logger.info("PostUpdate - Set {} version to {}", product, codeVersion);
        this.versionDao.setVersion(product, codeVersion);
    }
    
    protected Version getAndVerifyDatabaseVersionForUpdate(String product) {
        final Version codeVersion = this.requiredProductVersions.get(product);
        if (codeVersion == null) {
            throw new IllegalStateException("No code version configured for " + product);
        }
        
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
}
