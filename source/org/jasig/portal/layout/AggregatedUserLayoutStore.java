package org.jasig.portal.layout;

import org.jasig.portal.RDBMUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;

import java.util.Hashtable;


/**
 * <p>Title: The AggregatedUserLayoutStore class</p>
 * <p>Description: The Aggregated UserLayoutStore implementation using the relational database with SQL 92 </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic</p>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */

public class AggregatedUserLayoutStore extends RDBMUserLayoutStore implements IAggregatedUserLayoutStore {

  public AggregatedUserLayoutStore() throws Exception {
     super();
  }


    /**
     * Add the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>UserLayoutNode</code> object specifying the node
     * @return a <code>UserLayoutNode</code> object specifying the node with the generated node ID
     * @exception PortalException if an error occurs
     */
    public UserLayoutNode addUserLayoutNode (IPerson Person, UserProfile profile, UserLayoutNode node ) throws PortalException {
      return null;
    }

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>UserLayoutNode</code> object specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean updateUserLayoutNode (IPerson Person, UserProfile profile, UserLayoutNode node ) throws PortalException {
      return false;
    }

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param nodeId a <code>String</code> node ID specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean deleteUserLayoutNode (IPerson Person, UserProfile profile, String nodeId ) throws PortalException {
      return false;
    }

   /**
     * Gets the user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param nodeId a <code>String</code> node ID specifying the node
     * @return a <code>UserLayoutNode</code> object
     * @exception PortalException if an error occurs
     */
    public UserLayoutNode getUserLayoutNode (IPerson person, UserProfile profile, String nodeId ) throws PortalException {
      return null;
    }

