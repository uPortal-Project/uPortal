/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.security.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.services.LogService;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials using JAAS.
 *
 * @author Nathan Jacobs
 * @version $Revision$
 *
 */

class JAASSecurityContext extends ChainingSecurityContext implements ISecurityContext {

  private final int JAASSECURITYAUTHTYPE = 0xFF05;

  JAASSecurityContext() {
    super();
  }

  public int getAuthType() {
    return this.JAASSECURITYAUTHTYPE;
  }

  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = false;
    RDBMServices rdbmservices = new RDBMServices();

    if (this.myPrincipal.UID != null && this.myOpaqueCredentials.credentialstring != null) {

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rset = null;
      String first_name = null, last_name = null;
//      int globalUID;

      try {
        String query = "SELECT ID, FIRST_NAME, LAST_NAME FROM PORTAL_USERS WHERE " +
                       "PORTAL_USERS.USER_NAME = ?";

        conn = rdbmservices.getConnection();
        stmt = conn.prepareStatement(query);
        stmt.setString(1, this.myPrincipal.UID);
        rset = stmt.executeQuery();

        if (rset.next()) {

//          globalUID  = rset.getInt("ID");
          first_name = rset.getString("FIRST_NAME");
          last_name  = rset.getString("LAST_NAME");

          boolean isAuthenticated = false;

          // JAAS Stuff

          LoginContext lc = null;

          lc = new LoginContext("uPortal",
                   new JAASInlineCallbackHandler(
                               this.myPrincipal.UID,
                               (new String(this.myOpaqueCredentials.credentialstring)).toCharArray())); // could not come up w/ a better way to do this

          lc.login();

          // the above will throw an exception if authentication does not succeed

//          this.myPrincipal.globalUID = globalUID;
          this.myPrincipal.FullName = first_name + " " + last_name;
          LogService.log(LogService.INFO, "User " + this.myPrincipal.UID + " is authenticated");
          this.isauth = true;

        } else {
          LogService.log(LogService.INFO, "No such user: " + this.myPrincipal.UID);
        }
      } catch (SQLException e) {
        LogService.log(LogService.ERROR, new PortalSecurityException ("error"));
      } catch (LoginException e) {
        LogService.log(LogService.INFO, "User " + this.myPrincipal.UID + ": invalid password");
        LogService.log(LogService.DEBUG,"LoginException: " + e.getMessage());
      } finally {
        try { rset.close(); } catch (Exception e) { }
        try { stmt.close(); } catch (Exception e) { }
        rdbmservices.releaseConnection(conn);
      }
    } else {
      LogService.log (LogService.ERROR, "Principal or OpaqueCredentials not initialized prior to authenticate");
    }

    // authenticate all subcontexts.
    super.authenticate();

    return;
  }
}
