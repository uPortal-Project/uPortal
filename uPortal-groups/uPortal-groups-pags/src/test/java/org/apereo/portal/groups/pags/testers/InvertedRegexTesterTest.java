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
package org.apereo.portal.groups.pags.testers;

import java.util.ArrayList;
import java.util.List;
import org.apereo.portal.groups.pags.TestPersonAttributesGroupTestDefinition;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.provider.PersonImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InvertedRegexTesterTest {

    IPerson person;
    String strAttributeName = "mail";

    @Before
    public void setUp() {
        person = new PersonImpl();
        person.setUserName("testuser");

        List<Object> emailAddresses = new ArrayList<Object>();
        emailAddresses.add("testuser1@somewhere.com");
        emailAddresses.add("testuser1@elsewhere.com");
        person.setAttribute(strAttributeName, emailAddresses);
    }

    @Test
    public void testMultivaluedAttrsRegex() {
        InvertedRegexTester tester =
                new InvertedRegexTester(
                        new TestPersonAttributesGroupTestDefinition(
                                strAttributeName, "testuser1@elsewhere.com"));
        Assert.assertFalse(tester.test(person));

        tester =
                new InvertedRegexTester(
                        new TestPersonAttributesGroupTestDefinition(
                                strAttributeName, "testnone@notmatch.com"));
        Assert.assertTrue(tester.test(person));
    }

    @Test
    public void testRegexPatterns() {
        final String fakeAttribute = "fakeAttribute";
        InvertedRegexTester tester =
                new InvertedRegexTester(
                        new TestPersonAttributesGroupTestDefinition(fakeAttribute, "^02([A-D])*"));
        person.setAttribute(fakeAttribute, "02A");
        Assert.assertFalse(tester.test(person));
        person.setAttribute(fakeAttribute, "02ABCD");
        Assert.assertFalse(tester.test(person));
        person.setAttribute(fakeAttribute, "A02D");
        Assert.assertTrue(tester.test(person));
        person.setAttribute(fakeAttribute, "02");
        Assert.assertFalse(tester.test(person));
        person.setAttribute(fakeAttribute, "02MisMatch");
        Assert.assertTrue(tester.test(person));
        person.setAttribute(fakeAttribute, "PatternWillNeverMatch");
        Assert.assertTrue(tester.test(person));
    }
}