    /**
     * Returns the user layout internal representation.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @return a <code>Hashtable</code> object containing the internal representation of the user layout
     * @exception PortalException if an error occurs
     */
 public Hashtable getAggregatedUserLayout (IPerson person, UserProfile profile) throws PortalException {
    /*int userId = person.getID();
    int realUserId = userId;
    ResultSet rs;
    Connection con = RDBMServices.getConnection();

    RDBMServices.setAutoCommit(con, false);          // May speed things up, can't hurt

    try {
      DocumentImpl doc = new DocumentImpl();
      Element root = doc.createElement("layout");
      Statement stmt = con.createStatement();
      try {
        long startTime = System.currentTimeMillis();
        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        rs = stmt.executeQuery(subSelectString);
        try {
            rs.next();
            layoutId = rs.getInt(1);
            if (rs.wasNull()) {
                layoutId = 0;
            }
        } finally {
            rs.close();
        }

       if (layoutId == 0) { // First time, grab the default layout for this user
          String sQuery = "SELECT USER_DFLT_USR_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            userId = rs.getInt(1);
            layoutId = rs.getInt(2);
          } finally {
            rs.close();
          }

          // Make sure the next struct id is set in case the user adds a channel
          sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          int nextStructId;
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            nextStructId = rs.getInt(1);
          } finally {
            rs.close();
          }
          sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + realUserId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          stmt.executeUpdate(sQuery);

          sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID=" + realUserId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          stmt.executeUpdate(sQuery);

          String Insert = "INSERT INTO UP_SS_USER_ATTS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL) "+
            " SELECT "+realUserId+", USUA.PROFILE_ID, USUA.SS_ID, USUA.SS_TYPE, USUA.STRUCT_ID, USUA.PARAM_NAME, USUA.PARAM_TYPE, USUA.PARAM_VAL "+
            " FROM UP_SS_USER_ATTS USUA WHERE USUA.USER_ID="+userId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + Insert);
          stmt.executeUpdate(Insert);

          RDBMServices.commit(con); // Make sure it appears in the store
        }

        int firstStructId = -1;
        String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          rs.next();
          firstStructId = rs.getInt(1);
        } finally {
          rs.close();
        }

        String sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
          "ULS.UNREMOVABLE,ULS.IMMUTABLE";
        if (RDBMServices.supportsOuterJoins) {
          sql += ",USP.STRUCT_PARM_NM,USP.STRUCT_PARM_VAL FROM " + RDBMServices.joinQuery.getQuery("layout");
        } else {
          sql += " FROM UP_LAYOUT_STRUCT ULS WHERE ";
        }
        sql += " ULS.USER_ID=" + userId + " AND ULS.LAYOUT_ID=" + layoutId + " ORDER BY ULS.STRUCT_ID";
        HashMap layoutStructure = new HashMap();
        ArrayList chanIds = new ArrayList();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          StringBuffer structParms = new StringBuffer();
        rs = stmt.executeQuery(sql);
        try {
          int lastStructId = 0;
          LayoutStructure ls = null;
          String sepChar = "";
          if (rs.next()) {
            int structId = rs.getInt(1);
            if (rs.wasNull()) {
              structId = 0;
            }
            readLayout: while (true) {
              if (DEBUG > 1) System.err.println("Found layout structureID " + structId);

              int nextId = rs.getInt(2);
              if (rs.wasNull()) {
                nextId = 0;
              }
              int childId = rs.getInt(3);
              if (rs.wasNull()) {
                childId = 0;
              }
              int chanId = rs.getInt(4);
              if (rs.wasNull()) {
                chanId = 0;
              }
              ls = new LayoutStructure(structId, nextId, childId, chanId, rs.getString(7),rs.getString(8),rs.getString(9));
              layoutStructure.put(new Integer(structId), ls);
              lastStructId = structId;
              if (!ls.isChannel()) {
                ls.addFolderData(rs.getString(5), rs.getString(6));
              } else {
                chanIds.add(new Integer(chanId)); // For later
              }
              if (RDBMServices.supportsOuterJoins) {
                do {
                  String name = rs.getString(10);
                  String value = rs.getString(11); // Oracle JDBC requires us to do this for longs
                  if (name != null) { // may not be there because of the join
                    ls.addParameter(name, value);
                  }
                  if (!rs.next()) {
                    break readLayout;
                  }
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } while (structId == lastStructId);
              } else { // Do second SELECT later on for structure parameters
                if (ls.isChannel()) {
                  structParms.append(sepChar + ls.chanId);
                  sepChar = ",";
                }
                if (rs.next()) {
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } else {
                  break readLayout;
                }
              }
            } // while
          }
        } finally {
          rs.close();
        }

        // We have to retrieve the channel defition after the layout structure
        // since retrieving the channel data from the DB may interfere with the
        // layout structure ResultSet (in other words, Oracle is a pain to program for)
        if (chanIds.size() > 0) {
          RDBMServices.PreparedStatement pstmtChannel = crsdb.getChannelPstmt(con);
          try {
            RDBMServices.PreparedStatement pstmtChannelParm = crsdb.getChannelParmPstmt(con);
            try {
              // Pre-prime the channel pump
              for (int i = 0; i < chanIds.size(); i++) {
                int chanId = ((Integer) chanIds.get(i)).intValue();
                crsdb.getChannel(chanId, true, pstmtChannel, pstmtChannelParm);
                if (DEBUG > 1) {
                  System.err.println("Precached " + chanId);
                }
              }
            } finally {
              if (pstmtChannelParm != null) {
                pstmtChannelParm.close();
              }
            }
          } finally {
            pstmtChannel.close();
          }
          chanIds.clear();
        }

        if (!RDBMServices.supportsOuterJoins) { // Pick up structure parameters
          sql = "SELECT STRUCT_ID, STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId +
            " AND STRUCT_ID IN (" + structParms.toString() + ") ORDER BY STRUCT_ID";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          rs = stmt.executeQuery(sql);
          try {
            if (rs.next()) {
              int structId = rs.getInt(1);
              readParm: while(true) {
                LayoutStructure ls = (LayoutStructure)layoutStructure.get(new Integer(structId));
                int lastStructId = structId;
                do {
                  ls.addParameter(rs.getString(2), rs.getString(3));
                  if (!rs.next()) {
                    break readParm;
                  }
                } while ((structId = rs.getInt(1)) == lastStructId);
              }
            }
          } finally {
            rs.close();
          }
        }

        if (layoutStructure.size() > 0) { // We have a layout to work with
          createLayout(layoutStructure, doc, root, firstStructId);
          layoutStructure.clear();

          long stopTime = System.currentTimeMillis();
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): Layout document for user " + userId + " took " +
            (stopTime - startTime) + " milliseconds to create");
          doc.appendChild(root);

          if (DEBUG > 1) {
            System.err.println("--> created document");
            dumpDoc(doc, "");
            System.err.println("<--");
          }
        }
      } finally {
        stmt.close();
      }
      return  doc;
    } finally {
      RDBMServices.releaseConnection(con);
    }

       */
           return (Hashtable) null;
  }
}