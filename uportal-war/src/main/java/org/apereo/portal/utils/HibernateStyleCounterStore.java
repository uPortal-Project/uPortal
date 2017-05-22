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
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */

package org.apereo.portal.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Resource;
import org.apereo.portal.ICounterStore;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.Optimizer;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Mostly cloned from {@link TableGenerator}
 *
 */
@Repository("counterStore")
public class HibernateStyleCounterStore implements ICounterStore {

    private static final String TRANSACTION_OPERATIONS_BEAN_ID =
            "counterStoreTransactionOperations";

    private static final String SELECT_QUERY =
            "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?";
    private static final String UPDATE_QUERY =
            "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE=? WHERE SEQUENCE_NAME=? AND SEQUENCE_VALUE=?";
    private static final String INSERT_QUERY =
            "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME, SEQUENCE_VALUE) VALUES (?, ?)";

    private static final int MAX_ATTEMPTS = 3;
    private final Type identifierType = IntegerType.INSTANCE;

    private final ConcurrentMap<String, Callable<Optimizer>> counterOptimizers =
            new ConcurrentHashMap<String, Callable<Optimizer>>();
    private TransactionOperations transactionOperations;
    private JdbcOperations jdbcOperations;
    private int incrementSize = 50;
    private int initialValue = 10;

    @Resource(name = TRANSACTION_OPERATIONS_BEAN_ID)
    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    @Autowired
    public void setJdbcOperations(
            @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME) JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Value("${org.apereo.portal.utils.HibernateStyleCounterStore.incrementSize:50}")
    public void setIncrementSize(int incrementSize) {
        this.incrementSize = incrementSize;
    }

    @Value("${org.apereo.portal.utils.HibernateStyleCounterStore.initialValue:10}")
    public void setInitialValue(int initialValue) {
        this.initialValue = initialValue;
    }

    private Optimizer getCounterOptimizer(String counterName) {
        Callable<Optimizer> optimizer = counterOptimizers.get(counterName);
        if (optimizer == null) {
            optimizer =
                    new Callable<Optimizer>() {
                        private volatile Optimizer optimizer;

                        @Override
                        public Optimizer call() throws Exception {
                            Optimizer o = optimizer;
                            if (o != null) {
                                return o;
                            }

                            synchronized (this) {
                                o = optimizer;
                                if (o != null) {
                                    return o;
                                }

                                o =
                                        OptimizerFactory.buildOptimizer(
                                                OptimizerFactory.StandardOptimizerDescriptor.POOLED
                                                        .getExternalName(),
                                                identifierType.getReturnedClass(),
                                                incrementSize,
                                                initialValue);
                                this.optimizer = o;
                            }

                            return o;
                        }
                    };

            optimizer = ConcurrentMapUtils.putIfAbsent(counterOptimizers, counterName, optimizer);
        }

        try {
            return optimizer.call();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getNextId(String counterName) {
        final int id;
        final Optimizer counterOptimizer = this.getCounterOptimizer(counterName);
        synchronized (counterOptimizer) {
            id = getNextIdInternal(counterOptimizer, counterName);
        }
        return id;
    }

    private int getNextIdInternal(final Optimizer optimizer, final String counterName) {

        return (Integer)
                optimizer.generate(
                        new AccessCallback() {
                            @Override
                            public IntegralDataTypeHolder getNextValue() {

                                IntegralDataTypeHolder rslt = null;
                                for (int i = 0; rslt == null && i < MAX_ATTEMPTS; i++) {

                                    rslt =
                                            transactionOperations.execute(
                                                    new TransactionCallback<
                                                            IntegralDataTypeHolder>() {
                                                        @Override
                                                        public IntegralDataTypeHolder
                                                                doInTransaction(
                                                                        TransactionStatus status) {
                                                            final IntegralDataTypeHolder value =
                                                                    IdentifierGeneratorHelper
                                                                            .getIntegralDataTypeHolder(
                                                                                    identifierType
                                                                                            .getReturnedClass());

                                                            //Try and load the current value, returns true if the expected row exists, null otherwise
                                                            final boolean selected =
                                                                    jdbcOperations.query(
                                                                            SELECT_QUERY,
                                                                            new ResultSetExtractor<
                                                                                    Boolean>() {
                                                                                @Override
                                                                                public Boolean
                                                                                        extractData(
                                                                                                ResultSet
                                                                                                        rs)
                                                                                                throws
                                                                                                        SQLException,
                                                                                                        DataAccessException {
                                                                                    if (rs.next()) {
                                                                                        value
                                                                                                .initialize(
                                                                                                        rs,
                                                                                                        1);
                                                                                        return true;
                                                                                    }
                                                                                    return false;
                                                                                }
                                                                            },
                                                                            counterName);

                                                            //No row exists for the counter, insert it
                                                            if (!selected) {
                                                                value.initialize(initialValue);

                                                                jdbcOperations.update(
                                                                        INSERT_QUERY,
                                                                        new PreparedStatementSetter() {
                                                                            @Override
                                                                            public void setValues(
                                                                                    PreparedStatement
                                                                                            ps)
                                                                                    throws
                                                                                            SQLException {
                                                                                ps.setString(
                                                                                        1,
                                                                                        counterName);
                                                                                value.bind(ps, 2);
                                                                            }
                                                                        });
                                                            }

                                                            //Increment the counter row value
                                                            final IntegralDataTypeHolder
                                                                    updateValue = value.copy();
                                                            if (optimizer
                                                                    .applyIncrementSizeToSourceValues()) {
                                                                updateValue.add(incrementSize);
                                                            } else {
                                                                updateValue.increment();
                                                            }

                                                            //Update the counter row, if rows returns 0 the update failed due to a race condition, it will be retried
                                                            int rowsAltered =
                                                                    jdbcOperations.update(
                                                                            UPDATE_QUERY,
                                                                            new PreparedStatementSetter() {
                                                                                @Override
                                                                                public void
                                                                                        setValues(
                                                                                                PreparedStatement
                                                                                                        ps)
                                                                                                throws
                                                                                                        SQLException {
                                                                                    updateValue
                                                                                            .bind(
                                                                                                    ps,
                                                                                                    1);
                                                                                    ps.setString(
                                                                                            2,
                                                                                            counterName);
                                                                                    value.bind(
                                                                                            ps, 3);
                                                                                }
                                                                            });

                                                            return rowsAltered > 0
                                                                    ? value // Success
                                                                    : null; // Failed;  try again...
                                                        }
                                                    });
                                } // End for loop

                                if (rslt == null) {
                                    throw new RuntimeException(
                                            "Failed to fetch a new batch of sequence values after "
                                                    + MAX_ATTEMPTS
                                                    + " tries");
                                }

                                return rslt;
                            }
                        });
    }
}
