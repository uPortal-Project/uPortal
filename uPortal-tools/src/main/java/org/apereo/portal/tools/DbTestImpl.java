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
package org.apereo.portal.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apereo.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.apereo.portal.hibernate.HibernateConfigurationAware;
import org.apereo.portal.jdbc.RDBMServices;
import org.apereo.portal.jpa.BaseAggrEventsJpaDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.BaseRawEventsJpaDao;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;

/**
 * Title: DbTest Description: Displays database metadata information Company:
 *
 */
@Component("dbTest")
@Lazy
@DependsOn({
    BasePortalJpaDao.PERSISTENCE_UNIT_NAME + "EntityManagerFactory",
    BaseAggrEventsJpaDao.PERSISTENCE_UNIT_NAME + "EntityManagerFactory",
    BaseRawEventsJpaDao.PERSISTENCE_UNIT_NAME + "EntityManagerFactory",
    "jdbcOperations",
    "rawEventsJdbcOperations",
    "aggrEventsJdbcOperations",
    "hibernateConfigurationAwareInjector"
})
public class DbTestImpl implements HibernateConfigurationAware, DbTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, HibernateConfiguration> hibernateConfigurations =
            new ConcurrentHashMap<String, HibernateConfiguration>();
    private Map<String, JdbcOperations> jdbcOperations;

    @Override
    public boolean supports(String persistenceUnit) {
        return true;
    }

    @Override
    public void setConfiguration(
            String persistenceUnit, HibernateConfiguration hibernateConfiguration) {
        this.hibernateConfigurations.put(persistenceUnit, hibernateConfiguration);
    }

    @Autowired
    public void setJdbcOperations(Map<String, JdbcOperations> jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public void printDbInfo() {
        boolean fail = false;

        logger.info("JDBC DataSources");
        for (final Entry<String, JdbcOperations> jdbcEntry : this.jdbcOperations.entrySet()) {
            final String jdbcName = jdbcEntry.getKey();
            try {
                logger.info("\t" + jdbcName);
                final JdbcOperations jdbcOps = jdbcEntry.getValue();
                jdbcOps.execute(
                        new ConnectionCallback<Object>() {
                            @Override
                            public Object doInConnection(Connection con)
                                    throws SQLException, DataAccessException {
                                printInfo(con);
                                return null;
                            }
                        });
            } catch (Exception e) {
                logger.error("\t" + jdbcName + ": parse info", e);
                fail = true;
            }
            logger.info("");
        }

        logger.info("Hibernate Dialects");
        for (final Entry<String, HibernateConfiguration> configEntry :
                this.hibernateConfigurations.entrySet()) {
            final String persistenceUnit = configEntry.getKey();

            try {
                final HibernateConfiguration hibernateConfiguration = configEntry.getValue();
                final SessionFactoryImplementor sessionFactory =
                        hibernateConfiguration.getSessionFactory();
                final Dialect dialect = sessionFactory.getDialect();

                logger.info("\t" + persistenceUnit + ": " + dialect);
            } catch (Exception e) {
                logger.error("\t" + persistenceUnit + ": Failed to resolve Dialect", e);
                fail = true;
            }
            logger.info("");
        }

        if (fail) {
            throw new RuntimeException(
                    "One or more of the portal data sources is not configured correctly or the target database is not available.");
        }
    }

    private void printInfo(Connection conn) throws SQLException {
        DatabaseMetaData dbMetaData = conn.getMetaData();

        String dbName = dbMetaData.getDatabaseProductName();
        String dbVersion = dbMetaData.getDatabaseProductVersion();
        String driverName = dbMetaData.getDriverName();
        String driverVersion = dbMetaData.getDriverVersion();
        final int databaseMajorVersion = dbMetaData.getDatabaseMajorVersion();
        final int databaseMinorVersion = dbMetaData.getDatabaseMinorVersion();
        final int driverMajorVersion = dbMetaData.getDriverMajorVersion();
        final int driverMinorVersion = dbMetaData.getDriverMinorVersion();

        String driverClass = RDBMServices.getJdbcDriver();
        String url = RDBMServices.getJdbcUrl();
        String user = RDBMServices.getJdbcUser();

        boolean supportsANSI92EntryLevelSQL = dbMetaData.supportsANSI92EntryLevelSQL();
        boolean supportsANSI92FullSQL = dbMetaData.supportsANSI92FullSQL();
        boolean supportsBatchUpdates = dbMetaData.supportsBatchUpdates();
        boolean supportsColumnAliasing = dbMetaData.supportsColumnAliasing();
        boolean supportsCoreSQLGrammar = dbMetaData.supportsCoreSQLGrammar();
        boolean supportsExtendedSQLGrammar = dbMetaData.supportsExtendedSQLGrammar();
        boolean supportsExpressionsInOrderBy = dbMetaData.supportsExpressionsInOrderBy();
        boolean supportsOuterJoins = dbMetaData.supportsOuterJoins();
        boolean supportsFullOuterJoins = dbMetaData.supportsFullOuterJoins();
        boolean supportsLimitedOuterJoins = dbMetaData.supportsLimitedOuterJoins();

        boolean supportsMultipleTransactions = dbMetaData.supportsMultipleTransactions();
        boolean supportsOpenCursorsAcrossCommit = dbMetaData.supportsOpenCursorsAcrossCommit();
        boolean supportsOpenCursorsAcrossRollback = dbMetaData.supportsOpenCursorsAcrossRollback();
        boolean supportsOpenStatementsAcrossCommit =
                dbMetaData.supportsOpenStatementsAcrossCommit();
        boolean supportsOpenStatementsAcrossRollback =
                dbMetaData.supportsOpenStatementsAcrossRollback();

        boolean supportsOrderByUnrelated = dbMetaData.supportsOrderByUnrelated();
        boolean supportsPositionedDelete = dbMetaData.supportsPositionedDelete();

        boolean supportsSelectForUpdate = dbMetaData.supportsSelectForUpdate();
        boolean supportsStoredProcedures = dbMetaData.supportsStoredProcedures();
        boolean supportsTransactions = dbMetaData.supportsTransactions();
        boolean supportsUnion = dbMetaData.supportsUnion();
        boolean supportsUnionAll = dbMetaData.supportsUnionAll();

        int getMaxColumnNameLength = dbMetaData.getMaxColumnNameLength();
        int getMaxColumnsInIndex = dbMetaData.getMaxColumnsInIndex();
        int getMaxColumnsInOrderBy = dbMetaData.getMaxColumnsInOrderBy();
        int getMaxColumnsInSelect = dbMetaData.getMaxColumnsInSelect();
        int getMaxColumnsInTable = dbMetaData.getMaxColumnsInTable();
        int getMaxConnections = dbMetaData.getMaxConnections();
        int getMaxCursorNameLength = dbMetaData.getMaxCursorNameLength();
        int getMaxIndexLength = dbMetaData.getMaxIndexLength();
        int getMaxRowSize = dbMetaData.getMaxRowSize();
        int getMaxStatements = dbMetaData.getMaxStatements();
        int getMaxTableNameLength = dbMetaData.getMaxTableNameLength();
        int getMaxTablesInSelect = dbMetaData.getMaxTablesInSelect();
        int getMaxUserNameLength = dbMetaData.getMaxUserNameLength();

        String getSearchStringEscape = dbMetaData.getSearchStringEscape();
        String getStringFunctions = dbMetaData.getStringFunctions();
        String getSystemFunctions = dbMetaData.getSystemFunctions();
        String getTimeDateFunctions = dbMetaData.getTimeDateFunctions();

        logger.info("\t\tDatabase name:    '" + dbName + "'");
        logger.info(
                "\t\tDatabase version: '"
                        + dbVersion
                        + "' ("
                        + databaseMajorVersion
                        + "."
                        + databaseMinorVersion
                        + ")");
        logger.info("\t\tDriver name:      '" + driverName + "'");
        logger.info(
                "\t\tDriver version:   '"
                        + driverVersion
                        + "' ("
                        + driverMajorVersion
                        + "."
                        + driverMinorVersion
                        + ")");
        logger.info("\t\tDriver class:     '" + driverClass + "'");
        logger.info("\t\tConnection URL:   '" + url + "'");
        logger.info("\t\tUser:             '" + user + "'");
        logger.info("");

        logger.info("\t\tsupportsANSI92EntryLevelSQL: " + supportsANSI92EntryLevelSQL);
        logger.info("\t\tsupportsANSI92FullSQL:       " + supportsANSI92FullSQL);
        logger.info("\t\tsupportsCoreSQLGrammar:      " + supportsCoreSQLGrammar);
        logger.info("\t\tsupportsExtendedSQLGrammar:  " + supportsExtendedSQLGrammar);
        logger.info("");

        logger.info("\t\tsupportsTransactions:         " + supportsTransactions);
        logger.info("\t\tsupportsMultipleTransactions: " + supportsMultipleTransactions);

        logger.info("\t\tsupportsOpenCursorsAcrossCommit:      " + supportsOpenCursorsAcrossCommit);
        logger.info(
                "\t\tsupportsOpenCursorsAcrossRollback:    " + supportsOpenCursorsAcrossRollback);
        logger.info(
                "\t\tsupportsOpenStatementsAcrossCommit:   " + supportsOpenStatementsAcrossCommit);
        logger.info(
                "\t\tsupportsOpenStatementsAcrossRollback: "
                        + supportsOpenStatementsAcrossRollback);
        logger.info("");

        logger.info("\t\tsupportsStoredProcedures:     " + supportsStoredProcedures);
        logger.info("\t\tsupportsOuterJoins:           " + supportsOuterJoins);
        logger.info("\t\tsupportsFullOuterJoins:       " + supportsFullOuterJoins);
        logger.info("\t\tsupportsLimitedOuterJoins:    " + supportsLimitedOuterJoins);
        logger.info("\t\tsupportsBatchUpdates:         " + supportsBatchUpdates);
        logger.info("\t\tsupportsColumnAliasing:       " + supportsColumnAliasing);
        logger.info("\t\tsupportsExpressionsInOrderBy: " + supportsExpressionsInOrderBy);
        logger.info("\t\tsupportsOrderByUnrelated:     " + supportsOrderByUnrelated);
        logger.info("\t\tsupportsPositionedDelete:     " + supportsPositionedDelete);
        logger.info("\t\tsupportsSelectForUpdate:      " + supportsSelectForUpdate);
        logger.info("\t\tsupportsUnion:                " + supportsUnion);
        logger.info("\t\tsupportsUnionAll:             " + supportsUnionAll);
        logger.info("");

        logger.info("\t\tmaxColumnNameLength: " + getMaxColumnNameLength);
        logger.info("\t\tmaxColumnsInIndex:   " + getMaxColumnsInIndex);
        logger.info("\t\tmaxColumnsInOrderBy: " + getMaxColumnsInOrderBy);
        logger.info("\t\tmaxColumnsInSelect:  " + getMaxColumnsInSelect);
        logger.info("\t\tmaxColumnsInTable:   " + getMaxColumnsInTable);
        logger.info("\t\tmaxConnections:      " + getMaxConnections);
        logger.info("\t\tmaxCursorNameLength: " + getMaxCursorNameLength);
        logger.info("\t\tmaxIndexLength:      " + getMaxIndexLength);
        logger.info("\t\tmaxRowSize:          " + getMaxRowSize);
        logger.info("\t\tmaxStatements:       " + getMaxStatements);
        logger.info("\t\tmaxTableNameLength:  " + getMaxTableNameLength);
        logger.info("\t\tmaxTablesInSelect:   " + getMaxTablesInSelect);
        logger.info("\t\tmaxUserNameLength:   " + getMaxUserNameLength);
        logger.info("\t\tsearchStringEscape:  " + getSearchStringEscape);
        logger.info("\t\tstringFunctions:     " + getStringFunctions);
        logger.info("\t\tsystemFunctions:     " + getSystemFunctions);
        logger.info("\t\ttimeDateFunctions:   " + getTimeDateFunctions);
        logger.info("");

        ResultSet getTableTypes = null;
        ResultSet getTypeInfo = null;
        try {
            getTableTypes = dbMetaData.getTableTypes();
            StringBuilder tabletypes = new StringBuilder();
            while (getTableTypes.next()) {
                tabletypes.append(getTableTypes.getString(1)).append(",");
            }
            tabletypes = tabletypes.deleteCharAt(tabletypes.length() - 1);
            logger.info("\t\tTable Types: " + tabletypes);

            getTypeInfo = dbMetaData.getTypeInfo();
            StringBuilder typeinfo = new StringBuilder();
            while (getTypeInfo.next()) {
                typeinfo.append(getTypeInfo.getString(1)).append(",");
            }
            typeinfo = typeinfo.deleteCharAt(typeinfo.length() - 1);
            logger.info("\t\tSQL Types:   " + typeinfo);
        } catch (SQLException ex) {
        } finally {
            JdbcUtils.closeResultSet(getTableTypes);
            JdbcUtils.closeResultSet(getTypeInfo);
        }
    } // end printInfo
}
