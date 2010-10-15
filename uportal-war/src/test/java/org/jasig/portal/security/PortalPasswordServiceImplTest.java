package org.jasig.portal.security;

import org.junit.Test;

public class PortalPasswordServiceImplTest {

    IPortalPasswordService passwordService = new PortalPasswordServiceImpl();
    
    @Test
    public void testSHAPassword() {
        String encrypted = passwordService.encryptPassword("admin");
        assert encrypted.startsWith("(SHA256)");
        assert passwordService.validatePassword("admin", encrypted);
    }
    
    @Test
    public void testLegacyPassword() {
        assert passwordService.validatePassword("admin", "(MD5)PfgN2CNzDOPAociIqa31KrWXuxHTgLMp");
    }
}
