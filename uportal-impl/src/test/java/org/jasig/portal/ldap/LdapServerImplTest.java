/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.ldap;

import junit.framework.TestCase;

/**
 * Testcase for LdapServerImpl.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class LdapServerImplTest extends TestCase {
    
    // TODO: add testcase that tests actual well-functioning LDAP server.
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that negative port numbers are rejected.
     */
    public void testNegativePort() {
        try {
            LdapServerImpl impl = new LdapServerImpl("test", "mrfrumble.its.yale.edu", 
                    "-389", null, "uid", null, null, false, null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Negative port number should have prompted IllegalArgumentException.");
    }
    
    /**
     * Test that non-integer port numbers are rejected.
     */
    public void testInvalidPort() {
        try {
            LdapServerImpl impl = new LdapServerImpl("test", "mrfrumble.its.yale.edu", 
                    "grendel", null, "uid", null, null, false, null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Non-integer strings as port numbers should prompt IllegalArgumentException.");
    }
    
    /**
     * Test that null LdapServerImpl names are rejected.
     */
    public void testNullName() {
        try {
            LdapServerImpl impl = new LdapServerImpl(null, "mrfrumble.its.yale.edu", 
                    "675", null, "uid", null, null, false, null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Null name should have yielded IllegalArgumentException.");
    }
  
    /**
     * Test that null hosts are rejected.
     */
    public void testNullHost() {
        try {
            LdapServerImpl impl = new LdapServerImpl("name", null, 
                    "675", null, "uid", null, null, false, null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        }
        fail("Null host should have yielded IllegalArgumentException.");
    }
    
}