/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Properties;

import junit.framework.TestCase;

import org.jasig.portal.properties.PropertiesManager;

/**
 * Unit test for PortletPreferencesStoreFactory.
 * @version $Revision$ $Date$
 */
public class PortletPreferencesStoreFactoryTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        PortletPreferencesStoreFactory.reset();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        PortletPreferencesStoreFactory.reset();
    }

    /**
     * Test getting an IPortletPreferencesStore implementation
     * as specified in PropertiesManager.
     */
    public void testGetPortletPreferencesStoreImpl() {
        Properties properties = new Properties();
        properties.put(PortletPreferencesStoreFactory.PORTLET_PREF_STORE_PROPERTY, 
                StubPortletPreferencesStore.class.getName());
        PropertiesManager.setProperties(properties);
        
        IPortletPreferencesStore store = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
        
        assertNotNull(store);
        assertEquals(StubPortletPreferencesStore.class, store.getClass());
        
        // now destroy the properties and assert that when we ask for the store again
        // we get a reference to that same singleton
        
        properties.put(PortletPreferencesStoreFactory.PORTLET_PREF_STORE_PROPERTY, "wombat");
        PropertiesManager.setProperties(properties);
        
        IPortletPreferencesStore store2 = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
        
        assertSame(store, store2);

        
    }
    
    /**
     * Test falling back on the default implementation when the 
     * PropertiesManager does not have the property configured.
     */
    public void testGetPortletPreferencesStoreMissingProperty() {
        Properties properties = new Properties();
        PropertiesManager.setProperties(properties);
        
        IPortletPreferencesStore store = 
            PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
        
        assertNotNull(store);
        assertEquals(PortletPreferencesStoreFactory.DEFAULT_PREF_STORE_CLASS, store.getClass());
        
        // now add the property and assert that when we ask for the store again
        // we get a reference to that same (default) singleton.
        
        properties.put(PortletPreferencesStoreFactory.PORTLET_PREF_STORE_PROPERTY, 
                StubPortletPreferencesStore.class.getName());
        
        PropertiesManager.setProperties(properties);
        
        IPortletPreferencesStore store2 = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
        
        assertSame(store, store2);

        
    }

}

