/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.tools;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserLayoutStore;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.AggregatedLayout;
import org.jasig.portal.layout.AggregatedLayoutManager;
import org.jasig.portal.layout.AggregatedUserLayoutStore;
import org.jasig.portal.layout.IUserLayoutManager;
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
	   	
		int uid;
		int SimpleProfileId = 1;
		final int NEW_LAYOUT_ID =1;  /* id to use when creating new layout */
		UserProfile simpleProfile = null;
		UserProfile ALProfile = null;
		IUserLayoutManager sulm = null;
		IUserLayoutStore uls = null;
		int structSsId = 0, themeSsId = 0;
		int ALProfileId = 0;
		Connection con = null;
		Statement qstmt = null;
		ResultSet rs = null;
				
	   	if (args.length<1 ) {
	   		System.out.println("Usage \"SimpleLayout2ALIM <userid> [<profileid>]\" \n profileid defaults to 1 ");
	   		return;
	   	}
	   	else {
		uid = Integer.parseInt(args[0]);
		if (args[1]!=null) SimpleProfileId = Integer.parseInt(args[1]);
		}		

		IPerson user = PersonFactory.createPerson();
		user.setID(uid); 
		
		try {
			// read in the simple layout
			uls = new RDBMUserLayoutStore();
			simpleProfile = uls.getUserProfileById(user,SimpleProfileId);
			Document ul = uls.getUserLayout(user,simpleProfile);
			// wow - that was easy.   Now need to get rid of initial letters in IDs.
			stripNodes(ul.getChildNodes().item(0));
			
			// create a profile for the new layout
			
			try {
				con = RDBMServices.getConnection();
				qstmt = con.createStatement();
				
				rs = 
					qstmt.executeQuery("select ss_id from up_ss_struct where ss_uri like '%org/jasig/portal/layout/AL_TabColumn/AL_TabColumn.xsl'");
				if (rs.next()) 		structSsId = rs.getInt(1);
				else {
					System.out.println("No AL structure stylesheet found. \n Layout for user "+uid+" not converted.");
					con.rollback();
					return;
				}
				rs = 
					qstmt.executeQuery("select ss_id from up_ss_theme where ss_uri like '%org/jasig/portal/layout/AL_TabColumn/integratedModes/integratedModes.xsl'");
				if (rs.next()) 		themeSsId = rs.getInt(1);
				else {
					System.out.println("No IM theme stylesheet found. \n Layout for user "+uid+" not converted.");
					con.rollback();
					return;
				}
				
				// create a new profile id
				//rs = qstmt.executeQuery("select max(profile_id) from UP_USER_PROFILE where user_ID ="+ uid);
				//if (rs.next()) ALProfileId = rs.getInt(1)+1;
				
				ALProfile = new UserProfile(ALProfileId, "AL", "AL Profile", NEW_LAYOUT_ID, structSsId, themeSsId);
				
			} catch (SQLException se) {
				System.err.println("Error creating new profile for user "+uid);
				se.printStackTrace();
			}
			finally {
				if (qstmt != null) qstmt.close();
				if (rs != null) rs.close();
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
		System.out.println("Saved aggregated layout for user_id "+uid+" and new profile with id="+ALProfile.getProfileId());
		return;	
	}
	private static void stripNodes (Node node) {
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
}
