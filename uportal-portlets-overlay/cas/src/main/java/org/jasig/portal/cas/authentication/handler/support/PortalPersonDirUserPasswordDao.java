/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.cas.authentication.handler.support;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Retrieves password hashes from the uPortal UP_PERSON_DIR table
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalPersonDirUserPasswordDao implements UserPasswordDao {
    private static final String PERSON_DIR_QUERY = "SELECT ENCRPTD_PSWD FROM UP_PERSON_DIR WHERE USER_NAME = ?";

    private DataSource dataSource;
    private SimpleJdbcTemplate simpleJdbcTemplate;

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.cas.authentication.handler.support.UserPasswordDao#getPasswordHash(java.lang.String)
     */
    public String getPasswordHash(String userName) {
        return this.simpleJdbcTemplate.queryForObject(PERSON_DIR_QUERY, String.class, userName);
    }
}
