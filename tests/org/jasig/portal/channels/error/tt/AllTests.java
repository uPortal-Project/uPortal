/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Automatically generated test suite for the package.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for org.jasig.portal.channels.error.tt");
        //$JUnit-BEGIN$
        suite.addTestSuite(InternalTimeoutExceptionToElementTest.class);
        suite.addTestSuite(ResourceMissingExceptionToElementTest.class);
        suite.addTestSuite(AuthorizationExceptionToElementTest.class);
        suite.addTestSuite(ThrowableToElementTest.class);
        //$JUnit-END$
        return suite;
    }
}