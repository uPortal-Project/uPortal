/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *u
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

package org.jasig.portal.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.jasig.portal.RDBMServices;

/**
 * <p>A utility class that demonstrates changing and locking md5 passwords in
 * the UP_PERSON_DIR table. The program accepts two optional flags <code>-c</code>
 * causes the user to be created if he/she doesn't exist. The <code>-l</code>
 * flag causes the specified user's account to be locked.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public class md5passwd {

  static private final String SELECTSTMT =
    "SELECT COUNT(*) FROM UP_PERSON_DIR WHERE USER_NAME = ?";
  static private final String UPDATESTMT =
    "UPDATE UP_PERSON_DIR SET ENCRPTD_PSWD = ? WHERE USER_NAME = ?";
  static private final String INSERTSTMT =
    "INSERT INTO UP_PERSON_DIR (USER_NAME, ENCRPTD_PSWD) " +
    "VALUES (?, ?)";

  public md5passwd(String user, boolean create, boolean lock)
      throws IOException, NoSuchAlgorithmException, SQLException {
    byte[] hash, rnd = new byte[8], fin = new byte[24];
    Long date = new Long((new Date()).getTime());
    SecureRandom r = new SecureRandom((date.toString()).getBytes());
    MessageDigest md = MessageDigest.getInstance("MD5");
    RDBMServices rdbm = new RDBMServices();
    Connection conn;
    PreparedStatement stmt;
    ResultSet rset;
    String spass;
    int cnt = 0;

    // Make sure user is specified correctly
    if (user == null || user.trim().length() <= 0) {
      System.out.println("You did not specify a valid user name.  Please try again.");
      System.exit(0);
    }

    // Check to see if the user exists

    conn = rdbm.getConnection();
    stmt = conn.prepareStatement(SELECTSTMT);
    stmt.setString(1, user);
    rset = stmt.executeQuery();
    if (rset.next())
      cnt = rset.getInt(1);
    rset.close();
    stmt.close();
    if (cnt < 1 && create == false) {
      System.out.println("No such user: " + user);
      rdbm.releaseConnection(conn);
      return;
    }


    // Create a password for this user
    if (lock == false) {
      r.nextBytes(rnd);
      md.update(rnd);
      System.out.print("Enter Password for " + user + ": ");
      System.out.flush(); // Needed for prompt to appear when running from Ant.
      BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
      spass = d.readLine();
      hash = md.digest(spass.getBytes());
      System.arraycopy(rnd, 0, fin, 0, 8);
      System.arraycopy(hash, 0, fin, 8, 16);
    }
    else
      fin = "*LCK*".getBytes();

    // Commit it to the database
    if (cnt < 1) {
      stmt = conn.prepareStatement(INSERTSTMT);
      stmt.setString(1, user);
      stmt.setString(2, "(MD5)"+encode(fin));
      stmt.executeUpdate();
    }
    else {
      stmt = conn.prepareStatement(UPDATESTMT);
      stmt.setString(1, "(MD5)"+encode(fin));
      stmt.setString(2, user);
      stmt.executeUpdate();
    }
    stmt.close();
    rdbm.releaseConnection(conn);
    System.out.println("Password Updated...");
    return;
  }

  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, SQLException {
    md5passwd me;

    if (args.length == 1 && args[0].charAt(0) != '-')
      me = new md5passwd(args[0], false, false);
    else if (args.length == 2 && args[0].equals("-c") &&
        args[1].charAt(0) != '-')
      me = new md5passwd(args[1], true, false);
    else if (args.length == 2 && args[0].equals("-l") &&
        args[1].charAt(0) != '-')
      me = new md5passwd(args[1], false, true);
    else {
      System.err.println("Usage \"md5passwd [-c| -l] <user>\"");
      return;
    }
  }

//
// This was originally Jonathan B. Knudsen's Example from his book
// Java Cryptography published by O'Reilly Associates (1st Edition 1998)
//


  private static String encode(byte[] raw) {
    StringBuffer encoded = new StringBuffer();
    for (int i = 0; i < raw.length; i += 3) {
      encoded.append(encodeBlock(raw, i));
    }
    return encoded.toString();
  }

  private static char[] encodeBlock(byte[] raw, int offset) {
    int block = 0;
    int slack = raw.length - offset - 1;
    int end = (slack >= 2) ? 2 : slack;
    for (int i = 0; i <= end; i++) {
      byte b = raw[offset + i];
      int neuter = (b < 0) ? b + 256 : b;
      block += neuter << (8 * (2 - i));
    }
    char[] base64 = new char[4];
    for (int i = 0; i < 4; i++) {
      int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
      base64[i] = getChar(sixbit);
    }
    if (slack < 1) base64[2] = '=';
    if (slack < 2) base64[3] = '=';
    return base64;
  }

  private static char getChar(int sixBit) {
    if (sixBit >= 0 && sixBit <= 25)
      return (char)('A' + sixBit);
    if (sixBit >= 26 && sixBit <= 51)
      return (char)('a' + (sixBit - 26));
    if (sixBit >= 52 && sixBit <= 61)
      return (char)('0' + (sixBit - 52));
    if (sixBit == 62) return '+';
    if (sixBit == 63) return '/';
    return '?';
  }
}
