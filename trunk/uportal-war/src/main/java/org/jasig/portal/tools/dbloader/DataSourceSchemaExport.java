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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs the Hibernate Schema Export tool using the specified DataSource for the target DB.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DataSourceSchemaExport implements ISchemaExport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Resource configuration;
    private JdbcOperations jdbcOperations;
    private String dialect;
    
    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(Resource configuration) {
        this.configuration = configuration;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.afterPropertiesSet();
        this.jdbcOperations = jdbcTemplate;
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
    @Override
    public void hbm2ddl(final boolean export, final boolean create, final boolean drop, final String outputFile, final boolean haltOnError) {
        final Configuration configuration = new Configuration();
        try {
            configuration.configure(this.configuration.getURL());
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not load configuration file '" + this.configuration + "'", e);
        }
        
        
        if (StringUtils.isNotEmpty(this.dialect)) {
            configuration.setProperty(Environment.DIALECT, this.dialect);
        }
        else {
            final Dialect dialect = this.jdbcOperations.execute(new ConnectionCallback<Dialect>() {
                @Override
                public Dialect doInConnection(Connection con) throws SQLException, DataAccessException {
                    return DialectFactory.buildDialect(configuration.getProperties(), con);
                }
            });
            
            final String dialectName = dialect.getClass().getName();
            configuration.setProperty(Environment.DIALECT, dialectName);
            this.logger.info("Resolved Hibernate Dialect: {}", dialectName);
        }
            
            
        configuration.buildMappings();
        
        if (drop) {
            this.jdbcOperations.execute(new ConnectionCallback<Object>() {
                @Override
                public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                    final SchemaExport exporter = createExporter(outputFile, haltOnError, configuration, con);
                    exporter.execute(true, export, true, false);
                    return null;
                }
            });
        }
        
        if (create) {
            this.jdbcOperations.execute(new ConnectionCallback<Object>() {
                @Override
                public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                    final SchemaExport exporter = createExporter(outputFile, haltOnError, configuration, con);
                    exporter.execute(true, export, false, true);
        
                    if (haltOnError) {
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
                    return null;
                }
            });
        }
    }

    /**
     * @param outputFile
     * @param haltOnError
     * @param configuration
     * @param connection
     * @return
     */
    protected SchemaExport createExporter(String outputFile, boolean haltOnError, final Configuration configuration, Connection connection) {
        final SchemaExport exporter = new SchemaExport(configuration, connection);
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
}
