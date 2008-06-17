/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import junit.framework.TestCase;

/**
 * JUnit testcase for ClassPresenceCheck.
 * @version $Revision$ $Date$
 */
public class ClassPresenceCheckTest extends TestCase {

    /**
     * Test that checking for a present class succeeds.
     */
    public void testSuccess() {
        ClassPresenceCheck check = new ClassPresenceCheck("java.lang.Class");
        CheckResult result = check.doCheck();
        assertTrue(result.isSuccess());
    }
    
    /**
     * Test that checking for an absent class fails.
     */
    public void testFailure() {
        // check for the presence of a class we know will not exist.
        ClassPresenceCheck check = new ClassPresenceCheck("org.jasig.NoExist");
        CheckResult result = check.doCheck();
        assertFalse(result.isSuccess());
    }
}

