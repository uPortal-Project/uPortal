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
package org.jasig.portal.shell;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataHandlerService;
import org.jasig.portal.io.xml.PortalDataHandlerServiceUtils;
import org.jasig.portal.jpa.VersionedDataUpdater;
import org.jasig.portal.tools.DbTest;
import org.jasig.portal.tools.dbloader.DbLoaderConfigBuilder;
import org.jasig.portal.tools.dbloader.IDbLoader;
import org.jasig.portal.tools.dbloader.ISchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;

/**
 * Utility used by the groovy scripts generated in build.xml and passed into PortalShell 
 * 
 * @author Eric Dalquist
 */
@Service("portalShellBuildHelper")
public class PortalShellBuildHelperImpl implements PortalShellBuildHelper {
    private static final Pattern COMMA_DELIM = Pattern.compile(",");

    private IDbLoader dbLoader;
    private Map<String, ISchemaExport> schemaExportBeans;
    private IPortalDataHandlerService portalDataHandlerService;
    private IUserIdentityStore userIdentityStore;
    private DbTest dbTest;
    private VersionedDataUpdater versionedDataUpdater;
    
    @Autowired
    public void setVersionedDataUpdater(VersionedDataUpdater versionedDataUpdater) {
        this.versionedDataUpdater = versionedDataUpdater;
    }

    @Autowired
    public void setDbLoader(IDbLoader dbLoader) {
        this.dbLoader = dbLoader;
    }

    @Autowired
    public void setSchemaExportBeans(Map<String, ISchemaExport> schemaExportBeans) {
        this.schemaExportBeans = schemaExportBeans;
    }

