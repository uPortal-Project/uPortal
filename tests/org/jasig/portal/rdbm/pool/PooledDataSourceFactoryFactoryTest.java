/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import java.util.Properties;

import javax.sql.DataSource;

import org.jasig.portal.properties.PropertiesManager;

import junit.framework.TestCase;

/**
 * Testcase for PooledDataSourceFactoryFactory.
 * @version $Revision$ $Date$
 */
public class PooledDataSourceFactoryFactoryTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        PooledDataSourceFactoryFactory.reset();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        PooledDataSourceFactoryFactory.reset();
    }

    /**
     * Test getting a PooledDataSourceFactory that we've configured via 
     * PropertiesManager.
     */
    public void testGetPooledDataSourceFactory() {
        Properties dummyProperties = new Properties();
        dummyProperties.put(PooledDataSourceFactoryFactory.POOLED_DATA_SOURCE_FACTORY_PROPERTY, 
                "wombat");
        
        PropertiesManager.setProperties(dummyProperties);
        
        IPooledDataSourceFactory factory = PooledDataSourceFactoryFactory.getPooledDataSourceFactory();
        assertNotNull(factory);
        System.out.println(factory.getClass().getName());
        assertTrue(PooledDataSourceFactoryFactory.DEFAULT_POOLED_DATASOURCE_FACTORY == factory.getClass());
        
        // now set the property and assert that we get the same singleton instance
        // (we do not un-fail-over and use the fixed property, e.g.)
        
        dummyProperties.put(PooledDataSourceFactoryFactory.POOLED_DATA_SOURCE_FACTORY_PROPERTY, StubPooledDataSourceFactory.class.getName());
        PropertiesManager.setProperties(dummyProperties);
        
        IPooledDataSourceFactory factory2 = PooledDataSourceFactoryFactory.getPooledDataSourceFactory();
        assertSame(factory, factory2);
        
        
    }
    
    /**
     * Test falling back on our default when our property is set to a String that doesn't
     * represent a class at all.
     */
    public void testGetPooledDataSourceFactoryBadProperty() {
        Properties dummyProperties = new Properties();
        dummyProperties.put(PooledDataSourceFactoryFactory.POOLED_DATA_SOURCE_FACTORY_PROPERTY, 
                StubPooledDataSourceFactory.class.getName());
        
        PropertiesManager.setProperties(dummyProperties);
        
        IPooledDataSourceFactory factory = PooledDataSourceFactoryFactory.getPooledDataSourceFactory();
        assertNotNull(factory);
        System.out.println(factory.getClass().getName());
        assertTrue(StubPooledDataSourceFactory.class == factory.getClass());
        
        // now destroy the properties and assert that we get the same singleton instance
        
        dummyProperties.put(PooledDataSourceFactoryFactory.POOLED_DATA_SOURCE_FACTORY_PROPERTY, "wombat");
        PropertiesManager.setProperties(dummyProperties);
        
        IPooledDataSourceFactory factory2 = PooledDataSourceFactoryFactory.getPooledDataSourceFactory();
        assertSame(factory, factory2);
        
        
    }
    
}
