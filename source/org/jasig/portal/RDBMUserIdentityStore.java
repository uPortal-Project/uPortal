/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CounterStoreFactory;
/**
 * SQL implementation for managing creation and removal of User Portal Data
 * @author Susan Bramhall, Yale University
 */
public class RDBMUserIdentityStore  implements IUserIdentityStore {

  //*********************************************************************
  // Constants
    private static final String templateAttrName = "uPortalTemplateUserName";
    private static final int guestUID = 1;
    static int DEBUG = 0;

  protected RDBMServices rdbmService = null;

  /**
   * constructor gets an rdbm service
   */
  public RDBMUserIdentityStore () {
    rdbmService = new RDBMServices();
  }

 /**
   * getuPortalUID -  return a unique uPortal key for a user.
   *    calls alternate signature with createPortalData set to false.
   * @param person the person object
   * @return uPortalUID number
   * @throws Authorization exception if no user is found.
   */
  public int getPortalUID (IPerson person) throws AuthorizationException {
    int uPortalUID=-1;
    uPortalUID=this.getPortalUID(person, false);
    return uPortalUID;
    }

  /**
   *
   * removeuPortalUID
   * @param   uPortalUID integer key to uPortal data for a user
   * @throws Authorization exception if a sql error is encountered
   */
  public void removePortalUID(int uPortalUID) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      if (RDBMServices.supportsTransactions)
        con.setAutoCommit(false);

