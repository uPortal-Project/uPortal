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

import org.jasig.portal.RDBMUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.layout.*;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.CounterStoreFactory;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.layout.restrictions.*;
import org.jasig.portal.groups.*;


import  java.sql.Connection;
import  java.sql.ResultSet;
import  java.sql.Statement;
import  java.sql.PreparedStatement;
import  java.sql.Types;
import  java.sql.Timestamp;
import  java.sql.SQLException;
import  java.util.List;
import  java.util.Vector;
import  java.util.Collections;
import  java.util.ArrayList;
import  java.util.Date;
import  java.util.Enumeration;
import  java.util.Iterator;
import  java.util.HashMap;
import  java.util.Hashtable;



/**
 * <p>Title: The AggregatedUserLayoutStore class</p>
 * <p>Description: The Aggregated UserLayoutStore implementation using the relational database with SQL 92 </p>
 * <p>Company: Instructional Media & Magic</p>
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.0
 */

public class AggregatedUserLayoutStore extends RDBMUserLayoutStore implements IAggregatedUserLayoutStore {

  private static final int LOST_NODE_ID = -1;


  protected static final String FRAGMENT_UPDATE_SQL = "UPDATE up_fragments SET next_node_id=?,prev_node_id=?,chld_node_id=?,prnt_node_id=?,"+
                                                               "external_id=?,chan_id=?,name=?,type=?,hidden=?,immutable=?,unremovable=?,group=?,"+
                                                               "priority=? WHERE fragment_id=? AND node_id=?";
  protected static final String LAYOUT_UPDATE_SQL = "UPDATE up_layout_struct_aggr SET next_node_id=?,prev_node_id=?,chld_node_id=?,prnt_node_id=?,"+
                                                               "external_id=?,chan_id=?,name=?,type=?,hidden=?,immutable=?,unremovable=?,group=?,"+
                                                               "priority=?,fragment_id=?,fragment_node_id=? WHERE layout_id=? AND user_id=? AND node_id=?";
  protected static final String FRAGMENT_RESTRICTION_UPDATE_SQL = "UPDATE up_fragment_restrictions SET restriction_value=?"+
                                  " WHERE fragment_id=? AND node_id=? AND restriction_type=? AND restriction_tree_path=?";
  protected static final String LAYOUT_RESTRICTION_UPDATE_SQL = "UPDATE up_layout_restrictions SET restriction_value=?"+
                                  " WHERE layout_id=? AND user_id=? AND node_id=? AND restriction_type=? AND restriction_tree_path=?";
  protected static final String CHANNEL_PARAM_UPDATE_SQL = "UPDATE up_channel_param SET chan_parm_desc=?,chan_parm_val=?,chan_parm_ovrd=?" +
                                  " WHERE chan_id=? AND chan_parm_nm=?";
  protected static final String CHANNEL_UPDATE_SQL = "UPDATE up_channel SET chan_title=?,chan_name=?,chan_desc=?,chan_class=?,chan_type_id=?,"+
                      "chan_publ_id=?,chan_publ_dt=?,chan_apvl_id=?,chan_apvl_dt=?,chan_timeout=?,chan_editable=?,chan_has_help=?,chan_has_about=?,"+
                      "chan_fname=? WHERE chan_id=?";
  protected static final String FRAGMENT_ADD_SQL = "INSERT INTO up_fragments (fragment_id,node_id,next_node_id,prev_node_id,chld_node_id,prnt_node_id,"+
                                                               "external_id,chan_id,name,type,hidden,immutable,unremovable,group,priority)"+
                                                               " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  protected static final String LAYOUT_ADD_SQL = "INSERT INTO up_layout_struct_aggr (layout_id,user_id,node_id,next_node_id,prev_node_id,chld_node_id,prnt_node_id,"+
                                                               "external_id,chan_id,name,type,hidden,immutable,unremovable,group,priority,fragment_id,fragment_node_id)"+
                                                               " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  protected static final String FRAGMENT_RESTRICTION_ADD_SQL = "INSERT INTO up_fragment_restrictions (restriction_type,node_id,fragment_id,restriction_value,restriction_tree_path)"+
                                                               " VALUES (?,?,?,?,?)";
  protected static final String LAYOUT_RESTRICTION_ADD_SQL = "INSERT INTO up_layout_restrictions (restriction_type,layout_id,user_id,node_id,restriction_value,restriction_tree_path)"+
                                                               " VALUES (?,?,?,?,?,?)";
  protected static final String CHANNEL_PARAM_ADD_SQL = "INSERT INTO up_channel_param (chan_id,chan_parm_nm,chan_parm_desc,chan_parm_val,chan_parm_ovrd"+
                                                        " VALUES (?,?,?,?,?)";
  protected static final String CHANNEL_ADD_SQL = "INSERT INTO up_channel (chan_id,chan_title,chan_name,chan_desc,chan_class,chan_type_id,chan_publ_id,"+
                                  "chan_publ_dt,chan_apvl_id,chan_apvl_dt,chan_timeout,chan_editable,chan_has_help,chan_has_about,"+
                                  "chan_fname) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  private static String fragmentJoinQuery = "";

  public AggregatedUserLayoutStore() throws Exception {
    super();
    if (RDBMServices.supportsOuterJoins) {
      if (RDBMServices.joinQuery instanceof RDBMServices.JdbcDb) {
        RDBMServices.joinQuery.addQuery("layout_aggr",
          "{oj UP_LAYOUT_STRUCT_AGGR ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.NODE_ID = USP.STRUCT_ID} WHERE");
        fragmentJoinQuery =
          "{oj UP_FRAGMENTS UF LEFT OUTER JOIN UP_FRAGMENT_PARAM UFP ON UF.NODE_ID = UFP.NODE_ID} WHERE UF.FRAGMENT_ID = UFP.FRAGMENT_ID AND";
        //RDBMServices.joinQuery.addQuery("ss_struct", "{oj UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID} WHERE");
        //RDBMServices.joinQuery.addQuery("ss_theme", "{oj UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID} WHERE");
      } else if (RDBMServices.joinQuery instanceof RDBMServices.PostgreSQLDb) {
         RDBMServices.joinQuery.addQuery("layout_aggr",
          "UP_LAYOUT_STRUCT_AGGR ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.NODE_ID = USP.STRUCT_ID WHERE");
         fragmentJoinQuery =
          "UP_FRAGMENTS UF LEFT OUTER JOIN UP_FRAGMENT_PARAM UFP ON UF.NODE_ID = UFP.NODE_ID WHERE UF.FRAGMENT_ID = UFP.FRAGMENT_ID AND";
        //RDBMServices.joinQuery.addQuery("ss_struct", "UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID WHERE");
        //RDBMServices.joinQuery.addQuery("ss_theme", "UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID WHERE");
     } else if (RDBMServices.joinQuery instanceof RDBMServices.OracleDb) {
        RDBMServices.joinQuery.addQuery("layout_aggr",
          "UP_LAYOUT_STRUCT_AGGR ULS, UP_LAYOUT_PARAM USP WHERE ULS.NODE_ID = USP.STRUCT_ID(+) AND ULS.USER_ID = USP.USER_ID AND");
        fragmentJoinQuery =
          "UP_FRAGMENTS UF, UP_FRAGMENT_PARAM UFP WHERE UF.NODE_ID = UFP.NODE_ID(+) AND UF.FRAGMENT_ID = UFP.FRAGMENT_ID AND";
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
     * @param node a <code>ALNode</code> object specifying the node
     * @return a <code>ALNode</code> object specifying the node with the generated node ID
     * @exception PortalException if an error occurs
     */
    public ALNode addUserLayoutNode (IPerson person, UserProfile profile, ALNode node ) throws PortalException {
     Connection con = RDBMServices.getConnection();

     try {
      con.setAutoCommit(false);

      int userId = person.getID();
      IALNodeDescription nodeDesc = node.getNodeDescription();

      Statement stmt = con.createStatement();

        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        ResultSet rs = stmt.executeQuery(subSelectString);
        try {
            rs.next();
            layoutId = rs.getInt(1);
            if (rs.wasNull()) {
                layoutId = 0;
            }
        } finally {
            rs.close();
        }

          // Make sure the next struct id is set in case the user adds a channel
          String sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          int nodeId = 0;
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            nodeId = rs.getInt(1)+1;
          } finally {
            rs.close();
          }

        sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nodeId + " WHERE USER_ID=" + userId;
        stmt.executeUpdate(sQuery);

      PreparedStatement psAddNode, psAddRestriction;
      int fragmentId = CommonUtils.parseInt(nodeDesc.getFragmentId());
      int fragmentNodeId = CommonUtils.parseInt(nodeDesc.getFragmentNodeId());

       // Setting the node ID
       nodeDesc.setId(nodeId+"");

      if ( fragmentId > 0 && fragmentNodeId <= 0 ) {
         psAddNode = con.prepareStatement(FRAGMENT_ADD_SQL);
         psAddRestriction = con.prepareStatement(FRAGMENT_RESTRICTION_ADD_SQL);
      } else {
         psAddNode = con.prepareStatement(LAYOUT_ADD_SQL);
         psAddRestriction = con.prepareStatement(LAYOUT_RESTRICTION_ADD_SQL);
        }


        PreparedStatement  psAddChannelParam = null, psAddChannel = null;

        if ( "channel".equals(node.getNodeType()) ) {
          int subscribeId = CommonUtils.parseInt(((IALChannelDescription)nodeDesc).getChannelSubscribeId());
          if ( subscribeId > 0 ) {
           rs = stmt.executeQuery("SELECT CHAN_ID FROM UP_CHANNEL WHERE CHAN_ID=" + subscribeId);
           try {
            if ( !rs.next() ) {
               psAddChannelParam = con.prepareStatement(CHANNEL_PARAM_ADD_SQL);
               psAddChannel = con.prepareStatement(CHANNEL_ADD_SQL);
            }
           } finally {
            rs.close();
           }
          }
        }


      ALNode resultNode = addUserLayoutNode ( userId, layoutId, node, psAddNode, psAddRestriction, psAddChannel, psAddChannelParam );

      if ( psAddNode != null ) psAddNode.close();
      if ( psAddRestriction != null ) psAddRestriction.close();
      if ( psAddChannel != null ) psAddChannel.close();
      if ( psAddChannelParam != null ) psAddChannelParam.close();

      stmt.close();
      con.commit();
      con.close();

      return resultNode;

     } catch (Exception e) {
        String errorMessage = e.getMessage();
        try { con.rollback(); } catch ( SQLException sqle ) {
           LogService.instance().log(LogService.ERROR, sqle.toString() );
           errorMessage += ":" + sqle.getMessage();
        }
         throw new PortalException(errorMessage);
       }
    }

    /**
     * Add the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> object specifying the node
     * @return a <code>ALNode</code> object specifying the node with the generated node ID
     * @exception PortalException if an error occurs
     */
    private ALNode addUserLayoutNode ( int userId, int layoutId, ALNode node, PreparedStatement psAddNode, PreparedStatement psAddRestriction,
                                               PreparedStatement psAddChannel, PreparedStatement psAddChannelParam ) throws PortalException {

      IALNodeDescription nodeDesc = node.getNodeDescription();

      boolean isFolder = node.getNodeType().equals("folder");
      int fragmentId = CommonUtils.parseInt(nodeDesc.getFragmentId());
      int fragmentNodeId = CommonUtils.parseInt(nodeDesc.getFragmentNodeId());
      int nodeId = CommonUtils.parseInt(nodeDesc.getId());
      int tmpValue = -1;

    try {

      // if the node is in the fragment
      if ( fragmentId > 0 && fragmentNodeId <= 0 ) {

        psAddNode.setInt(1,fragmentId);
        psAddNode.setInt(2,nodeId);

        tmpValue = CommonUtils.parseInt(node.getNextNodeId());
        if ( tmpValue > 0 )
         psAddNode.setInt(3,tmpValue);
        else
         psAddNode.setNull(3,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getPreviousNodeId());
        if ( tmpValue > 0 )
         psAddNode.setInt(4,tmpValue);
        else
         psAddNode.setNull(4,Types.INTEGER);

        tmpValue = (isFolder)?CommonUtils.parseInt(((ALFolder)node).getFirstChildNodeId()):-1;
        if ( tmpValue > 0 )
         psAddNode.setInt(5,tmpValue);
        else
         psAddNode.setNull(5,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getParentNodeId());
        if ( tmpValue > 0 )
         psAddNode.setInt(6,tmpValue);
        else
         psAddNode.setNull(6,Types.INTEGER);


        psAddNode.setNull(7,Types.VARCHAR);

        tmpValue = (!isFolder)?CommonUtils.parseInt(((IALChannelDescription)nodeDesc).getChannelSubscribeId()):-1;
        if ( tmpValue > 0 )
         psAddNode.setInt(8,tmpValue);
        else
         psAddNode.setNull(8,Types.INTEGER);

        psAddNode.setString(9,nodeDesc.getName());
        if ( isFolder ) {
         IALFolderDescription folderDesc = (IALFolderDescription) nodeDesc;
         int type = folderDesc.getFolderType();
         switch ( type ) {
          case UserLayoutFolderDescription.HEADER_TYPE:
           psAddNode.setString(10,"header");
           break;
          case UserLayoutFolderDescription.FOOTER_TYPE:
           psAddNode.setString(10,"footer");
           break;
          default:
           psAddNode.setString(10,"regular");
         }
        } else
           psAddNode.setNull(10,Types.VARCHAR);

         psAddNode.setString(11,(nodeDesc.isHidden())?"Y":"N");
         psAddNode.setString(13,(nodeDesc.isImmutable())?"Y":"N");
         psAddNode.setString(14,(nodeDesc.isUnremovable())?"Y":"N");
         psAddNode.setString(15,nodeDesc.getGroup());
         /*if ( node.getFragmentId() != null )
          psLayout.setString(16,node.getFragmentId());
         else
          psLayout.setNull(16,Types.VARCHAR);*/
         psAddNode.setInt(16,node.getPriority());

         //execute update layout
         psAddNode.executeUpdate();

      // if fragment ID < 0
      } else {

        psAddNode.setInt(1,layoutId);
        psAddNode.setInt(2,userId);
        psAddNode.setInt(3,nodeId);

        tmpValue = CommonUtils.parseInt(node.getNextNodeId());
        if ( tmpValue > 0 )
         psAddNode.setInt(4,tmpValue);
        else
         psAddNode.setNull(4,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getPreviousNodeId());
        if ( tmpValue > 0 )
         psAddNode.setInt(5,tmpValue);
        else
         psAddNode.setNull(5,Types.INTEGER);


        tmpValue = (isFolder)?CommonUtils.parseInt(((ALFolder)node).getFirstChildNodeId()):-1;
        if ( tmpValue > 0 )
         psAddNode.setInt(6,tmpValue);
        else
         psAddNode.setNull(6,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getParentNodeId());
        if ( tmpValue > 0 )
         psAddNode.setInt(7,tmpValue);
        else
         psAddNode.setNull(7,Types.INTEGER);

        psAddNode.setNull(8,Types.VARCHAR);

        tmpValue = (!isFolder)?CommonUtils.parseInt(((IALChannelDescription)nodeDesc).getChannelSubscribeId()):-1;
        if ( tmpValue > 0 )
         psAddNode.setInt(9,tmpValue);
        else
         psAddNode.setNull(9,Types.INTEGER);

        psAddNode.setString(10,nodeDesc.getName());

        if ( isFolder ) {
         IALFolderDescription folderDesc = (IALFolderDescription) nodeDesc;
         int type = folderDesc.getFolderType();
         switch ( type ) {
          case UserLayoutFolderDescription.HEADER_TYPE:
           psAddNode.setString(11,"header");
           break;
          case UserLayoutFolderDescription.FOOTER_TYPE:
           psAddNode.setString(11,"footer");
           break;
          default:
           psAddNode.setString(11,"regular");
         }
        } else
           psAddNode.setNull(11,Types.VARCHAR);

         psAddNode.setString(12,(nodeDesc.isHidden())?"Y":"N");
         psAddNode.setString(13,(nodeDesc.isImmutable())?"Y":"N");
         psAddNode.setString(14,(nodeDesc.isUnremovable())?"Y":"N");
         psAddNode.setString(15,nodeDesc.getGroup());
         /*if ( node.getFragmentId() != null )
          psLayout.setString(16,node.getFragmentId());
         else
          psLayout.setNull(16,Types.VARCHAR);*/
         psAddNode.setInt(16,node.getPriority());
         if ( fragmentId > 0 )
          psAddNode.setInt(17,fragmentId);
         else
          psAddNode.setNull(17,Types.INTEGER);

         if ( fragmentNodeId > 0 )
          psAddNode.setInt(18,fragmentNodeId);
         else
          psAddNode.setNull(18,Types.INTEGER);


         //execute update layout
         psAddNode.executeUpdate();
       }

         // Insert node restrictions
        Hashtable restrHash = nodeDesc.getRestrictions();
        if ( restrHash != null ) {

         PreparedStatement psRestr = null;


         if ( fragmentId > 0 ) {

           Enumeration restrictions = restrHash.elements();
           for ( ;restrictions.hasMoreElements(); ) {
             IUserLayoutRestriction restriction = (IUserLayoutRestriction) restrictions.nextElement();

             psAddRestriction.setInt(1,restriction.getRestrictionType());
             psAddRestriction.setInt(2,nodeId);
             psAddRestriction.setInt(3,fragmentId);
             psAddRestriction.setString(4,restriction.getRestrictionExpression());

             String path = restriction.getRestrictionPath();
             if ( path != null )
              psAddRestriction.setString(5,path);
             else
              psAddRestriction.setNull(5,Types.VARCHAR);

             //execute update restrictions
             psAddRestriction.executeUpdate();

           } // end for

         } else {

            Enumeration restrictions = restrHash.elements();
            for ( ;restrictions.hasMoreElements(); ) {
             IUserLayoutRestriction restriction = (IUserLayoutRestriction) restrictions.nextElement();

             psAddRestriction.setInt(1,restriction.getRestrictionType());
             psAddRestriction.setInt(2,layoutId);
             psAddRestriction.setInt(3,userId);
             psAddRestriction.setInt(4,nodeId);
             psAddRestriction.setString(5,restriction.getRestrictionExpression());

             String path = restriction.getRestrictionPath();
             if ( path != null )
              psAddRestriction.setString(6,path);
             else
              psAddRestriction.setNull(6,Types.VARCHAR);

             //execute update restrictions
             psAddRestriction.executeUpdate();

            } // end for

           } // end else


        } // end if



       // if we have channel parameters
       if ( !isFolder && psAddChannel != null && psAddChannelParam != null ) {

         IALChannelDescription channelDesc = (IALChannelDescription) nodeDesc;

         int subscribeId = CommonUtils.parseInt(channelDesc.getChannelSubscribeId());
         if ( subscribeId > 0 ) {

          for ( Enumeration paramNames = channelDesc.getParameterNames(); paramNames.hasMoreElements(); ) {
            String paramName = (String) paramNames.nextElement();
            String paramValue = channelDesc.getParameterValue(paramName);

            psAddChannelParam.setInt(1,subscribeId);

            psAddChannelParam.setString(2,paramName);
            if ( channelDesc.getDescription() != null )
             psAddChannelParam.setString(3,channelDesc.getDescription());
            else
             psAddChannelParam.setNull(3,Types.VARCHAR);
            psAddChannelParam.setString(4,paramValue);
            psAddChannelParam.setString(5,(channelDesc.canOverrideParameter(paramName))?"Y":"N");

            //execute update parameters
            psAddChannelParam.executeUpdate();
          }

             // Inserting channel attributes
            psAddChannel.setInt(1,subscribeId);

             psAddChannel.setString(2,channelDesc.getTitle());
             psAddChannel.setString(3,channelDesc.getName());
             if ( channelDesc.getDescription() != null )
              psAddChannel.setString(4,channelDesc.getDescription());
             else
              psAddChannel.setNull(4,Types.VARCHAR);
             psAddChannel.setString(5,channelDesc.getClassName());
             tmpValue = CommonUtils.parseInt(channelDesc.getChannelTypeId());
             if ( tmpValue > 0 )
              psAddChannel.setInt(6,tmpValue);
             else
              psAddChannel.setNull(6,Types.INTEGER);

             tmpValue = CommonUtils.parseInt(channelDesc.getChannelPublishId());
             if ( tmpValue > 0 )
              psAddChannel.setInt(7,tmpValue);
             else
              psAddChannel.setNull(7,Types.INTEGER);

             Timestamp timestamp = new java.sql.Timestamp(new Date().getTime());
             psAddChannel.setTimestamp(8,timestamp);
             psAddChannel.setInt(9,0);
             psAddChannel.setTimestamp(10,timestamp);
             psAddChannel.setInt(11,(int)channelDesc.getTimeout());
             psAddChannel.setString(12,(channelDesc.isEditable())?"Y":"N");
             psAddChannel.setString(13,(channelDesc.hasHelp())?"Y":"N");
             psAddChannel.setString(14,(channelDesc.hasAbout())?"Y":"N");
             psAddChannel.setString(15,channelDesc.getFunctionalName());

             //execute update parameters
             psAddChannel.executeUpdate();
         }
        }

        return node;

     } catch (Exception e) {
        e.printStackTrace();
        String errorMessage = e.getMessage();
        throw new PortalException(errorMessage);
       }

    }


  /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> object specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
  public boolean updateUserLayoutNode (IPerson person, UserProfile profile, ALNode node ) throws PortalException {

     Connection con = RDBMServices.getConnection();

     try {

      con.setAutoCommit(false);

      int userId = person.getID();
      int nodeId = CommonUtils.parseInt(node.getId());
      IALNodeDescription nodeDesc = node.getNodeDescription();

      Statement stmt = con.createStatement();

        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        //LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        ResultSet rs = stmt.executeQuery(subSelectString);
        try {
            rs.next();
            layoutId = rs.getInt(1);
            if (rs.wasNull()) {
                layoutId = 0;
            }
        } finally {
            rs.close();
        }

      PreparedStatement psUpdateNode, psUpdateRestriction;
      int fragmentId = CommonUtils.parseInt(nodeDesc.getFragmentId());
      int fragmentNodeId = CommonUtils.parseInt(nodeDesc.getFragmentNodeId());

      if ( fragmentId > 0 && fragmentNodeId <= 0 ) {
         psUpdateNode = con.prepareStatement(FRAGMENT_UPDATE_SQL);
         psUpdateRestriction = con.prepareStatement(FRAGMENT_RESTRICTION_UPDATE_SQL);
      } else {
         psUpdateNode = con.prepareStatement(LAYOUT_UPDATE_SQL);
         psUpdateRestriction = con.prepareStatement(LAYOUT_RESTRICTION_UPDATE_SQL);
        }

      PreparedStatement  psUpdateChannelParam = con.prepareStatement(CHANNEL_PARAM_UPDATE_SQL);
      PreparedStatement  psUpdateChannel = con.prepareStatement(CHANNEL_UPDATE_SQL);

      boolean result = updateUserLayoutNode ( userId, layoutId, node, psUpdateNode, psUpdateRestriction, psUpdateChannel, psUpdateChannelParam );

      if ( psUpdateNode != null ) psUpdateNode.close();
      if ( psUpdateRestriction != null ) psUpdateRestriction.close();
      if ( psUpdateChannel != null ) psUpdateChannel.close();
      if ( psUpdateChannelParam != null ) psUpdateChannelParam.close();


      con.commit();

      // Closing
      stmt.close();
      con.close();

      return result;

     } catch (Exception e) {
        String errorMessage = e.getMessage();
        try { con.rollback(); } catch ( SQLException sqle ) {
           LogService.instance().log(LogService.ERROR, sqle.toString() );
           errorMessage += ":" + sqle.getMessage();
        }
         throw new PortalException(errorMessage);
       }
 }

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> object specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
  private boolean updateUserLayoutNode (int userId, int layoutId, ALNode node, PreparedStatement psUpdateNode,
                  PreparedStatement psUpdateRestriction, PreparedStatement psUpdateChannel, PreparedStatement psUpdateChannelParam ) throws PortalException {
      //boolean layoutUpdate = false, channelUpdate = false, paramUpdate = false, restrUpdate = false;

      int count = 0;

      boolean isFolder = node.getNodeType().equals("folder");
      IALNodeDescription nodeDesc = node.getNodeDescription();
      int nodeId = CommonUtils.parseInt(nodeDesc.getId());
      int fragmentId = CommonUtils.parseInt(nodeDesc.getFragmentId());
      int fragmentNodeId = CommonUtils.parseInt(nodeDesc.getFragmentNodeId());
      int tmpValue = -1;

      System.out.println( "fragmentId: " + fragmentId );
      System.out.println( "fragmentNodeId: " + fragmentNodeId );
      System.out.println( "nodeIdUpdate: " + nodeId );

   try {

     if ( fragmentId > 0 && fragmentNodeId <= 0 ) {

        tmpValue = CommonUtils.parseInt(node.getNextNodeId());
        if ( tmpValue > 0 )
         psUpdateNode.setInt(1,tmpValue);
        else
         psUpdateNode.setNull(1,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getPreviousNodeId());
        if ( tmpValue > 0 )
         psUpdateNode.setInt(2,tmpValue);
        else
         psUpdateNode.setNull(2,Types.INTEGER);

        tmpValue = (isFolder)?CommonUtils.parseInt(((ALFolder)node).getFirstChildNodeId()):-1;
        if ( tmpValue > 0 )
         psUpdateNode.setInt(3,tmpValue);
        else
         psUpdateNode.setNull(3,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getParentNodeId());
        if ( tmpValue > 0 )
         psUpdateNode.setInt(4,tmpValue);
        else
         psUpdateNode.setNull(4,Types.INTEGER);

        psUpdateNode.setNull(5,Types.VARCHAR);

        tmpValue = (!isFolder)?CommonUtils.parseInt(((IALChannelDescription)nodeDesc).getChannelSubscribeId()):-1;
        if ( tmpValue > 0 )
         psUpdateNode.setInt(6,tmpValue);
        else
         psUpdateNode.setNull(6,Types.INTEGER);

        psUpdateNode.setString(7,nodeDesc.getName());
        if ( isFolder ) {
         IALFolderDescription folderDesc = (IALFolderDescription) nodeDesc;
         int type = folderDesc.getFolderType();
         switch ( type ) {
          case UserLayoutFolderDescription.HEADER_TYPE:
           psUpdateNode.setString(8,"header");
           break;
          case UserLayoutFolderDescription.FOOTER_TYPE:
           psUpdateNode.setString(8,"footer");
           break;
          default:
           psUpdateNode.setString(8,"regular");
         }
        } else
           psUpdateNode.setNull(8,Types.VARCHAR);

         psUpdateNode.setString(9,(nodeDesc.isHidden())?"Y":"N");
         psUpdateNode.setString(10,(nodeDesc.isImmutable())?"Y":"N");
         psUpdateNode.setString(11,(nodeDesc.isUnremovable())?"Y":"N");
         psUpdateNode.setString(12,nodeDesc.getGroup());

         psUpdateNode.setInt(13,node.getPriority());

         psUpdateNode.setInt(14,nodeId);
         psUpdateNode.setInt(15,fragmentId);

         //execute update layout
         count += psUpdateNode.executeUpdate();

       // if fragment id <= 0
     } else {

        tmpValue = CommonUtils.parseInt(node.getNextNodeId());
        if ( tmpValue > 0 )
         psUpdateNode.setInt(1,tmpValue);
        else
         psUpdateNode.setNull(1,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getPreviousNodeId());
        if ( tmpValue > 0 )
         psUpdateNode.setInt(2,tmpValue);
        else
         psUpdateNode.setNull(2,Types.INTEGER);

        tmpValue = (isFolder)?CommonUtils.parseInt(((ALFolder)node).getFirstChildNodeId()):-1;
        if ( tmpValue > 0 )
         psUpdateNode.setInt(3,tmpValue);
        else
         psUpdateNode.setNull(3,Types.INTEGER);

        tmpValue = CommonUtils.parseInt(node.getParentNodeId());
        if ( tmpValue > 0 )
         psUpdateNode.setInt(4,tmpValue);
        else
         psUpdateNode.setNull(4,Types.INTEGER);


        psUpdateNode.setNull(5,Types.VARCHAR);

        tmpValue = (!isFolder)?CommonUtils.parseInt(((IALChannelDescription)nodeDesc).getChannelSubscribeId()):-1;
        if ( tmpValue > 0 )
         psUpdateNode.setInt(6,tmpValue);
        else
         psUpdateNode.setNull(6,Types.INTEGER);

        psUpdateNode.setString(7,nodeDesc.getName());

        if ( isFolder ) {
         IALFolderDescription folderDesc = (IALFolderDescription) nodeDesc;
         int type = folderDesc.getFolderType();
         switch ( type ) {
          case UserLayoutFolderDescription.HEADER_TYPE:
           psUpdateNode.setString(8,"header");
           break;
          case UserLayoutFolderDescription.FOOTER_TYPE:
           psUpdateNode.setString(8,"footer");
           break;
          default:
           psUpdateNode.setString(8,"regular");
         }
        } else
           psUpdateNode.setNull(8,Types.VARCHAR);

         psUpdateNode.setString(9,(nodeDesc.isHidden())?"Y":"N");
         psUpdateNode.setString(10,(nodeDesc.isImmutable())?"Y":"N");
         psUpdateNode.setString(11,(nodeDesc.isUnremovable())?"Y":"N");
         psUpdateNode.setString(12,nodeDesc.getGroup());
         /*if ( node.getFragmentId() != null )
          psLayout.setString(13,node.getFragmentId());
         else
          psLayout.setNull(13,Types.VARCHAR);*/

         psUpdateNode.setInt(13,node.getPriority());

         if ( fragmentId > 0 )
          psUpdateNode.setInt(14,fragmentId);
         else
          psUpdateNode.setNull(14,Types.INTEGER);

         if ( fragmentNodeId > 0 )
          psUpdateNode.setInt(15,fragmentNodeId);
         else
          psUpdateNode.setNull(15,Types.INTEGER);

         psUpdateNode.setInt(16,layoutId);
         psUpdateNode.setInt(17,userId);
         psUpdateNode.setInt(18,nodeId);

         //execute update layout
         count += psUpdateNode.executeUpdate();

       }

         // Insert node restrictions
         Hashtable restrHash = nodeDesc.getRestrictions();
         if ( restrHash != null ) {

          if ( fragmentId > 0 ) {

           Enumeration restrictions = restrHash.elements();
           for ( ;restrictions.hasMoreElements(); ) {
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) restrictions.nextElement();

            psUpdateRestriction.setString(1,restriction.getRestrictionExpression());
            psUpdateRestriction.setInt(2,fragmentId);
            psUpdateRestriction.setInt(3,nodeId);
            psUpdateRestriction.setInt(4,restriction.getRestrictionType());

            String path = restriction.getRestrictionPath();
            if ( path != null )
              psUpdateRestriction.setString(5,path);
            else
              psUpdateRestriction.setNull(5,Types.VARCHAR);

            //execute update restrictions
            count += psUpdateRestriction.executeUpdate();
           } // end for

          } else {

           Enumeration restrictions = restrHash.elements();
           for ( ;restrictions.hasMoreElements(); ) {
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) restrictions.nextElement();

            psUpdateRestriction.setString(1,restriction.getRestrictionExpression());
            psUpdateRestriction.setInt(2,layoutId);
            psUpdateRestriction.setInt(3,userId);
            psUpdateRestriction.setInt(4,nodeId);
            psUpdateRestriction.setInt(5,restriction.getRestrictionType());

            String path = restriction.getRestrictionPath();
            if ( path != null )
              psUpdateRestriction.setString(6,path);
            else
              psUpdateRestriction.setNull(6,Types.VARCHAR);

            //execute update restrictions
            count += psUpdateRestriction.executeUpdate();

           } // end for
          }  // end else
         } // end if


        // if we have channel parameters

        if ( !isFolder ) {
         IALChannelDescription channelDesc = (IALChannelDescription) nodeDesc;
         int subscribeId = CommonUtils.parseInt(channelDesc.getChannelSubscribeId());
         if ( subscribeId > 0 ) {

          for ( Enumeration paramNames = channelDesc.getParameterNames(); paramNames.hasMoreElements(); ) {
            String paramName = (String) paramNames.nextElement();
            String paramValue = channelDesc.getParameterValue(paramName);

            if ( channelDesc.getDescription() != null )
             psUpdateChannelParam.setString(1,channelDesc.getDescription());
            else
             psUpdateChannelParam.setNull(1,Types.VARCHAR);
            psUpdateChannelParam.setString(2,paramValue);
            psUpdateChannelParam.setString(3,(channelDesc.canOverrideParameter(paramName))?"Y":"N");

             psUpdateChannelParam.setInt(4,subscribeId);

             psUpdateChannelParam.setString(5,paramName);

            //execute update parameters
            count += psUpdateChannelParam.executeUpdate();
          }

           // Inserting channel attributes
             psUpdateChannel.setString(1,channelDesc.getTitle());
             psUpdateChannel.setString(2,channelDesc.getName());
             if ( channelDesc.getDescription() != null )
              psUpdateChannel.setString(3,channelDesc.getDescription());
             else
              psUpdateChannel.setNull(3,Types.VARCHAR);
             psUpdateChannel.setString(4,channelDesc.getClassName());

             tmpValue = CommonUtils.parseInt(channelDesc.getChannelTypeId());
             if ( tmpValue > 0 )
              psUpdateChannel.setInt(5,tmpValue);
             else
              psUpdateChannel.setNull(5,Types.INTEGER);

             tmpValue = CommonUtils.parseInt(channelDesc.getChannelPublishId());
             if ( tmpValue > 0 )
              psUpdateChannel.setInt(6,tmpValue);
             else
              psUpdateChannel.setNull(6,Types.INTEGER);

             Timestamp timestamp = new java.sql.Timestamp(new Date().getTime());
             psUpdateChannel.setTimestamp(7,timestamp);
             psUpdateChannel.setInt(8,0);
             psUpdateChannel.setTimestamp(9,timestamp);
             psUpdateChannel.setInt(10,(int)channelDesc.getTimeout());
             psUpdateChannel.setString(11,(channelDesc.isEditable())?"Y":"N");
             psUpdateChannel.setString(12,(channelDesc.hasHelp())?"Y":"N");
             psUpdateChannel.setString(13,(channelDesc.hasAbout())?"Y":"N");
             psUpdateChannel.setString(14,channelDesc.getFunctionalName());

             psUpdateChannel.setInt(15,subscribeId);

             //execute update parameters
             count += psUpdateChannel.executeUpdate();
             //psChan.close();
         }
        }

        return count > 0;

     } catch (Exception e) {
        e.printStackTrace();
        String errorMessage = e.getMessage();
        throw new PortalException(errorMessage);
       }
  }

    /**
     * Delete the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> node ID specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean deleteUserLayoutNode (IPerson person, UserProfile profile, ALNode node ) throws PortalException {
     Connection con = RDBMServices.getConnection();
     try {

      //boolean layoutUpdate = false, restrUpdate = false, paramUpdate = false, chanUpdate = false;

      int count = 0;

      con.setAutoCommit(false);

      int userId = person.getID();
      int nodeId = CommonUtils.parseInt(node.getId());
      IALNodeDescription nodeDesc = node.getNodeDescription();
      Statement stmt = con.createStatement();

        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        ResultSet rs = stmt.executeQuery(subSelectString);
        try {
            rs.next();
            layoutId = rs.getInt(1);
            if (rs.wasNull()) {
                layoutId = 0;
            }
        } finally {
            rs.close();
        }


      boolean isFolder = node.getNodeType().equals("folder");
      int fragmentId = CommonUtils.parseInt(nodeDesc.getFragmentId());
      int fragmentNodeId = CommonUtils.parseInt(nodeDesc.getFragmentNodeId());
      int tmpValue = -1;

      // if we have a channel
        if ( !isFolder ) {
          IALChannelDescription channelDesc = (IALChannelDescription) nodeDesc;
          PreparedStatement  psParam =
             con.prepareStatement("DELETE FROM up_channel_param WHERE chan_id=?,chan_parm_nm=?");
          tmpValue = CommonUtils.parseInt(channelDesc.getChannelSubscribeId());
          for ( Enumeration paramNames = channelDesc.getParameterNames(); paramNames.hasMoreElements(); ) {
            String paramName = (String) paramNames.nextElement();

            if ( tmpValue > 0 )
             psParam.setInt(1,tmpValue);
            else
             psParam.setNull(1,Types.INTEGER);

             psParam.setString(2,paramName);

            //execute update parameters
            count += psParam.executeUpdate();
          }
            psParam.close();

           // deleting channel attributes
           PreparedStatement  psChan =
             con.prepareStatement("DELETE FROM up_channel WHERE chan_id=?");

             if ( tmpValue > 0 )
              psChan.setInt(1,tmpValue);
             else
              psChan.setNull(1,Types.INTEGER);

             //execute update parameters
             count += psChan.executeUpdate();
             psChan.close();

        }


         // Delete node restrictions
         Hashtable restrHash = nodeDesc.getRestrictions();
         if ( restrHash != null ) {

          if ( fragmentId > 0 ) {

           PreparedStatement  psFragmentRestr =
             con.prepareStatement("DELETE FROM up_fragment_restrictions"+
                                  " WHERE fragment_id=? AND node_id=? AND restriction_type=? AND restriction_tree_path=?");
           Enumeration restrictions = restrHash.elements();
           for ( ;restrictions.hasMoreElements(); ) {
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) restrictions.nextElement();

            psFragmentRestr.setInt(1,fragmentId);
            psFragmentRestr.setInt(2,nodeId);
            psFragmentRestr.setInt(3,restriction.getRestrictionType());

            String path = restriction.getRestrictionPath();
            if ( path != null )
              psFragmentRestr.setString(4,path);
            else
              psFragmentRestr.setNull(4,Types.VARCHAR);

            //execute update restrictions
            count += psFragmentRestr.executeUpdate();

           } // end for
            psFragmentRestr.close();

          // fragment ID is null
          } else {

           PreparedStatement  psRestr =
             con.prepareStatement("DELETE FROM up_layout_restrictions"+
                                  " WHERE layout_id=? AND user_id=? AND node_id=? AND restriction_type=? AND restriction_tree_path=?");

           Enumeration restrictions = restrHash.elements();
           for ( ;restrictions.hasMoreElements(); ) {
            IUserLayoutRestriction restriction = (IUserLayoutRestriction) restrictions.nextElement();

            psRestr.setInt(1,layoutId);
            psRestr.setInt(2,userId);
            psRestr.setInt(3,nodeId);
            psRestr.setInt(4,restriction.getRestrictionType());

            String path = restriction.getRestrictionPath();
            if ( path != null )
              psRestr.setString(5,path);
            else
              psRestr.setNull(5,Types.VARCHAR);
            //execute update restrictions
            count += psRestr.executeUpdate();

           } // end for
            psRestr.close();
          } // end if for fragment ID
         } // end if


      if ( fragmentId > 0 && fragmentNodeId <= 0 ) {
       PreparedStatement  psFragment =
        con.prepareStatement("DELETE FROM up_fragments WHERE node_id=? AND fragment_id=?");

         psFragment.setInt(1,nodeId);
         psFragment.setInt(2,fragmentId);

         //execute update layout
         count += psFragment.executeUpdate();
         psFragment.close();

      } else {
       PreparedStatement  psLayout =
        con.prepareStatement("DELETE FROM up_layout_struct_aggr WHERE layout_id=? AND user_id=? AND node_id=?");

         psLayout.setInt(1,layoutId);
         psLayout.setInt(2,userId);
         psLayout.setInt(3,nodeId);

         //execute update layout
         count += psLayout.executeUpdate();
         psLayout.close();
       }


        stmt.close();
        con.commit();
        con.close();

        return count > 0;

     } catch (Exception e) {
        String errorMessage = e.getMessage();
        try { con.rollback(); } catch ( SQLException sqle ) {
           LogService.instance().log(LogService.ERROR, sqle.toString() );
           errorMessage += ":" + sqle.getMessage();
        }
         throw new PortalException(errorMessage);
       }
    }

   /**
     * Gets the user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param nodeId a <code>String</code> node ID specifying the node
     * @return a <code>ALNode</code> object
     * @exception PortalException if an error occurs
     */
    public ALNode getUserLayoutNode (IPerson person, UserProfile profile, String nodeId ) throws PortalException {
      return null;
    }

   /**
     * @param Person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param layout a <code>Document</code> containing an aggregated user layout
     * @exception Exception if an error occurs
     */
 public void setAggregatedUserLayout (IPerson person, UserProfile  profile, Object layoutObject ) throws Exception {

    int userId = person.getID();
    Connection con = RDBMServices.getConnection();
    if ( !(layoutObject instanceof Hashtable) )
       throw new PortalException("The user layout object should have \"Hashtable\" type");
    Hashtable layout = (Hashtable) layoutObject;

    RDBMServices.setAutoCommit(con, false);       // May speed things up, can't hurt

    try {

       Statement stmt = con.createStatement();

        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        //LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        ResultSet rs = stmt.executeQuery(subSelectString);
        try {
            rs.next();
            layoutId = rs.getInt(1);
            if (rs.wasNull()) {
                layoutId = 0;
            }
        } finally {
            rs.close();
            if ( stmt != null ) stmt.close();
        }

      // Clear the previous data related to the user layout
      PreparedStatement psDeleteLayout = con.prepareStatement("DELETE FROM up_layout_struct_aggr WHERE user_id=? AND layout_id=?");
      psDeleteLayout.setInt(1,userId);
      psDeleteLayout.setInt(2,layoutId);
      psDeleteLayout.executeUpdate();
      // Deleting restrictions
      psDeleteLayout = con.prepareStatement("DELETE FROM up_layout_restrictions WHERE user_id=? AND layout_id=?");
      psDeleteLayout.setInt(1,userId);
      psDeleteLayout.setInt(2,layoutId);
      psDeleteLayout.executeUpdate();
      psDeleteLayout.close();


      // Update prepared statements
      //PreparedStatement  psUpdateFragmentNode = con.prepareStatement(FRAGMENT_UPDATE_SQL);
      //PreparedStatement  psUpdateFragmentRestriction = con.prepareStatement(FRAGMENT_RESTRICTION_UPDATE_SQL);
      PreparedStatement  psUpdateLayoutNode = con.prepareStatement(LAYOUT_UPDATE_SQL);
      PreparedStatement  psUpdateLayoutRestriction = con.prepareStatement(LAYOUT_RESTRICTION_UPDATE_SQL);
      PreparedStatement  psUpdateChannelParam = con.prepareStatement(CHANNEL_PARAM_UPDATE_SQL);
      PreparedStatement  psUpdateChannel = con.prepareStatement(CHANNEL_UPDATE_SQL);

      // Add prepared statements
      PreparedStatement  psAddFragmentNode = con.prepareStatement(FRAGMENT_ADD_SQL);
      PreparedStatement  psAddFragmentRestriction = con.prepareStatement(FRAGMENT_RESTRICTION_ADD_SQL);
      PreparedStatement  psAddLayoutNode = con.prepareStatement(LAYOUT_ADD_SQL);
      PreparedStatement  psAddLayoutRestriction = con.prepareStatement(LAYOUT_RESTRICTION_ADD_SQL);
      PreparedStatement  psAddChannelParam = con.prepareStatement(CHANNEL_PARAM_ADD_SQL);
      PreparedStatement  psAddChannel = con.prepareStatement(CHANNEL_ADD_SQL);

      PreparedStatement psLayout = con.prepareStatement("SELECT node_id FROM up_layout_struct_aggr WHERE node_id=? AND user_id=? AND layout_id=?");
      //PreparedStatement psFragment = con.prepareStatement("SELECT node_id FROM up_layout_struct_aggr WHERE node_id=? AND fragment_id=?");

       // The loop for all the nodes from the hashtable
       for ( Enumeration nodeIds = layout.keys(); nodeIds.hasMoreElements() ;) {
        String strNodeId = nodeIds.nextElement().toString();

        if ( !strNodeId.equals(AggregatedUserLayoutImpl.ROOT_FOLDER_ID) && !strNodeId.equals(IALFolderDescription.LOST_FOLDER_ID) ) {
         //int nodeId = CommonUtils.parseInt(strNodeId);
         ALNode node = (ALNode) layout.get(strNodeId);
         int nodeId = CommonUtils.parseInt(node.getId());

         int fragmentId = CommonUtils.parseInt(node.getFragmentId());
         int fragmentNodeId = CommonUtils.parseInt(node.getFragmentNodeId());
         PreparedStatement ps;

         if ( fragmentId > 0 && fragmentNodeId <= 0 ) {
           /*ps = psFragment;
           ps.setInt(1,nodeId);
           ps.setInt(2,fragmentId);
           rs = ps.executeQuery();
           if ( rs.next() )
               updateUserLayoutNode(userId,layoutId,node,psUpdateFragmentNode,psUpdateFragmentRestriction,psUpdateChannel,psUpdateChannelParam);
           else
               addUserLayoutNode(userId,layoutId,node,psAddFragmentNode,psAddFragmentRestriction,psAddChannel,psAddChannelParam);
           rs.close();*/
         } else {

             ps = psLayout;
             ps.setInt(1,nodeId);
             ps.setInt(2,userId);
             ps.setInt(3,layoutId);
             rs = ps.executeQuery();
            if ( rs.next() )
               updateUserLayoutNode(userId,layoutId,node,psUpdateLayoutNode,psUpdateLayoutRestriction,psUpdateChannel,psUpdateChannelParam);
            else {

               boolean channelParamsExist = false;

               if ( "channel".equals(node.getNodeType()) ) {
                int subscribeId = CommonUtils.parseInt(((IALChannelDescription)node.getNodeDescription()).getChannelSubscribeId());
                  ResultSet rsChan = stmt.executeQuery("SELECT CHAN_ID FROM UP_CHANNEL WHERE CHAN_ID=" + subscribeId);
                  try {
                   if ( rsChan.next() )
                     channelParamsExist = true;
                  } finally {
                     rsChan.close();
                    }
               }

               if ( channelParamsExist )
                 addUserLayoutNode(userId,layoutId,node,psAddLayoutNode,psAddLayoutRestriction,null,null);
               else
                 addUserLayoutNode(userId,layoutId,node,psAddLayoutNode,psAddLayoutRestriction,psAddChannel,psAddChannelParam);
            }

             rs.close();
           }
         } // End if
        } // End for


      // Commit all the changes
      con.commit();

      //if ( psFragment != null ) psFragment.close();
      if ( psLayout != null ) psLayout.close();

      //if ( psUpdateFragmentNode != null ) psUpdateFragmentNode.close();
      //if ( psUpdateFragmentRestriction != null ) psUpdateFragmentRestriction.close();
      if ( psUpdateLayoutNode != null ) psUpdateLayoutNode.close();
      if ( psUpdateLayoutRestriction != null ) psUpdateLayoutRestriction.close();

      //if ( psAddFragmentNode != null ) psAddFragmentNode.close();
      //if ( psAddFragmentRestriction != null ) psAddFragmentRestriction.close();
      if ( psAddLayoutNode != null ) psAddLayoutNode.close();
      if ( psAddLayoutRestriction != null ) psAddLayoutRestriction.close();

      if ( psUpdateChannel != null ) psUpdateChannel.close();
      if ( psUpdateChannelParam != null ) psUpdateChannelParam.close();

      // Close the connection
      con.close();


    } catch (Exception e) {
        e.printStackTrace();
        String errorMessage = e.getMessage();
        try { con.rollback(); } catch ( SQLException sqle ) {
           LogService.instance().log(LogService.ERROR, sqle.toString() );
           errorMessage += ":" + sqle.getMessage();
        }
         throw new PortalException(errorMessage);
      }
 }

    /**
     * Returns the user layout internal representation.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @return a <code>Object</code> object containing the internal representation of the user layout
     * @exception PortalException if an error occurs
     */
 public Object getAggregatedUserLayout (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    int realUserId = userId;
    ResultSet rs;
    Connection con = RDBMServices.getConnection();
    Hashtable layout = null;
    ALFolder rootNode = new ALFolder();
    PreparedStatement psRestrLayout = null, psRestrFragment = null;

    EntityIdentifier personIdentifier = person.getEntityIdentifier();
    IGroupMember groupMember = GroupService.getGroupMember(personIdentifier);


    RDBMServices.setAutoCommit(con, false);          // May speed things up, can't hurt

    try {

        layout = new Hashtable(50);


        //delete from up_layout_struct_aggr where fragment_id in ( select uof.fragment_id from up_owner_fragment uof, up_layout_struct_aggr uls where uls.fragment_id != NULL and uof.fragment_id = uls.fragment_id and uof.pushed_fragment='Y' and uls.fragment_id not in (1) );


        // Getting the appropriate groups from the database for pushable fragments
        PreparedStatement psGroups = con.prepareStatement("SELECT uof.fragment_id, uof.fragment_root_id FROM up_group_fragment upg, up_owner_fragment uof " +
                                                          "WHERE upg.group_key=? AND upg.fragment_id = uof.fragment_id");
        Iterator containingGroups = groupMember.getAllContainingGroups();
        //Vector pushFragments = new Vector();
        Hashtable pushFragmentRoots = new Hashtable();
        String pushFragmentIds = null;
        while ( containingGroups.hasNext() ) {
          IEntityGroup entityGroup = (IEntityGroup) containingGroups.next();
          psGroups.setString(1,entityGroup.getKey());
          ResultSet rsGroups = psGroups.executeQuery();
          if ( rsGroups.next() ) {
           int fragmentId = rsGroups.getInt(1);
           if ( pushFragmentIds == null )
            pushFragmentIds = fragmentId+"";
           else
            pushFragmentIds += "," + fragmentId;
           pushFragmentRoots.put(""+fragmentId,rsGroups.getInt(2)+"");
          }
          while ( rsGroups.next() ) {
           //pushFragments.add ( rsGroups.getInt(1) + "" );
           int fragmentId = rsGroups.getInt(1);
           pushFragmentIds += "," + fragmentId;
           pushFragmentRoots.put(""+fragmentId,rsGroups.getInt(2)+"");
          }
          rsGroups.close();
        }

        if ( psGroups != null ) psGroups.close();

        Statement stmt = con.createStatement();

        // we have to delete all the records from up_layout_struct_aggr table related to the pushed fragments that an user is not allowed to have
        if ( pushFragmentIds != null ) {
         String deleteQuery = "DELETE FROM up_layout_struct_aggr where fragment_id IN " +
          "( SELECT uof.fragment_id FROM up_owner_fragment uof, up_layout_struct_aggr uls WHERE uls.fragment_id != NULL AND " +
          "uof.fragment_id = uls.fragment_id AND uof.pushed_fragment='Y' AND uls.fragment_id NOT IN ("+pushFragmentIds+") )";
         stmt.executeUpdate(deleteQuery);
        }

      // A separate statement is needed so as not to interfere with ResultSet
      // of statements used for queries
      Statement insertStmt = con.createStatement();

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
              rs.getString("STRUCT_ID")+"," +
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
        } // end if layoutID == null

        psRestrLayout =
                    con.prepareStatement("SELECT restriction_type, restriction_value, restriction_tree_path FROM up_layout_restrictions "+
                                      "WHERE layout_id="+layoutId+" AND user_id="+userId+" AND node_id=?");

        psRestrFragment =
                    con.prepareStatement("SELECT restriction_type, restriction_value, restriction_tree_path FROM up_fragment_restrictions "+
                                      "WHERE fragment_id=? AND node_id=?");

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

        // Setting the first layout node ID to the root folder
        rootNode.setFirstChildNodeId(firstStructId+"");

        //Assigning the root folder!!
        ALFolderDescription rootDescription=new ALFolderDescription(); rootDescription.setId(AggregatedUserLayoutImpl.ROOT_FOLDER_ID); rootDescription.setName("root");
        rootNode.setNodeDescription(rootDescription);
        // Putting the root node
        layout.put(AggregatedUserLayoutImpl.ROOT_FOLDER_ID,rootNode);
         // Putting the lost folder
        layout.put(IALFolderDescription.LOST_FOLDER_ID,ALFolder.createLostFolder());

        // layout query
        String sqlLayout = "SELECT ULS.NODE_ID,ULS.NEXT_NODE_ID,ULS.CHLD_NODE_ID,ULS.PREV_NODE_ID,ULS.PRNT_NODE_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
          "ULS.UNREMOVABLE,ULS.IMMUTABLE,ULS.PRIORITY,ULS.FRAGMENT_ID,ULS.FRAGMENT_NODE_ID";
        if (RDBMServices.supportsOuterJoins) {
          sqlLayout += ",USP.STRUCT_PARM_NM,USP.STRUCT_PARM_VAL FROM " + RDBMServices.joinQuery.getQuery("layout_aggr");
        } else {
          sqlLayout += " FROM UP_LAYOUT_STRUCT_AGGR ULS WHERE ";
        }
        sqlLayout += " ULS.USER_ID=? AND ULS.LAYOUT_ID=?";

        PreparedStatement psLayout = con.prepareStatement(sqlLayout);
        psLayout.setInt(1,userId);
        psLayout.setInt(2,layoutId);

        // The query for getting information of the fragments
        String sqlFragment = "SELECT DISTINCT UF.NODE_ID,UF.NEXT_NODE_ID,UF.CHLD_NODE_ID,UF.PREV_NODE_ID,UF.PRNT_NODE_ID,UF.CHAN_ID,UF.NAME,UF.TYPE,UF.HIDDEN,"+
          "UF.UNREMOVABLE,UF.IMMUTABLE,UF.PRIORITY,UF.FRAGMENT_ID";
        if (RDBMServices.supportsOuterJoins) {
          sqlFragment += ",UFP.PARAM_NAME,UFP.PARAM_VALUE FROM up_layout_struct_aggr ULS, " + fragmentJoinQuery;
        } else {
          sqlFragment += " FROM up_fragments UF, up_layout_struct_aggr ULS WHERE ";
        }
        sqlFragment += " UF.fragment_id=ULS.fragment_id" + ((pushFragmentIds!=null)?" OR UF.fragment_id IN ("+pushFragmentIds+")":"");
        PreparedStatement psFragment = con.prepareStatement(sqlFragment);
        System.out.println( "query: " + sqlFragment );
        //psFragment.setInt(1,userId);
        //psFragment.setInt(2,layoutId);

        // Getting fragment nodes query
        //String psFragment

        //StringBuffer structParms = null;
        //List chanIds = null;

        // The hashtable object containing the fragment nodes that are next to the user layout nodes
        Hashtable fragmentNodes = new Hashtable();

        int count = 0;
        for ( PreparedStatement ps = psLayout; count < 2; ps = psFragment, count++ ) {

         List chanIds = Collections.synchronizedList(new ArrayList());
         //LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
         StringBuffer structParms = new StringBuffer();


         rs = ps.executeQuery();

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

              int fragmentId = rs.getInt(13);
              int fragmentNodeId = ( ps.equals(psLayout) )?rs.getInt(14):0;

              IALNodeDescription nodeDesc= null;
              // Trying to get the node if it already exists
              //ALNode node = (ALNode) layout.get(structId+"");
              ALNode node;
              String childIdStr = null;
              if ( childId != 0 || fragmentNodeId > 0 ) {
                //if ( node == null )
                node = new ALFolder();
                IALFolderDescription folderDesc = new ALFolderDescription();
                childIdStr = ( fragmentId > 0 && fragmentNodeId <= 0 )?(fragmentId+":"+childId):(childId+"");
                ((ALFolder)node).setFirstChildNodeId(childIdStr);
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
                 node = new ALNode();
                 ALChannelDescription channelDesc = new ALChannelDescription();
                 //System.out.println( "5" );
                 channelDesc.setChannelPublishId(rs.getString(6));
                 nodeDesc = channelDesc;
                }

              //System.out.println( "6" );

              // Setting node description attributes
              if ("folder".equalsIgnoreCase(node.getNodeType()))
                 nodeDesc.setName(rs.getString(7));
              nodeDesc.setHidden(("Y".equalsIgnoreCase(rs.getString(9))?true:false));
              nodeDesc.setImmutable(("Y".equalsIgnoreCase(rs.getString(11))?true:false));
              nodeDesc.setUnremovable(("Y".equalsIgnoreCase(rs.getString(10))?true:false));
              node.setPriority(rs.getInt(12));


              nodeDesc.setFragmentId((fragmentId>0)?fragmentId+"":null);

              if ( ps.equals(psLayout) ) {
               nodeDesc.setFragmentNodeId((fragmentNodeId>0)?fragmentNodeId+"":null);
              }

              // Setting the node id
              if ( fragmentId > 0 && fragmentNodeId <= 0 )
               nodeDesc.setId(fragmentId+":"+structId);
              else
               nodeDesc.setId((structId!=LOST_NODE_ID)?(structId+""):IALFolderDescription.LOST_FOLDER_ID);

              // Setting the next node id
              if ( nextId != 0 ) {
                 //node.setNextNodeId((nextId!=LOST_NODE_ID)?(nextId+""):IALFolderDescription.LOST_FOLDER_ID);
               String nextIdStr = ( fragmentId > 0 && fragmentNodeId <= 0 )?(fragmentId+":"+nextId):(nextId+"");
               node.setNextNodeId(nextIdStr);
              }

              String parentId;
              switch ( prntId ) {
               case 0:
                               parentId = AggregatedUserLayoutImpl.ROOT_FOLDER_ID;
                               break;
               case LOST_NODE_ID:
                               parentId = IALFolderDescription.LOST_FOLDER_ID;
                               break;
               default:
                               parentId = ( fragmentId > 0 && fragmentNodeId <= 0 )?(fragmentId+":"+prntId):(prntId+"");

              }

              // Setting up the parent id
              node.setParentNodeId(parentId);

               /*  //Setting the current node to the parent
                 ALFolder parentFolder = (ALFolder) layout.get(node.getParentNodeId());
                 // If parent node is null we have to create the new node and put it into the layout hashtable
                 if ( parentFolder == null ) {
                   parentFolder = new ALFolder();
                   layout.put(node.getParentNodeId(),parentFolder);
                 }
                  parentFolder.addChildNode(structId+"");
             */

              // Setting the previous node id
              if ( prevId != 0 ) {
                //node.setPreviousNodeId((prevId!=LOST_NODE_ID)?(prevId+""):IALFolderDescription.LOST_FOLDER_ID);
               String prevIdStr = ( fragmentId > 0 && fragmentNodeId <= 0 )?(fragmentId+":"+prevId):(prevId+"");
               node.setPreviousNodeId(prevIdStr);
              }

              //node.setPriority();
              //node.setRestrictions();

              //System.out.println( "8" );

              /*ls = new LayoutStructure(structId, nextId, childId, chanId, rs.getString(8),rs.getString(8),rs.getString(9));
              layoutStructure.put(new Integer(structId), ls);*/
              lastStructId = structId;


            String fragmentNodeIdStr = nodeDesc.getFragmentNodeId();
            String fragmentIdStr = nodeDesc.getFragmentId();
            String nodeIdStr = structId+"";
            String key = fragmentId+":"+structId;

              // Putting the node into the layout hashtable with an appropriate key
              node.setNodeDescription(nodeDesc);
              if ( fragmentNodeIdStr != null ) {
               fragmentNodes.put(fragmentIdStr+":"+fragmentNodeIdStr,node);
              } else {
                  if ( fragmentIdStr != null && fragmentNodes.containsKey(key) ) {
                    ALNode fragNode = (ALNode) fragmentNodes.get(key);
                    //Setting the actual node ID
                    nodeDesc.setId(fragNode.getId());
                    nodeDesc.setFragmentNodeId(fragNode.getFragmentNodeId());
                    fragNode.setNodeDescription(nodeDesc);
                    if ( "folder".equalsIgnoreCase(fragNode.getNodeType()) ) {
                     ((ALFolder)fragNode).setFirstChildNodeId(childIdStr);
                    }
                    layout.put(nodeDesc.getId(),fragNode);
                  } else
                      layout.put(nodeDesc.getId(),node);
                }

              // If there is a channel we need to get its parameters
              IALChannelDescription channelDesc = null;
              if ("channel".equalsIgnoreCase(node.getNodeType())) {
                channelDesc = (IALChannelDescription) nodeDesc;
                chanIds.add(nodeDesc.getId());
              }

              int index = (ps.equals(psLayout))?15:14;

              if (RDBMServices.supportsOuterJoins) {
                do {
                  String name = rs.getString(index);
                  String value = rs.getString(index+1); // Oracle JDBC requires us to do this for longs
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

             // getting restrictions for the nodes
             PreparedStatement psRestr = null;
             if ( ps.equals(psLayout) ) {

              psRestrLayout.setInt(1,CommonUtils.parseInt(nodeDesc.getId()));
              psRestr = psRestrLayout;
             } else {
                psRestrFragment.setInt(1,CommonUtils.parseInt(nodeDesc.getFragmentId()));
                psRestrFragment.setInt(2,CommonUtils.parseInt(nodeDesc.getId()));
                psRestr = psRestrFragment;
               }
                ResultSet rsRestr = psRestr.executeQuery();
                while (rsRestr.next()) {
                  int restrType = rsRestr.getInt(1);
                  String restrExp = rsRestr.getString(2);
                  String restrPath = rsRestr.getString(3);
                  IUserLayoutRestriction restriction = UserLayoutRestrictionFactory.createRestriction(restrType,restrExp,restrPath);
                  nodeDesc.addRestriction(restriction);
                }
                rsRestr.close();


                // Setting up the priority values based on the appropriate priority restrictions
                PriorityRestriction priorityRestriction = AggregatedUserLayoutImpl.getPriorityRestriction(node);
                if ( priorityRestriction != null ) {
                 int priority = node.getPriority();
                 int[] range = priorityRestriction.getRange();

                 int newPriority = priority;
                 if ( range[0] > priority )
                     newPriority = CommonUtils.max(range[0],priority);
                 else if ( range[1] < priority )
                     newPriority = CommonUtils.min(range[1],priority);

                 // Changing the node priority if it's been changed
                 if ( newPriority != priority )
                 node.setPriority(newPriority);
                }


            } // while

            if ( psRestrLayout != null ) psRestrLayout.close();
            if ( psRestrFragment != null ) psRestrFragment.close();
          }
        } finally {
          rs.close();
        }


        // We have to retrieve the channel defition after the layout structure
        // since retrieving the channel data from the DB may interfere with the
        // layout structure ResultSet (in other words, Oracle is a pain to program for)
        if (chanIds.size() > 0) {
          //RDBMServices.PreparedStatement pstmtChannel = crs.getChannelPstmt(con);
          //try {
            //RDBMServices.PreparedStatement pstmtChannelParm = crs.getChannelParmPstmt(con);
            //try {
              // Pre-prime the channel pump
              System.out.println( layout.toString() );
              for (int i = 0; i < chanIds.size(); i++) {


                String key = (String) chanIds.get(i);
                ALNode node = (ALNode) layout.get(key);

                //IALChannelDescription channelDesc = (IALChannelDescription) node.getNodeDescription();
                //ChannelDefinition channelDef = crs.getChannel(Integer.parseInt(channelDesc.getChannelPublishId()), true, pstmtChannel, pstmtChannelParm);

                IALChannelDescription channelDesc = (IALChannelDescription) node.getNodeDescription();
                ChannelDefinition channelDef = crs.getChannelDefinition(CommonUtils.parseInt(channelDesc.getChannelPublishId()));

                if ( channelDef != null ) {

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


                 for ( int j = 0; j < channelParams.length; j++ ) {
                  String paramName = channelParams[j].getName();
                  if ( channelDesc.getParameterValue(paramName) == null ) {
                   channelDesc.setParameterOverride(paramName,channelParams[j].getOverride());
                   channelDesc.setParameterValue(paramName,channelParams[j].getValue());
                  }
                 }
                 channelDesc.setTimeout(channelDef.getTimeout());
                 channelDesc.setTitle(channelDef.getTitle());

                }

              }
            /*} finally {
              if (pstmtChannelParm != null) {
                pstmtChannelParm.close();
              }
            }
          } finally {
            pstmtChannel.close();
          }*/
          chanIds.clear();
        }

        if (!RDBMServices.supportsOuterJoins) { // Pick up structure parameters
          String sql = "SELECT STRUCT_ID, STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId +
            " AND STRUCT_ID IN (" + structParms.toString() + ") ORDER BY STRUCT_ID";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          rs = stmt.executeQuery(sql);
          try {
            if (rs.next()) {
              int structId = rs.getInt(1);
              readParm: while(true) {
                //LayoutStructure ls = (LayoutStructure)layoutStructure.get(new Integer(structId));
                ALNode node = (ALNode) layout.get(structId+"");
                if ( node != null ) {
                 IALChannelDescription channelDesc = (IALChannelDescription) node.getNodeDescription();
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

          ps.close();
       } // End of for

        /*if (layoutStructure.size() > 0) { // We have a layout to work with
          createLayout(layoutStructure, doc, root, firstStructId);
          layoutStructure.clear();
        */


       // Very suspicious place !!!!
       // Check if the node from an user layout points to a fragment node, we have to bind them
       // The loop for all the nodes from the hashtable
       //System.out.println( "Getting layout size: " + layout.size() );
       /*for ( Enumeration nodeIds = layout.keys(); nodeIds.hasMoreElements() ;) {
          String strNodeId = nodeIds.nextElement().toString();
          //System.out.println( "Getting nodeId: " + strNodeId );
          ALNode node = (ALNode) layout.get(strNodeId);
           if ( node.getFragmentId() == null ) {

                  String nextNodeId = node.getNextNodeId();
                  ALNode nextNode = null;
                  if ( nextNodeId != null ) nextNode =  (ALNode) layout.get(nextNodeId);

                  String prevNodeId = node.getPreviousNodeId();
                  ALNode prevNode = null;
                  if ( prevNodeId != null ) prevNode =  (ALNode) layout.get(prevNodeId);

                  String prntNodeId = node.getParentNodeId();
                  ALNode prntNode = null;
                  if ( prntNodeId != null ) prntNode =  (ALNode) layout.get(prntNodeId);

                  String firstChildId = ("folder".equals(node.getNodeType()))?((ALFolder)node).getFirstChildNodeId():null;
                  ALNode childNode = null;
                  if ( firstChildId != null )
                   childNode = (ALNode)layout.get(firstChildId);

                  if ( nextNode != null && nextNode.getFragmentId() != null ) nextNode.setPreviousNodeId(strNodeId);
                  if ( prevNode != null && prevNode.getFragmentId() != null ) prevNode.setNextNodeId(strNodeId);
                  // if we have the following: node = the first child of his parent what is a fragment node :))
                  if ( prntNode != null && prntNode.getFragmentId() != null && node.getPreviousNodeId() == null )
                     if ( "folder".equals(prntNode.getNodeType()) )
                        ((ALFolder)prntNode).setFirstChildNodeId(strNodeId);
                  // Checking all the children
                  if ( childNode != null ) {
                   for ( String tmpNodeId = childNode.getId(); tmpNodeId != null; ) {
                    ALNode tmpChildNode =  (ALNode) layout.get(tmpNodeId);
                    // if we got tmpChildNode == NULL we have to get out of the loop
                    //if ( tmpChildNode != null ) {
                     if ( tmpChildNode.getFragmentId() != null )
                       tmpChildNode.setParentNodeId(strNodeId);
                     tmpNodeId = tmpChildNode.getNextNodeId();
                    //} else break;
                   }
                  }
            }
       }
         */



        // finding the last node in the sibling line of the root children
        ALNode lastNode = null;
        String nextId = rootNode.getFirstChildNodeId();
        while ( nextId != null ) {
          lastNode = (ALNode)layout.get(nextId);
          nextId = lastNode.getNextNodeId();
        }

        // Binding the push-fragments to the end of the sibling line of the root children
        for ( Enumeration fragmentIds = pushFragmentRoots.keys(); fragmentIds.hasMoreElements() ;) {
              String strFragmentId = fragmentIds.nextElement().toString();
              String strFragmentRootId = pushFragmentRoots.get(strFragmentId).toString();
              ALNode node = (ALNode) layout.get(strFragmentId+":"+strFragmentRootId);
              if ( node != null ) {
                 if ( lastNode != null ) {
                    lastNode.setNextNodeId(node.getId());
                    node.setPreviousNodeId(lastNode.getId());
                 } else
                    rootNode.setFirstChildNodeId(node.getId());

                 node.setParentNodeId(AggregatedUserLayoutImpl.ROOT_FOLDER_ID);
                 lastNode = node;
              }
        }

        for ( Enumeration fragmentNodesEnum = fragmentNodes.keys(); fragmentNodesEnum.hasMoreElements() ;) {
               String key = fragmentNodesEnum.nextElement().toString();
               ALNode node  = (ALNode ) fragmentNodes.get(key);
               if ( "folder".equalsIgnoreCase(node.getNodeType()) ) {
                 for ( String nextIdStr = ((ALFolder)node).getFirstChildNodeId(); nextIdStr != null; ) {
                       ALNode child = (ALNode) layout.get(nextIdStr);
                       System.out.println ( "nextIdStr: " + nextIdStr );
                       System.out.println ( "child: " + child );
                       System.out.println ( "node: " + node );
                       System.out.println ( "node.getId(): " + node.getId() );
                       child.setParentNodeId(node.getId());
                       nextIdStr = child.getNextNodeId();
                 }
               }
        }


          long stopTime = System.currentTimeMillis();
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): Layout document for user " + userId + " took " +
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
