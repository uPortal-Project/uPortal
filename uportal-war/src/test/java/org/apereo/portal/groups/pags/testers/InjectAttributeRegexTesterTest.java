/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apereo.portal.groups.pags.testers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apereo.portal.groups.pags.IPersonTester;
import org.jasig.portal.security.IPerson;
import org.apereo.portal.security.provider.PersonImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the PAGS testers.
 *
 * @author: Julien Gribonvald
 */
public class InjectAttributeRegexTesterTest {
    private static Class IPERSON_CLASS;
    private Random random = new Random();
    private String[] attributeNames = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
    private String[] randomStrings;
    private String[] randomIntStrings;
    private String key1 = null;
    private String key2 = null;
    private String key3 = null;
    private String missingKey = null;
    private List intStringList = null;
    private List stringList = null;
    private int testListSize = 10;


    /**
     * @return org.apereo.portal.groups.IEntity
     */
    private IPerson getIPerson(String key) {
        IPerson ip = new PersonImpl();
        ip.setAttribute(IPerson.USERNAME, key);
        return  ip;
    }
    /**
     *  @return java.lang.String
     * @param length int
     */
    private String getRandomString(java.util.Random r, int length) {

        char[] chars = new char[length];

        for(int i=0; i<length; i++)
        {
            int diff = ( r.nextInt(25) );
            int charValue =  (int)'A' + diff;
            chars[i] = (char) charValue;
        }
        return new String(chars);
    }

    /**
     */
    @Before
    public void setUp() throws ClassNotFoundException {
        if ( IPERSON_CLASS == null )
        { IPERSON_CLASS = Class.forName("org.jasig.portal.security.IPerson"); }

        randomStrings = new String[100];
        for (int idx=0; idx<100; idx++) {
            int length = random.nextInt(10) + 1;  // between 1 and 11.
            randomStrings[idx] = getRandomString(random, length);
        }

        randomIntStrings = new String[100];
        for (int idx=0; idx<100; idx++)  {
            int randomInt = random.nextInt(99999) + 1;  // between 1 and 100000.
            randomIntStrings[idx] = randomInt + "";
        }

        intStringList = new ArrayList();
        for (int idx=2; idx<testListSize; idx++)
        { intStringList.add(randomIntStrings[idx]); }

        stringList = new ArrayList();
        for (int idx=2; idx<testListSize; idx++)
        { stringList.add(randomStrings[idx]); }

        key1 = attributeNames[0];
        key2 = attributeNames[1];
        key3 = attributeNames[2];
        missingKey = attributeNames[3];
    }

    @Test
    public void testInjectAttributeRegexTester() throws Exception {
        IPerson newPerson = getIPerson("de3");
        Assert.assertNotNull(newPerson);

        // add matching value of Person attribute in key 2
        stringList.add(new String(randomStrings[0] + randomStrings[1] + randomStrings[0]));

        final String key4 = attributeNames[4];
        final String key5 = "ESCOUAICourant";

        newPerson.setAttribute(key1, randomStrings[0]);
        newPerson.setAttribute(key2, randomStrings[1]);
        newPerson.setAttribute(key3, stringList);
        newPerson.setAttribute(key4, null);
        newPerson.setAttribute(key5, "0450822X");

        //  test with an injected attribute not given, so like equals test.  Should return true.
        IPersonTester tester1 = new InjectAttributeRegexTester(key1, randomStrings[0]);

        // test with an injected attributes.  Should return true.
        String testValue2 = randomStrings[0] + "@" + key2 + "@.*";
        IPersonTester tester2 = new InjectAttributeRegexTester(key3, testValue2);

        // test an injected attributes that doesn't exist.  Should return false.
        String testValue3 = randomStrings[0] + "@" + missingKey + "@.*";
        IPersonTester tester3 = new InjectAttributeRegexTester(key3, testValue3);

        //  test an injected attribute with same String.  Should return true.
        String testValue4 = ".*@" + key2 + "@.*";
        IPersonTester tester4 = new InjectAttributeRegexTester(key2, testValue4);

        // test an injected attribute with different String.  Should return false.
        String testValue5 = ".*@" + key2 + "@.*";
        IPersonTester tester5 = new InjectAttributeRegexTester(key1, testValue5);

        // test value on null user attribute value.  Should return false.
        String testValue6 = ".*@" + key2 + "@.*";
        IPersonTester tester6 = new InjectAttributeRegexTester(key4, testValue6);

        Assert.assertTrue(tester1.test(newPerson));
        Assert.assertTrue(tester2.test(newPerson));
        Assert.assertFalse(tester3.test(newPerson));
        Assert.assertTrue(tester4.test(newPerson));
        Assert.assertFalse(tester5.test(newPerson));
        Assert.assertFalse(tester6.test(newPerson));

    }

}
