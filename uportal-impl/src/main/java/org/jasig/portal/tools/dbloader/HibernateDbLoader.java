/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Can drop and/or create tables based on an XML table definition file and populate tables
 * based on a XML data definition file. Table creation is done using the Hibernate mapping
 * APIs to allow for a full range of database support.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HibernateDbLoader implements IDbLoader {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private Dialect dialect;
    
    /**
     * @param jdbcTemplate the jdbcTemplate to set
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * @param transactionTemplate the transactionTemplate to set
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * @return the dialect
     */
    public Dialect getDialect() {
        return this.dialect;
    }
    /**
     * @param dialect the dialect to set
     */
    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    
    /* (non-Javadoc)
     * @see org.jasig.portal.tools.dbloader.IDbLoader#process(org.jasig.portal.tools.dbloader.DbLoaderConfiguration)
     */
    public void process(DbLoaderConfiguration configuration) throws ParserConfigurationException, SAXException, IOException {
        final List<String> script = new ArrayList<String>();
        
        //Handle table drop/create
        if (configuration.isDropTables() || configuration.isCreateTables()) {
            //Load Table object model
            final Map<String, Table> tables = this.loadTables(configuration);
            
            //TODO don't know if this data can be pulled from somewhere?
            final Mapping mapping = null;
            final String defaultCatalog = null;
            final String defaultSchema = null;
            
            final Map<String, DataAccessException> failedSql = new LinkedHashMap<String, DataAccessException>();
            
            //Generate and execute drop table scripts
            if (configuration.isDropTables()) {
                this.logger.info("Dropping existing tables");
                final List<String> dropScript = this.dropScript(tables.values(), dialect, defaultCatalog, defaultSchema);

                for (final String sql : dropScript) {
                    this.logger.info(sql);
                    try {
                        jdbcTemplate.update(sql);
                    }
                    catch (DataAccessException dae) {
                        failedSql.put(sql, dae);
                    }
                }
                
                script.addAll(dropScript);
            }
            
            //Generate and execute create table scripts
            if (configuration.isCreateTables()) {
                this.logger.info("Creating tables");
                final List<String> createScript = this.createScript(tables.values(), dialect, mapping, defaultCatalog, defaultSchema);

                for (final String sql : createScript) {
                    this.logger.info(sql);
                    try {
                        jdbcTemplate.update(sql);
                    }
                    catch (DataAccessException dae) {
                        failedSql.put(sql, dae);
                    }
                }
                
                script.addAll(createScript);
            }
            
            //Log any drop/create statements that failed 
            for (final Map.Entry<String, DataAccessException> failedSqlEntry : failedSql.entrySet()) {
                this.logger.warn("'" + failedSqlEntry.getKey() + "' failed to execute due to " + failedSqlEntry.getValue());
            }
        }
        
        //Perform database population
        if (configuration.isPopulateTables()) {
            this.logger.info("Populating database");
            this.populateTables(configuration);
        }
        
        //Write out the script file
        final String scriptFile = configuration.getScriptFile();
        if (scriptFile != null) {
            for (final ListIterator<String> iterator = script.listIterator(); iterator.hasNext(); ) {
                final String sql = iterator.next();
                iterator.set(sql + ";");
            }
            
            FileUtils.writeLines(new File(scriptFile), script);
        }
    }
    
    protected Map<String, Table> loadTables(DbLoaderConfiguration configuration) throws ParserConfigurationException, SAXException, IOException {
        //Locate tables.xml
        final Resource tablesFile = configuration.getTablesFile();
        if (!tablesFile.exists()) {
            throw new IllegalArgumentException("Could not find tables file: " + configuration.getTablesFile());
        }

        //Setup parser with custom handler to generate Table model and parse
        final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        final TableXmlHandler dh = new TableXmlHandler(dialect);
        saxParser.parse(new InputSource(tablesFile.getInputStream()), dh);

        return dh.getTables();
    }

    /**
     * Load the appropriate database dialect
     */
    @SuppressWarnings("unchecked")
    protected Dialect getDialect(String dialectName) {
        final Dialect dialect;
        try {
            final Class<Dialect> dialectClass = (Class<Dialect>) Class.forName(dialectName);
            dialect = dialectClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No Hibernate Dialect Class found for configured Dialect '" + dialectName + "'", e);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("No Hibernate Dialect Class found for configured Dialect '" + dialectName + "'", e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("No Hibernate Dialect Class found for configured Dialect '" + dialectName + "'", e);
        }
        return dialect;
    }
    
    /**
     * Generate the drop scripts and add them to the script list
     */
    @SuppressWarnings("unchecked")
    protected List<String> dropScript(Collection<Table> tables, Dialect dialect, String defaultCatalog, String defaultSchema) {
        final List<String> script = new ArrayList<String>(tables.size() * 2);
        
        if (dialect.dropConstraints()) {
            for (final Table table : tables) {
                if (table.isPhysicalTable()) {
                    for (final Iterator<ForeignKey> subIter = table.getForeignKeyIterator(); subIter.hasNext(); ) {
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
    
    /**
     * Generate create scripts and add them to the script list
     */
    @SuppressWarnings("unchecked")
    protected List<String> createScript(Collection<Table> tables, Dialect dialect, Mapping mapping, String defaultCatalog, String defaultSchema) {
        final List<String> script = new ArrayList<String>(tables.size() * 2);
        
        for (final Table table : tables) {
            if (table.isPhysicalTable()) {
                script.add(table.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
                for (final Iterator<String> comments = table.sqlCommentStrings(dialect, defaultCatalog, defaultSchema); comments.hasNext(); ) {
                    script.add(comments.next());
                }
            }
        }

        for (final Table table : tables) {
            if (table.isPhysicalTable()) {
                if (!dialect.supportsUniqueConstraintInCreateAlterTable()) {
                    for (final Iterator<UniqueKey> subIter = table.getUniqueKeyIterator(); subIter.hasNext(); ) {
                        final UniqueKey uk = subIter.next();
                        final String constraintString = uk.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
                        if (constraintString != null) {
                            script.add(constraintString);
                        }
                    }
                }

                for (final Iterator<Index> subIter = table.getIndexIterator(); subIter.hasNext(); ) {
                    final Index index = subIter.next();
                    script.add(index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
                }

                if (dialect.hasAlterTable()) {
                    for (final Iterator<ForeignKey> subIter = table.getForeignKeyIterator(); subIter.hasNext(); ) {
                        final ForeignKey fk = subIter.next();
                        if (fk.isPhysicalConstraint()) {
                            script.add(fk.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
                        }
                    }
                }
            }
        }
        
        return script;
    }
    
    protected void populateTables(DbLoaderConfiguration configuration) throws ParserConfigurationException, SAXException, IOException {
        //Locate tables.xml
        final Resource dataFile = configuration.getDataFile();
        if (!dataFile.exists()) {
            throw new IllegalArgumentException("Could not find data file: " + configuration.getDataFile());
        }

        //Setup parser with custom handler to generate Table model and parse
        final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        final DataXmlHandler dh = new DataXmlHandler(jdbcTemplate, transactionTemplate);
        saxParser.parse(new InputSource(dataFile.getInputStream()), dh);
    }
}
