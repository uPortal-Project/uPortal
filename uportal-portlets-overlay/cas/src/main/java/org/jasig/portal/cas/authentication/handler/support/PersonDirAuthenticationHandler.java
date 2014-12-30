/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasypt.digest.config.SimpleDigesterConfig;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;

/**
 * Impl of the uPortal MD5 password checking algorithm
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonDirAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final String MD5_PREFIX = "(MD5)";
    
    private static final String SHA256_PREFIX = "(SHA256)";

    private UserPasswordDao userPasswordDao;
    
    private ConfigurablePasswordEncryptor md5Encryptor;    
    private ConfigurablePasswordEncryptor sha256Encryptor;
    
    public PersonDirAuthenticationHandler() {
        /*
         * Create an MD5 password encryptor that uses an 8-byte salt with one
         * hash iteration.  This encryptor should be  capable of validating 
         * legacy uPortal passwords. 
         */
        md5Encryptor = new ConfigurablePasswordEncryptor();
        SimpleDigesterConfig md5Config = new SimpleDigesterConfig();
        md5Config.setIterations(1);
        md5Config.setAlgorithm("MD5");
        md5Config.setSaltSizeBytes(8);
        md5Encryptor.setConfig(md5Config);
        
        /*
         * Create a stronger SHA-256 password encryptor for setting and 
         * validating new passwords.
         */
        sha256Encryptor = new ConfigurablePasswordEncryptor();
        SimpleDigesterConfig shaConfig = new SimpleDigesterConfig();
        shaConfig.setIterations(1000);
        shaConfig.setAlgorithm("SHA-256");
        shaConfig.setSaltSizeBytes(8);
        sha256Encryptor.setConfig(shaConfig);
    }
    
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
        final String cleartextPassword = credentials.getPassword();
        
        final String expectedFullHash = this.userPasswordDao.getPasswordHash(username);
        
        if (expectedFullHash == null) {
        	 return false;
        }
        
        if (expectedFullHash.startsWith(MD5_PREFIX)) {
        	
        	String hashWithoutAlgorithmPrefix = expectedFullHash.substring(5);
            return md5Encryptor.checkPassword(cleartextPassword, hashWithoutAlgorithmPrefix);
        	
        } else if (expectedFullHash.startsWith(SHA256_PREFIX)) {
        	
        	String hashWithoutAlgorithmPrefix = expectedFullHash.substring(8);
            return sha256Encryptor.checkPassword(cleartextPassword, hashWithoutAlgorithmPrefix);
        
        } else {
            this.log.error("Existing password hash for user '" + username + "' is not a valid hash. It does not start with a supported algorithm prefix");
            return false;
        }
        
        
       
    }

    
}
