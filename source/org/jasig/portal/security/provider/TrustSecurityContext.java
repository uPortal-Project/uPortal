package org.jasig.portal.security.provider;

import org.jasig.portal.security.*;
import org.jasig.portal.Logger;
import org.jasig.portal.RdbmServices;
import java.util.*;
import java.sql.*;

/**
 * <p>This is an implementation of a SecurityContext that merely checks to see
 * if the user exists in the portal_users database table but otherwise presumes
 * to be pre-authenticated by the context from which it is called. The typical
 * system where this might be used is a portal whose main page is protected by
 * HTTP authentication (BASIC or otherwise).</p>
 *
 * <p>Copyright (c) Yale University 2000</p>
 *
 * <p>(Note that Yale University intends to relinquish copyright claims to
 * this code once an appropriate JASIG copyright arrangement can be
 * established -ADN)</p>
 *
 * @author Andrew Newman
 * @version $Revision$
 *
 * @author Andrew Newman
 * @version $Revision$
 */

class TrustSecurityContext extends ChainingSecurityContext
    implements SecurityContext {

  private final int TRUSTSECURITYAUTHTYPE = 0xFF01;

  TrustSecurityContext() {
    super();
  }

  public int getAuthType() {
    return this.TRUSTSECURITYAUTHTYPE;
  }

  public synchronized void authenticate() {
    this.isauth = false;
    RdbmServices rdbmservices = new RdbmServices();
    if (this.myPrincipal.UID != null) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rset = null;
      try {
        String first_name, last_name;
        String query = "SELECT FIRST_NAME, LAST_NAME " +
            "FROM PORTAL_USERS WHERE USER_NAME = ?";
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
        Logger.log(Logger.ERROR, new PortalSecurityException
          ("SQL Database Error"));
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