      try {
        String SQLDelete = "DELETE FROM UP_USER WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_LAYOUT  WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_PARAM WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_PROFILE  WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_SS_USER_PARM  WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_LAYOUT_PARAM WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_LAYOUT_STRUCT  WHERE USER_ID = " + uPortalUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        if (RDBMServices.supportsTransactions)
          con.commit();

      } finally {
        stmt.close();
      }
    }
    catch (SQLException se) {
      try {
        if (RDBMServices.supportsTransactions)
          con.rollback();
      }
      catch (SQLException e) {
      }
        if (DEBUG>0){
        System.err.println("SQLException: " + se.getMessage());
        System.err.println("SQLState:  " + se.getSQLState());
        System.err.println("Message:  " + se.getMessage());
        System.err.println("Vendor:  " + se.getErrorCode());}

        AuthorizationException ae = new AuthorizationException("SQL Database Error");
        LogService.log(LogService.ERROR, "RDBMUserIdentityStore::removePortalUID(): " + ae);
        throw  (ae);
      }
    finally {
      RDBMServices.releaseConnection(con);
    }
    }

   /**
    * Get the portal user ID for this person object.
    * @param person 
    * @param createPortalData indicating whether to try to create all uPortal data for this user from template prototype
    * @return uPortalUID number or -1 if unable to create user.
    * @throws Authorization exception if createPortalData is false and no user is found
    *  or if a sql error is encountered
    */
   public synchronized int getPortalUID (IPerson person, boolean createPortalData) throws AuthorizationException {
    int uPortalUID=-1;
    // Get a connection to the database
    Connection con = RDBMServices.getConnection();
    Statement stmt = null;
    Statement insertStmt = null;
    ResultSet rset = null;

    try
    {
      // Create the JDBC statement
      stmt = con.createStatement();
      // Create a separate statement for inserts so it doesn't
      // interfere with ResultSets
      insertStmt = con.createStatement();
    }
    catch(SQLException se)
    {
      try
      {
        // Release the connection
        RDBMServices.releaseConnection(con);
      }
      catch(Exception e) {}

      // Log the exception
      LogService.log(LogService.ERROR, "RDBMUserIdentityStore::getPortalUID(): Could not create database statement", se);
      throw new AuthorizationException("RDBMUserIdentityStore: Could not create database statement");
    }

    try
    {
      // Retrieve the USER_ID that is mapped to their portal UID
      String query = "SELECT USER_ID, USER_NAME FROM UP_USER WHERE USER_NAME = '" + person.getAttribute(IPerson.USERNAME) + "'";

      // DEBUG
      LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
      // Execute the query
      rset = stmt.executeQuery(query);

      // Check to see if we've got a result
      if (rset.next())
      {
        uPortalUID = rset.getInt("USER_ID");
      }
      // If no result and we're not creating new users then fail
      else if(!createPortalData)
      {
        throw new AuthorizationException("No portal information exists for user " + person.getAttribute(IPerson.USERNAME));
      }
      else
      {
        /* attempt to create portal data for a new user */
        int newUID;
        int templateUID;
        int templateUSER_DFLT_USR_ID ;
        int templateUSER_DFLT_LAY_ID;
        java.sql.Date templateLST_CHAN_UPDT_DT = new java.sql.Date(System.currentTimeMillis());
        String defaultTemplateUserName = PropertiesManager.getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName");

        // Retrieve the username of the user to use as a template for this new user
        String templateName=(String) person.getAttribute(templateAttrName);
        if (DEBUG>0) System.err.println("Attempting to autocreate user from template "+templateName);
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + "template name is " + templateName);

        // Just use the default template if requested template not populated
        if (templateName == null || templateName.equals(""))
        {
          templateName=defaultTemplateUserName;
        }

        // Retrieve the information for the template user
        query = "SELECT USER_ID, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT FROM UP_USER WHERE USER_NAME = '"+templateName+"'";
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
        // Execute the query
        rset.close();
        rset = stmt.executeQuery(query);
        // Check to see if the template user exists
        if (rset.next())
        {
          templateUID = rset.getInt("USER_ID");
          templateUSER_DFLT_USR_ID = templateUID;
          templateUSER_DFLT_LAY_ID = rset.getInt("USER_DFLT_LAY_ID");
        }
        // if no results on default template throw error
        // otherwise try the default
        else {
          if (templateName.equals(defaultTemplateUserName))
            throw new AuthorizationException("No information found for template user = " + templateName
              + ". Cannot create new account for " + person.getAttribute(IPerson.USERNAME));
          else {
          templateName=defaultTemplateUserName;
          query = "SELECT USER_ID, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT FROM UP_USER WHERE USER_NAME = '"+
            templateName+"'";
          LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
          // Execute the query
          rset.close();
          rset = stmt.executeQuery(query);
          // Check to see if the template user exists
          if (rset.next())   {
            templateUID = rset.getInt("USER_ID");
            templateUSER_DFLT_USR_ID = templateUID;
            templateUSER_DFLT_LAY_ID = rset.getInt("USER_DFLT_LAY_ID");
            }
          else throw new AuthorizationException("No information found for template user = " + templateName
              + ". Cannot create new account for " + person.getAttribute(IPerson.USERNAME));
          }
        }

        /* get a new uid for the person */
        try
        {
          newUID = CounterStoreFactory.getCounterStoreImpl().getIncrementIntegerId("UP_USER");
        }
        catch (Exception e)
        {
          LogService.log(LogService.ERROR, "RDBMUserIdentityStore::getPortalUID(): error getting next sequence: ", e);
          throw new AuthorizationException("RDBMUserIdentityStore error, see error log.");
        }

        /* Put the new user in the template user's groups */
        try{
          IGroupMember me = GroupService.getGroupMember(person.getEntityIdentifier());
          IGroupMember template = GroupService.getEntity(templateName, Class.forName("org.jasig.portal.security.IPerson"));
          java.util.Iterator templateGroups =  template.getContainingGroups();
          while (templateGroups.hasNext())
          {
            IEntityGroup eg = (IEntityGroup) templateGroups.next();
            if (eg.isEditable()) {
              eg.addMember(me);
              eg.updateMembers();
            }

          }      // end while()
        }        // end try
        catch (Exception e) {
          LogService.log(LogService.ERROR, "RDBMUserIdentityStore::getPortalUID(): error adding new user to groups: ", e);
        }

        try
        {
          // Turn off autocommit if the database supports it
          if (RDBMServices.supportsTransactions)
          {
            con.setAutoCommit(false);
          }
        }
        catch(SQLException se)
        {
          // Log the exception
          LogService.log(LogService.WARN, "RDBMUserIdentityStore: Could not turn off autocommit", se);
        }

        String Insert = new String();
        /* insert new user record in UP_USER */
        Insert = "INSERT INTO UP_USER "+
          "(USER_ID, USER_NAME, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT) "+
          " VALUES ("+
            newUID+", '"+
            person.getAttribute(IPerson.USERNAME)+ "',"+
            templateUSER_DFLT_USR_ID+", "+
            templateUSER_DFLT_LAY_ID+", "+
            "null, "+
            "null)";
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + Insert);
        stmt.executeUpdate(Insert);

        // replaced INSERT INTO SELECT statements with queries followed
        // by INSERTS because MySQL does not support this using the same
        // table.
        // Courtesy of John Fereira <jaf30@cornell.edu>

        /* insert row into up_user_layout */
        query =  "SELECT USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
        if (DEBUG>0) System.err.println(query);
        rset.close();
        rset = stmt.executeQuery(query);
        while (rset.next()) {
           Insert = "INSERT INTO UP_USER_LAYOUT (USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID) "+
           "VALUES("+
           newUID+","+
           rset.getInt("LAYOUT_ID")+","+
           "'"+rset.getString("LAYOUT_TITLE")+"',"+
           "NULL)";
           LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + Insert);
           if (DEBUG>0) System.err.println(Insert);
           insertStmt.executeUpdate(Insert);
        }

        /* insert row into up_user_param */
        query = "SELECT USER_ID,USER_PARAM_NAME,USER_PARAM_VALUE FROM UP_USER_PARAM WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
        if (DEBUG>0) System.err.println(query);
        rset.close();
        rset = stmt.executeQuery(query);
        while (rset.next()) {
           Insert = "INSERT INTO UP_USER_PARAM (USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE ) "+
           "VALUES("+
           newUID+","+
           ",'"+rset.getString("USER_PARAM_NAME")+"',"+
           ",'"+rset.getString("USER_PARAM_VALUE")+"')";

           LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + Insert);
           if (DEBUG>0) System.err.println(Insert);
           stmt.executeUpdate(Insert);
        }

        /* insert row into up_user_profile */

        query = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION "+
                "FROM UP_USER_PROFILE WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
        if (DEBUG>0) System.err.println(query);
        rset.close();
        rset = stmt.executeQuery(query);
        while (rset.next()) {

           Insert = "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID ) "+
           "VALUES("+
           newUID+","+
           rset.getInt("PROFILE_ID")+","+
           "'"+rset.getString("PROFILE_NAME")+"',"+
           "'"+rset.getString("DESCRIPTION")+"',"+
           "NULL,"+
           "NULL,"+
           "NULL)";

           LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + Insert);
           if (DEBUG>0) System.err.println(Insert);
           insertStmt.executeUpdate(Insert);
        }

        /* insert row into up_user_ua_map */
        query = " SELECT USER_ID, USER_AGENT, PROFILE_ID"+
                " FROM UP_USER_UA_MAP WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
        if (DEBUG>0) System.err.println(query);
        rset.close();
        rset = stmt.executeQuery(query);
        while (rset.next()) {
           Insert = "INSERT INTO UP_USER_UA_MAP (USER_ID, USER_AGENT, PROFILE_ID) "+
           "VALUES("+
           newUID+","+
           "'"+rset.getString("USER_AGENT")+"',"+
           rset.getInt("PROFILE_ID")+")";

           LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + Insert);
           if (DEBUG>0) System.err.println(Insert);
           insertStmt.executeUpdate(Insert);
        }

        /* insert row(s) into up_ss_user_parm */
        query = "SELECT USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL "+
          " FROM UP_SS_USER_PARM WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + query);
        if (DEBUG>0) System.err.println(query);
        rset.close();
        rset = stmt.executeQuery(query);
        while (rset.next()) {
           Insert = "INSERT INTO UP_SS_USER_PARM (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL) "+
           "VALUES("+
           newUID+","+
           rset.getInt("PROFILE_ID")+","+
           rset.getInt("SS_ID")+","+
           rset.getInt("SS_TYPE")+","+
           "'"+rset.getString("PARAM_NAME")+"',"+
           "'"+rset.getString("PARAM_VAL")+"')";

           LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + Insert);
           if (DEBUG>0) System.err.println(Insert);
           insertStmt.executeUpdate(Insert);
        }

        // end of changes for MySQL support

        // Check to see if the database supports transactions
        if(RDBMServices.supportsTransactions)
        {
          // Commit the transaction
          con.commit();
          // Use our new ID if we made it through all of the SQL
          uPortalUID = newUID;
        }
        else
        {
          // Use our new ID if we made it through all of the SQL
          uPortalUID = newUID;
        }
      }
    }
    catch (SQLException se)
    {
      // Log the exception
      LogService.log(LogService.ERROR, "RDBMUserIdentityStore::getPortalUID(): " + se);
      // Rollback the transaction
      try
      {
        if (RDBMServices.supportsTransactions)
        {
          con.rollback();
        }
      }
      catch (SQLException e)
      {
        LogService.log(LogService.WARN, "RDBMUserIdentityStore.getPortalUID(): Unable to rollback transaction", se);
      }
      // DEBUG
      if (DEBUG>0)
      {
        System.err.println("SQLException: " + se.getMessage());
        System.err.println("SQLState:  " + se.getSQLState());
        System.err.println("Message:  " + se.getMessage());
        System.err.println("Vendor:  " + se.getErrorCode());
      }
      // Throw an exception
      throw  (new AuthorizationException("SQL database error while retrieving user's portal UID"));
    }
    finally
    {
      try { 
        if (rset != null) rset.close(); 
      } catch (Exception e) { 
        LogService.log(LogService.ERROR, e); 
      } 
    
      try { 
        if (stmt != null) stmt.close(); 
      } catch (Exception e) { 
        LogService.log(LogService.ERROR, e); 
      } 
    
      try { 
        if (insertStmt != null) insertStmt.close(); 
      } catch (Exception e) { 
        LogService.log(LogService.ERROR, e); 
      } 

      RDBMServices.releaseConnection(con);
    }
    // Return the user's ID
    return(uPortalUID);
  }

  static final protected void commit (Connection connection) {
    try {
      if (RDBMServices.supportsTransactions)
        connection.commit();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, "RDBMUserIdentityStore::commit(): " + e);
    }
  }

  static final protected void rollback (Connection connection) {
    try {
      if (RDBMServices.supportsTransactions)
        connection.rollback();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, "RDBMUserIdentityStore::rollback(): " + e);
    }
  }


}



