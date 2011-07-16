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

package org.jasig.portal.io.xml.crn;

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
 * Simple Function for use in {@link CernunnosDataExporter#setPortalDataRetriever(Function)} that uses a
 * parameter-less sql query to build a list of IPortalData objects
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SqlPortalDataFunction implements Function<IPortalDataType, Iterable<? extends IPortalData>> {
    private JdbcOperations jdbcOperations;
    private String query;
    private String idColumn;
    private String nameColumn;
    private String descColumn;

    /**
     * @param query Query to run
     */
    @Required
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @param id Column column name that contains the id, if null the first column is used
     */
    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    /**
     * @param nameColumn Column column name that contains the name, if null no name is set
     */
    public void setNameColumn(String nameColumn) {
        this.nameColumn = nameColumn;
    }

    /**
     * @param nameColumn Column column name that contains the description, if null no description is set
     */
    public void setDescColumn(String descColumn) {
        this.descColumn = descColumn;
    }


    @Required
    public void setDataSource(DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.afterPropertiesSet();
        this.jdbcOperations = jdbcTemplate;
    }
    

    @Override
    public Iterable<? extends IPortalData> apply(IPortalDataType input) {
        return this.jdbcOperations.query(this.query, new RowMapper<IPortalData>() {
            @Override
            public IPortalData mapRow(ResultSet rs, int rowNum) throws SQLException {
                final String id;
                if (idColumn != null) {
                    id = rs.getString(idColumn);
                }
                else {
                    id = rs.getString(1);
                }
                
                final String name = getValue(rs, nameColumn);
                final String description = getValue(rs, descColumn);
                return new SimpleStringPortalData(id, name, description);
            }

            protected String getValue(ResultSet rs, String column) throws SQLException {
                if (column != null) {
                    return rs.getString(column);
                }
                return null;
            }
        });
    }
}
