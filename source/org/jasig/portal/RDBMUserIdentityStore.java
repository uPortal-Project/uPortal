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
 *
 */


package  org.jasig.portal;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.RDBMUserLayoutStore;
import org.jasig.portal.services.LogService;
import  java.sql.*;

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

  protected RdbmServices rdbmService = null;

  /**
   * constructor gets an rdbm service
   */
  public void RDBMUserIdentityStore () {
    rdbmService = new RdbmServices();
  }

 /**
   * getuPortalUID -  return a unique uPortal key for a user.
   *    calls alternate signature with createPortalData set to false.
   * @param   IPerson object
   * @return  uPortalUID number
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
     Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      if (con.getMetaData().supportsTransactions())  con.setAutoCommit(false);

      try {
        String SQLDelete = "DELETE FROM UP_USER WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_LAYOUT  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_PARAM WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_PROFILE  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_SS_USER_PARM  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_LAYOUT_PARAM WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_LAYOUT_STRUCT  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        if (con.getMetaData().supportsTransactions())  con.commit();

      } finally {
        stmt.close();
      }
    }
    catch (SQLException se) {
      try {
        if (con.getMetaData().supportsTransactions())  con.rollback();
        }
        catch (SQLException e) {}
        if (DEBUG>0){
        System.err.println("SQLException: " + se.getMessage());
        System.err.println("SQLState:  " + se.getSQLState());
        System.err.println("Message:  " + se.getMessage());
        System.err.println("Vendor:  " + se.getErrorCode());}

        AuthorizationException ae = new AuthorizationException("SQL Database Error");
        LogService.log(LogService.ERROR, ae);
        throw  (ae);
      }
    finally {
      rdbmService.releaseConnection(con);
    }
    }

    /**
   *
   * getuPortalUID
   * @param   IPerson object, boolean createPortalData indicating whether to  try to create
   *  all uPortal data for this user from temp[late prototype
   * @return  uPortalUID number or -1 if unable to create user.
   * @throws Authorization exception if createPortalData is false and no user is found
   *  or if a sql error is encountered
   */
   public int getPortalUID (IPerson person, boolean createPortalData) throws AuthorizationException {
    int uPortalUID=-1;
    // Get a connection to the database
    Connection con = rdbmService.getConnection();
    Statement stmt = null;

    try
    {
      // Create the JDBC statement
      stmt = con.createStatement();
    }
    catch(SQLException se)
    {
      try
      {
        // Release the connection
        rdbmService.releaseConnection(con);
      }
      catch(Exception e) {}

      // Log the exception
      LogService.instance().log(LogService.ERROR, "RDBMUserIdentityStore: Could not create database statement", se);
      throw new AuthorizationException("RDBMUserIdentityStore: Could not create database statement");
    }

    try
    {
      // Retrieve the USER_ID that is mapped to their portal UID
      String query = "SELECT USER_ID, USER_NAME FROM UP_USER WHERE USER_NAME = '" + person.getAttribute("username") + "'";

      // DEBUG
      LogService.log(LogService.DEBUG, query);
      // Execute the query
      ResultSet rset = stmt.executeQuery(query);

      // Check to see if we've got a result
      if (rset.next())
      {
        uPortalUID = rset.getInt("USER_ID");
      }
      // If no result and we're not creating new users then fail
      else if(!createPortalData)
      {
        throw new AuthorizationException("No portal information exists for user " + person.getAttribute("username"));
      }
      else
      {
        /* attempt to create portal data for a new user */
        int newUID;
        int templateUID;
        int templateUSER_DFLT_USR_ID ;
        int templateUSER_DFLT_LAY_ID;
        int templateCURR_LAY_ID ;
        int templateNEXT_STRUCT_ID;
        java.sql.Date templateLST_CHAN_UPDT_DT = new java.sql.Date(System.currentTimeMillis());

        // Retrieve the username of the user to use as a template for this new user
        String templateName=(String) person.getAttribute(templateAttrName);
        if (DEBUG>0) System.err.println("template name is "+templateName);

        // Just use the guest account if the template could not be found
        if (templateName == null || templateName=="")
        {
          templateUID = guestUID;
        }

        // Retrieve the information for the template user
        query = "SELECT USER_ID, USER_DFLT_USR_ID, USER_DFLT_LAY_ID,CURR_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT FROM UP_USER WHERE USER_NAME = '"+templateName+"'";
        // DEBUG
        if (DEBUG>0) System.err.println(query);
        LogService.log(LogService.DEBUG, query);
        // Execute the query
        rset = stmt.executeQuery(query);
        // Check to see if the template user exists
        if (rset.next())
        {
          templateUID = rset.getInt("USER_ID");
          templateUSER_DFLT_USR_ID = rset.getInt("USER_DFLT_USR_ID");
          templateUSER_DFLT_LAY_ID = rset.getInt("USER_DFLT_LAY_ID");
          templateCURR_LAY_ID = rset.getInt("CURR_LAY_ID");
          templateNEXT_STRUCT_ID = rset.getInt("NEXT_STRUCT_ID");
        }
        else
        {
          throw new AuthorizationException("No information found for template user = " + templateName + ". Cannot create new account for " + person.getAttribute("username"));
        }

        /* get a new uid for the person */
        try
        {
          newUID = UserLayoutStoreFactory.getUserLayoutStoreImpl().getIncrementIntegerId("UP_USER");
        }
        catch (Exception e)
        {
          LogService.log(LogService.ERROR, "RDBMUserIdentityStore error getting next sequence: ", e);
          throw new AuthorizationException("RDBMUserIdentityStore error, see error log.");
        }

        try
        {
          // Turn off autocommit if the database supports it
          if (con.getMetaData().supportsTransactions())
          {
            con.setAutoCommit(false);
          }
        }
        catch(SQLException se)
        {
          // Log the exception
          LogService.instance().log(LogService.WARN, "RDBMUserIdentityStore: Could not turn off autocommit", se);
        }

        String Insert = new String();
        /* insert new user record in UP_USER */
        Insert = "INSERT INTO UP_USER "+
          "(USER_ID, USER_NAME, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, CURR_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT) "+
          " VALUES ("+
            newUID+", '"+
            person.getAttribute("username")+ "',"+
            templateUSER_DFLT_USR_ID+", "+
            templateUSER_DFLT_LAY_ID+", "+
            templateCURR_LAY_ID+", "+
            templateNEXT_STRUCT_ID+", "+
            "null)";
            //"'"+templateLST_CHAN_UPDT_DT+"')";
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_user_layout */
        Insert = "INSERT INTO UP_USER_LAYOUT (USER_ID, LAYOUT_ID, LAYOUT_TITLE, INIT_STRUCT_ID ) "+
          " SELECT "+newUID+", LAYOUT_ID, LAYOUT_TITLE, INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_user_param */
        Insert = "INSERT INTO UP_USER_PARAM (USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE ) "+
          " SELECT "+newUID+", USER_PARAM_NAME, USER_PARAM_VALUE FROM UP_USER_PARAM WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_user_profile */
        Insert = "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID ) "+
          " SELECT "+newUID+", PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID "+
          "FROM UP_USER_PROFILE WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_user_role */
        Insert = "INSERT INTO UP_USER_ROLE (USER_ID, ROLE_ID, PRIM_ROLE_IND) "+
          " SELECT "+newUID+", ROLE_ID, PRIM_ROLE_IND FROM UP_USER_ROLE WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_ss_user_atts */
        Insert = "INSERT INTO UP_SS_USER_ATTS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL) "+
          " SELECT "+newUID+", PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL "+
          " FROM UP_SS_USER_ATTS WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_ss_user_parm */
        Insert = "INSERT INTO UP_SS_USER_PARM (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL) "+
          " SELECT "+newUID+", PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL "+
          " FROM UP_SS_USER_PARM WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_layout_param */
        Insert = "INSERT INTO UP_LAYOUT_PARAM (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) "+
          " SELECT "+newUID+", LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL "+
          " FROM UP_LAYOUT_PARAM WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_user_ua_map */
        Insert = "INSERT INTO UP_USER_UA_MAP (USER_ID, USER_AGENT, PROFILE_ID) "+
          " SELECT "+newUID+", USER_AGENT, PROFILE_ID"+
          " FROM UP_USER_UA_MAP WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, Insert);
        stmt.executeUpdate(Insert);

        /* insert row into up_layout_struct  */
        Insert = "INSERT INTO UP_LAYOUT_STRUCT (USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID, EXTERNAL_ID, CHAN_ID, NAME, TYPE, HIDDEN, IMMUTABLE, UNREMOVABLE) "+
          " SELECT "+newUID+", LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID, EXTERNAL_ID, CHAN_ID, NAME, TYPE, HIDDEN, IMMUTABLE, UNREMOVABLE"+
          " FROM UP_LAYOUT_STRUCT WHERE USER_ID="+templateUID;
        LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
        stmt.executeUpdate(Insert);

        // Check to see if the database supports transactions
        boolean supportsTransactions = false;
        try
        {
           supportsTransactions = con.getMetaData().supportsTransactions();
        }
        catch(Exception e)  {}

        if(supportsTransactions)
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
      LogService.log(LogService.ERROR, se);
      // Rollback the transaction
      try
      {
        if (con.getMetaData().supportsTransactions())
        {
          con.rollback();
        }
      }
      catch (SQLException e)
      {
        LogService.instance().log(LogService.WARN, "RDBMUserIdentityStore.getPortalUID(): Unable to rollback transaction", se);
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
      rdbmService.releaseConnection(con);
    }
    // Return the user's ID
    return(uPortalUID);
  }

  /**
   * put your documentation comment here
   * @param connection
   */
  static final protected void commit (Connection connection) {
    try {
      if (connection.getMetaData().supportsTransactions())
        connection.commit();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param connection
   */
  static final protected void rollback (Connection connection) {
    try {
      if (connection.getMetaData().supportsTransactions())
        connection.rollback();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
  }


}



