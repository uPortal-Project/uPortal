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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.apereo.portal.hibernate.HibernateConfigurationAware;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tool.hbm2ddl.FixedDatabaseMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Runs the Hibernate Schema Export tool using the specified DataSource for the target DB.
 *
 */
public class DataSourceSchemaExport implements ISchemaExport, HibernateConfigurationAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Formatter formatter = FormatStyle.DDL.getFormatter();
    private JdbcOperations jdbcOperations;
    private Configuration configuration;
    private Dialect dialect;
    private String persistenceUnit;

    /** The name of the persistence unit to use */
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
    public String getPersistenceUnitName() {
        return this.persistenceUnit;
    }

    @Override
    public void setConfiguration(
            String persistenceUnit, HibernateConfiguration hibernateConfiguration) {
        this.configuration = hibernateConfiguration.getConfiguration();
        final SessionFactoryImplementor sessionFactory = hibernateConfiguration.getSessionFactory();
        this.dialect = sessionFactory.getDialect();
    }

    @Override
    public void drop(boolean export, String outputFile, boolean append) {
        final String[] dropSQL = configuration.generateDropSchemaScript(dialect);
        perform(dropSQL, export, outputFile, append, false);
    }

    @Override
    public void create(boolean export, String outputFile, boolean append) {
        final String[] createSQL = configuration.generateSchemaCreationScript(dialect);
        perform(createSQL, export, outputFile, append, true);
    }

    @Override
    public void update(boolean export, String outputFile, boolean append) {
        final String[] updateSQL =
                this.jdbcOperations.execute(
                        new ConnectionCallback<String[]>() {
                            @Override
                            public String[] doInConnection(Connection con)
                                    throws SQLException, DataAccessException {
                                final FixedDatabaseMetadata databaseMetadata =
                                        new FixedDatabaseMetadata(con, dialect);
                                return configuration.generateSchemaUpdateScript(
                                        dialect, databaseMetadata);
                            }
                        });

        perform(updateSQL, export, outputFile, append, true);
    }

    private void perform(
            String[] sqlCommands,
            boolean executeSql,
            String outputFile,
            boolean append,
            boolean failFast) {
        final PrintWriter sqlWriter = getSqlWriter(outputFile, append);
        try {
            for (final String sqlCommand : sqlCommands) {
                final String formatted = formatter.format(sqlCommand);
                sqlWriter.println(formatted);

                if (executeSql) {
                    try {
                        jdbcOperations.execute(sqlCommand);
                        logger.info(sqlCommand);
                    } catch (BadSqlGrammarException | UncategorizedSQLException e) {
                        // For HSQL database and ant db-update to avoid failing when attempting to
                        // delete a sequence that does not exist.
                        // Needed until Hibernate 5.  See https://hibernate.atlassian.net/browse/HHH-7002.
                        if (sqlCommand.contains("drop constraint")) {
                            logger.info(
                                    "Failed to execute (probably ignorable): {}, error message {}",
                                    sqlCommand,
                                    e.getMessage());
                        } else {
                            handleSqlException(failFast, sqlCommand, e);
                        }
                    } catch (Exception e) {
                        handleSqlException(failFast, sqlCommand, e);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(sqlWriter);
        }
    }

    private void handleSqlException(boolean failFast, String sqlCommand, Exception e) {
        if (failFast) {
            logger.error("Failed to execute: {}\n\t{}", sqlCommand, e.getMessage());
            throw new RuntimeException("Failed to execute: " + sqlCommand, e);
        } else {
            logger.info(
                    "Failed to execute (probably ignorable): {}, error message {}",
                    sqlCommand,
                    e.getMessage());
        }
    }

    private PrintWriter getSqlWriter(String outputFile, boolean append) {
        final Writer sqlWriter;
        if (StringUtils.trimToNull(outputFile) != null) {
            try {
                // Insure any parent directories are created so we don't fail creating the SQL file.
                File file = new File(outputFile);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                sqlWriter = new BufferedWriter(new FileWriter(file, append));
            } catch (IOException e) {
                throw new RuntimeException("", e);
            }
        } else {
            sqlWriter = new NullWriter();
        }
        return new PrintWriter(sqlWriter);
    }
}
