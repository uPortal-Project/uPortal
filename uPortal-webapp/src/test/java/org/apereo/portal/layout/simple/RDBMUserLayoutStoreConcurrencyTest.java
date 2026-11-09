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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.layout.LayoutStructure;
import org.apereo.portal.security.IPerson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Two requests of the same user write the layout tables at once, such as parallel sign-ins that
 * each save the merged layout. Under snapshot isolation a write to a row that another transaction
 * changed after this one first read fails: MariaDB 11.6 and later do that by default under
 * REPEATABLE READ (innodb_snapshot_isolation), with error 1020 "Record has changed since last read
 * in table 'up_layout_param'".
 *
 * <p>The test's connections default to SERIALIZABLE (rdbmUserLayoutStoreTestContext.xml), so a save
 * that doesn't ask for READ COMMITTED itself shows up on every database, HSQLDB included.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:rdbmUserLayoutStoreTestContext.xml")
public class RDBMUserLayoutStoreConcurrencyTest {
    private static final int USER_ID = 9001;
    private static final int SYSTEM_USER_ID = 0;
    private static final int PROFILE_ID = 1;
    private static final int LAYOUT_ID = 1;

    @Autowired private DataSource dataSource;
    @Autowired private PlatformTransactionManager transactionManager;

    private JdbcTemplate jdbcTemplate;
    private PausingLayoutStore store;
    private IPerson person;
    private IUserProfile profile;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        runSql(
                "CREATE TABLE UP_USER (USER_ID integer NOT NULL, USER_NAME varchar(100),"
                        + " NEXT_STRUCT_ID integer, LST_CHAN_UPDT_DT timestamp,"
                        + " PRIMARY KEY (USER_ID))");
        runSql(
                "CREATE TABLE UP_USER_PROFILE (USER_ID integer NOT NULL,"
                        + " PROFILE_ID integer NOT NULL, LAYOUT_ID integer,"
                        + " PRIMARY KEY (USER_ID, PROFILE_ID))");
        runSql(
                "CREATE TABLE UP_USER_LAYOUT (LAYOUT_ID integer NOT NULL,"
                        + " USER_ID integer NOT NULL, LAYOUT_TITLE varchar(15),"
                        + " INIT_STRUCT_ID integer, PRIMARY KEY (LAYOUT_ID, USER_ID))");
        runSql(
                "CREATE TABLE UP_LAYOUT_STRUCT (USER_ID integer NOT NULL,"
                        + " LAYOUT_ID integer NOT NULL, STRUCT_ID integer NOT NULL,"
                        + " NEXT_STRUCT_ID integer, CHLD_STRUCT_ID integer,"
                        + " EXTERNAL_ID varchar(64), CHAN_ID integer, NAME varchar(35),"
                        + " TYPE varchar(35), HIDDEN varchar(1), IMMUTABLE varchar(1),"
                        + " UNREMOVABLE varchar(1), PRIMARY KEY (LAYOUT_ID, USER_ID, STRUCT_ID))");
        runSql(
                "CREATE TABLE UP_LAYOUT_PARAM (USER_ID integer NOT NULL,"
                        + " LAYOUT_ID integer NOT NULL, STRUCT_ID integer NOT NULL,"
                        + " STRUCT_PARM_NM varchar(35) NOT NULL,"
                        + " STRUCT_PARM_VAL varchar(2000) NOT NULL,"
                        + " PRIMARY KEY (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM))");
        jdbcTemplate.update(
                "INSERT INTO UP_USER (USER_ID, USER_NAME, NEXT_STRUCT_ID) VALUES (?, 'student', 0)",
                USER_ID);
        // The template of a user's first layout
        jdbcTemplate.update(
                "INSERT INTO UP_USER (USER_ID, USER_NAME, NEXT_STRUCT_ID) VALUES (?, 'system', 0)",
                SYSTEM_USER_ID);
        jdbcTemplate.update(
                "INSERT INTO UP_USER_LAYOUT (LAYOUT_ID, USER_ID, LAYOUT_TITLE, INIT_STRUCT_ID)"
                        + " VALUES (?, ?, 'default layout', 1)",
                LAYOUT_ID,
                SYSTEM_USER_ID);
        jdbcTemplate.update(
                "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, LAYOUT_ID) VALUES (?, ?, ?)",
                USER_ID,
                PROFILE_ID,
                LAYOUT_ID);
        jdbcTemplate.update(
                "INSERT INTO UP_USER_LAYOUT (LAYOUT_ID, USER_ID, LAYOUT_TITLE, INIT_STRUCT_ID)"
                        + " VALUES (?, ?, 'default layout', 1)",
                LAYOUT_ID,
                USER_ID);

