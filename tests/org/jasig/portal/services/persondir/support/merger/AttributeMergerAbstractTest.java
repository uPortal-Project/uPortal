/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support.merger;

import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Abstract test for the AttributeMerger interface.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public abstract class AttributeMergerAbstractTest extends TestCase {

    /**
     * Test that attempting to merge attributes into a null Map results in
     * an illegal argument exception.
     */
    public void testNullToModify() {
        try {
            getAttributeMerger().mergeAttributes(null, new HashMap());
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have thrown IAE on null argument.");
    }
    
    /**
     * Test that attempting to merge attributes into a null Map results in
     * an illegal argument exception.
     */
    public void testNullToConsider() {
        try {
            getAttributeMerger().mergeAttributes(new HashMap(), null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Should have thrown IAE on null argument.");
    }
    
    protected abstract AttributeMerger getAttributeMerger();
    
}