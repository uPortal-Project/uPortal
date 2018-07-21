/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.groups;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.apereo.portal.security.IPerson;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.ComplexStubPersonAttributeDao;

/** Testcase for PersonDirNameFinder */
public class PersonDirNameFinderTest extends TestCase {

    /** Test PersonDirNameFinder instance backed by a stub IPersonAttributeDao. */
    PersonDirNameFinder finder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Map<String, List<Object>> userWithDisplayNameAttributes =
                new HashMap<String, List<Object>>();
        userWithDisplayNameAttributes.put("phone", Arrays.asList((Object) "777-7777"));
        userWithDisplayNameAttributes.put("displayName", Arrays.asList((Object) "Display Name"));

        Map<String, List<Object>> userWithEmptyDisplayNameAttributes =
                new HashMap<String, List<Object>>();
        userWithEmptyDisplayNameAttributes.put("phone", Arrays.asList((Object) "888-8888"));
        userWithEmptyDisplayNameAttributes.put("displayName", Arrays.asList((Object) ""));

        Map<String, List<Object>> userWithoutDisplayNameAttributes =
                new HashMap<String, List<Object>>();
        userWithoutDisplayNameAttributes.put("phone", Arrays.asList((Object) "666-6666"));
        userWithoutDisplayNameAttributes.put("givenName", Arrays.asList((Object) "Howard"));

        Map<String, Map<String, List<Object>>> daoBackingMap =
                new HashMap<String, Map<String, List<Object>>>();

        daoBackingMap.put("userWithDisplayName", userWithDisplayNameAttributes);
        daoBackingMap.put("userWithEmptyDisplayName", userWithEmptyDisplayNameAttributes);
        daoBackingMap.put("userWithoutDisplayName", userWithoutDisplayNameAttributes);

        IPersonAttributeDao paDao = new ComplexStubPersonAttributeDao(daoBackingMap);

        this.finder = new PersonDirNameFinder(paDao);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /** Test getting the display name for a user. */
    public void testGetName() {
        assertEquals("Display Name", this.finder.getName("userWithDisplayName"));
    }

    public void testGetNameWhereDisplayNameEmpty() {
        assertEquals("userWithEmptyDisplayName", this.finder.getName("userWithEmptyDisplayName"));
    }

    /** Test that getting the name for a user without a display name returns the uid. */
    public void testGetNameWhereNoDisplayName() {
        assertEquals("userWithoutDisplayName", this.finder.getName("userWithoutDisplayName"));
    }

    /** Test that getting the name for an unknown user returns the uid. */
    public void testGetNameUnknownUser() {
        assertEquals("unknownUser", this.finder.getName("unknownUser"));
    }

    /** Test that PersonDirNameFinders report their type as being IPerson. */
    public void testGetType() {
        assertEquals(IPerson.class, this.finder.getType());
    }
}
