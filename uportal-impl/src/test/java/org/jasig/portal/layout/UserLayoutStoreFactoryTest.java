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

