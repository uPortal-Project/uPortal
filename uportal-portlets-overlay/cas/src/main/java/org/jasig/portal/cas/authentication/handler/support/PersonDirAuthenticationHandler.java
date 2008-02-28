/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.cas.authentication.handler.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonDirAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final String PERSON_DIR_QUERY = "SELECT ENCRPTD_PSWD FROM UP_PERSON_DIR WHERE USER_NAME = ?";

    private DataSource dataSource;
    private SimpleJdbcTemplate simpleJdbcTemplate;

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler#authenticateUsernamePasswordInternal(org.jasig.cas.authentication.principal.UsernamePasswordCredentials)
     */
    @Override
    protected boolean authenticateUsernamePasswordInternal(UsernamePasswordCredentials credentials) throws AuthenticationException {
        final String username = credentials.getUsername();
        final String expectedFullHash = this.simpleJdbcTemplate.queryForObject(PERSON_DIR_QUERY, String.class, username);

        if (!expectedFullHash.substring(0, 5).equals("(MD5)")) {
            this.log.error("Password not an MD5 hash: " + expectedFullHash.substring(0, 5));
            return false;
        }
        
        final String expectedHash = expectedFullHash.substring(5);
        final byte[] expectedHashBytes;
        final byte[] salt = new byte[8];
        final byte[] expectedPasswordHashBytes = new byte[16];
  
        expectedHashBytes = Base64.decodeBase64(expectedHash.getBytes());
//        whole = decode(passwordHash);
        if (expectedHashBytes.length != 24) {
            this.log.info("Invalid MD5 hash length");
            return false;
        }

        //Split the expected bytes into the salt and actual hashed value.
        System.arraycopy(expectedHashBytes, 0, salt, 0, 8);
        System.arraycopy(expectedHashBytes, 8, expectedPasswordHashBytes, 0, 16);
        
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            this.log.warn("No 'MD5' Algorithm exists");
            return false;
        }
        md.update(salt);
        md.update(credentials.getPassword().getBytes());
        final byte[] passwordHashBytes = md.digest();
        
        return Arrays.equals(expectedPasswordHashBytes, passwordHashBytes);
    }
//
//    //
//    // This was originally Jonathan B. Knudsen's Example from his book
//    // Java Cryptography published by O'Reilly Associates (1st Edition 1998)
//    //
//    public static byte[] decode(String base64) {
//        int pad = 0;
//        for (int i = base64.length() - 1; base64.charAt(i) == '='; i--)
//            pad++;
//        int length = base64.length() * 6 / 8 - pad;
//        byte[] raw = new byte[length];
//        int rawIndex = 0;
//        for (int i = 0; i < base64.length(); i += 4) {
//            int block = (getValue(base64.charAt(i)) << 18) + (getValue(base64.charAt(i + 1)) << 12)
//                    + (getValue(base64.charAt(i + 2)) << 6) + (getValue(base64.charAt(i + 3)));
//            for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
//                raw[rawIndex + j] = (byte) ((block >> (8 * (2 - j))) & 0xff);
//            rawIndex += 3;
//        }
//        return raw;
//    }
//
//    protected static int getValue(char c) {
//        if (c >= 'A' && c <= 'Z')
//            return c - 'A';
//        if (c >= 'a' && c <= 'z')
//            return c - 'a' + 26;
//        if (c >= '0' && c <= '9')
//            return c - '0' + 52;
//        if (c == '+')
//            return 62;
//        if (c == '/')
//            return 63;
//        if (c == '=')
//            return 0;
//        return -1;
//    }

}
