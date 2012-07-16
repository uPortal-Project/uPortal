package org.jasig.portal.version;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.jasig.portal.version.om.Version;
import org.junit.Test;

public class VersionUtilsTest {
    @Test
    public void testVersionParsing() {
        Version version = VersionUtils.parseVersion("4.0.5");
        assertNotNull(version);
        assertEquals(4, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(5, version.getPatch());
    }
}
