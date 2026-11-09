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
package org.apereo.portal.layout.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.apereo.portal.security.IPerson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Two requests of the same user ask for the next structure id at once, such as two portlets added
 * from two browser tabs. Each request has to get an id of its own.
 */
public class RDBMUserLayoutStoreStructIdTest {
    private static final int USER_ID = 9001;

    private EmbeddedDatabase dataSource;
    private JdbcTemplate jdbcTemplate;
    private PausingLayoutStore store;
    private IPerson person;

    @Before
    public void setUp() {
        dataSource =
                new EmbeddedDatabaseBuilder()
                        .generateUniqueName(true)
                        .setType(EmbeddedDatabaseType.HSQL)
                        .build();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(
                "CREATE TABLE UP_USER (USER_ID integer NOT NULL, NEXT_STRUCT_ID integer,"
                        + " PRIMARY KEY (USER_ID))");
        jdbcTemplate.update("INSERT INTO UP_USER (USER_ID, NEXT_STRUCT_ID) VALUES (?, 0)", USER_ID);

        store =
                mock(
                        PausingLayoutStore.class,
                        withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        store.setPlatformTransactionManager(new DataSourceTransactionManager(dataSource));
        store.setDataSource(dataSource);
        person = mock(IPerson.class);
        when(person.getID()).thenReturn(USER_ID);
    }

    @After
    public void tearDown() {
        dataSource.shutdown();
    }

    /**
     * A asks for the next structure id and stops before its write, B takes an id meanwhile, then A
     * finishes. Both get an id, and the two ids differ.
     */
    @Test
    public void testStructIdTakenBetweenReadAndWrite() throws Exception {
        final AtomicReference<String> idOfA = new AtomicReference<>();
        final AtomicReference<Throwable> errorOfA = new AtomicReference<>();
        final Thread threadA =
                new Thread(
                        () -> {
                            try {
                                idOfA.set(store.getNextStructId(person, "s"));
                            } catch (Throwable t) {
                                errorOfA.set(t);
                            }
                        },
                        "RDBMUserLayoutStoreStructIdTest-A");
        store.pausedThread = threadA;

        threadA.start();
        assertTrue("A reached its write", store.paused.await(30, TimeUnit.SECONDS));
        final String idOfB = store.getNextStructId(person, "s");
        store.resume.countDown();
        threadA.join(TimeUnit.SECONDS.toMillis(60));
        assertFalse(threadA.isAlive());
        if (errorOfA.get() != null) {
            throw new AssertionError("A failed", errorOfA.get());
        }

        assertNotEquals("A and B got the same id", idOfB, idOfA.get());
        assertEquals(
                Integer.valueOf(2),
                jdbcTemplate.queryForObject(
                        "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=?",
                        Integer.class,
                        USER_ID));
    }

    /**
     * Stops one thread before its update of NEXT_STRUCT_ID until the test lets it go on. The
     * abstract methods are left abstract: the test creates the store with Mockito, which calls the
     * real methods.
     */
    abstract static class PausingLayoutStore extends RDBMUserLayoutStore {
        private static final String PAUSE_BEFORE = "UPDATE UP_USER SET NEXT_STRUCT_ID";

        final CountDownLatch paused = new CountDownLatch(1);
        final CountDownLatch resume = new CountDownLatch(1);
        volatile Thread pausedThread;

        @Override
        public void setDataSource(DataSource dataSource) {
            jdbcOperations =
                    new JdbcTemplate(dataSource) {
                        @Override
                        protected Connection createConnectionProxy(Connection con) {
                            return (Connection)
                                    pausing(super.createConnectionProxy(con), Connection.class);
                        }
                    };
        }

        /** Wraps a connection or statement so that the paused thread stops before its update. */
        private Object pausing(Object target, Class<?> type) {
            final InvocationHandler handler =
                    (proxy, method, args) -> {
                        if (args != null && args.length > 0 && args[0] instanceof String) {
                            pauseIfDue((String) args[0]);
                        }
                        try {
                            final Object result = method.invoke(target, args);
                            return "createStatement".equals(method.getName())
                                    ? pausing(result, Statement.class)
                                    : result;
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    };
            return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
        }

        private void pauseIfDue(String sql) throws InterruptedException {
            if (Thread.currentThread() == pausedThread && sql.startsWith(PAUSE_BEFORE)) {
                pausedThread = null;
                paused.countDown();
                if (!resume.await(30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Not resumed");
                }
            }
        }
    }
}
