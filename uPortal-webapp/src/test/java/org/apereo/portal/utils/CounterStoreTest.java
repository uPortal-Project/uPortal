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
package org.apereo.portal.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.dbcp.BasicDataSource;
import org.apereo.portal.ICounterStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;

/** */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:pooledCounterStoreTestApplicationContext.xml")
public class CounterStoreTest {
    @Autowired
    @Qualifier("one")
    private ICounterStore counterStoreOne;

    @Autowired
    @Qualifier("two")
    private ICounterStore counterStoreTwo;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Before
    public void onSetUp() throws Exception {
        jdbcTemplate.update(
                "CREATE TABLE UP_SEQUENCE (SEQUENCE_NAME VARCHAR(1000), SEQUENCE_VALUE INTEGER)");
        assertEquals(0, JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "UP_SEQUENCE"));
    }

    @After
    public void onTearDown() throws Exception {
        jdbcTemplate.update("DROP TABLE UP_SEQUENCE");
    }

    @Test
    public void testCounterSingleThread() {
        // Values reflect the POOLED_LO optimizer (Hibernate 4.3): the persisted
        // UP_SEQUENCE value is the next low value to hand out, so the table
        // advances one increment (3) ahead of the issued IDs rather than two.
        // Get until DB has to increment
        this.getValue(this.counterStoreOne, "Test1", 1, 4, 1);

        this.getValue(this.counterStoreTwo, "Test1", 1, 7, 4);

        this.getValue(this.counterStoreOne, "Test1", 1, 7, 2);

        this.getValue(this.counterStoreTwo, "Test1", 1, 7, 5);

        this.getValue(this.counterStoreOne, "Test1", 1, 7, 3);

        this.getValue(this.counterStoreTwo, "Test1", 1, 7, 6);

        this.getValue(this.counterStoreOne, "Test1", 1, 10, 7);

        this.getValue(this.counterStoreTwo, "Test1", 1, 13, 10);

        this.getValue(this.counterStoreOne, "Test1", 1, 13, 8);

        this.getValue(this.counterStoreTwo, "Test1", 1, 13, 11);

        this.getValue(this.counterStoreOne, "Test1", 1, 13, 9);

        this.getValue(this.counterStoreOne, "Test1", 1, 16, 13);

        this.getValue(this.counterStoreOne, "Test2", 2, 4, 1);

        this.getValue(this.counterStoreTwo, "Test2", 2, 7, 4);

        assertEquals(
                "rowCount", 2, JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "UP_SEQUENCE"));
        assertEquals(
                "Test1 counter value",
                16,
                (int)
                        jdbcTemplate.queryForObject(
                                "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?",
                                Integer.class,
                                "Test1"));
        assertEquals(
                "Test2 counter value",
                7,
                (int)
                        jdbcTemplate.queryForObject(
                                "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?",
                                Integer.class,
                                "Test2"));
    }

    /**
     * Two servers fetch a new block of the same counter at once: B reads the counter, A takes a
     * block and commits, then B writes. B has to see that it lost the race and try again, as the
     * store's compare-and-set expects. Under snapshot isolation the write of a row that changed
     * after the transaction read it fails instead: MariaDB 11.6 and later do that by default under
     * REPEATABLE READ ("Record has changed since last read in table 'UP_SEQUENCE'").
     *
     * <p>Both servers use the portal's own counterStoreTransactionOperations definition, on an
     * HSQLDB database with MVCC transactions whose connections default to SERIALIZABLE. HSQLDB then
     * refuses B's write in the same way, unless the definition asks for READ COMMITTED.
     */
    @Test
    public void testBlockTakenBetweenReadAndWrite() throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:counterStoreRace;hsqldb.tx=mvcc");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            final JdbcTemplate raceJdbcTemplate = new JdbcTemplate(dataSource);
            raceJdbcTemplate.update(
                    "CREATE TABLE UP_SEQUENCE (SEQUENCE_NAME VARCHAR(1000), SEQUENCE_VALUE INTEGER)");
            raceJdbcTemplate.update(
                    "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME, SEQUENCE_VALUE) VALUES ('Race', 1)");
            final TransactionOperations transactionOperations =
                    portalCounterStoreTransactionOperations(
                            new DataSourceTransactionManager(dataSource));

            final HibernateStyleCounterStore serverA = new HibernateStyleCounterStore();
            serverA.setIncrementSize(3);
            serverA.setInitialValue(1);
            serverA.setTransactionOperations(transactionOperations);
            serverA.setJdbcOperations(raceJdbcTemplate);

            final CountDownLatch read = new CountDownLatch(1);
            final CountDownLatch resume = new CountDownLatch(1);
            final AtomicReference<Thread> pausedThread = new AtomicReference<>();
            final HibernateStyleCounterStore serverB = new HibernateStyleCounterStore();
            serverB.setIncrementSize(3);
            serverB.setInitialValue(1);
            serverB.setTransactionOperations(transactionOperations);
            serverB.setJdbcOperations(
                    new JdbcTemplate(dataSource) {
                        @Override
                        public int update(String sql, PreparedStatementSetter pss) {
                            if (sql.startsWith("UPDATE UP_SEQUENCE")
                                    && pausedThread.compareAndSet(Thread.currentThread(), null)) {
                                read.countDown();
                                try {
                                    if (!resume.await(30, TimeUnit.SECONDS)) {
                                        throw new IllegalStateException("Not resumed");
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException(e);
                                }
                            }
                            return super.update(sql, pss);
                        }
                    });

            final AtomicInteger idOfB = new AtomicInteger();
            final AtomicReference<Throwable> errorOfB = new AtomicReference<>();
            final Thread threadB =
                    new Thread(
                            () -> {
                                try {
                                    idOfB.set(serverB.getNextId("Race"));
                                } catch (Throwable t) {
                                    errorOfB.set(t);
                                }
                            },
                            "CounterStoreTest-serverB");
            pausedThread.set(threadB);
            threadB.start();
            assertTrue("B read the counter", read.await(30, TimeUnit.SECONDS));

            final int idOfA = serverA.getNextId("Race");

            resume.countDown();
            threadB.join(TimeUnit.SECONDS.toMillis(30));
            assertFalse(threadB.isAlive());
            if (errorOfB.get() != null) {
                throw new AssertionError("B failed instead of trying again", errorOfB.get());
            }
            assertNotEquals("A and B got the same id", idOfA, idOfB.get());
        } finally {
            new JdbcTemplate(dataSource).execute("SHUTDOWN");
            dataSource.close();
        }
    }

    /**
     * The portal's counterStoreTransactionOperations bean, built from persistenceContext.xml on the
     * given transaction manager.
     */
    private static TransactionOperations portalCounterStoreTransactionOperations(
            PlatformTransactionManager transactionManager) {
        final DefaultListableBeanFactory portalDefinitions = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(portalDefinitions)
                .loadBeanDefinitions("classpath:properties/contexts/persistenceContext.xml");
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
        beanFactory.registerSingleton("transactionManager", transactionManager);
        beanFactory.registerBeanDefinition(
                "counterStoreTransactionOperations",
                portalDefinitions.getBeanDefinition("counterStoreTransactionOperations"));
        return beanFactory.getBean(
                "counterStoreTransactionOperations", TransactionOperations.class);
    }

    protected void getValue(
            ICounterStore counterStore,
            String counter,
            int rows,
            int tableValue,
            int counterValue) {
        final int v = counterStore.getNextId(counter);
        assertEquals("counterValue", counterValue, v);
        assertEquals("rows", rows, JdbcTestUtils.countRowsInTable(jdbcTemplate, "UP_SEQUENCE"));
        assertEquals(
                "tableValue",
                tableValue,
                (int)
                        jdbcTemplate.queryForObject(
                                "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME=?",
                                Integer.class,
                                counter));
    }
}
