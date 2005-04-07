/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * JUnit testcase for MultivaluedPersonAttributeUtils.
 * @version $Revision$ $Date$
 */
public class MultivaluedPersonAttributeUtilsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that an attempt to parse a null Map results in an empty Map.
     */
    public void testParseNullMapping() {
        Map emptyMap = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(null);
        assertEquals(Collections.EMPTY_MAP, emptyMap);
    }
    
    /**
     * Test that an attempt to parse a Map with a null key results in
     * IllegalArgumentException.
     */
    public void testNullKeyMapping() {
        Map nullKeyMap = new HashMap();
        nullKeyMap.put("A", "B");
        nullKeyMap.put(null, "wombat");
        
        try {
            MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have rejected map argument containing null key.");
    }
    
    /**
     * Test that an attempt to parse a Map with a null value results in
     * IllegalArgumentException.
     */
    public void testNullValueMapping() {
        Map nullKeyMap = new HashMap();
        nullKeyMap.put("A", "B");
        nullKeyMap.put("wombat", null);
        
        try {
            MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have rejected map argument containing null value.");
    }
    
    /**
     * Test that an attempt to parse a Map with a non-String key results in
     * IllegalArgumentException.
     */
    public void testNonStringKeyMapping() {
        Map nullKeyMap = new HashMap();
        nullKeyMap.put("A", "B");
        nullKeyMap.put(new Date(), "wombat");
        
        try {
            MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have rejected map argument containing non-String key.");
    }
    
    /**
     * Test that an attempt to parse a Map with a non-String, non-Set key results in
     * IllegalArgumentException.
     */
    public void testNonStringNonSetValueMapping() {
        Map nullKeyMap = new HashMap();
        nullKeyMap.put("A", "B");
        nullKeyMap.put("wombat", new ArrayList());
        
        try {
            MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have rejected map argument containing non-String, non-Set value.");
    }
    
    /**
     * Test that an attempt to parse a Map containing as a value a Set containing
     * something other than a String fails with an IllegalArgumentException.
     */
    public void testIllegalContentsInValueSet() {
        Map nullKeyMap = new HashMap();
        nullKeyMap.put("A", "B");
        
        Set badSet = new HashSet();
        badSet.add("goodString");
        badSet.add(new Date());
        badSet.add("anotherGoodString");
        
        nullKeyMap.put("wombat", badSet);
        
        try {
            MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(nullKeyMap);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have rejected Map argument with a value that was a Set containing something other than a String.");
    }
    
    /**
     * Test a mapping for which no change is required.
     */
    public void testSimpleMapping() {
        Map simpleMapping = new HashMap();
        simpleMapping.put("displayName", Collections.singleton("display_name"));
        
        Set uPortalEmailAttributeNames = new HashSet();
        uPortalEmailAttributeNames.add("mail");
        uPortalEmailAttributeNames.add("user.home-info.online.email");
        
        simpleMapping.put("email", uPortalEmailAttributeNames);
        
        assertEquals(simpleMapping, MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping));
        assertNotSame(simpleMapping, MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping));
        
        // test that the returned Map is immutable
        
        Map returnedMap = MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(simpleMapping);
        try {
            returnedMap.put("foo", "bar");
        } catch (UnsupportedOperationException uoe) {
            // good, map was immutable
            return;
        }
        fail("Returned map should have been immutable and thus put should have failed.");
        
    }
    
    /**
     * Test parsing a more complex mapping in which Sets need to be created.
     */
    public void testComplexMapping() {
        Map testMap = new HashMap();
        Map expectedResult = new HashMap();
        
        // we expect translation from Strings to Set containing the String
        testMap.put("display_name", "displayName");
        expectedResult.put("display_name", Collections.singleton("displayName"));
        
        // we expect Sets containing a String to be left alone
        testMap.put("template_name", Collections.singleton("uPortalTemplateUserName"));
        expectedResult.put("template_name", Collections.singleton("uPortalTemplateUserName"));
        
        Set severalAttributes = new HashSet();
        
        severalAttributes.add("user.name.given");
        severalAttributes.add("givenName");
        
        // we expect Sets containing several Strings to be left alone
        testMap.put("given_name", severalAttributes );
        expectedResult.put("given_name", severalAttributes);
        
        assertEquals(expectedResult, MultivaluedPersonAttributeUtils.parseAttributeToAttributeMapping(testMap));
        
    }
    

    /**
     * Test that attempting to add a result to a null map yields IllegalArgumentException.
     */
    public void testAddResultToNullMap() {
        try {
            MultivaluedPersonAttributeUtils.addResult(null, "key", "value");
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Attempting to addResult on a null Map should yield IllegalArgumentException.");
    }
    
    /**
     * Test that attempting to add a result with a null key yields IllegalArgumentException.
     */
    public void testAddResultNullKey() {
        try {
            MultivaluedPersonAttributeUtils.addResult(new HashMap(), null, "value");
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Attempting to add a result with a null key should yield IllegalArgumentException.");
    }
    
    /**
     * Test that attempting to add a result with a null value yields IllegalArgumentException.
     */
    public void testAddResultNullValue() {
        try {
            MultivaluedPersonAttributeUtils.addResult(new HashMap(), "key", null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Attempting to add a result with a null value should yield IllegalArgumentException.");
    }

    /**
     * Test a simple non-colliding add.
     */
    public void testSimpleAdd() {
        Map testMap = new HashMap();
        Map expectedResult = new HashMap();
        expectedResult.put("mail", "andrew.petro@yale.edu");
        
        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", "andrew.petro@yale.edu");
        
        assertEquals(expectedResult, testMap);
        
    }
    
    /**
     * Test that adding a second value for a given attribute converts that
     * attribute value to a List.
     */
    public void testCollidingAdd() {
        Map testMap = new HashMap();
        Map expectedMap = new HashMap();
        List emailAddys = new ArrayList();
        emailAddys.add("andrew.petro@yale.edu");
        emailAddys.add("awp9@pantheon.yale.edu");
        
        expectedMap.put("mail", "andrew.petro@yale.edu");
        
        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", "andrew.petro@yale.edu");
        
        assertEquals(expectedMap, testMap);
        
        expectedMap.put("mail", emailAddys);
        
        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", "awp9@pantheon.yale.edu");
        
        assertEquals(expectedMap, testMap);
        
    }
    
    /**
     * Test adding a List where the current attribute value is not a List.
     */
    public void testAddListToNonList() {
        
        Map testMap = new HashMap();
        Map expectedMap = new HashMap();
        
        testMap.put("mail", "andrew.petro@yale.edu");
        
        List additionalEmails = new ArrayList();
        additionalEmails.add("awp9@pantheon.yale.edu");
        additionalEmails.add("awp9@tp.its.yale.edu");
        
        List expectedList = new ArrayList();
        expectedList.add("andrew.petro@yale.edu");
        expectedList.addAll(additionalEmails);
        
        expectedMap.put("mail", expectedList);
        
        MultivaluedPersonAttributeUtils.addResult(testMap, "mail", additionalEmails);
        
        assertEquals(expectedMap, testMap);
        
    }
    
    /**
     * Test adding a non-List to an attribute that is currently a List.
     */
    public void testAddStringToList() {
        Map testMap = new HashMap();
        Map expectedMap = new HashMap();
        
        Date loginDate = new Date();
        
        expectedMap.put("loginTimes", loginDate);
        
        MultivaluedPersonAttributeUtils.addResult(testMap, "loginTimes", loginDate);
        
        assertEquals(expectedMap, testMap);
        
        Date anotherLoginDate = new Date();
        
        Date yetAnotherLoginDate = new Date();
        
        List dateList = new ArrayList();
        dateList.add( anotherLoginDate);
        dateList.add(yetAnotherLoginDate);
        
        List expectedDateList = new ArrayList();
        expectedDateList.add(loginDate);
        expectedDateList.add(anotherLoginDate);
        expectedDateList.add(yetAnotherLoginDate);
        
        expectedMap.put("loginTimes", expectedDateList);
        
        MultivaluedPersonAttributeUtils.addResult(testMap, "loginTimes", dateList);
        
        assertEquals(expectedMap, testMap);
    }
    
    /**
     * Test that attempting to flatten a null Collection yields
     * IllegalArgumentException.
     */
    public void testFlattenNullCollection() {
        try {
            MultivaluedPersonAttributeUtils.flattenCollection(null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Attempt to flatten a null collection should yield IllegalArgumentException.");
    }
    
    /**
     * Test flattening a Collection containing collections (and collections of collections).
     *
     */
    public void testFlattenCollection() {
        Set setOfSets = new HashSet();
        Set setOfLists = new HashSet();
        List listOfStrings = new ArrayList();
        listOfStrings.add("wombat");
        listOfStrings.add("fido");
        listOfStrings.add("foo");
        listOfStrings.add("bar");
        
        List listOfDates = new ArrayList();
        Date date1 = new Date();
        Date date2 = new Date();
        // ensure that date2 does not equal date1.
        date2.setTime(date1.getTime() + 100);
        
        listOfDates.add(date1);
        listOfDates.add(date2);
        
        setOfLists.add(listOfStrings);
        setOfLists.add(listOfDates);
        
        setOfSets.add(setOfLists);
        
        Set expectedResult = new HashSet();
        expectedResult.addAll(listOfStrings);
        expectedResult.addAll(listOfDates);
        
        Collection flattened = MultivaluedPersonAttributeUtils.flattenCollection(setOfSets);
        assertTrue(expectedResult.containsAll(flattened));
        assertTrue(flattened.containsAll(expectedResult));
        assertEquals(expectedResult.size(), flattened.size());
        
    }

}

