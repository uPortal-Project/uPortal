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

package org.jasig.portal.tools.dbloader;

import java.util.Collections;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.InjectionSettingsFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;

import com.google.common.collect.ImmutableMap;

/**
 * Runs the Hibernate Schema Export tool using the specified DataSource for the target DB.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DataSourceSchemaExport implements ISchemaExport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private DataSource dataSource;
    
    private final Object configLock = new Object();
    private Configuration cachedConfiguration;
    private Settings cachedSettings;
    private String persistenceUnitName;


    /**
     * Specify the name of the EntityManagerFactory configuration.
     * <p>Default is none, indicating the default EntityManagerFactory
     * configuration. The persistence provider will throw an exception if
     * ambiguous EntityManager configurations are found.
     * @see javax.persistence.Persistence#createEntityManagerFactory(String)
     */
    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * @param dataSource the dataSource to use
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.tools.dbl.ISchemaExport#hbm2ddl(boolean, boolean, boolean, java.lang.String, boolean)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void hbm2ddl(boolean export, boolean create, boolean drop, String outputFile, boolean haltOnError) {
        this.create(export, create, drop, outputFile, haltOnError);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.dbloader.ISchemaExport#create(boolean, boolean, boolean, java.lang.String, boolean)
     */
    @Override
    public void create(final boolean export, final boolean create, final boolean drop, final String outputFile, final boolean haltOnError) {
        final SchemaExport exporter = createExporter(outputFile, haltOnError);
        
        if (drop) {
            exporter.execute(true, export, true, false);
        }
        
        if (create) {
            exporter.execute(true, export, false, true);

            if (haltOnError) {
                @SuppressWarnings("unchecked")
                final List<Exception> exceptions = exporter.getExceptions();
                if (!exceptions.isEmpty()) {
                    final Exception e = exceptions.get(exceptions.size() - 1);
                    
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException)e;
                    }
                    
                    logger.error("Schema Export threw " + exceptions.size() + " exceptions and was halted");
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected SchemaExport createExporter(String outputFile, boolean haltOnError) {
        final Configuration configuration = this.getConfiguration();
        final Settings settings = this.getSettings();
        
        final SchemaExport exporter = new SchemaExport(configuration, settings);
        exporter.setHaltOnError(haltOnError);
        if (outputFile != null) {
            exporter.setFormat(true);
            exporter.setOutputFile(outputFile);
        }
        else {
            exporter.setFormat(false);
        }
        return exporter;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.tools.dbloader.ISchemaExport#update(boolean, boolean, boolean, java.lang.String, boolean)
     */
    @Override
    public void update(boolean export, String outputFile, boolean haltOnError) {
        final SchemaUpdate updater = this.createUpdater(outputFile, haltOnError);
        
        updater.execute(true, export);

        if (haltOnError) {
            @SuppressWarnings("unchecked")
            final List<Exception> exceptions = updater.getExceptions();
            if (!exceptions.isEmpty()) {
                final Exception e = exceptions.get(exceptions.size() - 1);
                
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                
                logger.error("Schema Update threw " + exceptions.size() + " exceptions and was halted");
                throw new RuntimeException(e);
            }
        }
    }

    protected SchemaUpdate createUpdater(String outputFile, boolean haltOnError) {
        final Configuration configuration = this.getConfiguration();
        final Settings settings = this.getSettings();
        
        final SchemaUpdate updateer = new SchemaUpdate(configuration, settings);
        updateer.setHaltOnError(haltOnError);
        if (outputFile != null) {
            updateer.setFormat(true);
            updateer.setOutputFile(outputFile);
        }
        else {
            updateer.setFormat(false);
        }
        return updateer;
    }
    
    protected final Configuration getConfiguration() {
        synchronized (configLock) {
            Configuration configuration = this.cachedConfiguration;
            if (configuration != null) {
                return configuration;
            }
            
            //Use DefaultPersistenceUnitManager and Ejb3Configuration to build hibernate configuration object
            final DefaultPersistenceUnitManager defaultPersistenceUnitManager = new DefaultPersistenceUnitManager();
            defaultPersistenceUnitManager.setDataSourceLookup(new SingleDataSourceLookup(dataSource));
            defaultPersistenceUnitManager.setDefaultDataSource(dataSource);
            defaultPersistenceUnitManager.afterPropertiesSet();
            final PersistenceUnitInfo persistenceUnitInfo = defaultPersistenceUnitManager.obtainPersistenceUnitInfo(this.persistenceUnitName);
            
            final Ejb3Configuration configBuilder = new Ejb3Configuration();
            final Ejb3Configuration jpaConfiguration = configBuilder.configure(
                    persistenceUnitInfo,
                    ImmutableMap.of(
                            "hibernate.cache.use_query_cache", "false",
                            "hibernate.cache.use_second_level_cache", "false"));
            
            configuration = jpaConfiguration.getHibernateConfiguration();

            //Build the entity mappings
            configuration.buildMappings();

            //Cache then return the config
            this.cachedConfiguration = configuration;
            return configuration;
        }
    }

    protected final Settings getSettings() {
        synchronized (configLock) {
            Settings settings = this.cachedSettings;
            if (settings != null) {
                return settings;
            }

            final InjectionSettingsFactory injectionSettingsFactory = new InjectionSettingsFactory();
            injectionSettingsFactory.setConnectionProviderInjectionData(Collections.singletonMap("dataSource", this.dataSource));
            
            final Configuration configuration = this.getConfiguration();
            settings = injectionSettingsFactory.buildSettings(configuration.getProperties());

            //Cache then config the settings
            this.cachedSettings = settings;
            return settings;
        }
    }
}
