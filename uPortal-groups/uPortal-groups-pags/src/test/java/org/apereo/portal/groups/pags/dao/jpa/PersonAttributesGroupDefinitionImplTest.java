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
package org.apereo.portal.groups.pags.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.junit.Before;
import org.junit.Test;

public class PersonAttributesGroupDefinitionImplTest {

    PersonAttributesGroupDefinitionImpl a;
    PersonAttributesGroupDefinitionImpl b;
    PersonAttributesGroupDefinitionImpl c;
    PersonAttributesGroupDefinitionImpl d;
    Set<IPersonAttributesGroupDefinition> setOfA;

    @Before
    public void initTestVars() {
        a = new PersonAttributesGroupDefinitionImpl("a", "An a");
        b = new PersonAttributesGroupDefinitionImpl("b", "An b");
        c = new PersonAttributesGroupDefinitionImpl("c", "An c");
        d = new PersonAttributesGroupDefinitionImpl("d", "An d");
        setOfA = new HashSet<>(1);
        setOfA.add(a);
    }

    @Test
    public void testSetMember() {
        b.setMembers(setOfA);
        assertNotNull("member set null", b.getMembers());
        assertEquals("member is not one", 1, b.getMembers().size());
        assertTrue("member is 'a'", b.getMembers().contains(a));
    }

    @Test
    public void testGetDeepMembers() {
        Set<IPersonAttributesGroupDefinition> setOfB = new HashSet<>(1);
        setOfB.add(b);
        b.setMembers(setOfA);
        c.setMembers(setOfB);
        assertNotNull("member set null", b.getDeepMembers());
        assertEquals("member is not 1", 1, b.getDeepMembers().size());
        assertTrue("member is 'a'", b.getDeepMembers().contains(a));
        assertNotNull("member set null", c.getDeepMembers());
        assertEquals("member is not 2", 2, c.getDeepMembers().size());
        assertTrue("member is 'a'", c.getDeepMembers().contains(a));
        assertTrue("member is 'b'", c.getDeepMembers().contains(b));
        Set<IPersonAttributesGroupDefinition> setOfBC = new HashSet<>(1);
        setOfBC.add(b);
        setOfBC.add(c);
        c.setMembers(setOfB);
        d.setMembers(setOfBC);
        assertNotNull("member set null", d.getDeepMembers());
        assertEquals("member is not 3", 3, d.getDeepMembers().size());
        assertTrue("member is 'a'", d.getDeepMembers().contains(a));
        assertTrue("member is 'b'", d.getDeepMembers().contains(b));
        assertTrue("member is 'b'", d.getDeepMembers().contains(c));
    }

    @Test(expected = AssertionError.class)
    public void testExceptionSelfMemberRef() {
        a.setMembers(setOfA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMembersRecursion() {
        Set<IPersonAttributesGroupDefinition> setOfB = new HashSet<>(1);
        setOfB.add(b);
        Set<IPersonAttributesGroupDefinition> setOfC = new HashSet<>(1);
        setOfC.add(c);

        b.setMembers(setOfA);
        c.setMembers(setOfB);
        System.out.println("Count: " + c.getDeepMembers().size());
        System.out.println("Count: " + c.getDeepMembers().size());
        a.setMembers(setOfC);
    }
}
