package org.jasig.portal.layout;

import junit.framework.*;
import junit.runner.BaseTestRunner;
import org.jasig.portal.layout.SimpleUserLayoutManager;

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
