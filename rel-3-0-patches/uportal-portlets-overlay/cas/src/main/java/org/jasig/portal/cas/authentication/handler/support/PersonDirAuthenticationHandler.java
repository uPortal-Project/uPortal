/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.cas.authentication.handler.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Impl of the uPortal MD5 password checking algorithm
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonDirAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final String MD5_PREFIX = "(MD5)";

    private UserPasswordDao userPasswordDao;
    
    /**
     * @return the userPasswordDao
     */
    public UserPasswordDao getUserPasswordDao() {
        return this.userPasswordDao;
    }
    /**
     * @param userPasswordDao the userPasswordDao to set
     */
    public void setUserPasswordDao(UserPasswordDao userPasswordDao) {
        this.userPasswordDao = userPasswordDao;
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler#authenticateUsernamePasswordInternal(org.jasig.cas.authentication.principal.UsernamePasswordCredentials)
     */
    @Override
    protected boolean authenticateUsernamePasswordInternal(UsernamePasswordCredentials credentials) throws AuthenticationException {
        final String username = credentials.getUsername();
        final String expectedFullHash = this.userPasswordDao.getPasswordHash(username);
        
        if (expectedFullHash == null) {
            return false;
        }

        if (!expectedFullHash.substring(0, 5).equals(MD5_PREFIX)) {
            this.log.error("Existing password hash for user '" + username + "' is not a valid hash. It does not start with: '" + MD5_PREFIX + "'");
            return false;
        }
        
        final String expectedHash = expectedFullHash.substring(5);
        final byte[] expectedHashBytes = Base64.decodeBase64(expectedHash.getBytes());
        if (expectedHashBytes.length != 24) {
            this.log.error("Existing password hash for user '" + username + "' is not a valid hash. It has a length of " + expectedHashBytes.length + " but 24 is expected.");
            return false;
        }

        //Split the expected bytes into the salt and actual hashed value.
        final byte[] salt = new byte[8];
        System.arraycopy(expectedHashBytes, 0, salt, 0, 8);
        
        final byte[] expectedPasswordHashBytes = new byte[16];
        System.arraycopy(expectedHashBytes, 8, expectedPasswordHashBytes, 0, 16);
        
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            this.log.error("No 'MD5' MessageDigest algorithm exists.", e);
            return false;
        }
        
        //Hash the salt + entered password
        md.update(salt);
        md.update(credentials.getPassword().getBytes());
        final byte[] passwordHashBytes = md.digest();
        
        return Arrays.equals(expectedPasswordHashBytes, passwordHashBytes);
    }
}
