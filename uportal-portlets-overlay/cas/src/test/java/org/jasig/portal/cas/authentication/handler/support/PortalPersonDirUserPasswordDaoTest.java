/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.cas.authentication.handler.support;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

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
        this.dataSource = new SimpleDriverDataSource(new org.hsqldb.jdbcDriver(), "jdbc:hsqldb:mem:CasTest", "sa", "");
        
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.jdbcTemplate.execute("CREATE TABLE UP_PERSON_DIR (USER_NAME VARCHAR(1000), ENCRPTD_PSWD VARCHAR(1000))");
        
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
