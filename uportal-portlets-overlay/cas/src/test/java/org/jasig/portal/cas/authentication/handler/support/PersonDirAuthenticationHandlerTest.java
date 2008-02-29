/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.cas.authentication.handler.support;

import org.easymock.EasyMock;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonDirAuthenticationHandlerTest extends TestCase {
    public void testValidPassword() throws Exception {
        final UserPasswordDao userPasswordDao = EasyMock.createMock(UserPasswordDao.class);
        EasyMock.expect(userPasswordDao.getPasswordHash("admin")).andReturn("(MD5)OP2Z89LDMIY6gHAwfoFPRSQWDl5Z16Vt");
        
        final PersonDirAuthenticationHandler authenticationHandler = new PersonDirAuthenticationHandler();
        authenticationHandler.setUserPasswordDao(userPasswordDao);
        
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("admin");
        credentials.setPassword("admin");
        
        EasyMock.replay(userPasswordDao);
        
        final boolean auth = authenticationHandler.authenticateUsernamePasswordInternal(credentials);
        
        EasyMock.verify(userPasswordDao);
        
        assertTrue(auth);
    }
    
    public void testInvalidPassword() throws Exception {
        final UserPasswordDao userPasswordDao = EasyMock.createMock(UserPasswordDao.class);
        EasyMock.expect(userPasswordDao.getPasswordHash("admin")).andReturn("(MD5)OP2Z89LDMIY5gHAwfoFPRSQWDl5Z16Vt");
        
        final PersonDirAuthenticationHandler authenticationHandler = new PersonDirAuthenticationHandler();
        authenticationHandler.setUserPasswordDao(userPasswordDao);
        
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("admin");
        credentials.setPassword("admin");
        
        EasyMock.replay(userPasswordDao);
        
        final boolean auth = authenticationHandler.authenticateUsernamePasswordInternal(credentials);
        
        EasyMock.verify(userPasswordDao);
        
        assertFalse(auth);
    }
}
