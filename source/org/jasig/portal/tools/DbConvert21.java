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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;
import org.springframework.dao.DataAccessException;

/**
 * Adjusts stored layouts in 2.1 database to work in 2.2+.
 * @author Susan Bramhall, susan.bramhall@yale.edu
 * @version $Revision$
 */
public class DbConvert21 {

    private static final Log log = LogFactory.getLog(DbConvert21.class);

   public static void main(String[] args) {

	Statement stmt = null;
	PreparedStatement  stmtTest = null;
	PreparedStatement testStructStmt = null;
	Statement modifyStmt = null;
	ResultSet rset = null;
	ResultSet rsetTest = null;
    Connection con = null;
    int updateCount = 0;

    // the query to select all the layouts that need to be adjusted
	String query =
    "select uul.USER_ID, uul.LAYOUT_ID, max(uls.struct_id)+100 as new_struct_id, " +
    "init_struct_id as new_child_id from up_layout_struct uls, up_user_layout uul " +
	"where uls.user_id=uul.user_id and uls.layout_id=uul.layout_id " +
	"group by uul.user_id, uul.layout_id, init_struct_id";

	String testQuery = "select count(*) as ct from up_layout_struct where type='root' and user_id= ? and layout_id=?";
	String testNextStructId = "SELECT NEXT_STRUCT_ID FROM UP_USER where user_id=? ";

      try {
         con = RDBMServices.getConnection ();
         


		if (RDBMServices.supportsTransactions)
		  con.setAutoCommit(false);

		// Create the JDBC statement
		stmt = con.createStatement();

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
					log.debug("DbConvert21 update: "+updateSsUri);
				}
				ssDescUri = rset.getString(3);
				if (ssDescUri.startsWith("stylesheets/")) {
					newSsUri = ssDescUri.substring("stylesheets/".length());
					updateSsUri = "UPDATE UP_SS_STRUCT set SS_DESCRIPTION_URI = '"+newSsUri+"' "+
						"where SS_ID = "+ssId;
					ssModifyStmt.execute(updateSsUri);
					log.debug("DbConvert21 update: "+updateSsUri);
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
					log.debug("DbConvert21 update: "+updateSsUri);
				}
				ssDescUri = rset.getString(3);
				if (ssDescUri.startsWith("stylesheets/")) {
					newSsUri = ssDescUri.substring("stylesheets/".length());
					updateSsUri = "UPDATE UP_SS_THEME set SS_DESCRIPTION_URI = '"+newSsUri+"' "+
						"where SS_ID = "+ssId;
					ssModifyStmt.execute(updateSsUri);
					log.debug("DbConvert21 update: "+updateSsUri);
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
				stmtTest = con.prepareStatement(testQuery);
				// to test if need to increment next struct id for user
				testStructStmt = con.prepareStatement(testNextStructId);

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
					log.debug("DbConvert inserted: " + insertString);

					String updateString = "UPDATE UP_USER_LAYOUT set INIT_STRUCT_ID="+new_struct_id+
					" where user_id="+user_id + " and layout_id=" + layout_id;
					modifyStmt.execute(updateString);
					log.debug("DbConvert updated layout: " + updateString);

					testStructStmt.clearParameters();
					testStructStmt.setInt(1,user_id);
					ResultSet testNext = testStructStmt.executeQuery();
					int newNext = new_struct_id+1;
					if (testNext.next() && testNext.getInt(1)<=newNext){
						updateString = "UPDATE UP_USER set NEXT_STRUCT_ID = " + newNext +
						" where user_id="+user_id ;
						modifyStmt.execute(updateString);
						log.debug("DbConvert updated next struct id : " + updateString);
					}

					log.debug("DbConvert updated: " + updateString);
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

     } catch (DataAccessException dae) {
        // we know this was thrown by RDBMServices.getConnection().
        System.err.println("Unable to get a database connection");
        return;
     } catch (Exception e) {
        e.printStackTrace();
      }
      finally {
         try {
         	con.commit();
         	RDBMServices.releaseConnection(con); }
         catch (Exception e) {}
      }

      	System.out.println("DbConvert21 updated " + updateCount +" user layouts");
    	return;

   }//end main

}
