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
import java.sql.Statement;

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
	Statement stmtTest = null;
	Statement modifyStmt = null;
	ResultSet rset = null;
	ResultSet rsetTest = null;
    Connection con = null;
    int updateCount = 0;
    
    // the query to select all the layouts that need to be adjusted 
	String query = 
	"select uls.USER_ID, uls.LAYOUT_ID, max(uls.struct_id)+100 new_struct_id, "+
	"init_struct_id new_child_id from up_layout_struct uls, up_user_layout uul " +
	"where uls.user_id=uul.user_id and uls.layout_id=uul.layout_id "+
	"group by uls.user_id, uls.layout_id, init_struct_id";
		
	String testQuery = "select count(*) ct from up_layout_struct where type='root' and user_id=";
	
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
		stmtTest = con.createStatement();
		// Retrieve the USER_ID that is mapped to their portal UID
		try {
		// Execute the query
		rset = stmt.executeQuery(query);
		try {
			// Create a separate statement for inserts so it doesn't
			// interfere with ResultSets
			modifyStmt = con.createStatement();
			
			// Check to see if we've got a result
			while (rset.next())
			{
				int user_id = rset.getInt("USER_ID");
				int layout_id = rset.getInt("LAYOUT_ID");
				int new_struct_id = rset.getInt("new_struct_id");
				int new_child_id = rset.getInt("new_child_id");
				String tq = testQuery + user_id;
				rsetTest = stmtTest.executeQuery(tq);
				if (rsetTest.next() && rsetTest.getInt("ct")>0) {
					System.err.println("DbConvert: root folder already exists.  USER_ID " + user_id + " ignored");
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
		} catch (Exception e) {e.printStackTrace();		}
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
      System.out.println("DbConvert21 updated " + updateCount);
   }//end main

}
