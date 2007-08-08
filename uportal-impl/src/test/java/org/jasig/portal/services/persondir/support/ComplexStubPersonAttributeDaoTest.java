/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;


/**
 * Testcase for ComplexStubPersonAttributeDao.
 * @version $Revision$ $Date$
 */
public class ComplexStubPersonAttributeDaoTest 
    extends AbstractPersonAttributeDaoTest {

    private IPersonAttributeDao testInstance;
    private Map backingMap;
    
    
    protected void setUp() throws Exception {
        // built the user attributes for awp9
        Map awp9Map = new HashMap();
        awp9Map.put("shirtColor", "blue");
        awp9Map.put("phone", "777-7777");
        awp9Map.put("wearsTie", "false");
        
        // build the user attributes for aam26
        Map aam26Map = new HashMap();
        aam26Map.put("shirtColor", "white");
        aam26Map.put("phone", "666-6666");
        aam26Map.put("musicalInstrumentOfChoice", "trumpet");
        
        // build the backing map, which maps from username to attribute map
        Map bMap = new HashMap();
        bMap.put("awp9", awp9Map);
        bMap.put("aam26", aam26Map);
        
        this.backingMap = bMap;
        
        ComplexStubPersonAttributeDao stub = new ComplexStubPersonAttributeDao(this.backingMap);
        
        this.testInstance = stub;
        
        super.setUp();
    }
    
    /**
     * Test that when the backing map is set properly reports possible 
     * attribute names and when the map is not set returns null for
     * possible attribute names.
     */
    public void testGetPossibleUserAttributeNames() {
        HashSet expectedAttributeNames = new HashSet();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        expectedAttributeNames.add("musicalInstrumentOfChoice");
        expectedAttributeNames.add("wearsTie");
        Set possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames();
        
        // test that it properly computed the set of possible attribute names
        
        assertEquals(expectedAttributeNames, possibleAttributeNames);
        
        // here we test that it returns the same Set each time
        // this is an implementation detail - the impl could implement the interface
        // by making a new Set each time, but since we know it's trying to cache
        // the computed set, we can test whether it's doing what it indends.
        
        assertSame(possibleAttributeNames, this.testInstance.getPossibleUserAttributeNames());
        
    }

    /**
     * Test getting user attributes using a Map key.
     */
    public void testGetUserAttributesMap() {
        Map awp9Key = new HashMap();
        awp9Key.put("uid", "awp9");
        assertEquals(this.backingMap.get("awp9"), this.testInstance.getUserAttributes(awp9Key));
        
        Map unknownUserKey = new HashMap();
        unknownUserKey.put("uid", "unknownUser");
        
        assertNull(this.testInstance.getUserAttributes(unknownUserKey));
        
    }

    /**
     * Test getting user attributes using a String key.
     */
    public void testGetUserAttributesString() {
        assertEquals(this.backingMap.get("aam26"), this.testInstance.getUserAttributes("aam26"));
        
        assertNull(this.testInstance.getUserAttributes("unknownUser"));
    }

    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.testInstance;
    }

}

