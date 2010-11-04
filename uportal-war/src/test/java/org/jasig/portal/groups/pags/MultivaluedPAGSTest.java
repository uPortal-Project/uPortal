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

package org.jasig.portal.groups.pags;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.groups.pags.testers.IntegerLTTester;
import org.jasig.portal.groups.pags.testers.RegexTester;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Test PAGS IPersonTester implementations against multi-valued attributes.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class MultivaluedPAGSTest {
    
    IPerson person;
    
    String strAttributeName = "mail";
    String intAttributeName = "num";
    
    @Before
    public void setUp() {
        person = new PersonImpl();
        person.setUserName("testuser");
        
        List<Object> emailAddresses = new ArrayList<Object>();
        emailAddresses.add("testuser1@somewhere.com");
        emailAddresses.add("testuser1@elsewhere.com");
        person.setAttribute(strAttributeName, emailAddresses);
        
        List<Object> nums = new ArrayList<Object>();
        nums.add("123");
        nums.add("246");
        person.setAttribute(intAttributeName, nums);
    }
    
    @Test
    public void testMultivaluedRegex() {
        IPersonTester tester = new RegexTester(strAttributeName, ".*somewhere.*");
        assertTrue(tester.test(person));
        
        tester = new RegexTester(strAttributeName, ".*nowhere.*");
        assertFalse(tester.test(person));
    }
    
    @Test
    public void testIntegerLT() {
        IPersonTester tester = new IntegerLTTester(intAttributeName, "124");
        assertTrue(tester.test(person));
        
        tester = new IntegerLTTester(intAttributeName, "122");
        assertFalse(tester.test(person));
    }

}
