/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

import junit.framework.TestCase;

/**
 * Test conformance to IPersonAttributeDao interface specified 
 * IllegalArgumentException throwing for illegal invocations of interface methods.
 * @version $Revision$ $Date$
 */
public abstract class AbstractPersonAttributeDaoTest extends TestCase {
    
    /**
     * Get an instance of the type of IPersonAttributeDao the implementing
     * testcase is intended to test.
     * 
     * This method will be invoked exactly once per invocation of each test method
     * implemented in this abstract class.
     * 
     * @return an IPersonAttributeDao instance for us to test
     */
    protected abstract IPersonAttributeDao getPersonAttributeDaoInstance();

    /**
     * Test that invocation of getUserAttributes(Map null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's 
     * interface declaration.
     */
    public void testNullSeed() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        Map nullMap = null;
        try {
            dao.getUserAttributes(nullMap);
        } catch (IllegalArgumentException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getUserAttributes(String null)");

    }
    
    /**
     * Test that invocation of getUserAttributes(String null) throws
     * IllegalArgumentException as specified in IPersonAttributeDao's
     * interface declaration.
     */
    public void testNullUid() {
        IPersonAttributeDao dao = getPersonAttributeDaoInstance();
        String nullString = null;
        try {
            dao.getUserAttributes(nullString);
        } catch (IllegalArgumentException iae) {
            // good, as expected
            return;
        }
        fail("Expected IllegalArgumentException on getUserAttributes(String null)");
    }
    
}