        store =
                mock(
                        PausingLayoutStore.class,
                        withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        store.setPlatformTransactionManager(transactionManager);
        store.setDataSource(dataSource);
        person = mock(IPerson.class);
        when(person.getID()).thenReturn(USER_ID);
        when(person.getUserName()).thenReturn("student");
        profile = mock(IUserProfile.class);
        when(profile.getProfileId()).thenReturn(PROFILE_ID);
    }

    @After
    public void tearDown() {
        runSql("DROP TABLE UP_LAYOUT_PARAM");
        runSql("DROP TABLE UP_LAYOUT_STRUCT");
        runSql("DROP TABLE UP_USER_LAYOUT");
        runSql("DROP TABLE UP_USER_PROFILE");
        runSql("DROP TABLE UP_USER");
    }

    /**
     * A reads the user's layout id and stops before its write, and B starts to save meanwhile. On
     * MariaDB 11.6+ under REPEATABLE READ, B committed and A's delete then failed with 1020. Under
     * READ COMMITTED A replaces the rows B committed: both save, and A's layout remains.
     */
    @Test
    public void testLayoutSavedBetweenReadAndWrite() throws Exception {
        store.setUserLayout(person, profile, layout("first"), false);

        runPausedAt(
                "DELETE FROM UP_LAYOUT_PARAM",
                () -> store.setUserLayout(person, profile, layout("A"), false),
                () -> store.setUserLayout(person, profile, layout("B"), false));

        assertStoredLayout("A");
        assertSavedUnderReadCommitted();
    }

    /** {@link #runPausedAt(String, Runnable, Runnable, int)} with two seconds for B. */
    private boolean runPausedAt(String sqlPrefix, Runnable taskOfA, Runnable taskOfB)
            throws InterruptedException {
        return runPausedAt(sqlPrefix, taskOfA, taskOfB, 2);
    }

    /**
     * Runs {@code taskOfA} in a thread that stops before its first statement that starts with
     * {@code sqlPrefix}, then {@code taskOfB} in a second thread. B has {@code secondsForB} seconds
     * to finish, unless the database makes it wait for A, and then A goes on. Both tasks have to
     * succeed.
     *
     * @return whether B was still running when A went on
     */
    private boolean runPausedAt(
            String sqlPrefix, Runnable taskOfA, Runnable taskOfB, int secondsForB)
            throws InterruptedException {
        final AtomicReference<Throwable> errorOfA = new AtomicReference<>();
        final AtomicReference<Throwable> errorOfB = new AtomicReference<>();
        final Thread threadA = inThread("A", taskOfA, errorOfA);
        final Thread threadB = inThread("B", taskOfB, errorOfB);
        store.pause(threadA, sqlPrefix, threadB);

        threadA.start();
        assertTrue("A reached " + sqlPrefix, store.paused.await(30, TimeUnit.SECONDS));
        threadB.start();
        assertTrue("B started", store.otherStarted.await(30, TimeUnit.SECONDS));
        threadB.join(TimeUnit.SECONDS.toMillis(secondsForB));
        final boolean bWaited = threadB.isAlive();
        store.resume.countDown();
        threadA.join(TimeUnit.SECONDS.toMillis(60));
        threadB.join(TimeUnit.SECONDS.toMillis(60));
        assertFalse(threadA.isAlive());
        assertFalse(threadB.isAlive());

        if (errorOfA.get() != null) {
            throw new AssertionError("A failed", errorOfA.get());
        }
        if (errorOfB.get() != null) {
            throw new AssertionError("B failed", errorOfB.get());
        }
        return bWaited;
    }

