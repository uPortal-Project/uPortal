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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;

/**
 * Adjusts stored layouts in 2.1 database to work in 2.2+.
 * @author Susan Bramhall, susan.bramhall@yale.edu
 * @version $Revision$
 */
public class DbConvert21 {

   public static void main(String[] args) {

	Statement stmt = null;
	RDBMServices.PreparedStatement  stmtTest = null;
	RDBMServices.PreparedStatement testStructStmt = null;
	Statement modifyStmt = null;
	ResultSet rset = null;
	ResultSet rsetTest = null;
    Connection con = null;
    int updateCount = 0;
    
    // the query to select all the layouts that need to be adjusted 
	String query =
	"select uul.USER_ID, uul.LAYOUT_ID, max(uls.struct_id)+100 new_struct_id, " +
	"init_struct_id new_child_id from up_layout_struct uls, up_user_layout uul " +
	"where uls.user_id=uul.user_id and uls.layout_id=uul.layout_id " +
	"group by uul.user_id, uul.layout_id, init_struct_id";
	
	String testQuery = "select count(*) ct from up_layout_struct where type='root' and user_id= ? and layout_id=?";
	String testNextStructId = "SELECT NEXT_STRUCT_ID FROM UP_USER where user_id=? ";
	
      try {
         con = RDBMServices.getConnection ();
	     if (con == null) {
            System.err.println("Unable to get a database connection");
            return;
         }
                 
		if (RDBMServices.supportsTransactions)
		  con.setAutoCommit(false);

		// Create the JDBC statement
		stmt = con.createStatement();
		
		// Add CHAN_SECURE column to UP_CHANNEL if not already there
		ResultSet rsMeta = null;
		DatabaseMetaData dbMeta = con.getMetaData();
		try {
			rsMeta = dbMeta.getColumns(null,null,"UP_CHANNEL","CHAN_SECURE");
			if (!rsMeta.next()){
				String alter = "ALTER TABLE UP_CHANNEL ADD (CHAN_SECURE  VARCHAR(1) DEFAULT 'N')";
				con.createStatement().execute(alter);
				System.out.println("Added CHAN_SECURE column to UP_CHANNEL table.");
			}
			else System.out.println("CHAN_SECURE column already exists in UP_CHANNEL table.");
		} catch (SQLException se) {
			System.err.println("Error attempting to add CHAN_SECURE column to UP_CHANNEL table");
			se.printStackTrace();
		}
		try {
			rsMeta = dbMeta.getColumns(null,null,"UP_ENTITY_CACHE_INVALIDATION","ENTITY_CACHE_ID");
			if (!rsMeta.next()){
				String alter = "ALTER TABLE UP_ENTITY_CACHE_INVALIDATION ADD (ENTITY_CACHE_ID  NUMBER NOT NULL)";
				con.createStatement().execute(alter);
				System.out.println("Added ENTITY_CACHE_ID column to UP_ENTITY_CACHE_INVALIDATION table.");
			}
			else System.out.println("ENTITY_CACHE_ID column already exists in UP_ENTITY_CACHE_INVALIDATION table.");
		} catch (SQLException se) {
			System.err.println("Error attempting to add ENTITY_CACHE_ID column to UP_ENTITY_CACHE_INVALIDATION table");
			se.printStackTrace();
		}
		finally {
			if (rsMeta !=null) rsMeta.close();
		}
		
		// change stylesheet URIs to classpath reference for resource manager
		try {
			Statement ssModifyStmt = con.createStatement();  
			rset = stmt.executeQuery("SELECT SS_ID, SS_URI, SS_DESCRIPTION_URI FROM UP_SS_STRUCT ");
			String newSsUri, ssUri, ssDescUri, updateSsUri;
			
			while (rset.next()) {
				int ssId = rset.getInt(1);
				ssUri = rset.getString(2);
				if (ssUri.startsWith("stylesheets/")) {
					newSsUri = ssUri.substring("stylesheets/".length());
					updateSsUri = "UPDATE UP_SS_STRUCT set SS_URI = '"+newSsUri+"' "+
						"where SS_ID = "+ssId;
					ssModifyStmt.execute(updateSsUri);
					LogService.log(LogService.DEBUG,"DbConvert21 update: "+updateSsUri);
				}
				ssDescUri = rset.getString(3);
				if (ssDescUri.startsWith("stylesheets/")) {
					newSsUri = ssDescUri.substring("stylesheets/".length());
					updateSsUri = "UPDATE UP_SS_STRUCT set SS_DESCRIPTION_URI = '"+newSsUri+"' "+
						"where SS_ID = "+ssId;
					ssModifyStmt.execute(updateSsUri);
					LogService.log(LogService.DEBUG,"DbConvert21 update: "+updateSsUri);
				}
			}
			rset = stmt.executeQuery("SELECT SS_ID, SS_URI, SS_DESCRIPTION_URI FROM UP_SS_THEME ");
			while (rset.next()) {
				int ssId = rset.getInt(1);
				ssUri = rset.getString(2);
				if (ssUri.startsWith("stylesheets/")) {
					newSsUri = ssUri.substring("stylesheets/".length());
					updateSsUri = "UPDATE UP_SS_THEME set SS_URI = '"+newSsUri+"' "+ 
						"where SS_ID = "+ssId;
					ssModifyStmt.execute(updateSsUri);
					LogService.log(LogService.DEBUG,"DbConvert21 update: "+updateSsUri);
				}
				ssDescUri = rset.getString(3);
				if (ssDescUri.startsWith("stylesheets/")) {
					newSsUri = ssDescUri.substring("stylesheets/".length());
					updateSsUri = "UPDATE UP_SS_THEME set SS_DESCRIPTION_URI = '"+newSsUri+"' "+
						"where SS_ID = "+ssId;
					ssModifyStmt.execute(updateSsUri);
					LogService.log(LogService.DEBUG,"DbConvert21 update: "+updateSsUri);
				}
				
			}
		}
		catch (SQLException se) {
			System.err.println("Error updating stylesheet Uri");
			se.printStackTrace();
		}
		finally {
			if (rset!=null) rset.close();
			System.out.println("stylesheet references updated.");
		}

		// update layouts to add new folder
		try {
		rset = stmt.executeQuery(query);
			try {
				// Create statements for modifications
				// for updating the layout
				modifyStmt = con.createStatement();  
				// to test if already modfied 
				stmtTest = new RDBMServices.PreparedStatement(con, testQuery); 
				// to test if need to increment next struct id for user
				testStructStmt = new RDBMServices.PreparedStatement(con, testNextStructId);
				
				// loop through returned results 
				while (rset.next())
				{
					int user_id = rset.getInt("USER_ID");
					int layout_id = rset.getInt("LAYOUT_ID");
					int new_struct_id = rset.getInt("new_struct_id");
					int new_child_id = rset.getInt("new_child_id");
					
					stmtTest.clearParameters();
					stmtTest.setInt(1,user_id);
					stmtTest.setInt(2,layout_id);
					rsetTest = stmtTest.executeQuery();
					if (rsetTest.next() && rsetTest.getInt("ct")>0) {
						System.err.println("DbConvert: root folder already exists.  USER_ID " + 
						user_id + ", LAYOUT_ID " + layout_id + " ignored");
					}
					else {
					String insertString = "INSERT INTO UP_LAYOUT_STRUCT ( USER_ID, LAYOUT_ID, STRUCT_ID, "+
		   				"NEXT_STRUCT_ID, CHLD_STRUCT_ID, NAME, TYPE, IMMUTABLE, UNREMOVABLE) VALUES ("+
						user_id+", "+layout_id+", "+new_struct_id+", null, "+new_child_id+
						", 'Root Folder', 'root',	'N', 'Y')" ;
					modifyStmt.execute(insertString);
					// DEBUG
					LogService.log(LogService.DEBUG, "DbConvert inserted: " + insertString);
					
					String updateString = "UPDATE UP_USER_LAYOUT set INIT_STRUCT_ID="+new_struct_id+
					" where user_id="+user_id + " and layout_id=" + layout_id;
					modifyStmt.execute(updateString);
					LogService.log(LogService.DEBUG, "DbConvert updated layout: " + updateString);
					
					testStructStmt.clearParameters();
					testStructStmt.setInt(1,user_id);
					ResultSet testNext = testStructStmt.executeQuery();	
					int newNext = new_struct_id+1;
					if (testNext.next() && testNext.getInt(1)<=newNext){
						updateString = "UPDATE UP_USER set NEXT_STRUCT_ID = " + newNext +
						" where user_id="+user_id ;
						modifyStmt.execute(updateString);
						LogService.log(LogService.DEBUG, "DbConvert updated next struct id : " + updateString);
					}
					
					LogService.log(LogService.DEBUG, "DbConvert updated: " + updateString);
					updateCount++;
					}				
				}
	
				if (RDBMServices.supportsTransactions)
				  con.commit();
	
				} finally {
				stmt.close();
				modifyStmt.close();
				if (stmtTest != null) stmtTest.close();
			} 
		} catch (Exception e) {
			System.err.println("Error attempting to update layouts.");
			e.printStackTrace();		}
		finally {
			rset.close();
			if (rsetTest!=null) rsetTest.close();
		}

     }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
         try { RDBMServices.releaseConnection(con); } 
         catch (Exception e) {}
      }
      System.out.println("DbConvert21 updated " + updateCount +" user layouts");
   }//end main

}
