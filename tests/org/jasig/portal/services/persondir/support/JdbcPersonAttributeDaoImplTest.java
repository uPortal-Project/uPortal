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

   public void testDao() throws SQLException {
       DataSource testDataSource = new TransientDatasource();
       
       Connection con = testDataSource.getConnection();
       
       con.prepareStatement("CREATE TABLE user_table " +
            "(netid VARCHAR, " +
            "name VARCHAR, " +
            "email VARCHAR, " +
            "shirt_color VARCHAR)").execute();
       
       con.prepareStatement("INSERT INTO user_table " +
            "(netid, name, email, shirt_color) " +
            "VALUES ('awp9', 'Andrew', 'andrew.petro@yale.edu', 'blue')").execute();
       
       con.close();
       
       JdbcPersonAttributeDaoImpl impl = 
           new JdbcPersonAttributeDaoImpl(testDataSource, 
                   "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");
       
       Set emailAttributeNames = new HashSet();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);
       
       Map attribs = impl.attributesForUser("awp9");
       assertEquals("andrew.petro@yale.edu", attribs.get("email"));
       assertEquals("andrew.petro@yale.edu", attribs.get("emailAddress"));
       assertEquals("blue", attribs.get("dressShirtColor"));
       assertNull(attribs.get("shirt_color"));
       assertEquals("Andrew", attribs.get("firstName"));
       
   }

}
