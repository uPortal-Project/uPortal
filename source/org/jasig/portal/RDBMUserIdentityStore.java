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
  public int getuPortalUID (IPerson person) throws AuthorizationException {
    int uPortalUID=-1;
    uPortalUID=this.getuPortalUID(person, false);
    return uPortalUID;
    }

  /**
   *
   * removeuPortalUID
   * @param   uPortalUID integer key to uPortal data for a user
   * @throws Authorization exception if a sql error is encountered
   */
  public void removeuPortalUID(int uPortalUID) throws Exception {
     Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      if (con.getMetaData().supportsTransactions())  con.setAutoCommit(false);

      try {
        String SQLDelete = "DELETE FROM UP_USER WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_LAYOUT  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_PARAM WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_PROFILES  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_SS_ATTS WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_SS_PARMS  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_STRUCT_PARAM WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
        stmt.executeUpdate(SQLDelete);

        SQLDelete = "DELETE FROM UP_LAYOUT_STRUCT  WHERE USER_ID = '" + uPortalUID + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + SQLDelete);
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
   public int getuPortalUID (IPerson person, boolean createPortalData) throws AuthorizationException {
    int uPortalUID=-1;

    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      if (con.getMetaData().supportsTransactions())  con.setAutoCommit(false);

      try {
        String query = "SELECT UP_USER.USER_ID FROM UP_USER "+
        "WHERE UP_USER.USER_NAME = '" + person.getAttribute("username") + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + query);
        ResultSet rset = stmt.executeQuery(query);
        try {
          if (rset.next()) {
            uPortalUID = rset.getInt("USER_ID");
           }
          else
            if (createPortalData==true)
            {
            /* attempt to create portal data for a new user */
            int newUID;
            int templateUID;
            /* examine person object for role title in affiliation attribute.
            If not present return uPortalUID=-1.  */
            String templateName=(String) person.getAttribute(templateAttrName);
            if (templateName == null || templateName=="")
              templateUID=guestUID;
            else {
            /*If found select template UID from UP_ROLE.ROLE_DFLT_USR_ID
            If no result use guest userid constant.  */
            query = "SELECT USER_ID FROM UP_USER WHERE USER_NAME = '"+templateName+"'";
            if (DEBUG>0) System.err.println(query);
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getuPortalUID(): " + query);
            rset = stmt.executeQuery(query);
            if (rset.next()) templateUID = rset.getInt("USER_ID");
            else templateUID = guestUID;
            }
            /* get a new uid for the person */
            try {
              newUID = GenericPortalBean.getUserLayoutStore().getIncrementIntegerId("UP_USER");
              }
              catch (Exception e) {
              LogService.log(LogService.ERROR, "RDBMUserIdentityStore error getting next sequence: " + e.getMessage());
              throw new AuthorizationException("RDBMUserIdentityStore error, see error log.");
              }
            String Insert;
            /* insert new user record in UP_USER */
            Insert = "INSERT INTO UP_USER "+
              "(USER_ID, USER_NAME, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, CURR_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT) "+
              " SELECT "+newUID+", '"+ person.getAttribute("username")+ "',"+
              "USER_DFLT_USR_ID, USER_DFLT_LAY_ID,CURR_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT "+
              "FROM UP_USER WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_layout */
            Insert = "INSERT INTO UP_USER_LAYOUT (USER_ID, LAYOUT_ID, LAYOUT_TITLE, INIT_STRUCT_ID ) "+
              " SELECT "+newUID+", LAYOUT_ID, LAYOUT_TITLE, INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_param */
            Insert = "INSERT INTO UP_USER_PARAM (USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE ) "+
              " SELECT "+newUID+", USER_PARAM_NAME, USER_PARAM_VALUE FROM UP_USER_PARAM WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_profiles */
            Insert = "INSERT INTO UP_USER_PROFILES (USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID ) "+
              " SELECT "+newUID+", PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID "+
              "FROM UP_USER_PROFILES WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_role */
            Insert = "INSERT INTO UP_USER_ROLE (USER_ID, ROLE_ID, PRIM_ROLE_IND) "+
              " SELECT "+newUID+", ROLE_ID, PRIM_ROLE_IND FROM UP_USER_ROLE WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_ss_atts */
            Insert = "INSERT INTO UP_USER_SS_ATTS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL) "+
              " SELECT "+newUID+", PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL "+
              " FROM UP_USER_SS_ATTS WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_ss_parms */
            Insert = "INSERT INTO UP_USER_SS_PARMS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL) "+
              " SELECT "+newUID+", PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL "+
              " FROM UP_USER_SS_PARMS WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_struct_param */
            Insert = "INSERT INTO UP_STRUCT_PARAM (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) "+
              " SELECT "+newUID+", LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL "+
              " FROM UP_STRUCT_PARAM WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_user_ua_map */
            Insert = "INSERT INTO UP_USER_UA_MAP (USER_ID, USER_AGENT, PROFILE_ID) "+
              " SELECT "+newUID+", USER_AGENT, PROFILE_ID"+
              " FROM UP_USER_UA_MAP WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            /* insert row into up_layout_struct  */
            Insert = "INSERT INTO UP_LAYOUT_STRUCT (USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID, EXTERNAL_ID, CHAN_ID, NAME, TYPE, HIDDEN, IMMUTABLE, UNREMOVABLE) "+
              " SELECT "+newUID+", LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID, EXTERNAL_ID, CHAN_ID, NAME, TYPE, HIDDEN, IMMUTABLE, UNREMOVABLE"+
              " FROM UP_LAYOUT_STRUCT WHERE USER_ID="+templateUID;
            LogService.log(LogService.DEBUG, "RDBMUserIdentityStore " + Insert);
            stmt.executeUpdate(Insert);

            if (con.getMetaData().supportsTransactions())  con.commit();

            uPortalUID=newUID;
           }
        /* if not creating new data and user not found, throw exception */
        else throw new AuthorizationException("User "+person.getAttribute("username")+ "not found");
        } finally {
          rset.close();
        }
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
    return  uPortalUID;
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



