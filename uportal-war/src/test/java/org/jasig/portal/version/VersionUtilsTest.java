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
package org.jasig.portal.version;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

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
        assertNull(version.getLocal());
        
        
        version = VersionUtils.parseVersion("4.0.5.3");
        assertNotNull(version);
        assertEquals(4, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(5, version.getPatch());
        assertEquals(Integer.valueOf(3), version.getLocal());
    }
    

    @Test
    public void testVersionUpgrade() {
        Version v1 = VersionUtils.parseVersion("4.0.5");
        Version v2 = VersionUtils.parseVersion("4.0.5");
        
        assertTrue(VersionUtils.canUpdate(v1, v2));
        assertTrue(VersionUtils.canUpdate(v2, v1));
        
        v2 = VersionUtils.parseVersion("4.0.5.1");
        assertTrue(VersionUtils.canUpdate(v1, v2));
        assertFalse(VersionUtils.canUpdate(v2, v1));
        
        v1 = VersionUtils.parseVersion("4.0.5.2");
        v2 = VersionUtils.parseVersion("4.0.5.3");
        assertTrue(VersionUtils.canUpdate(v1, v2));
        assertFalse(VersionUtils.canUpdate(v2, v1));
        
        v2 = VersionUtils.parseVersion("4.0.6");
        assertTrue(VersionUtils.canUpdate(v1, v2));
        assertFalse(VersionUtils.canUpdate(v2, v1));
    }
    

    @Test
    public void testGetMostSpecificMatchingField() {
        Version v1 = VersionUtils.parseVersion("4.0.5");
        Version v2 = VersionUtils.parseVersion("4.0.5");
        
        assertEquals(Version.Field.LOCAL, VersionUtils.getMostSpecificMatchingField(v1, v2));
        assertEquals(Version.Field.LOCAL, VersionUtils.getMostSpecificMatchingField(v2, v1));
        
        v2 = VersionUtils.parseVersion("4.0.5.1");
        assertEquals(Version.Field.PATCH, VersionUtils.getMostSpecificMatchingField(v1, v2));
        assertEquals(Version.Field.PATCH, VersionUtils.getMostSpecificMatchingField(v2, v1));
        
        v1 = VersionUtils.parseVersion("4.0.5.2");
        v2 = VersionUtils.parseVersion("4.0.5.3");
        assertEquals(Version.Field.PATCH, VersionUtils.getMostSpecificMatchingField(v1, v2));
        assertEquals(Version.Field.PATCH, VersionUtils.getMostSpecificMatchingField(v2, v1));
        
        v2 = VersionUtils.parseVersion("4.0.6");
        assertEquals(Version.Field.MINOR, VersionUtils.getMostSpecificMatchingField(v1, v2));
        assertEquals(Version.Field.MINOR, VersionUtils.getMostSpecificMatchingField(v2, v1));
        
        v2 = VersionUtils.parseVersion("4.1.0.0");
        assertEquals(Version.Field.MAJOR, VersionUtils.getMostSpecificMatchingField(v1, v2));
        assertEquals(Version.Field.MAJOR, VersionUtils.getMostSpecificMatchingField(v2, v1));
        
        v2 = VersionUtils.parseVersion("5.6.7");
        assertNull(VersionUtils.getMostSpecificMatchingField(v1, v2));
        assertNull(VersionUtils.getMostSpecificMatchingField(v2, v1));
    }
}
