/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.HashMap;
import java.util.Map;

/**
 * Testcase for the NoncollidingAttributeAdder.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class NoncollidingAttributeAdderTest extends AttributeMergerAbstractTest {

    private NoncollidingAttributeAdder adder = new NoncollidingAttributeAdder();

    /**
     * Test identity of adding an empty map.
     */
    public void testAddEmpty() {
        Map someAttributes = new HashMap();
        someAttributes.put("attName", "attValue");
        someAttributes.put("attName2", "attValue2");
        
        Map expected = new HashMap();
        expected.putAll(someAttributes);
        
        Map result = this.adder.mergeAttributes(someAttributes, new HashMap());
        
        assertEquals(expected, result);
    }

    /**
     * Test a simple case of adding one map of attributes to another, with
     * no collisions.
     */
    public void testAddNoncolliding() {
        Map someAttributes = new HashMap();
        someAttributes.put("attName", "attValue");
        someAttributes.put("attName2", "attValue2");
        
        Map otherAttributes = new HashMap();
        otherAttributes.put("attName3", "attValue3");
        otherAttributes.put("attName4", "attValue4");
        
        Map expected = new HashMap();
        expected.putAll(someAttributes);
        expected.putAll(otherAttributes);
        
        Map result = this.adder.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }
    
    
    
    /**
     * Test that colliding attributes are not added.
     */
    public void testColliding() {
        Map someAttributes = new HashMap();
        someAttributes.put("attName", "attValue");
        someAttributes.put("attName2", "attValue2");
        
        Map otherAttributes = new HashMap();
        otherAttributes.put("attName", "attValue3");
        otherAttributes.put("attName4", "attValue4");
        
        Map expected = new HashMap();
        expected.putAll(someAttributes);
        expected.put("attName4", "attValue4");
        
        Map result = this.adder.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.support.merger.AttributeMergerAbstractTest#getAttributeMerger()
     */
    protected AttributeMerger getAttributeMerger() {
        return new NoncollidingAttributeAdder();
    }
    
}
