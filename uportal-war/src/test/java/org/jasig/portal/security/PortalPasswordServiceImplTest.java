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


package org.jasig.portal.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PortalPasswordServiceImplTest {

    IPortalPasswordService passwordService = new PortalPasswordServiceImpl();
    
    /**
     * Test that new password encryptions are via SHA256.
     */
    @Test
    public void testSHAPassword() {
        String encrypted = passwordService.encryptPassword("admin");
        
        assertTrue("Expected encrypted password to start with '(SHA256)' but was [" + encrypted + "]", encrypted.startsWith("(SHA256)"));
        assertTrue(passwordService.validatePassword("admin", encrypted));
        
        assertFalse(passwordService.validatePassword("bob", encrypted));
    }
    
    /**
     * Test that validating passwords against the legacy, MD5-encoding of passwords still works.
     */
    @Test
    public void testLegacyPassword() {
        assertTrue(passwordService.validatePassword("admin", "(MD5)PfgN2CNzDOPAociIqa31KrWXuxHTgLMp"));
        assertFalse(passwordService.validatePassword("admin", "(MD5)PfgN2CNzDOPAociIqa31KrWXuxHTgLMz"));
    }
}
