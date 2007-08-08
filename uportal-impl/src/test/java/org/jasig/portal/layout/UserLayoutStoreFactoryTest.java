/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Properties;

import org.jasig.portal.properties.PropertiesManager;

import junit.framework.TestCase;

/**
 * JUnit testcase for UserLayoutStoreFactory.
 * @version $Revision$ $Date$
 */
public class UserLayoutStoreFactoryTest extends TestCase {
    
    /**
     * Test that when the property is set, we return an instance of the class defined by that
     * property.
     */
    public void testGetUserLayoutStorePropertySet() {
        
        Properties properties = new Properties();
        properties.put(UserLayoutStoreFactory.LAYOUT_STORE_IMPL_PROPERTY, UserLayoutStoreMock.class.getName());
        PropertiesManager.setProperties(properties);
        IUserLayoutStore store = UserLayoutStoreFactory.getUserLayoutStoreImpl();
        assertNotNull(store);
        assertTrue(UserLayoutStoreMock.class == store.getClass());
        
        // now destroy the properties and test that UserLayoutStoreFactory still
        // gives us that singleton.
        
        properties.put(UserLayoutStoreFactory.LAYOUT_STORE_IMPL_PROPERTY, "wombat");
        PropertiesManager.setProperties(properties);
        
        IUserLayoutStore store2 = UserLayoutStoreFactory.getUserLayoutStoreImpl();
        assertSame(store, store2);
        
    }
    
}

