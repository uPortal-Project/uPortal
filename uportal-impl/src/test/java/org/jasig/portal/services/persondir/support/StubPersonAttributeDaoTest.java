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
 * Testcase for StubPersonAttributeDao.
 * @version $Revision$ $Date$
 */
public class StubPersonAttributeDaoTest 
    extends AbstractPersonAttributeDaoTest {

    private IPersonAttributeDao testInstance;
    private Map backingMap;
    
    
    protected void setUp() throws Exception {
        Map map = new HashMap();
        map.put("shirtColor", "blue");
        map.put("phone", "777-7777");
        
        this.backingMap = map;
        
        StubPersonAttributeDao stub = new StubPersonAttributeDao();
        stub.setBackingMap(map);
        
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
        Set possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames();
        assertEquals(expectedAttributeNames, possibleAttributeNames);
        
        StubPersonAttributeDao nullBacking = new StubPersonAttributeDao();
        assertNull(nullBacking.getPossibleUserAttributeNames());
    }

    public void testGetUserAttributesMap() {
        assertSame(this.backingMap, this.testInstance.getUserAttributes(new HashMap()));
        assertEquals(this.backingMap, this.testInstance.getUserAttributes(new HashMap()));

    }

    public void testGetUserAttributesString() {
        assertSame(this.backingMap, this.testInstance.getUserAttributes("anyone"));
        assertEquals(this.backingMap, this.testInstance.getUserAttributes("wombat"));
    }

    protected IPersonAttributeDao getPersonAttributeDaoInstance() {
        return this.testInstance;
    }

}

