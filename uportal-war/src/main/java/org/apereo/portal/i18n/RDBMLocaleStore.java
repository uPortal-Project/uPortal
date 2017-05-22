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
package org.apereo.portal.i18n;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.security.IPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Database implementation of locale storage interface.
 *
 */
@Repository("localeStore")
public class RDBMLocaleStore implements ILocaleStore {

    protected final Log logger = LogFactory.getLog(this.getClass());

    protected TransactionOperations transactionOperations;
    protected JdbcOperations jdbcOperations;

    @Autowired
    public void setPlatformTransactionManager(
            @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
                    PlatformTransactionManager platformTransactionManager) {
        this.transactionOperations = new TransactionTemplate(platformTransactionManager);
    }

    @Resource(name = BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setDataSource(DataSource dataSource) {
        this.jdbcOperations = new JdbcTemplate(dataSource);
    }

    @Override
    public Locale[] getUserLocales(final IPerson person) {
        return jdbcOperations.execute(
                new ConnectionCallback<Locale[]>() {
                    @Override
                    public Locale[] doInConnection(Connection con)
                            throws SQLException, DataAccessException {

                        final List<Locale> localeList = new ArrayList<Locale>();
                        final String query =
                                "SELECT * FROM UP_USER_LOCALE WHERE USER_ID=? ORDER BY PRIORITY";
                        final PreparedStatement pstmt = con.prepareStatement(query);
                        try {
                            pstmt.clearParameters();
                            pstmt.setInt(1, person.getID());
                            logger.debug(query);
                            final ResultSet rs = pstmt.executeQuery();
                            try {
                                while (rs.next()) {
                                    final String localeString = rs.getString("LOCALE");
                                    final Locale locale = LocaleManager.parseLocale(localeString);
                                    localeList.add(locale);
                                }
                            } finally {
                                rs.close();
                            }
                        } finally {
                            pstmt.close();
                        }

                        return localeList.toArray(new Locale[localeList.size()]);
                    }
                });
    }

    @Override
    public void updateUserLocales(final IPerson person, final Locale[] locales) {
        this.transactionOperations.execute(
                new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus status) {
                        return jdbcOperations.execute(
                                new ConnectionCallback<Object>() {
                                    @Override
                                    public Object doInConnection(Connection con)
                                            throws SQLException, DataAccessException {

                                        // Delete the existing list of locales
                                        final String delete =
                                                "DELETE FROM UP_USER_LOCALE WHERE USER_ID=?";
                                        PreparedStatement pstmt = con.prepareStatement(delete);
                                        try {
                                            pstmt.clearParameters();
                                            pstmt.setInt(1, person.getID());
                                            logger.debug(delete);
                                            pstmt.executeUpdate();

                                        } finally {
                                            pstmt.close();
                                        }
                                        // Insert the new list of locales
                                        final String insert =
                                                "INSERT INTO UP_USER_LOCALE VALUES (?, ?, ?)";
                                        pstmt = con.prepareStatement(insert);
                                        try {
                                            for (int i = 0; i < locales.length; i++) {
                                                pstmt.clearParameters();
                                                pstmt.setInt(1, person.getID());
                                                pstmt.setString(2, locales[i].toString());
                                                pstmt.setInt(3, i);
                                                logger.debug(insert);
                                                pstmt.executeUpdate();
                                            }

                                        } finally {
                                            pstmt.close();
                                        }

                                        return null;
                                    }
                                });
                    }
                });
    }
}
