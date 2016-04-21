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
package org.jasig.portal.io.xml.permission;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.jasig.portal.security.IPermission;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Function;

/**
 * Lists each Permission Set in the portal during the export process.
 * 
 * @author Eric Dalquist
 */
public class PermissionSetsDataFunction implements Function<IPortalDataType, Iterable<? extends IPortalData>> {
    private JdbcOperations jdbcOperations;

    /**
     * NOTE:  Both SUBSCRIBE and BROWSE are handled inside the portlet-definition.xml
     * file whenever they apply to <em>portlets</em>.  (They are handled here, per
     * normal, when they apply to categories.)
     */
    private static final String QUERY =
        "SELECT DISTINCT UPP.OWNER, UPET.ENTITY_TYPE_NAME, UPP.PRINCIPAL_KEY, UPP.ACTIVITY, UPP.PRINCIPAL_TYPE\n " + 
        "FROM UP_PERMISSION UPP\n " + 
        "LEFT JOIN UP_ENTITY_TYPE UPET ON UPP.PRINCIPAL_TYPE = UPET.ENTITY_TYPE_ID\n " + 
        "WHERE NOT (UPP.ACTIVITY = 'SUBSCRIBE' AND UPP.TARGET LIKE '" + IPermission.PORTLET_PREFIX + "%')\n " +
        "AND NOT (UPP.ACTIVITY = 'BROWSE' AND UPP.TARGET LIKE '" + IPermission.PORTLET_PREFIX + "%')\n ";

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

                key.append(rs.getString("OWNER")).append("|");
                key.append(rs.getString("ENTITY_TYPE_NAME")).append("|");
                key.append(rs.getString("PRINCIPAL_KEY")).append("|");
                key.append(rs.getString("ACTIVITY")).append("|");
                key.append(rs.getString("PRINCIPAL_TYPE"));

                return new SimpleStringPortalData(key.toString(), null, null);
            }
        });
    }
}
