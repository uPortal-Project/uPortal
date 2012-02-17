/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml.layout;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Function;

/**
 * Lists each Permission Set in the portal
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ProfilesDataFunction implements Function<IPortalDataType, Iterable<? extends IPortalData>> {
    private JdbcOperations jdbcOperations;
    private static final String QUERY =
        "SELECT upup.profile_fname, upu.user_name " +
        "FROM up_user_profile upup " +
        "LEFT JOIN up_user upu ON upu.user_id = upup.user_id";

    @Required
    public void setDataSource(DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.afterPropertiesSet();
        this.jdbcOperations = jdbcTemplate;
    }

    @Override
    public Iterable<? extends IPortalData> apply(IPortalDataType input) {
        return this.jdbcOperations.query(QUERY, new RowMapper<IPortalData>() {
            @Override
            public IPortalData mapRow(ResultSet rs, int rowNum) throws SQLException {
                final StringBuilder key = new StringBuilder();
                
                key.append(rs.getString("user_name")).append("|");
                key.append(rs.getString("profile_fname"));
                
                return new SimpleStringPortalData(key.toString(), null, null);
            }
        });
    }
}
