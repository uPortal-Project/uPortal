/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jasig.portal.rdbm.TransientDatasource;

import junit.framework.TestCase;

/**
 * Test the JdbcPersonAttributeDaoImpl against a dummy DataSource.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class JdbcPersonAttributeDaoImplTest extends TestCase {

    /**
     * The JDBC person attribute dao implementation we are testing.
     */
    private JdbcPersonAttributeDaoImpl dao;
    
    private DataSource ds;
    
    protected void setUp() throws SQLException {
        this.ds = new TransientDatasource();
        
        
        
        JdbcPersonAttributeDaoImpl impl = 
            new JdbcPersonAttributeDaoImpl(this.ds, 
                    "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");
        
        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("name", "firstName");
        
        Set emailAttributeNames = new HashSet();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setColumnsToAttributes(columnsToAttributes);
        
        this.dao = impl;
    }
    
    protected void tearDown() throws SQLException {
       
    }
    
    /**
     * Test that the implementation properly looks up attributes for a user.
     * @throws SQLException
     */
   public void testAttributesForUser() throws SQLException {
       
       //  set up the datasource
       Connection con = this.ds.getConnection();
       
       con.prepareStatement("CREATE TABLE user_table " +
            "(netid VARCHAR, " +
            "name VARCHAR, " +
            "email VARCHAR, " +
            "shirt_color VARCHAR)").execute();
       
       con.prepareStatement("INSERT INTO user_table " +
            "(netid, name, email, shirt_color) " +
            "VALUES ('awp9', 'Andrew', 'andrew.petro@yale.edu', 'blue')").execute();
       
       con.close();
       
       try {
           Map attribs = this.dao.attributesForUser("awp9");
           assertEquals("andrew.petro@yale.edu", attribs.get("email"));
           assertEquals("andrew.petro@yale.edu", attribs.get("emailAddress"));
           assertEquals("blue", attribs.get("dressShirtColor"));
           assertNull(attribs.get("shirt_color"));
           assertEquals("Andrew", attribs.get("firstName"));
       } finally {
           // reset the database
           con = this.ds.getConnection();
            
            con.prepareStatement("DROP TABLE user_table").execute();
            
            
            con.close();
       }

   }
   
   /**
    * Test that the implementation properly reports the attribute names it
    * expects to map.
    */
   public void testAttributeNames() {
       Set expectedAttributeNames = new HashSet();
       expectedAttributeNames.add("firstName");
       expectedAttributeNames.add("email");
       expectedAttributeNames.add("emailAddress");
       expectedAttributeNames.add("dressShirtColor");
       
       Set attributeNames = this.dao.getAttributeNames();
       assertEquals(attributeNames, expectedAttributeNames);
   }

}
