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

package  org.jasig.portal.channels.groupsmanager;

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  java.util.*;
import  org.jasig.portal.*;
import  org.jasig.portal.services.*;
import  java.sql.*;
import  org.jasig.portal.groups.*;

/**
 * An RDBM implementation of IInitialGroupContextStore.
 */
public class RDBMInitialGroupContextStore
      implements IInitialGroupContextStore, GroupsManagerConstants {
   private static RDBMInitialGroupContextStore singleton;
   // SQL strings for Initial Group Context crud:
   // first igc's for the individual
   private static String FIND_INITIAL_CONTEXTS_FOR_OWNER_SQL = "SELECT OWNER_ID, OWNER_TYPE, GROUP_ID, ORDINAL, EXPANDED, DATE_CREATED FROM UPC_GROUP_MGR WHERE OWNER_ID = ? and OWNER_TYPE = 'p' ORDER BY ORDINAL";
   // next igc's for the for the groups that the individual belongs to
   private static String FIND_INITIAL_CONTEXTS_FOR_GROUPS_SQL = "SELECT OWNER_ID, OWNER_TYPE, GROUP_ID, ORDINAL, EXPANDED, DATE_CREATED FROM UPC_GROUP_MGR WHERE OWNER_ID = ? and OWNER_TYPE = 'g' ORDER BY ORDINAL";
   private static String DELETE_INITIAL_CONTEXT_SQL = "DELETE FROM UPC_GROUP_MGR WHERE OWNER_ID = ? and GROUP_ID = ?";
   private static String FIND_INITIAL_CONTEXT_SQL = "SELECT OWNER_ID, OWNER_TYPE, GROUP_ID, ORDINAL, EXPANDED, DATE_CREATED FROM UPC_GROUP_MGR WHERE OWNER_ID = ? and GROUP_ID = ?";
   private static String UPDATE_INITIAL_CONTEXT_SQL = "UPDATE UPC_GROUP_MGR SET ORDINAL = ?, SET EXPANDED = ? WHERE OWNER_ID = ? and GROUP_ID = ?";
   private static String INSERT_INITIAL_CONTEXT_SQL = "INSERT INTO UPC_GROUP_MGR (OWNER_ID, OWNER_TYPE, GROUP_ID, ORDINAL, EXPANDED, DATE_CREATED) VALUES(?, ?, ?, ?, ?, ?)";

   /**
    * UserGroupHomeImpl constructor comment.
    * @exception ChainedException
    */
   public RDBMInitialGroupContextStore () throws ChainedException
   {
      super();
      initialize();
   }

   /**
    * Persistently deletes an Initial Group Context
    * @param igc
    * @exception ChainedException
    */
   public void delete (IInitialGroupContext igc) throws ChainedException {
      java.sql.Connection conn = null;
      String errString;
      if (existsInDatabase(igc)) {
         try {
            conn = RDBMServices.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(DELETE_INITIAL_CONTEXT_SQL);
            ps.setString(1, igc.getOwnerID());
            ps.setString(2, igc.getGroupID());
            try {
               int rc = ps.executeUpdate();
               if (rc != 1) {
                  errString = "InitialGroupContextStoreRDBM::delete(): Problem deleting "
                        + igc;
                  Utility.logMessage("ERROR", errString);
                  throw  new ChainedException(errString);
               }
            } finally {
               ps.close();
            }
         } catch (SQLException sqle) {
            throw  new ChainedException("Problem deleting " + igc + ": " + sqle.getMessage());
         } catch (Exception e) {
            errString = "InitialGroupContextStoreRDBM::delete(): " + e.toString();
            Utility.logMessage("ERROR", errString);
            throw  new ChainedException(errString + e);
         } finally {
            RDBMServices.releaseConnection(conn);
         }
         return;
      }
   }

   /**
    * Find the inital contexts  with this ownerID.
    * @param owner
    * @return java.util.Iterator
    * @exception ChainedException
    */
   public java.util.Iterator findInitialGroupContextsForOwner (IGroupMember owner) throws ChainedException {
      java.sql.Connection conn = null;
      Collection ctxs = new ArrayList();
      IInitialGroupContext igc = null;
      IGroupMember gm;
      IEntityGroup eg;
      try {
         gm = owner;
         Iterator itrGrps = gm.getAllContainingGroups();
         conn = RDBMServices.getConnection();
         java.sql.PreparedStatement ps = conn.prepareStatement(FIND_INITIAL_CONTEXTS_FOR_OWNER_SQL);
         ps.setString(1, owner.getKey());
         java.sql.ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            igc = instanceFromResultSet(rs);
            ctxs.add(igc);
         }
         ps = conn.prepareStatement(FIND_INITIAL_CONTEXTS_FOR_GROUPS_SQL);
         while (itrGrps.hasNext()) {
            eg = (IEntityGroup)itrGrps.next();
            ps.setString(1, eg.getKey());
            rs = ps.executeQuery();
            while (rs.next()) {
               igc = instanceFromResultSet(rs);
               ctxs.add(igc);
            }
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "InitialGroupContextStoreRDBM::findInitialGroupContextsForOwner(); "
               + e.toString());
         throw  new ChainedException("Problem retrieving initial group contexts: " +
               e);
      } finally {
         RDBMServices.releaseConnection(conn);
      }
      return  ctxs.iterator();
   }

   /**
    * Answer if the IInitialGroupContext entity exists in the database.
    * @param igc IInitialGroupContext
    * @return boolean
    * @exception ChainedException
    */
   private boolean existsInDatabase (IInitialGroupContext igc) throws ChainedException {
      IInitialGroupContext igcRet = this.find(igc.getOwnerID(), igc.getGroupID());
      return  igcRet != null;
   }

   /**
    * Find and return an instance of the inital group context.
    * @param ownerID
    * @param groupID
    * @return org.jasig.portal.channels.groupsmanager.IInitialGroupContext
    * @exception ChainedException
    */
   public IInitialGroupContext find (String ownerID, String groupID) throws ChainedException {
      IInitialGroupContext igc = null;
      java.sql.Connection conn = null;
      try {
         conn = RDBMServices.getConnection();
         //RDBMServices.PreparedStatement ps = new RDBMServices.PreparedStatement(conn, sql);
         java.sql.PreparedStatement ps = conn.prepareStatement(FIND_INITIAL_CONTEXT_SQL);
         ps.setString(1, ownerID);
         ps.setString(2, groupID);
         try {
            java.sql.ResultSet rs = ps.executeQuery();
            try {
               while (rs.next()) {
                  igc = instanceFromResultSet(rs);
               }
            } finally {
               rs.close();
            }
         } finally {
            ps.close();
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "EntityGroupStoreRDBM.find(): " + e);
         throw  new ChainedException("Error retrieving " + groupID + ": " + e);
      } finally {
         RDBMServices.releaseConnection(conn);
      }
      return  igc;
   }

   /**
    * Cache entityTypes.
    * @exception ChainedException
    */
   private void initialize () throws ChainedException {}

   /**
    * Find and return an instance of the group.
    * @param rs
    * @return org.jasig.portal.groups.IEntityGroup
    * @exception ChainedException
    * @exception SQLException
    */
   private IInitialGroupContext instanceFromResultSet (java.sql.ResultSet rs) throws SQLException,
         ChainedException {
      IInitialGroupContext igc = null;
      // Boolean expanded = new Boolean(rs.getBoolean(5));
      // new Integer(rs.getInt(4))
      String ownerID = rs.getString(1);
      String ownerType = rs.getString(2);
      String groupID = rs.getString(3);
      Integer ordinal = new Integer(rs.getInt(4));
      Boolean expanded = new Boolean(rs.getString(5));
      Timestamp dateCreated = rs.getTimestamp(6);
      if (ownerID != null) {
         try {
            igc = newInstance(ownerID, ownerType, groupID, ordinal.intValue(), expanded.booleanValue(),
                  dateCreated);
            Utility.logMessage("DEBUG", "InitialGroupContextStoreRDBM.instanceFromResultSet(): added group "
                  + groupID);
         } catch (Exception ex) {
            Utility.logMessage("ERROR", "InitialGroupContextStoreRDBM.instanceFromResultSet(): Unable to create initial group context! "
                  + ex.getMessage());
         }
      }
      return  igc;
   }

   /**
    * Returns a new instance of IInitialGroupsContext
    *
    * @param ownerID
    * @param ownerType g=group, p=person
    * @param groupID The id of the associated group.
    * @param ordinal Used to display the initial group contexts in a specified order.
    * @param expanded Indicates whether or not the inital group context will be expanded when the gui is first displayed.
    * @param dateCreated
    * @return org.jasig.portal.groups.IEntityGroup
    * @exception ChainedException
    */
   public IInitialGroupContext newInstance (String ownerID, String ownerType, String groupID,
         int ordinal, boolean expanded, Timestamp dateCreated) throws ChainedException {
      InitialGroupContextImpl igc = new InitialGroupContextImpl(ownerID, ownerType, groupID,
            ordinal, expanded, dateCreated);
      return  igc;
   }

   /**
    * Writes a new Initial Group Context
    * @param igc
    * @exception ChainedException
    */
   public void primAdd (IInitialGroupContext igc) throws ChainedException {
      java.sql.Connection conn = null;
      String errString;
      if (!existsInDatabase(igc)) {
         try {
            Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
            conn = RDBMServices.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(INSERT_INITIAL_CONTEXT_SQL);
            ps.setString(1, igc.getOwnerID());
            ps.setString(2, igc.getOwnerType());
            ps.setString(3, igc.getGroupID());
            ps.setInt(4, igc.getOrdinal());
            //ps.setBoolean(5, igc.isExpanded());
            ps.setString(5, (igc.isExpanded() ? "y" : "n"));
            ps.setTimestamp(6, timeStamp);
            try {
               int rc = ps.executeUpdate();
               if (rc != 1) {
                  errString = "InitialGroupContextStoreRDBM::primAdd(): Problem adding "
                        + igc;
                  Utility.logMessage("ERROR", errString);
                  throw  new ChainedException(errString);
               }
            } finally {
               ps.close();
            }
         } catch (SQLException sqle) {
            throw  new ChainedException("Problem inserting " + igc + ": " + sqle.getMessage(),
                  sqle);
         } catch (Exception e) {
            errString = "InitialGroupContextStoreRDBM::primAdd(): " + e.toString();
            Utility.logMessage("ERROR", errString);
            throw  new ChainedException(errString, e);
         } finally {
            RDBMServices.releaseConnection(conn);
         }
         return;
      }
      else {
         errString = "InitialGroupContextStoreRDBM::primAdd(): group is already an initial group context";
         Utility.logMessage("ERROR", errString);
         throw  new ChainedException(errString);
      }
   }

   /**
    * Updates a new Initial Group Context
    * @param igc
    * @exception ChainedException
    */
   public void primUpdate (IInitialGroupContext igc) throws ChainedException {
      java.sql.Connection conn = null;
      String errString;
      if (existsInDatabase(igc)) {
         try {
            conn = RDBMServices.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(UPDATE_INITIAL_CONTEXT_SQL);
            ps.setInt(1, igc.getOrdinal());
            ps.setBoolean(2, igc.isExpanded());
            ps.setString(3, igc.getOwnerID());
            ps.setString(4, igc.getGroupID());
            try {
               int rc = ps.executeUpdate();
               if (rc != 1) {
                  errString = "InitialGroupContextStoreRDBM::primUpdate(): Problem updating "
                        + igc;
                  Utility.logMessage("ERROR", errString);
                  throw  new ChainedException(errString);
               }
            } finally {
               ps.close();
            }
         } catch (SQLException sqle) {
            throw  new ChainedException("Problem updating " + igc + ": " + sqle.getMessage(),
                  sqle);
         } catch (Exception e) {
            errString = "InitialGroupContextStoreRDBM::primUpdate(): " + e.toString();
            Utility.logMessage("ERROR", errString);
            throw  new ChainedException(errString, e);
         } finally {
            RDBMServices.releaseConnection(conn);
         }
         return;
      }
   }

   /**
    * Instantiates the singleton if not already instantiated and returns it.
    * @exception ChainedException
    * @return org.jasig.portal.groups.EntityGroupStoreRDBM
    */
   public static synchronized RDBMInitialGroupContextStore singleton () throws ChainedException {
      if (singleton == null) {
         singleton = new RDBMInitialGroupContextStore();
      }
      return  singleton;
   }

   /**
    * Commits changes made to an Initial Group Context to the database.
    * @param igc The Initial Group Context to be committed.
    * @exception ChainedException
    */
   public void update (IInitialGroupContext igc) throws ChainedException {
      try {
         if (existsInDatabase(igc)) {
            primUpdate(igc);
         }
         else {
            primAdd(igc);
         }
      } catch (Exception ex) {
         throw  new ChainedException("Problem updating " + this + ex);
      }
   }
}

