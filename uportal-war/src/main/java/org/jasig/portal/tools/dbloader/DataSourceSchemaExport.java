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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jasig.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.jasig.portal.hibernate.HibernateConfigurationAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Runs the Hibernate Schema Export tool using the specified DataSource for the target DB.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DataSourceSchemaExport implements ISchemaExport, HibernateConfigurationAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Formatter formatter = FormatStyle.DDL.getFormatter();
    private JdbcOperations jdbcOperations;
    private Configuration configuration;
    private Dialect dialect;
    private String persistenceUnit;
    
    /**
     * The name of the persistence unit to use
     */
    @Required
    public void setPersistenceUnit(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }
    
    @Required
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public boolean supports(String persistenceUnit) {
        return this.persistenceUnit.equals(persistenceUnit);
    }

    @Override
    public void setConfiguration(String persistenceUnit, HibernateConfiguration hibernateConfiguration) {
        this.configuration = hibernateConfiguration.getConfiguration();
        final SessionFactoryImplementor sessionFactory = hibernateConfiguration.getSessionFactory();
        this.dialect = sessionFactory.getDialect();
    }
    
    @Override
    public void drop(boolean export, String outputFile, boolean append) {
        final String[] dropSQL = configuration.generateDropSchemaScript(dialect);
        perform(dropSQL, outputFile, append, false);
    }
    
    @Override
    public void create(boolean export, String outputFile, boolean append) {
        final String[] createSQL = configuration.generateSchemaCreationScript(dialect);
        perform(createSQL, outputFile, append, true);
    }
    
    @Override
    public void update(boolean export, String outputFile, boolean append) {
        final DatabaseMetadata databaseMetadata = this.jdbcOperations.execute(new ConnectionCallback<DatabaseMetadata>() {
            @Override
            public DatabaseMetadata doInConnection(Connection con) throws SQLException, DataAccessException {
                return new DatabaseMetadata( con, dialect );
            }
        });
        
        final String[] updateSQL = configuration.generateSchemaUpdateScript(dialect, databaseMetadata);
        perform(updateSQL, outputFile, append, true);
    }

    private void perform(String[] sqlCommands, String outputFile, boolean append, boolean failFast) {
        final PrintWriter sqlWriter = getSqlWriter(outputFile, append);
        try {
            for (final String sqlCommand : sqlCommands) {
                final String formatted = formatter.format(sqlCommand);
                sqlWriter.println(formatted);

                try {
                    jdbcOperations.execute(sqlCommand);
                    logger.info(sqlCommand);
                }
                catch (Exception e) {
                    if (failFast) {
                        logger.error("Failed to execute: {}\n\t{}", sqlCommand, e.getMessage());
                        throw new RuntimeException("Failed to execute: " + sqlCommand, e);
                    }
                    else {
                        if (logger.isDebugEnabled()) {
                            logger.info("Failed to execute: " + sqlCommand, e);
                        }
                        else {
                            logger.info("Failed to execute (probably ignorable): {}", sqlCommand);
                        }
                    }
                }
            }
        }
        finally {
            IOUtils.closeQuietly(sqlWriter);
        }
    }

    private PrintWriter getSqlWriter(String outputFile, boolean append) {
        final Writer sqlWriter;
        if (StringUtils.trimToNull(outputFile) != null) {
            try {
                sqlWriter = new BufferedWriter(new FileWriter(outputFile, append));
            }
            catch (IOException e) {
                throw new RuntimeException("", e);
            }
        }
        else {
            sqlWriter = new NullWriter();
        }
        return new PrintWriter(sqlWriter);
    }
}
