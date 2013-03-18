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
package org.jasig.portal.version.om;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

public class VersionFieldTest {
    @Test
    public void testIsLessImportantThanLocal() {
        try {
            Version.Field.LOCAL.isLessImportantThan(null);
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        
        assertFalse(Version.Field.LOCAL.isLessImportantThan(Version.Field.LOCAL));
        assertTrue(Version.Field.LOCAL.isLessImportantThan(Version.Field.PATCH));
        assertTrue(Version.Field.LOCAL.isLessImportantThan(Version.Field.MINOR));
        assertTrue(Version.Field.LOCAL.isLessImportantThan(Version.Field.MAJOR));
    }

    @Test
    public void testIsLessImportantThanPatch() {
        try {
            Version.Field.PATCH.isLessImportantThan(null);
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        
        assertFalse(Version.Field.PATCH.isLessImportantThan(Version.Field.LOCAL));
        assertFalse(Version.Field.PATCH.isLessImportantThan(Version.Field.PATCH));
        assertTrue(Version.Field.PATCH.isLessImportantThan(Version.Field.MINOR));
        assertTrue(Version.Field.PATCH.isLessImportantThan(Version.Field.MAJOR));
    }

    @Test
    public void testIsLessImportantThanMinor() {
        try {
            Version.Field.MINOR.isLessImportantThan(null);
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        
        assertFalse(Version.Field.MINOR.isLessImportantThan(Version.Field.LOCAL));
        assertFalse(Version.Field.MINOR.isLessImportantThan(Version.Field.PATCH));
        assertFalse(Version.Field.MINOR.isLessImportantThan(Version.Field.MINOR));
        assertTrue(Version.Field.MINOR.isLessImportantThan(Version.Field.MAJOR));
    }

    @Test
    public void testIsLessImportantThanMajor() {
        try {
            Version.Field.MAJOR.isLessImportantThan(null);
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        
        assertFalse(Version.Field.MAJOR.isLessImportantThan(Version.Field.LOCAL));
        assertFalse(Version.Field.MAJOR.isLessImportantThan(Version.Field.PATCH));
        assertFalse(Version.Field.MAJOR.isLessImportantThan(Version.Field.MINOR));
        assertFalse(Version.Field.MAJOR.isLessImportantThan(Version.Field.MAJOR));
    }

    @Test
    public void testGetLessImportant() {
        assertEquals(Version.Field.MINOR, Version.Field.MAJOR.getLessImportant());
        assertEquals(Version.Field.PATCH, Version.Field.MINOR.getLessImportant());
        assertEquals(Version.Field.LOCAL, Version.Field.PATCH.getLessImportant());
        assertNull(Version.Field.LOCAL.getLessImportant());
        
    }

    @Test
    public void testGetMoreImportant() {
        assertNull(Version.Field.MAJOR.getMoreImportant());
        assertEquals(Version.Field.MAJOR, Version.Field.MINOR.getMoreImportant());
        assertEquals(Version.Field.MINOR, Version.Field.PATCH.getMoreImportant());
        assertEquals(Version.Field.PATCH, Version.Field.LOCAL.getMoreImportant());
        
    }
}
