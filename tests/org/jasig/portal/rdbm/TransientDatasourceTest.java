/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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

