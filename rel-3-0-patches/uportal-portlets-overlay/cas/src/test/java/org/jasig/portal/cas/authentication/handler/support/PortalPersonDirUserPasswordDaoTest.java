/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.cas.authentication.handler.support;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalPersonDirUserPasswordDaoTest extends TestCase {
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private PortalPersonDirUserPasswordDao userPasswordDao;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.dataSource = new DriverManagerDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:CasTest", "sa", "");
        
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.jdbcTemplate.execute("CREATE TABLE UP_PERSON_DIR (USER_NAME VARCHAR, ENCRPTD_PSWD VARCHAR)");
        
        this.userPasswordDao = new PortalPersonDirUserPasswordDao();
        this.userPasswordDao.setDataSource(this.dataSource);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.jdbcTemplate.execute("SHUTDOWN");
        
        this.dataSource = null;
        this.jdbcTemplate = null;
        this.userPasswordDao = null;
    }

    public void testNonExistantUser() {
        final String passwordHash = this.userPasswordDao.getPasswordHash("foobar");
        assertNull(passwordHash);
    }

    public void testSingleUser() {
        this.jdbcTemplate.update("INSERT INTO UP_PERSON_DIR VALUES ('foobar', 'pass1')");
        

        final String passwordHash = this.userPasswordDao.getPasswordHash("foobar");
        assertEquals("pass1", passwordHash);
    }

    public void testDuplicateUser() {
        this.jdbcTemplate.update("INSERT INTO UP_PERSON_DIR VALUES ('foobar', 'pass1')");
        this.jdbcTemplate.update("INSERT INTO UP_PERSON_DIR VALUES ('foobar', 'pass2')");
        
        try {
            this.userPasswordDao.getPasswordHash("foobar");
            fail("should have thrown IncorrectResultSizeDataAccessException");
        }
        catch (IncorrectResultSizeDataAccessException e) {
            //expected
        }
    }
}
