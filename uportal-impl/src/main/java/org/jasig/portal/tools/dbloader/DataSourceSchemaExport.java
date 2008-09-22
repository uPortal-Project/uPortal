/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import java.io.IOException;
import java.sql.Connection;

import javax.sql.DataSource;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Runs the Hibernate Schema Export tool using the specified DataSource for the target DB.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DataSourceSchemaExport implements ISchemaExport {
    private Resource configuration;
    private DataSource dataSource;
    private String dialect;
    
    /**
     * @return the configuration
     */
    public Resource getConfiguration() {
        return configuration;
    }
    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(Resource configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return the dialect
     */
    public String getDialect() {
        return dialect;
    }
    /**
     * @param dialect the dialect to set
     */
    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    /**
     * @param export If the database should have the SQL executed agaisnt it
     * @param drop If existing database objects should be dropped before creating new objects
     * @param outputFile Optional file to write out the SQL to.
     */
    public void hbm2ddl(boolean export, boolean drop, String outputFile) {
        final AnnotationConfiguration configuration = new AnnotationConfiguration();
        try {
            configuration.configure(this.configuration.getURL());
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not load configuration file '" + this.configuration + "'", e);
        }
        
        configuration.setProperty("hibernate.dialect", this.dialect);
        configuration.buildMappings();
        
        final Connection connection = DataSourceUtils.getConnection(this.dataSource);
        try {
            final SchemaExport exporter = new SchemaExport(configuration, connection);
            exporter.setFormat(false);
            if (outputFile != null) {
                exporter.setOutputFile(outputFile);
            }
            
            exporter.execute(true, export, !export, !export && !drop);
        }
        finally {
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
    }
}
