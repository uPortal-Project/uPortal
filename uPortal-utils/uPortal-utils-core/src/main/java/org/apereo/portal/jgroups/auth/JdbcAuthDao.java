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
package org.apereo.portal.jgroups.auth;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.sql.DataSource;
import org.apereo.portal.utils.JdbcUtils;
import org.apereo.portal.utils.RandomTokenGenerator;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link AuthDao} that uses the Spring JDBC APIs to do its work.
 *
 */
public class JdbcAuthDao implements AuthDao, InitializingBean {
    /**
     * This class is ONLY used to provide for creation of the table/index required by the {@link
     * JdbcAuthDao}. Due to the JPA -> Hibernate -> Ehcache -> JGroups -> DAO_PING -> JdbcAuthDao
     * reference chain this class CANNOT directly reference the JPA entity manager or transaction
     * manager
     */
    @Entity(name = Table.NAME)
    public static class Table implements Serializable {
        private static final long serialVersionUID = 1L;

        static final String NAME = "UP_JGROUPS_AUTH";

        static final String COL_SERVICE_NAME = "SERVICE_NAME";
        static final String COL_RANDOM_TOKEN = "RANDOM_TOKEN";

        @Id
        @Column(name = COL_SERVICE_NAME, length = 100)
        private final String serviceName = null;

        @Column(name = COL_RANDOM_TOKEN, length = 4000)
        private final String randomToken = null;
    }

    private static final String PRM_SERVICE_NAME = "serviceName";
    private static final String PRM_RANDOM_TOKEN = "randomToken";

    private static final String INSERT_SQL =
            "INSERT INTO "
                    + Table.NAME
                    + " "
                    + "("
                    + Table.COL_SERVICE_NAME
                    + ", "
                    + Table.COL_RANDOM_TOKEN
                    + ") "
                    + "values (:"
                    + PRM_SERVICE_NAME
                    + ", :"
                    + PRM_RANDOM_TOKEN
                    + ")";

    private static final String SELECT_SQL =
            "SELECT "
                    + Table.COL_RANDOM_TOKEN
                    + " "
                    + "FROM "
                    + Table.NAME
                    + " "
                    + "WHERE "
                    + Table.COL_SERVICE_NAME
                    + "=:"
                    + PRM_SERVICE_NAME;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int authTokenLength = 1000;
    private JdbcOperations jdbcOperations;
    private NamedParameterJdbcOperations namedParameterJdbcOperations;
    private volatile boolean ready = false;

    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
        this.namedParameterJdbcOperations = new NamedParameterJdbcTemplate(this.jdbcOperations);
    }

    @Value("${org.apereo.portal.jgroups.auth.token_length:1000}")
    public void setAuthTokenLength(int authTokenLength) {
        this.authTokenLength = authTokenLength;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        HashedDaoAuthToken.setAuthDao(this);
    }

    @Override
    public String getAuthToken(String serviceName) {
        if (!isReady()) {
            return null;
        }

        for (int count = 0; count < 10; count++) {
            final String token =
                    DataAccessUtils.singleResult(
                            this.namedParameterJdbcOperations.queryForList(
                                    SELECT_SQL,
                                    Collections.singletonMap(PRM_SERVICE_NAME, serviceName),
                                    String.class));
            if (token != null) {
                return token;
            }

            //No token found, try creating it
            createToken(serviceName);
        }

        logger.warn("Failed to get/create auth token for {} after 10 tries", serviceName);

        return null;
    }

    protected void createToken(final String serviceName) {
        try {
            this.jdbcOperations.execute(
                    new ConnectionCallback<Object>() {
                        @Override
                        public Object doInConnection(Connection con)
                                throws SQLException, DataAccessException {
                            //This is horribly hacky but we can't rely on the main uPortal TM directly or we get
                            //into a circular dependency loop from JPA to Ehcache to jGroups and back to JPA
                            final DataSource ds = new SingleConnectionDataSource(con, true);
                            final PlatformTransactionManager ptm =
                                    new DataSourceTransactionManager(ds);
                            final TransactionOperations to = new TransactionTemplate(ptm);

                            to.execute(
                                    new TransactionCallbackWithoutResult() {
                                        @Override
                                        protected void doInTransactionWithoutResult(
                                                TransactionStatus status) {
                                            logger.info("Creating jGroups auth token");
                                            final String authToken =
                                                    RandomTokenGenerator.INSTANCE
                                                            .generateRandomToken(authTokenLength);

                                            final ImmutableMap<String, String> params =
                                                    ImmutableMap.of(
                                                            PRM_SERVICE_NAME, serviceName,
                                                            PRM_RANDOM_TOKEN, authToken);

                                            namedParameterJdbcOperations.update(INSERT_SQL, params);
                                        }
                                    });

                            return null;
                        }
                    });
        } catch (ConstraintViolationException e) {
            //Ignore, just means a concurrent token creation
        } catch (DataIntegrityViolationException e) {
            //Ignore, just means a concurrent token creation
        }
    }

    protected boolean isReady() {
        boolean r = this.ready;
        if (!r) {
            r = JdbcUtils.doesTableExist(this.jdbcOperations, Table.NAME);

            if (r) {
                this.ready = r;
            }
        }

        return r;
    }
}
