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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;;

/**
 * Retrieves password hashes from the uPortal UP_PERSON_DIR table
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalPersonDirUserPasswordDao implements UserPasswordDao {
    private static final String PERSON_DIR_QUERY = "SELECT ENCRPTD_PSWD FROM UP_PERSON_DIR WHERE USER_NAME = ?";

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

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
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.cas.authentication.handler.support.UserPasswordDao#getPasswordHash(java.lang.String)
     */
    public String getPasswordHash(String userName) {
        try {
            return this.jdbcTemplate.queryForObject(PERSON_DIR_QUERY, String.class, userName);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
