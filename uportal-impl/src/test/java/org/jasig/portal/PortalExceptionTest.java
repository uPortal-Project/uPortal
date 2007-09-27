/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import junit.framework.TestCase;

/**
 * Testcase for PortalException. The PortalException implementation of
 * initCause() catches the Throwable implementation's thrown exceptions in the
 * case of illegal argument (null argument) or illegal state (cause already
 * init'ed). Therefore PortalException.initCause() should never throw anything
 * and should always return a reference to the PortalException.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PortalExceptionTest extends TestCase {

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
     * Test that calling the deprecated legacy method
     * setRecordedException(null) does not throw any exceptions.
     */
    public void testSetNullRecordedException() {
        PortalException pe = new PortalException("Dummy message");
        pe.initCause(null);
    }
    
    /**
     * Test that setRecordedException populates the Throwable.getCause()
     * of a PortalException.
     */
    public void testSetRecordedException(){
        PortalException pe = new PortalException("Dummy message");
        Exception cause = new Exception();
        pe.initCause(cause);
        assertEquals(cause, pe.getCause());
    }
    
}