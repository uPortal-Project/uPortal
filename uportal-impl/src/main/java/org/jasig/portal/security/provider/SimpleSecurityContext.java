/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.security.MessageDigest;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PortalSecurityException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>This is an implementation of a SecurityContext that checks a user's
 * credentials against an MD5 hashed password entry.
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public class SimpleSecurityContext extends ChainingSecurityContext
    implements ISecurityContext {
    private static final Log log = LogFactory.getLog(SimpleSecurityContext.class);
    
  private final int SIMPLESECURITYAUTHTYPE = 0xFF02;


  SimpleSecurityContext () {
    super();
  }

  public int getAuthType () {
    return  this.SIMPLESECURITYAUTHTYPE;
  }

  /**
   * Authenticate user.
   * @exception PortalSecurityException
   */
  public synchronized void authenticate() throws PortalSecurityException {
    this.isauth = false;
    if (this.myPrincipal.UID != null && this.myOpaqueCredentials.credentialstring != null) {
      String first_name = null, last_name = null, md5_passwd = null;

      try {
        String acct[] = AccountStoreFactory.getAccountStoreImpl().getUserAccountInformation(this.myPrincipal.UID);
        if (acct[0] != null) {

          first_name = acct[1];
          last_name = acct[2];
          md5_passwd = acct[0];
          if (!md5_passwd.substring(0, 5).equals("(MD5)")) {
            log.error( "Password not an MD5 hash: " + md5_passwd.substring(0, 5));
            return;
          }
          String txthash = md5_passwd.substring(5);
          byte[] whole, salt = new byte[8], compare = new byte[16], dgx;
          whole = decode(txthash);
          if (whole.length != 24) {
            log.info( "Invalid MD5 hash value");
            return;
          }
          System.arraycopy(whole, 0, salt, 0, 8);
          System.arraycopy(whole, 8, compare, 0, 16);
          MessageDigest md = MessageDigest.getInstance("MD5");
          md.update(salt);
          dgx = md.digest(this.myOpaqueCredentials.credentialstring);
          boolean same = true;
          int i;
          for (i = 0; i < dgx.length; i++)
            if (dgx[i] != compare[i])
              same = false;
          if (same) {
            this.myPrincipal.FullName = first_name + " " + last_name;
            if (log.isInfoEnabled())
                log.info( "User " + this.myPrincipal.UID + " is authenticated");
            this.isauth = true;
          }
          else
            log.info( "MD5 Password Invalid");
        }
        else {
            if (log.isInfoEnabled())
                log.info( "No such user: " + this.myPrincipal.UID);
        }
      } catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException("SQL Database Error");
        log.error("Error authenticating user", e);
        throw  (ep);
      }
    }
    // If the principal and/or credential are missing, the context authentication
    // simply fails. It should not be construed that this is an error.
    else {
        log.info( "Principal or OpaqueCredentials not initialized prior to authenticate");
    }
    // Ok...we are now ready to authenticate all of our subcontexts.
    super.authenticate();
    return;
  }

  //
  // This was originally Jonathan B. Knudsen's Example from his book
  // Java Cryptography published by O'Reilly Associates (1st Edition 1998)
  //
  public static byte[] decode(String base64) {
    int pad = 0;
    for (int i = base64.length() - 1; base64.charAt(i) == '='; i--)
      pad++;
    int length = base64.length()*6/8 - pad;
    byte[] raw = new byte[length];
    int rawIndex = 0;
    for (int i = 0; i < base64.length(); i += 4) {
      int block = (getValue(base64.charAt(i)) << 18) + (getValue(base64.charAt(i + 1)) << 12) + (getValue(base64.charAt(
          i + 2)) << 6) + (getValue(base64.charAt(i + 3)));
      for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
        raw[rawIndex + j] = (byte)((block >> (8*(2 - j))) & 0xff);
      rawIndex += 3;
    }
    return  raw;
  }


  protected static int getValue(char c) {
    if (c >= 'A' && c <= 'Z')
      return  c - 'A';
    if (c >= 'a' && c <= 'z')
      return  c - 'a' + 26;
    if (c >= '0' && c <= '9')
      return  c - '0' + 52;
    if (c == '+')
      return  62;
    if (c == '/')
      return  63;
    if (c == '=')
      return  0;
    return  -1;
  }
}
