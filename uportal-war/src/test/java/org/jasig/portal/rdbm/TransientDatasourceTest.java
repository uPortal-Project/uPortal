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

package org.jasig.portal.rdbm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

/**
 * Testcase for HsqlDatasource
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class TransientDatasourceTest extends TestCase {

    /**
     * Test basic single usage of a Transient datasource.
     * @throws SQLException
     */
    public void testBasics() throws SQLException {
        TransientDatasource source = new TransientDatasource();
        
        Connection c = source.getConnection();
        
        c.prepareStatement("CREATE TABLE foo_table (bar_col INT)").execute();
        PreparedStatement inserter = c.prepareStatement("INSERT INTO foo_table (bar_col) VALUES (?)");
        inserter.setInt(1, 10);
        inserter.execute();
        inserter.setInt(1, 11);
        inserter.execute();
        inserter.setInt(1, 12);
        inserter.execute();
        inserter.setInt(1, 13);
        inserter.execute();
        
        PreparedStatement query = c.prepareStatement("SELECT bar_col FROM foo_table ORDER BY bar_col");
        ResultSet rs = query.executeQuery();
        
        for (int i = 10; i < 14; i++) {
            rs.next();
            assertEquals(i, rs.getInt("bar_col"));
        }
        
        c.prepareStatement("DROP TABLE foo_table").execute();
        
        c.close();
        
    }
    
    /**
     * Test that changes made to the database persist across requests for the
     * connection.
     * @throws SQLException
     */
    public void testMultiuse() throws SQLException {
        TransientDatasource source = new TransientDatasource();
        
        Connection connectionOne = source.getConnection();
        
        connectionOne.prepareStatement("CREATE TABLE foo_table (bar_col INT)").execute();
        PreparedStatement inserter = connectionOne.prepareStatement("INSERT INTO foo_table (bar_col) VALUES (?)");
        inserter.setInt(1, 10);
        inserter.execute();
        inserter.setInt(1, 11);
        inserter.execute();
        inserter.setInt(1, 12);
        inserter.execute();
        inserter.setInt(1, 13);
        inserter.execute();
        
        connectionOne.close();
        
        Connection connectionTwo = source.getConnection();
        
        PreparedStatement query = connectionTwo.prepareStatement("SELECT bar_col FROM foo_table ORDER BY bar_col");
        ResultSet rs = query.executeQuery();
        
        for (int i = 10; i < 14; i++) {
            rs.next();
            assertEquals(i, rs.getInt("bar_col"));
        }
        
        connectionTwo.prepareStatement("DROP TABLE foo_table").execute();
        
        connectionTwo.close();
        
    }
    
}

