/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import org.apache.pluto.om.common.ObjectID;
import org.jasig.portal.container.om.common.ObjectIDImpl;

import junit.framework.TestCase;

/**
 * Testcase for StaticInformationProviderImpl.
 * Currently tests some argument checking on the getPortletDefinition() method.
 */
public class StaticInformationProviderImplTest 
    extends TestCase {

    /**
     * Test that getPortletDefinition() throws IllegalArgumentException when
     * invoked on a null argument.
     */
    public void testGetPortletDefForNullGuid() {
        StaticInformationProviderImpl impl = new StaticInformationProviderImpl();
        
        try {
            impl.getPortletDefinition(null);
        } catch (IllegalArgumentException iae) {
            // good.  IllegalArgumentException expected
            return;
        }
        fail("Should have thrown IllegalArgumentException.");
    }
    
    /**
     * Test that getPortletDefinition() throws IllegalArgumentException when
     * invoked on a guid that doesn't contain the required '.' character.
     */
    public void testGetPortletDefForNoDotGuid() {
        StaticInformationProviderImpl impl = new StaticInformationProviderImpl();
        ObjectID objectId = ObjectIDImpl.createFromString("foo");
        
        try {
            impl.getPortletDefinition(objectId);
        } catch(IllegalArgumentException iae) {
            // good.  IllegalArgumentException expected
            return;
        }
        fail("Should have thrown IllegalArgumentException.");
    }
    
    
}
