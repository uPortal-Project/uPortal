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
            log.info( "User " + this.myPrincipal.UID + " is authenticated");
            this.isauth = true;
          }
          else
            log.info( "MD5 Password Invalid");
        }
        else {
            log.info( "No such user: " + this.myPrincipal.UID);
        }
      } catch (Exception e) {
        PortalSecurityException ep = new PortalSecurityException("SQL Database Error");
        log.error( e);
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