    private static Thread inThread(String name, Runnable task, AtomicReference<Throwable> error) {
        return new Thread(
                () -> {
                    try {
                        task.run();
                    } catch (Throwable t) {
                        error.set(t);
                    }
                },
                "RDBMUserLayoutStoreConcurrencyTest-" + name);
    }

    /** The user's layout is the one folder {@code name} saved, with one row in each table. */
    private void assertStoredLayout(String name) {
        assertEquals(
                Collections.singletonList(name),
                jdbcTemplate.queryForList(
                        "SELECT STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=?",
                        String.class,
                        USER_ID));
        assertEquals(
                Integer.valueOf(1),
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM UP_LAYOUT_STRUCT WHERE USER_ID=?",
                        Integer.class,
                        USER_ID));
    }

    /**
     * The test's connections default to SERIALIZABLE, so a save that doesn't ask for READ COMMITTED
     * itself shows up here on every database.
     */
    private void assertSavedUnderReadCommitted() {
        assertFalse("The store prepared statements", store.isolations.isEmpty());
        for (final int isolation : store.isolations) {
            assertEquals(
                    "Isolation of a save's transaction",
                    Connection.TRANSACTION_READ_COMMITTED,
                    isolation);
        }
    }

    private void runSql(String sql) {
        new ResourceDatabasePopulator(new ByteArrayResource(sql.getBytes())).execute(dataSource);
    }

    /** A layout whose one folder carries {@code name} in its parameter row. */
    private static Document layout(String name) {
        final Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        final Element root = document.createElement("layout");
        final Element folder = document.createElement("folder");
        folder.setAttribute("name", name);
        root.appendChild(folder);
        document.appendChild(root);
        return document;
    }

    /**
     * Saves one structure row and one parameter row per layout, stops one thread before a given
     * statement until the test lets it go on, tells the test when another thread sends its first
     * statement, and records the isolation level of every prepared statement's connection. The DLM
     * methods are left abstract: the test creates it with Mockito, which calls the real methods.
     */
    abstract static class PausingLayoutStore extends RDBMUserLayoutStore {
        final CountDownLatch paused = new CountDownLatch(1);
        final CountDownLatch resume = new CountDownLatch(1);
        final CountDownLatch otherStarted = new CountDownLatch(1);
        final List<Integer> isolations = new CopyOnWriteArrayList<>();
        private volatile Thread pausedThread;
        private volatile String pauseBefore;
        private volatile Thread otherThread;

        void pause(Thread thread, String sqlPrefix, Thread other) {
            pausedThread = thread;
            pauseBefore = sqlPrefix;
            otherThread = other;
        }

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

        /** Wraps a connection or statement so that the paused thread stops before the statement. */
        private Object pausing(Object target, Class<?> type) {
            final InvocationHandler handler =
                    (proxy, method, args) -> {
                        if ("prepareStatement".equals(method.getName())) {
                            isolations.add(((Connection) target).getTransactionIsolation());
                        }
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
            if (Thread.currentThread() == otherThread) {
                otherStarted.countDown();
            } else if (Thread.currentThread() == pausedThread && sql.startsWith(pauseBefore)) {
                pausedThread = null;
                paused.countDown();
                if (!resume.await(30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Not resumed");
                }
            }
        }

        @Override
        protected Element getStructure(Document doc, LayoutStructure ls) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected int saveStructure(
                Node node, PreparedStatement structStmt, PreparedStatement parmStmt)
                throws SQLException {
            structStmt.setInt(1, 1);
            structStmt.setInt(2, 0);
            structStmt.setInt(3, 0);
            structStmt.setNull(4, Types.VARCHAR);
            structStmt.setNull(5, Types.INTEGER);
            structStmt.setString(6, "folder");
            structStmt.setString(7, "regular");
            structStmt.setString(8, "N");
            structStmt.setString(9, "N");
            structStmt.setString(10, "N");
            structStmt.executeUpdate();

            parmStmt.setInt(1, 1);
            parmStmt.setString(2, "name");
            parmStmt.setString(3, ((Element) node).getAttribute("name"));
            parmStmt.executeUpdate();
            return 1;
        }
    }
}
