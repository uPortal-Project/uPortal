/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import org.jasig.portal.security.*;
import org.jasig.portal.Logger;
import org.jasig.portal.RdbmServices;
import java.util.*;
import java.sql.*;

/**
 * <p>This is an implementation of a SecurityContext that merely checks to see
 * if the user exists in the UP_USERS database table but otherwise presumes
 * to be pre-authenticated by the context from which it is called. The typical
 * system where this might be used is a portal whose main page is protected by
 * HTTP authentication (BASIC or otherwise).</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

class TrustSecurityContext extends ChainingSecurityContext implements ISecurityContext {

  private final int TRUSTSECURITYAUTHTYPE = 0xFF01;

  TrustSecurityContext() {
    super();
  }

  public int getAuthType() {
    return this.TRUSTSECURITYAUTHTYPE;
  }

  public synchronized void authenticate() throws PortalSecurityException{
    this.isauth = false;
    RdbmServices rdbmservices = new RdbmServices();
    if (this.myPrincipal.UID != null) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rset = null;
      try {
        String first_name, last_name;
        String query = "SELECT FIRST_NAME, LAST_NAME " +
            "FROM UP_USERS WHERE USER_NAME = ?";
        conn = rdbmservices.getConnection();
        stmt = conn.prepareStatement(query);
        stmt.setString(1, this.myPrincipal.UID);
        rset = stmt.executeQuery();
        if (rset.next()) {
          first_name = rset.getString("FIRST_NAME");
          last_name  = rset.getString("LAST_NAME");
          this.myPrincipal.FullName = first_name + " " + last_name;
          Logger.log(Logger.INFO, "User " + this.myPrincipal.UID +
              " is authenticated");
          this.isauth = true;
        }
        else
          Logger.log(Logger.INFO, "No such user: " + this.myPrincipal.UID);
      }
      catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException
          ("SQL Database Error");
        Logger.log(Logger.ERROR, ep);
        throw(ep);
      }
      finally {
        try { rset.close(); } catch (Exception e) { }
        try { stmt.close(); } catch (Exception e) { }
        rdbmservices.releaseConnection(conn);
      }
    }
    else
      Logger.log
        (Logger.ERROR, "Principal not initialized prior to authenticate");

    // Ok...we are now ready to authenticate all of our subcontexts.

    super.authenticate();
    return;
  }
}