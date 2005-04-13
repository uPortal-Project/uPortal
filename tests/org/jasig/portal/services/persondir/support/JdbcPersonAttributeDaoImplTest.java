/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.jasig.portal.rdbm.TransientDatasource;
import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * Test the {@link JdbcPersonAttributeDaoImpl} against a dummy DataSource.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class JdbcPersonAttributeDaoImplTest 
    extends AbstractPersonAttributeDaoTest {
    
    private DataSource testDataSource;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        this.testDataSource = new TransientDatasource();
        Connection con = testDataSource.getConnection();
        
        con.prepareStatement("CREATE TABLE user_table " +
                                  "(netid VARCHAR, " +
                                  "name VARCHAR, " +
                                  "email VARCHAR, " +
                                  "shirt_color VARCHAR)").execute();

        con.prepareStatement("INSERT INTO user_table " +
                                  "(netid, name, email, shirt_color) " +
                                  "VALUES ('awp9', 'Andrew', 'andrew.petro@yale.edu', 'blue')").execute();

        con.prepareStatement("INSERT INTO user_table " +
                                 "(netid, name, email, shirt_color) " +
                                 "VALUES ('edalquist', 'Eric', 'edalquist@unicon.net', 'blue')").execute();

        con.prepareStatement("INSERT INTO user_table " +
				                "(netid, name, email, shirt_color) " +
				                "VALUES ('atest', 'Andrew', 'andrew.test@test.net', 'red')").execute();
        
        con.prepareStatement("INSERT INTO user_table " +
				                "(netid, name, email, shirt_color) " +
				                "VALUES ('susan', 'Susan', 'susan.test@test.net', null)").execute();
        con.close();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        Connection con = this.testDataSource.getConnection();
        
        con.prepareStatement("DROP TABLE user_table").execute();
        con.prepareStatement("SHUTDOWN").execute();

        con.close();
        
        this.testDataSource = null;
    }
    

   /**
    * Test that the implementation properly reports the attribute names it
    * expects to map.
    */
   public void testPossibleUserAttributeNames() {
       final String queryAttr = "uid";
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       JdbcPersonAttributeDaoImpl impl = 
           new JdbcPersonAttributeDaoImpl(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");

       Set emailAttributeNames = new HashSet();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);

       Set expectedAttributeNames = new HashSet();
       expectedAttributeNames.add("firstName");
       expectedAttributeNames.add("email");
       expectedAttributeNames.add("emailAddress");
       expectedAttributeNames.add("dressShirtColor");
       
       Set attributeNames = impl.getPossibleUserAttributeNames();
       assertEquals(attributeNames, expectedAttributeNames);
   }

   /**
    * Test for a query with a single attribute
    */
   public void testSingleAttrQuery() {
       final String queryAttr = "uid";
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       JdbcPersonAttributeDaoImpl impl = 
           new JdbcPersonAttributeDaoImpl(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");

       Set emailAttributeNames = new HashSet();
       emailAttributeNames.add("email");
       emailAttributeNames.add("emailAddress");
       columnsToAttributes.put("email", emailAttributeNames);
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);

       Map attribs = impl.getUserAttributes("awp9");
       assertEquals("andrew.petro@yale.edu", attribs.get("email"));
       assertEquals("andrew.petro@yale.edu", attribs.get("emailAddress"));
       assertEquals("blue", attribs.get("dressShirtColor"));
       assertNull(attribs.get("shirt_color"));
       assertEquals("Andrew", attribs.get("firstName"));
   }
   
   
   /**
    * Test for a query with a null value attribute
    */
   public void testNullAttrQuery() {
       final String queryAttr = "uid";
       final List queryAttrList = new LinkedList();
       queryAttrList.add(queryAttr);

       JdbcPersonAttributeDaoImpl impl = 
           new JdbcPersonAttributeDaoImpl(testDataSource, queryAttrList,
               "SELECT name, email, shirt_color FROM user_table WHERE netid = ?");

       impl.setDefaultAttributeName(queryAttr);
       
       Map columnsToAttributes = new HashMap();
       columnsToAttributes.put("name", "firstName");
       columnsToAttributes.put("shirt_color", "dressShirtColor");
       impl.setColumnsToAttributes(columnsToAttributes);

       Map attribs = impl.getUserAttributes("susan");
       assertNull(attribs.get("dressShirtColor"));
       assertEquals("Susan", attribs.get("firstName"));
   }
   
   
    /**
     * Test case for a query that needs multiple attributes to complete and
     * more attributes than are needed to complete are passed to it.
     */
    public void testMultiAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "shirtColor";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);

        JdbcPersonAttributeDaoImpl impl = 
            new JdbcPersonAttributeDaoImpl(testDataSource, queryAttrList,
                "SELECT name, email FROM user_table WHERE netid = ? AND shirt_color = ?");

        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("name", "firstName");

        Set emailAttributeNames = new HashSet();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setColumnsToAttributes(columnsToAttributes);

        Map queryMap = new HashMap();
        queryMap.put(queryAttr1, "awp9");
        queryMap.put(queryAttr2, "blue");
        queryMap.put("Name", "John");

        Map attribs = impl.getUserAttributes(queryMap);
        assertEquals("andrew.petro@yale.edu", attribs.get("email"));
        assertEquals("andrew.petro@yale.edu", attribs.get("emailAddress"));
        assertEquals("Andrew", attribs.get("firstName"));
    }

    
    /**
     * A query that needs mulitple attributes to complete but the needed
     * attributes aren't passed to it.
     */
    public void testInsufficientAttrQuery() {
        final String queryAttr1 = "uid";
        final String queryAttr2 = "shirtColor";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr1);
        queryAttrList.add(queryAttr2);

        JdbcPersonAttributeDaoImpl impl = 
            new JdbcPersonAttributeDaoImpl(testDataSource, queryAttrList,
                "SELECT name, email FROM user_table WHERE netid = ? AND shirt_color = ?");

        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("name", "firstName");

        Set emailAttributeNames = new HashSet();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setColumnsToAttributes(columnsToAttributes);

        Map queryMap = new HashMap();
        queryMap.put(queryAttr1, "awp9");
        queryMap.put("Name", "John");

        Map attribs = impl.getUserAttributes(queryMap);
        assertNull(attribs);
    }
    
    /**
     * Test for a query with a single attribute
     */
    public void testMultiPersonQuery() {
        final String queryAttr = "shirt";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);

        JdbcPersonAttributeDaoImpl impl = 
            new JdbcPersonAttributeDaoImpl(testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");

        Map columnsToAttributes = new HashMap();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        Set emailAttributeNames = new HashSet();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        
        impl.setColumnsToAttributes(columnsToAttributes);

        Map queryMap = new HashMap();
        queryMap.put(queryAttr, "blue");
        
        try {
            impl.getUserAttributes(queryMap);
        } 
        catch (IncorrectResultSizeDataAccessException irsdae) {
            // good, exception thrown for multiple results
            return;
        }
        
        fail("JdbcPersonAttributeDao should have thrown IncorrectResultSizeDataAccessException for multiple results");
    }

    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        final String queryAttr = "shirt";
        final List queryAttrList = new LinkedList();
        queryAttrList.add(queryAttr);
        JdbcPersonAttributeDaoImpl impl = 
            new JdbcPersonAttributeDaoImpl(this.testDataSource, queryAttrList,
                "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");

        return impl;
    }

}
