/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import junit.framework.TestSuite;

public class AllLayoutTests {

    public AllLayoutTests() {
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
    public static TestSuite suite() {
        TestSuite suite= new TestSuite("uPortal Layout Tests");
        suite.addTestSuite(SimpleUserLayoutManagerTest.class);
        return suite;
    }
}
