/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.cusermanager.provider;

import java.io.*;
import java.util.Date;
import java.security.*;

import org.jasig.portal.security.provider.AccountStoreFactory;

/**
 * <p>A utility class that demonstrates changing and locking md5 passwords in
 * the UP_PERSON_DIR table. The program accepts two optional flags <code>-c</code>
 * causes the user to be created if he/she doesn't exist. The <code>-l</code>
 * flag causes the specified user's account to be locked.</p>
 *
 * copied from uportal package path to correct it for use
 * @author smb1@cornell.edu
 * @author Andrew Newman, newman@yale.edu (heavily modified by smb1@cornell.edu)
 * @version $Revision$
 */
class Md5passwd {

  public static final String ACCOUNTLOCK = "*LCK*";

  /**
   * Returns the MD5 encoded password.
   * @param ProposedPassword
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws SQLException
   */
  protected static String encode( String ProposedPassword )
               throws IOException, NoSuchAlgorithmException {

    byte[] hash, rnd = new byte[8], fin = new byte[24];
    Long date = new Long((new Date()).getTime());
    SecureRandom r = new SecureRandom((date.toString()).getBytes());
    MessageDigest md = MessageDigest.getInstance("MD5");

    // Create a password for this user
    if( !ProposedPassword.equals( ACCOUNTLOCK )) {

      r.nextBytes(rnd);
      md.update(rnd);

      hash = md.digest( ProposedPassword.getBytes());
      System.arraycopy(rnd, 0, fin, 0, 8);
      System.arraycopy(hash, 0, fin, 8, 16);
     }else
      fin = ACCOUNTLOCK.getBytes();

    return "(MD5)" + encodeRaw(fin);
  }// Encode

  /**
   * Check entered password against stored password
   * @param uid
   * @param EnteredPassword
   * @return
   * @throws Exception
   */
  protected static boolean verifyPassword( String uid, String EnteredPassword ) throws Exception {

      boolean isauth = false;

      try {

        String acct[] = AccountStoreFactory.getAccountStoreImpl().getUserAccountInformation( uid );
        if( acct[0] != null && !acct[0].equals("")) {

          byte[] whole, salt = new byte[8], compare = new byte[16], dgx;
          whole = decode( acct[0].substring(5) );

          if (whole.length == 24) {

            System.arraycopy(whole, 0, salt, 0, 8);
            System.arraycopy(whole, 8, compare, 0, 16);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt);

            dgx = md.digest( EnteredPassword.getBytes() );

            int i;
            for (i = 0; i < dgx.length; i++) {
              if (dgx[i] != compare[i]) {
                isauth = false;
                break;
              }//if

              isauth = true;
            }// for

          }// if, 24.length
        }// if !null
      } catch (Exception e) {
        throw  (e);
       }

     return isauth;
  }// verifyPassword

//
// This was originally Jonathan B. Knudsen's Example from his book
// Java Cryptography published by O'Reilly Associates (1st Edition 1998)
//

  private static String encodeRaw(byte[] raw) {
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

  //
  // This was originally Jonathan B. Knudsen's Example from his book
  // Java Cryptography published by O'Reilly Associates (1st Edition 1998)
  //
  protected static byte[] decode(String base64) {
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

}// eoc
