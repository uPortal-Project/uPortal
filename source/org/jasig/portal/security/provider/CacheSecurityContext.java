package org.jasig.portal.security.provider;

import org.jasig.portal.security.*;
import org.jasig.portal.Logger;
import org.jasig.portal.RdbmServices;
import java.util.*;
import java.security.MessageDigest;
import java.sql.*;

/**
 * <p>This is an implementation of a SecurityContext that performs absolutely
 * NO validation of the Principal but merely caches the claimed password.
 * We implement this to provide the illusion of single-signon but it comes
 * with significant risk. A channel is able to retrieve the originally
 * validated password of passphrase to perform just-in-time validation but the
 * means of validation is now COMPLETELY in the hands of the channel. If the
 * channel utilizes a weak authenticity-checking mechanism and the password is
 * the same as the one that portal users regard as secure, then unbeknownst to
 * the user, their "secure" password is being placed in jeopardy. PLEASE use
 * this SecurityContext implementation sparingly and with your eyes open!</p>
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

class CacheSecurityContext extends ChainingSecurityContext
    implements SecurityContext {

  private final int CACHESECURITYAUTHTYPE = 0xFF03;

  private byte[] cachedcredentials;

  CacheSecurityContext() {
    super();
}

  public int getAuthType() {
    return this.CACHESECURITYAUTHTYPE;
  }

  public synchronized void authenticate() {
    this.isauth = false;
    RdbmServices rdbmservices = new RdbmServices();
    if (this.myPrincipal.UID != null &&
        this.myOpaqueCredentials.credentialstring != null) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rset = null;
      String first_name = null, last_name = null;
      try {
        String query = "SELECT FIRST_NAME, LAST_NAME " +
            "FROM PORTAL_USERS WHERE " +
            "USER_NAME = ?";
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

          // Save our credentials so that the parent's authenticate()
          // method doesn't blow them away.

          this.cachedcredentials =
              new byte[this.myOpaqueCredentials.credentialstring.length];
          System.arraycopy(this.myOpaqueCredentials.credentialstring,
            0, this.cachedcredentials, 0,
            this.myOpaqueCredentials.credentialstring.length);
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
        (Logger.ERROR,
        "Principal or OpaqueCredentials not initialized prior to authenticate");

    // Ok...we are now ready to authenticate all of our subcontexts.

    super.authenticate();
    return;
  }

  /**
   * We need to override this method in order to return a class that implements
   * the NotSoOpaqueCredentals interface.
   */
  public OpaqueCredentials getOpaqueCredentials() {
    if (this.isauth) {
      NotSoOpaqueCredentials oc = new CacheOpaqueCredentials();
      oc.setCredentials(this.cachedcredentials);
      return oc;
    }
    else
      return null;
  }

  /**
   * This is a new implementation of an OpaqueCredentials class that
   * implements the less-opaque NotSoOpaqueCredentials.
   */
  private class CacheOpaqueCredentials
      extends ChainingSecurityContext.ChainingOpaqueCredentials
      implements NotSoOpaqueCredentials {

    public String getCredentials() {
      if (this.credentialstring != null)
        return new String(this.credentialstring);
      else
        return null;
    }
  }
}