    @Autowired
    public void setPortalDataHandlerService(IPortalDataHandlerService portalDataHandlerService) {
        this.portalDataHandlerService = portalDataHandlerService;
    }

    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
    }

    @Autowired
    public void setDbTest(DbTest dbTest) {
        this.dbTest = dbTest;
    }
    
    @Override
    public void dbTest() {
        this.dbTest.printDbInfo();
    }

    @Override
    public void db(String target, String tablesFile, String dataFile, String scriptFile, boolean dropTables,
            boolean createTables, boolean populateTables) {
        try {
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("");
            
            //DbLoader Script
            final DbLoaderConfigBuilder dbLoaderConfig = new DbLoaderConfigBuilder();
            dbLoaderConfig.setTablesFile(tablesFile);
            dbLoaderConfig.setDataFile(dataFile);
            if (StringUtils.isNotBlank(scriptFile)) {
                dbLoaderConfig.setScriptFile(scriptFile);
            }
            dbLoaderConfig.setDropTables(dropTables);
            dbLoaderConfig.setCreateTables(createTables);
            dbLoaderConfig.setPopulateTables(populateTables);
            PortalShell.LOGGER.info("Running DbLoader with: " + dbLoaderConfig);
            dbLoader.process(dbLoaderConfig);
        }
        catch (Exception e) {
            throw new RuntimeException(target + " for " + tablesFile + " and " + dataFile + " failed", e);
        }
    }

    @Override
    public void hibernateCreate(String target, String databaseQualifier, boolean export, String outputFile) {
        final ISchemaExport schemaExportBean = getSchemaExport(databaseQualifier);
        if (schemaExportBean == null) {
            throw new RuntimeException(target + " could not find schemaExportBean " + databaseQualifier);
        }

        try {
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("Hibernate Create DDL: " + databaseQualifier);

            outputFile = StringUtils.trimToNull(outputFile);

            schemaExportBean.create(export, outputFile, true);
            
            this.versionedDataUpdater.postInitDatabase(databaseQualifier);
        }
        catch (Exception e) {
            throw new RuntimeException(target + " for " + databaseQualifier + " failed", e);
        }
    }

    @Override
    public void hibernateDrop(String target, String databaseQualifier, boolean export, String outputFile) {
        final ISchemaExport schemaExportBean = this.getSchemaExport(databaseQualifier);
        if (schemaExportBean == null) {
            throw new RuntimeException(target + " could not find schemaExportBean " + databaseQualifier);
        }

        try {
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("Hibernate Drop DDL: " + databaseQualifier);

            outputFile = StringUtils.trimToNull(outputFile);

            schemaExportBean.drop(export, outputFile, true);
        }
        catch (Exception e) {
            throw new RuntimeException(target + " for " + databaseQualifier + " failed", e);
        }
    }

    @Override
    public void hibernateUpdate(String target, String databaseQualifier, boolean export, String outputFile) {
        //TODO add upgrade-state to up_version table to handle a failure during the hbm2ddl update
        this.versionedDataUpdater.preUpdateDatabase(databaseQualifier);
        
        final ISchemaExport schemaExportBean = this.getSchemaExport(databaseQualifier);
        if (schemaExportBean == null) {
            throw new RuntimeException(target + " could not find schemaExportBean " + databaseQualifier);
        }

        try {
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("Hibernate Update DDL: " + databaseQualifier);

            outputFile = StringUtils.trimToNull(outputFile);

            schemaExportBean.update(export, outputFile, true);
        }
        catch (Exception e) {
            throw new RuntimeException(target + " for " + databaseQualifier + " failed", e);
        }
        
        this.versionedDataUpdater.postUpdateDatabase(databaseQualifier);
    }

    @Override
    public void dataList(String target, String type) {
        try {
            if (StringUtils.isBlank(type)) {
                //List Data Types Script
                PortalShell.LOGGER.info("");
                PortalShell.LOGGER.info("");
                PortalShell.LOGGER.info("Export and Delete Support by Data Type");
                PortalDataHandlerServiceUtils.format(portalDataHandlerService, PortalShell.LOGGER);
                PortalShell.LOGGER.info("Add -Dtype=dataType To get a list of data keys for the type");
            }
            else {
                //List Data Script
                final Iterable<? extends IPortalData> data = portalDataHandlerService.getPortalData(type);
                PortalShell.LOGGER.info("");
                PortalShell.LOGGER.info("");
                PortalShell.LOGGER.info("All " + type + " data");
                PortalDataHandlerServiceUtils.format(data, PortalShell.LOGGER);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(target + " failed", e);
        }
    }

    @Override
    public void dataExport(String target, String dataDir, String type, String sysid, String logDir) {
        PortalShell.LOGGER.info("");
        PortalShell.LOGGER.info("");

        final File dataDirFile = new File(dataDir);
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(sysid)) {
            try {
                for (final String id : COMMA_DELIM.split(sysid)) {
                    PortalShell.LOGGER.info("Exporting Data " + id + " of type " + type + " to: " + dataDir);
                    portalDataHandlerService.exportData(type, id, dataDirFile);
                }
            }
            catch (Exception e) {
                throw new RuntimeException(target + " to " + dataDir + " of " + type + " " + sysid + " failed", e);
            }
        }
        else if (StringUtils.isNotBlank(type)) {
            try {
                final Set<String> types = ImmutableSet.copyOf(COMMA_DELIM.split(type));

                if (types.size() == 1) {
                    PortalShell.LOGGER.info("Exporting All Data of type " + type + " to: " + dataDir);
                }
                else {
                    PortalShell.LOGGER.info("Exporting All Data of types " + types + " to: " + dataDir);
                }

                portalDataHandlerService.exportAllDataOfType(types,
                        dataDirFile,
                        new IPortalDataHandlerService.BatchExportOptions().setLogDirectoryParent(logDir));
            }
            catch (Exception e) {
                throw new RuntimeException(target + " to " + dataDir + " of " + type + " failed", e);
            }
        }
        else {
            try {
                PortalShell.LOGGER.info("Exporting All Data to: " + dataDir);
                portalDataHandlerService.exportAllData(dataDirFile,
                        new IPortalDataHandlerService.BatchExportOptions().setLogDirectoryParent(logDir));
            }
            catch (Exception e) {
                throw new RuntimeException(target + " to " + dataDir + " failed", e);
            }
        }
    }
    
    @Override
    public void dataImport(String target, String dataDir, String pattern, String file, String logDir) {
        dataImport(target, dataDir, pattern, file, null, logDir);
    }

    @Override
    public void dataImport(String target, String dataDir, String pattern, String file, String archive, String logDir) {
        PortalShell.LOGGER.info("");
        PortalShell.LOGGER.info("");

        if (!StringUtils.isBlank(file)) {
            PortalShell.LOGGER.info("Importing Data from: " + file);
            try {
                portalDataHandlerService.importData(new FileSystemResource(file));
            }
            catch (Exception e) {
                throw new RuntimeException(target + " for " + file + " failed", e);
            }
        }
        else if (!StringUtils.isBlank(archive)) {
            PortalShell.LOGGER.info("Importing Data from: " + archive);
            try {
                portalDataHandlerService.importDataArchive(new FileSystemResource(archive),
                        new IPortalDataHandlerService.BatchImportOptions().setLogDirectoryParent(logDir));
            }
            catch (Exception e) {
                throw new RuntimeException(target + " for " + archive + " failed", e);
            }
            
        }
        else if (!StringUtils.isBlank(dataDir)) {
            PortalShell.LOGGER.info("Importing Data from: " + dataDir + " that matches " + pattern);
            pattern = StringUtils.trimToNull(pattern);

            try {
                portalDataHandlerService.importData(new File(dataDir),
                        pattern,
                        new IPortalDataHandlerService.BatchImportOptions().setLogDirectoryParent(logDir));
            }
            catch (Exception e) {
                if (pattern != null) {
                    throw new RuntimeException(target + " from " + dataDir + " matching " + pattern + " failed", e);
                }
                else {
                    throw new RuntimeException(target + " from " + dataDir + " failed", e);
                }
            }
        }
        else {
            throw new RuntimeException(target + " failed: One of dataDir, file, or archive must be specified");
        }
    }

    @Override
    public void dataDelete(String target, String type, String sysid) {
            //Data Delete Script
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("");

        for (final String id : COMMA_DELIM.split(sysid)) {
            try {
                PortalShell.LOGGER.info("Deleting Data " + id + " of type " + type);
                portalDataHandlerService.deleteData(type, id);
            }
            catch (Exception e) {
                throw new RuntimeException(target + " for " + type + " and " + id + " failed", e);
            }
        }
    }
    
    @Override
    public void deleteUser(String target, String user) {
        try {
            //Delete USer Script
            PortalShell.LOGGER.info("");
            PortalShell.LOGGER.info("");
        
            userIdentityStore.removePortalUID(user);
        }
        catch (Exception e) {
            throw new RuntimeException(target + " for " + user + " failed", e);
        }
    }
    
    private ISchemaExport getSchemaExport(String persistenceUnit) {
        for (final ISchemaExport schemaExport : this.schemaExportBeans.values()) {
            if (persistenceUnit.equals(schemaExport.getPersistenceUnitName())) {
                return schemaExport;
            }
        }
        
        throw new IllegalArgumentException("No ISchemaExport bean found for persistence unit: '" + persistenceUnit + "'");
    }
}
