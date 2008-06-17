/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.portal.security.IPerson;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.ComplexStubPersonAttributeDao;

/**
 * Testcase for PersonDirNameFinder
 * @version $Revision$ $Date$
 */
public class PersonDirNameFinderTest extends TestCase {
    
    /**
     * Test PersonDirNameFinder instance backed by a stub 
     * IPersonAttributeDao.
     */
    PersonDirNameFinder finder;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Map<String, List<Object>> userWithDisplayNameAttributes = new HashMap<String, List<Object>>();
        userWithDisplayNameAttributes.put("phone", Arrays.asList((Object)"777-7777"));
        userWithDisplayNameAttributes.put("displayName", Arrays.asList((Object)"Display Name"));
        
        Map<String, List<Object>> userWithEmptyDisplayNameAttributes = new HashMap<String, List<Object>>();
        userWithEmptyDisplayNameAttributes.put("phone", Arrays.asList((Object)"888-8888"));
        userWithEmptyDisplayNameAttributes.put("displayName", Arrays.asList((Object)""));
        
        Map<String, List<Object>> userWithoutDisplayNameAttributes = new HashMap<String, List<Object>>();
        userWithoutDisplayNameAttributes.put("phone", Arrays.asList((Object)"666-6666"));
        userWithoutDisplayNameAttributes.put("givenName", Arrays.asList((Object)"Howard"));
        
        Map<String, Map<String, List<Object>>> daoBackingMap = new HashMap<String, Map<String, List<Object>>>();
        
        daoBackingMap.put("userWithDisplayName", userWithDisplayNameAttributes);
        daoBackingMap.put("userWithEmptyDisplayName", userWithEmptyDisplayNameAttributes);
        daoBackingMap.put("userWithoutDisplayName", userWithoutDisplayNameAttributes);
              
        IPersonAttributeDao paDao = new ComplexStubPersonAttributeDao(daoBackingMap);
        
        this.finder = new PersonDirNameFinder(paDao);
        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test getting the display name for a user.
     */
    public void testGetName() {
       assertEquals("Display Name", this.finder.getName("userWithDisplayName"));
    }

    public void testGetNameWhereDisplayNameEmpty() {
        assertEquals("userWithEmptyDisplayName", this.finder.getName("userWithEmptyDisplayName"));
    }
    
    /**
     * Test that getting the name for a user without a display name returns the 
     * uid.
     */
    public void testGetNameWhereNoDisplayName() {
       assertEquals("userWithoutDisplayName", this.finder.getName("userWithoutDisplayName"));
    }

    /**
     * Test that getting the name for an unknown user returns the uid.
     */
    public void testGetNameUnknownUser() {
        assertEquals("unknownUser", this.finder.getName("unknownUser"));
    }
    
    /**
     * Test getting display name for several users.  Uses individual users tested
     * in other test methods in this testcase.
     */
    public void testGetNames() {
        String[] keys = {"userWithDisplayName", "userWithEmptyDisplayName", "userWithoutDisplayName", "unknownUser"};
        
        Map expected = new HashMap();
        expected.put("userWithDisplayName", "Display Name");
        expected.put("userWithEmptyDisplayName", "userWithEmptyDisplayName");
        expected.put("userWithoutDisplayName", "userWithoutDisplayName");
        expected.put("unknownUser", "unknownUser");
        
        assertEquals(expected, this.finder.getNames(keys));
    }
    
    /**
     * Test that PersonDirNameFinders report their type as being IPerson.
     */
    public void testGetType() {
        assertEquals(IPerson.class, this.finder.getType());
    }

}


