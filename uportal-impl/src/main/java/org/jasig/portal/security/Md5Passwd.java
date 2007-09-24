/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
public class Md5Passwd {

  static private final String SELECTSTMT =
    "SELECT COUNT(*) FROM UP_PERSON_DIR WHERE USER_NAME = ?";
  static private final String UPDATESTMT =
    "UPDATE UP_PERSON_DIR SET ENCRPTD_PSWD = ? WHERE USER_NAME = ?";
  static private final String INSERTSTMT =
    "INSERT INTO UP_PERSON_DIR (USER_NAME, ENCRPTD_PSWD) " +
    "VALUES (?, ?)";

  public Md5Passwd(String user, boolean create, boolean lock)
      throws IOException, NoSuchAlgorithmException, SQLException {
    byte[] hash, rnd = new byte[8], fin = new byte[24];
    Long date = new Long((new Date()).getTime());
    SecureRandom r = new SecureRandom((date.toString()).getBytes());
    MessageDigest md = MessageDigest.getInstance("MD5");
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

    conn = RDBMServices.getConnection();
    stmt = conn.prepareStatement(SELECTSTMT);
    stmt.setString(1, user);
    rset = stmt.executeQuery();
    if (rset.next())
      cnt = rset.getInt(1);
    rset.close();
    stmt.close();
    if (cnt < 1 && create == false) {
      System.out.println("No such user: " + user);
      RDBMServices.releaseConnection(conn);
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
	RDBMServices.releaseConnection(conn);
    System.out.println("Password Updated...");
    return;
  }

  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, SQLException {

    if (args.length == 1 && args[0].charAt(0) != '-')
      new Md5Passwd(args[0], false, false);
    else if (args.length == 2 && args[0].equals("-c") &&
        args[1].charAt(0) != '-')
      new Md5Passwd(args[1], true, false);
    else if (args.length == 2 && args[0].equals("-l") &&
        args[1].charAt(0) != '-')
      new Md5Passwd(args[1], false, true);
    else {
      System.err.println("Usage \"Md5Passwd [-c| -l] <user>\"");
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
