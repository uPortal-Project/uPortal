/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.HashMap;
import java.util.Map;

/**
 * Testcase for ReplacingAttributeAdder.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ReplacingAttributeAdderTest extends AttributeMergerAbstractTest {

    private ReplacingAttributeAdder adder = new ReplacingAttributeAdder();
    
    /**
     * Test that this implementation replaces colliding attributes with the new 
     * attribute values.
     */
    public void testReplacement() {
        Map mapOne = new HashMap();
        mapOne.put("aaa", "111");
        mapOne.put("bbb", "222");
        
        Map mapTwo = new HashMap();
        mapTwo.put("bbb", "bbb");
        mapTwo.put("ccc", "333");
        
        Map expected = new HashMap();
        expected.putAll(mapOne);
        expected.putAll(mapTwo);
        
        Map result = this.adder.mergeAttributes(mapOne, mapTwo);
        assertEquals(expected, result);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.support.merger.AttributeMergerAbstractTest#getAttributeMerger()
     */
    protected AttributeMerger getAttributeMerger() {
        return new ReplacingAttributeAdder();
    }

}