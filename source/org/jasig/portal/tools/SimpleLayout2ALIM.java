/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserLayoutStore;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.AggregatedLayout;
import org.jasig.portal.layout.AggregatedLayoutManager;
import org.jasig.portal.layout.AggregatedUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Converts Simple Layouts to AggregatedLayout / IntegratedMode.
 * A new profile is also created.
 * To use the new layout, an administrator must update the UP_USER_UA_MAP table
 * to map the appropriate media to the new profile
 * @author Susan Bramhall, susan.bramhall@yale.edu
 * @version $Revision$
 */

public class SimpleLayout2ALIM {
    public static void main(String[] args) {
        RDBMServices.setGetDatasourceFromJndi(false); /*don't try jndi when not in web app */        

        if (args.length<1 ) {
            System.out.println("Usage \"SimpleLayout2ALIM [-all] <userid> [<profileid>]\" \n profileid defaults to 1 ");
            return;
	   	}

        SimpleLayout2ALIM converter = new SimpleLayout2ALIM();

        if ("-all".equals(args[0])) {
            converter.convertAllLayouts();
        } else {
            int uid = Integer.parseInt(args[0]);
            int simpleProfileId = 1;
            if (args.length >= 2) {
                simpleProfileId = Integer.parseInt(args[1]);
            }
            converter.convertLayout(uid, simpleProfileId);
        }
    }

    public SimpleLayout2ALIM() {
    }

    public void convertAllLayouts() {
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct p.user_id, p.profile_id ");
        sql.append("from up_user_profile p, up_layout_struct s ");
        sql.append("where p.user_id = s.user_id and p.layout_id = s.layout_id");
        List userProfiles = new ArrayList();
        // db objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        int userId;
        int profileId;

        try {
            conn = RDBMServices.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                userId = Integer.parseInt(rs.getString("user_id"));
                profileId = Integer.parseInt(rs.getString("profile_id"));
                userProfiles.add(new UserProfileInfo(userId, profileId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RDBMServices.closeResultSet(rs);
            RDBMServices.closeStatement(pstmt);
            RDBMServices.releaseConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }

        UserProfileInfo upinfo;
        Iterator itr = userProfiles.iterator();
        while (itr.hasNext()) {
            upinfo = (UserProfileInfo)itr.next();
            convertLayout(upinfo.getUserId(), upinfo.getProfileId());
        }
    }

    public void convertLayout(int uid, int simpleProfileId) {
	   	
        final int NEW_LAYOUT_ID =1;  /* id to use when creating new layout */
        UserProfile simpleProfile = null;
        UserProfile ALProfile = null;
        IUserLayoutStore uls = null;
        int structSsId = 0, themeSsId = 0;
        int ALProfileId = 0;
        Connection con = null;
        Statement qstmt = null;
        ResultSet rs = null;

        IPerson user = PersonFactory.createPerson();
        user.setID(uid); 

        try {
            // read in the simple layout
            uls = new RDBMUserLayoutStore();
            simpleProfile = uls.getUserProfileById(user,simpleProfileId);
            Document ul = uls.getUserLayout(user,simpleProfile);
            // wow - that was easy.   Now need to get rid of initial letters in IDs.
            stripNodes(ul.getChildNodes().item(0));
			
            // create a profile for the new layout
			
            try {
                con = RDBMServices.getConnection();
                qstmt = con.createStatement();

                rs = qstmt.executeQuery(
                    "select ss_id from up_ss_struct where ss_uri like " +
                    "'%org/jasig/portal/layout/AL_TabColumn/AL_TabColumn.xsl'");
                if (rs.next()) {
                    structSsId = rs.getInt(1);
                } else {
                    System.out.println("No AL structure stylesheet found. \n Layout for user "+uid+" not converted.");
                    con.rollback();
                    return;
                }
                rs.close();
                rs = qstmt.executeQuery(
                    "select ss_id from up_ss_theme where ss_uri like " +
                    "'%org/jasig/portal/layout/AL_TabColumn/integratedModes/integratedModes.xsl'");
                if (rs.next()) {
                    themeSsId = rs.getInt(1);
                } else {
                    System.out.println("No IM theme stylesheet found. \n Layout for user "+uid+" not converted.");
                    con.rollback();
                    return;
                }
                rs.close();

                // create a new profile id
                //rs = qstmt.executeQuery("select max(profile_id) from UP_USER_PROFILE where user_ID ="+ uid);
                //if (rs.next()) ALProfileId = rs.getInt(1)+1;

                ALProfile = new UserProfile(ALProfileId, "AL", "AL Profile", NEW_LAYOUT_ID, structSsId, themeSsId);

            } catch (SQLException se) {
                System.err.println("Error creating new profile for user "+uid);
                se.printStackTrace();
            } finally {
                RDBMServices.closeResultSet(rs);
                RDBMServices.closeStatement(qstmt);
                RDBMServices.releaseConnection(con); 
            }

            // new AggregatedLayoutManager to save layout
            AggregatedLayoutManager alm = new AggregatedLayoutManager(user, ALProfile);
            // new AggregatedLayout with root node only to get started
            AggregatedLayout al = new AggregatedLayout("userLayoutRootNode",alm);
            // Give it to the layout manager
            alm.setUserLayout(al);
            // Initialize the layout store
            AggregatedUserLayoutStore als = new AggregatedUserLayoutStore();
            // Set the layout manager to use the new store
            alm.setLayoutStore(als);
            // set the layout to the DOM created from the old simple manager
            alm.setUserLayoutDOM(ul);
            // persist the new layout to the store
            // creates new layout with id 1
            alm.saveUserLayout();
            // add the new profile 
            als.addUserProfile(user,ALProfile);
        }  catch (Exception e) {
            System.out.println("Error saving aggregated layout for user_id "+uid);
            e.printStackTrace();
        }
        System.out.println("Saved aggregated layout for user_id "+uid+
            " and new profile with id="+ALProfile.getProfileId());
        return;	
    }

    private void stripNodes (Node node) {
        NodeList nlist = node.getChildNodes();
        for (int i = 0; i<nlist.getLength(); i++){
            if (nlist.item(i).getAttributes().getNamedItem("ID")!=null) {
                String oldId = nlist.item(i).getAttributes().getNamedItem("ID").getNodeValue();
                Node attrNode  = nlist.item(i).getAttributes().getNamedItem("ID");
                attrNode.setNodeValue(oldId.substring(1));	
            }
            stripNodes(nlist.item(i));
        }
    }

    private class UserProfileInfo {
        int userId;
        int profileId;

        public UserProfileInfo(int userId, int profileId) {
            this.userId = userId;
            this.profileId = profileId;
        }

        public int getUserId() { return userId; }
        public int getProfileId() { return profileId; }
    }
}
