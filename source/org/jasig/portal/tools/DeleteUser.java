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

package org.jasig.portal.tools;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserIdentityStore;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.AuthorizationException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/**
 * Title:        Delete Portal User
 * Description:  Deletes all traces of a portal user from the database
 * Company:
 * @author  Atul Shenoy and Susan Bramhall
 * @version $Revision$
 */

public class DeleteUser {

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage \"DeleteUser <username>\"");
      return;
    }
    int portalUID=-1;
    IPerson per= new PersonImpl();
    per.setAttribute(per.USERNAME,args[0]);

    IUserIdentityStore rdbmuser = new RDBMUserIdentityStore();

    try {
      portalUID=rdbmuser.getPortalUID(per, false);
    }
    catch(AuthorizationException e){
      System.err.println("Attempting to delete user " +args[0] +" - Authorization exception");
      return;
    }
    try {
    if(portalUID>0)
         rdbmuser.removePortalUID(portalUID);
      else
	 System.err.println("Attempting to delete user " +args[0] +"; unable to find user.");
    }
    catch(Exception e){
      e.printStackTrace();
    }

	 // Still left: bookmarks
	 // No current way to notify channels
	 // So directly access the database
         // This is the wrong way to do this
         // We need a way for channels to remove
         // data for a user when the user is deleted

    String bookmarksSQL="DELETE FROM UPC_BOOKMARKS "
    		      + "WHERE PORTAL_USER_ID=" + portalUID;

    if (portalUID>0) {
      DatabaseMetaData metadata = null;
      Connection con=null;
      try {
           con = RDBMServices.getConnection ();
	   if (con == null) {
                 System.err.println("Unable to get a database connection");
                 return;
           }
          metadata = con.getMetaData();
          String[] names = {"TABLE"};
          ResultSet tableNames = metadata.getTables(null,"%", "UPC_BOOKMARKS", names);

          if (tableNames.next()) {
             Statement stmt = con.createStatement();
             try {
                System.err.println("Deleting bookmarks from UPC_BOOKMARKS");
                // System.err.println("SQL: " +bookmarksSQL);
                boolean b= stmt.execute(bookmarksSQL);
             }
             catch(SQLException e){
                System.err.println("An exception occurred");
                e.printStackTrace();
             }
             finally {
                stmt.close();
             }
          }
       }
       catch (SQLException e) {
          e.printStackTrace();
          if (con != null) {
          RDBMServices.releaseConnection(con);
          }
       }

    }//end if

  }//end main
}
