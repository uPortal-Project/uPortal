/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    
    public void testNullPassword() throws Exception {
        final UserPasswordDao userPasswordDao = EasyMock.createMock(UserPasswordDao.class);
        EasyMock.expect(userPasswordDao.getPasswordHash("admin")).andReturn(null);
        
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
