/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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


package org.jasig.portal.layout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserLayoutStore;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.CounterStoreFactory;


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
    crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
    csdb = CounterStoreFactory.getCounterStoreImpl();
    if (RDBMServices.supportsOuterJoins) {
      if (RDBMServices.joinQuery instanceof RDBMServices.JdbcDb) {
        RDBMServices.joinQuery.addQuery("layout_aggr",
          "{oj UP_LAYOUT_STRUCT_AGGR ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.STRUCT_ID = USP.STRUCT_ID} WHERE");
        //RDBMServices.joinQuery.addQuery("ss_struct", "{oj UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID} WHERE");
        //RDBMServices.joinQuery.addQuery("ss_theme", "{oj UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID} WHERE");
      } else if (RDBMServices.joinQuery instanceof RDBMServices.PostgreSQLDb) {
         RDBMServices.joinQuery.addQuery("layout_aggr",
          "UP_LAYOUT_STRUCT_AGGR ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.STRUCT_ID = USP.STRUCT_ID WHERE");
        //RDBMServices.joinQuery.addQuery("ss_struct", "UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID WHERE");
        //RDBMServices.joinQuery.addQuery("ss_theme", "UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID WHERE");
     } else if (RDBMServices.joinQuery instanceof RDBMServices.OracleDb) {
        RDBMServices.joinQuery.addQuery("layout_aggr",
          "UP_LAYOUT_STRUCT_AGGR ULS, UP_LAYOUT_PARAM USP WHERE ULS.STRUCT_ID = USP.STRUCT_ID(+) AND ULS.USER_ID = USP.USER_ID AND");
        //RDBMServices.joinQuery.addQuery("ss_struct", "UP_SS_STRUCT USS, UP_SS_STRUCT_PAR USP WHERE USS.SS_ID=USP.SS_ID(+) AND");
        //RDBMServices.joinQuery.addQuery("ss_theme", "UP_SS_THEME UTS, UP_SS_THEME_PARM UTP WHERE UTS.SS_ID=UTP.SS_ID(+) AND");
      } else {
        throw new Exception("Unknown database!");
      }
    }
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
 public Hashtable getAggregatedUserLayout (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    int realUserId = userId;
    ResultSet rs;
    Connection con = RDBMServices.getConnection();
    Hashtable layout = null;
    UserLayoutFolder rootNode = new UserLayoutFolder();


    RDBMServices.setAutoCommit(con, false);          // May speed things up, can't hurt

    try {

        layout = new Hashtable(20);



      Statement stmt = con.createStatement();

      // A separate statement is needed so as not to interfere with ResultSet
      // of statements used for queries
      Statement insertStmt = con.createStatement();

      try {
        long startTime = System.currentTimeMillis();
        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
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
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
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
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          int nextStructId;
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            nextStructId = rs.getInt(1);
          } finally {
            rs.close();
          }
          sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + realUserId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          stmt.executeUpdate(sQuery);

          sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID=" + realUserId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          stmt.executeUpdate(sQuery);

          // modifed INSERT INTO SELECT statement for MySQL support
          sQuery = " SELECT "+realUserId+", PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL "+
            " FROM UP_SS_USER_ATTS WHERE USER_ID="+userId;
          rs = stmt.executeQuery(sQuery);

          while (rs.next()) {
             String Insert = "INSERT INTO UP_SS_USER_ATTS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL) " +
             "VALUES("+realUserId+","+
              rs.getInt("PROFILE_ID")+","+
              rs.getInt("SS_ID")+"," +
              rs.getInt("SS_TYPE")+"," +
              rs.getInt("STRUCT_ID")+"," +
              "'"+rs.getString("PARAM_NAME")+"'," +
              rs.getInt("PARAM_TYPE")+"," +
              "'"+rs.getString("PARAM_VAL")+"')";
// old code
//          String Insert = "INSERT INTO UP_SS_USER_ATTS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL) "+
//            " SELECT "+realUserId+", USUA.PROFILE_ID, USUA.SS_ID, USUA.SS_TYPE, USUA.STRUCT_ID, USUA.PARAM_NAME, USUA.PARAM_TYPE, USUA.PARAM_VAL "+
//            " FROM UP_SS_USER_ATTS USUA WHERE USUA.USER_ID="+userId;

          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + Insert);
          insertStmt.executeUpdate(Insert);
         }

          // Close Result Set
          if ( rs != null ) rs.close();

          RDBMServices.commit(con); // Make sure it appears in the store
        }

        int firstStructId = -1;
        String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          rs.next();
          firstStructId = rs.getInt(1);
        } finally {
          rs.close();
        }

        // Setting the first layout node ID to the root folder
        rootNode.setFirstChildNodeId(firstStructId+"");

        //Assigning the root folder!!
        rootNode.setNodeDescription(new UserLayoutRootDescription());
        layout.put(UserLayoutNodeDescription.ROOT_FOLDER_ID,rootNode);

        String sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.PREV_STRUCT_ID,ULS.PRNT_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
          "ULS.UNREMOVABLE,ULS.IMMUTABLE";
        if (RDBMServices.supportsOuterJoins) {
          sql += ",USP.STRUCT_PARM_NM,USP.STRUCT_PARM_VAL FROM " + RDBMServices.joinQuery.getQuery("layout_aggr");
        } else {
          sql += " FROM UP_LAYOUT_STRUCT_AGGR ULS WHERE ";
        }
        sql += " ULS.USER_ID=" + userId + " AND ULS.LAYOUT_ID=" + layoutId + " ORDER BY ULS.STRUCT_ID";
        List chanIds = Collections.synchronizedList(new ArrayList());
        //LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
        StringBuffer structParms = new StringBuffer();
        rs = stmt.executeQuery(sql);
        try {
          int lastStructId = 0;
          String sepChar = "";
          if (rs.next()) {
            int structId = rs.getInt(1);
            /*if (rs.wasNull()) {
              structId = 0;
            }*/

            readLayout: while (true) {

              if (DEBUG > 1) System.err.println("Found layout structureID " + structId);


              int nextId = rs.getInt(2);
              /*if (rs.wasNull()) {
                nextId = 0;
              }*/


              int childId = rs.getInt(3);
              /*if (rs.wasNull()) {
                childId = 0;
              }*/

              int prevId = rs.getInt(4);
              /*if (rs.wasNull()) {
                prevId = 0;
              }*/
              int prntId = rs.getInt(5);
              /*if (rs.wasNull()) {
                prntId = 0;
              }*/
              int chanId = rs.getInt(6);
              /*if (rs.wasNull()) {
                chanId = 0;
              }*/

              UserLayoutNodeDescription nodeDesc= null;
              // Trying to get the node if it already exists
              //UserLayoutNode node = (UserLayoutNode) layout.get(structId+"");
              UserLayoutNode node;
              if ( childId != 0 ) {
                //if ( node == null )
                node = new UserLayoutFolder();
                UserLayoutFolderDescription folderDesc = new UserLayoutFolderDescription();
                ((UserLayoutFolder)node).setFirstChildNodeId(childId+"");
                //System.out.println("FIRST!!!!!!!!! INSIDE: " + childId );
                //System.out.println( "3" );
                String type = rs.getString(8);
                int intType;
                if ( "header".equalsIgnoreCase(type))
                 intType = UserLayoutFolderDescription.HEADER_TYPE;
                else if ( "footer".equalsIgnoreCase(type))
                 intType = UserLayoutFolderDescription.FOOTER_TYPE;
                else
                 intType = UserLayoutFolderDescription.REGULAR_TYPE;

                folderDesc.setFolderType(intType);
                nodeDesc = folderDesc;
              } else {
                 //if ( node == null )
                 node = new UserLayoutNode();
                 UserLayoutChannelDescription channelDesc = new UserLayoutChannelDescription();
                 //System.out.println( "5" );
                 channelDesc.setChannelPublishId(rs.getString(6));
                 nodeDesc = channelDesc;
                }

              //System.out.println( "6" );

              // Setting node description attributes
              nodeDesc.setId(structId+"");
              if ("folder".equalsIgnoreCase(node.getNodeType()))
                 nodeDesc.setName(rs.getString(7));
              nodeDesc.setHidden(("Y".equalsIgnoreCase(rs.getString(9))?true:false));
              nodeDesc.setImmutable(("Y".equalsIgnoreCase(rs.getString(11))?true:false));
              nodeDesc.setUnremovable(("Y".equalsIgnoreCase(rs.getString(10))?true:false));

              //System.out.println( "7" );

              // Setting node attributes
              //node.setDepth();
              //node.setGroupName();

              // Setting the next node id
              if ( nextId != 0 ) node.setNextNodeId(nextId+"");

             /*    node.setParentNodeId((prntId<=0)?UserLayoutNodeDescription.ROOT_FOLDER_ID:prntId+"");
                 //Setting the current node to the parent
                 UserLayoutFolder parentFolder = (UserLayoutFolder) layout.get(node.getParentNodeId());
                 // If parent node is null we have to create the new node and put it into the layout hashtable
                 if ( parentFolder == null ) {
                   parentFolder = new UserLayoutFolder();
                   layout.put(node.getParentNodeId(),parentFolder);
                 }
                  parentFolder.addChildNode(structId+"");
             */

              // Setting the previous node id
              if ( prevId != 0 ) node.setPreviousNodeId(prevId+"");
              //node.setPriority();
              //node.setRestrictions();

              //System.out.println( "8" );

              /*ls = new LayoutStructure(structId, nextId, childId, chanId, rs.getString(8),rs.getString(8),rs.getString(9));
              layoutStructure.put(new Integer(structId), ls);*/
              lastStructId = structId;

           // If there is a channel we need to get its parameters

           UserLayoutChannelDescription channelDesc = null;
           if ("channel".equalsIgnoreCase(node.getNodeType())) {
                channelDesc = (UserLayoutChannelDescription) nodeDesc;
                chanIds.add(structId+""); // For later
           }

              //System.out.println( "9" );

              // Setting the node to the layout
              node.setNodeDescription(nodeDesc);
              layout.put(node.getId(),node);

              //System.out.println( "Putting to the layout: node: " + node + " nodeId: " + node.getId()  );


              if (RDBMServices.supportsOuterJoins) {
                do {
                  String name = rs.getString(12);
                  String value = rs.getString(13); // Oracle JDBC requires us to do this for longs
                  if (name != null) { // may not be there because of the join
                    //ls.addParameter(name, value);
                    if ( channelDesc != null ) {
                     //System.out.println( "param name: " + name + " param value: " + value + " nodeId: " + node.getId() );
                     channelDesc.setParameterValue(name,value);
                    }
                  }

                  //System.out.println( "10" );

                  if (!rs.next()) {
                    break readLayout;
                  }
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } while (structId == lastStructId);
              } else { // Do second SELECT later on for structure parameters

                //System.out.println( "11" );
                  // Adding the channel ID to the String buffer
                  if ("channel".equalsIgnoreCase(node.getNodeType())) {
                   structParms.append(sepChar + chanId);
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
                } //end else

              //System.out.println( "12" );


              //System.out.println( "13" );

            } // while
          }
        } finally {
          rs.close();
        }

        // We have to retrieve the channel defition after the layout structure
        // since retrieving the channel data from the DB may interfere with the
        // layout structure ResultSet (in other words, Oracle is a pain to program for)
        if (chanIds.size() > 0) {
          // Pre-prime the channel pump
          for (int i = 0; i < chanIds.size(); i++) {

            //System.out.println( "14" );

            String nodeId = (String) chanIds.get(i);
            //System.out.println( "before" );
            UserLayoutNode node = (UserLayoutNode) layout.get(nodeId+"");

            UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription) node.getNodeDescription();
            ChannelDefinition channelDef = crs.getChannelDefinition(Integer.parseInt(channelDesc.getChannelPublishId()));

            //System.out.println( "after" );
            //channelDesc.setChannelSubscribeId(channelDef.);
            channelDesc.setChannelTypeId(channelDef.getTypeId()+"");
            channelDesc.setClassName(channelDef.getJavaClass());
            channelDesc.setDescription(channelDef.getDescription());
            channelDesc.setEditable(channelDef.isEditable());
            channelDesc.setFunctionalName(CommonUtils.nvl(channelDef.getFName()));
            channelDesc.setHasAbout(channelDef.hasAbout());
            channelDesc.setHasHelp(channelDef.hasHelp());
            channelDesc.setName(channelDef.getName());
            channelDesc.setTitle(channelDef.getTitle());
            channelDesc.setChannelPublishId(channelDef.getId()+"");
            ChannelParameter[] channelParams = channelDef.getParameters();

            //System.out.println( "15" );

            for ( int j = 0; j < channelParams.length; j++ ) {
             String paramName = channelParams[j].getName();
             if ( channelDesc.getParameterValue(paramName) == null ) {
              channelDesc.setParameterOverride(paramName,channelParams[j].getOverride());
              channelDesc.setParameterValue(paramName,channelParams[j].getValue());
             }
            }
            channelDesc.setTimeout(channelDef.getTimeout());
            channelDesc.setTitle(channelDef.getTitle());
            if (DEBUG > 1) {
              System.err.println("Precached " + nodeId);
            }

            //System.out.println( "16" );

          }
          chanIds.clear();
        }

        if (!RDBMServices.supportsOuterJoins) { // Pick up structure parameters
          sql = "SELECT STRUCT_ID, STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId +
            " AND STRUCT_ID IN (" + structParms.toString() + ") ORDER BY STRUCT_ID";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          rs = stmt.executeQuery(sql);
          try {
            if (rs.next()) {
              int structId = rs.getInt(1);
              readParm: while(true) {
                //LayoutStructure ls = (LayoutStructure)layoutStructure.get(new Integer(structId));
                UserLayoutNode node = (UserLayoutNode) layout.get(structId+"");
                if ( node != null ) {
                 UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription) node.getNodeDescription();
                 int lastStructId = structId;
                 do {
                   //ls.addParameter(rs.getString(2), rs.getString(3));
                   String name = rs.getString(2);
                   String value = rs.getString(3);
                   channelDesc.setParameterValue(name,value);
                   if (!rs.next()) {
                     break readParm;
                   }
                 } while ((structId = rs.getInt(1)) == lastStructId);
                } // end if
              }
            }
          } finally {
            rs.close();
          }
        }

        /*if (layoutStructure.size() > 0) { // We have a layout to work with
          createLayout(layoutStructure, doc, root, firstStructId);
          layoutStructure.clear();
        */

          long stopTime = System.currentTimeMillis();
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): Layout document for user " + userId + " took " +
            (stopTime - startTime) + " milliseconds to create");
          //doc.appendChild(root);

          /*if (DEBUG > 1) {
            System.err.println("--> created document");
            dumpDoc(doc, "");
            System.err.println("<--");
          }*/
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }


           return layout;
  }
}