/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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

