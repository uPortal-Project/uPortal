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
