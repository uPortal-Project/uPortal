/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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