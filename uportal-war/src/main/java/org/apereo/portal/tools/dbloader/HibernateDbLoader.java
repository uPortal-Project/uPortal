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
package org.apereo.portal.tools.dbloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.apereo.portal.hibernate.HibernateConfigurationAware;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Can drop and/or create tables based on an XML table definition file and populate tables based on
 * a XML data definition file. Table creation is done using the Hibernate mapping APIs to allow for
 * a full range of database support.
 *
 */
@Component("dbLoader")
@Lazy
@DependsOn({
    BasePortalJpaDao.PERSISTENCE_UNIT_NAME + "EntityManagerFactory",
    "hibernateConfigurationAwareInjector"
})
public class HibernateDbLoader
        implements IDbLoader, ResourceLoaderAware, HibernateConfigurationAware {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private Configuration configuration;
    private Dialect dialect;
    private JdbcOperations jdbcOperations;
    private TransactionOperations transactionOperations;
    private ResourceLoader resourceLoader;

    @Autowired
    public void setJdbcOperations(
            @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME) JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Autowired
    public void setTransactionOperations(
            @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
                    TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Override
    public boolean supports(String persistenceUnit) {
        return BasePortalJpaDao.PERSISTENCE_UNIT_NAME.equals(persistenceUnit);
    }

    @Override
    public void setConfiguration(
            String persistenceUnit, HibernateConfiguration hibernateConfiguration) {
        final SessionFactoryImplementor sessionFactory = hibernateConfiguration.getSessionFactory();
        this.dialect = sessionFactory.getDialect();
        this.configuration = hibernateConfiguration.getConfiguration();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void process(DbLoaderConfig configuration)
            throws ParserConfigurationException, SAXException, IOException {
        final String scriptFile = configuration.getScriptFile();
        final List<String> script;
        if (scriptFile == null) {
            script = null;
        } else {
            script = new LinkedList<String>();
        }

        final ITableDataProvider tableData = this.loadTables(configuration, dialect);

        //Handle table drop/create
        if (configuration.isDropTables() || configuration.isCreateTables()) {
            //Load Table object model
            final Map<String, Table> tables = tableData.getTables();

            final Mapping mapping = this.configuration.buildMapping();
            final String defaultCatalog =
                    this.configuration.getProperty(Environment.DEFAULT_CATALOG);
            final String defaultSchema = this.configuration.getProperty(Environment.DEFAULT_SCHEMA);

            final Map<String, DataAccessException> failedSql =
                    new LinkedHashMap<String, DataAccessException>();

            //Generate and execute drop table scripts
            if (configuration.isDropTables()) {
                final List<String> dropScript =
                        this.dropScript(tables.values(), dialect, defaultCatalog, defaultSchema);

                if (script == null) {
                    this.logger.info("Dropping existing tables");
                    for (final String sql : dropScript) {
                        this.logger.info(sql);
                        try {
                            jdbcOperations.update(sql);
                        } catch (NonTransientDataAccessResourceException dae) {
                            throw dae;
                        } catch (DataAccessException dae) {
                            failedSql.put(sql, dae);
                        }
                    }
                } else {
                    script.addAll(dropScript);
                }
            }

            //Log any drop/create statements that failed
            for (final Map.Entry<String, DataAccessException> failedSqlEntry :
                    failedSql.entrySet()) {
                this.logger.warn(
                        "'"
                                + failedSqlEntry.getKey()
                                + "' failed to execute due to "
                                + failedSqlEntry.getValue());
            }

            //Generate and execute create table scripts
            if (configuration.isCreateTables()) {
                final List<String> createScript =
                        this.createScript(
                                tables.values(), dialect, mapping, defaultCatalog, defaultSchema);

                if (script == null) {
                    this.logger.info("Creating tables");
                    for (final String sql : createScript) {
                        this.logger.info(sql);
                        jdbcOperations.update(sql);
                    }
                } else {
                    script.addAll(createScript);
                }
            }
        }

        //Perform database population
        if (script == null && configuration.isPopulateTables()) {
            this.logger.info("Populating database");
            final Map<String, Map<String, Integer>> tableColumnTypes =
                    tableData.getTableColumnTypes();
            this.populateTables(configuration, tableColumnTypes);
        }

        //Write out the script file
        if (script != null) {
            for (final ListIterator<String> iterator = script.listIterator();
                    iterator.hasNext();
                    ) {
                final String sql = iterator.next();
                iterator.set(sql + ";");
            }

            final File outputFile = new File(scriptFile);
            FileUtils.writeLines(outputFile, script);
            this.logger.info("Saved DDL to: " + outputFile.getAbsolutePath());
        }
    }

    protected ITableDataProvider loadTables(DbLoaderConfig configuration, Dialect dialect)
            throws ParserConfigurationException, SAXException, IOException {
        //Locate tables.xml
        final String tablesFileName = configuration.getTablesFile();
        final Resource tablesFile = this.resourceLoader.getResource(tablesFileName);
        if (!tablesFile.exists()) {
            throw new IllegalArgumentException("Could not find tables file: " + tablesFile);
        }

        //Setup parser with custom handler to generate Table model and parse
        final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        final TableXmlHandler dh = new TableXmlHandler(dialect);
        saxParser.parse(new InputSource(tablesFile.getInputStream()), dh);

        return dh;
    }

    /** Generate the drop scripts and add them to the script list */
    @SuppressWarnings("unchecked")
    protected List<String> dropScript(
            Collection<Table> tables,
            Dialect dialect,
            String defaultCatalog,
            String defaultSchema) {
        final List<String> script = new ArrayList<String>(tables.size() * 2);

        if (dialect.dropConstraints()) {
            for (final Table table : tables) {
                if (table.isPhysicalTable()) {
                    for (final Iterator<ForeignKey> subIter = table.getForeignKeyIterator();
                            subIter.hasNext();
                            ) {
                        final ForeignKey fk = subIter.next();
                        if (fk.isPhysicalConstraint()) {
                            script.add(fk.sqlDropString(dialect, defaultCatalog, defaultSchema));
                        }
                    }
                }
            }
        }

        for (final Table table : tables) {
            if (table.isPhysicalTable()) {
                script.add(table.sqlDropString(dialect, defaultCatalog, defaultSchema));
            }
        }

        return script;
    }

    /** Generate create scripts and add them to the script list */
    @SuppressWarnings("unchecked")
    protected List<String> createScript(
            Collection<Table> tables,
            Dialect dialect,
            Mapping mapping,
            String defaultCatalog,
            String defaultSchema) {
        final List<String> script = new ArrayList<String>(tables.size() * 2);

        for (final Table table : tables) {
            if (table.isPhysicalTable()) {
                script.add(table.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
            }
        }

        for (final Table table : tables) {
            if (table.isPhysicalTable()) {
                if (!dialect.supportsUniqueConstraintInCreateAlterTable()) {
                    for (final Iterator<UniqueKey> subIter = table.getUniqueKeyIterator();
                            subIter.hasNext();
                            ) {
                        final UniqueKey uk = subIter.next();
                        final String constraintString =
                                uk.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
                        if (constraintString != null) {
                            script.add(constraintString);
                        }
                    }
                }

                for (final Iterator<Index> subIter = table.getIndexIterator();
                        subIter.hasNext();
                        ) {
                    final Index index = subIter.next();
                    script.add(
                            index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
                }

                if (dialect.hasAlterTable()) {
                    for (final Iterator<ForeignKey> subIter = table.getForeignKeyIterator();
                            subIter.hasNext();
                            ) {
                        final ForeignKey fk = subIter.next();
                        if (fk.isPhysicalConstraint()) {
                            script.add(
                                    fk.sqlCreateString(
                                            dialect, mapping, defaultCatalog, defaultSchema));
                        }
                    }
                }
            }
        }

        return script;
    }

    protected void populateTables(
            DbLoaderConfig configuration, Map<String, Map<String, Integer>> tableColumnTypes)
            throws ParserConfigurationException, SAXException, IOException {
        //Locate tables.xml
        final String dataFileName = configuration.getDataFile();
        final Resource dataFile = this.resourceLoader.getResource(dataFileName);
        if (!dataFile.exists()) {
            throw new IllegalArgumentException("Could not find data file: " + dataFile);
        }

        //Setup parser with custom handler to generate Table model and parse
        final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        final DataXmlHandler dh =
                new DataXmlHandler(jdbcOperations, transactionOperations, tableColumnTypes);
        saxParser.parse(new InputSource(dataFile.getInputStream()), dh);
    }
}
