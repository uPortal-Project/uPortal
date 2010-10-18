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

package org.jasig.portal.portlet.container.properties;

import java.util.Map;

import javax.portlet.RenderResponse;

import junit.framework.TestCase;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.easymock.EasyMock;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindow;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheRequestPropertiesManagerTest extends TestCase {
    private CacheRequestPropertiesManager cacheRequestPropertiesManager;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        this.cacheRequestPropertiesManager = new CacheRequestPropertiesManager();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        this.cacheRequestPropertiesManager = null;
    }

    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)}.
     */
    public void testGetUnsetCache() throws Exception {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(Integer.MIN_VALUE);
        EasyMock.replay(portletDD);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        
        EasyMock.replay(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        final Map<String, String[]> requestProperties = this.cacheRequestPropertiesManager.getRequestProperties(httpServletRequest, portletWindow);
        
        EasyMock.verify(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
        
        assertEquals(0, requestProperties.size());
    }
    
    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)}.
     */
    public void testGetCacheFromDD() throws Exception {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(1);
        EasyMock.replay(portletDD);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        
        EasyMock.replay(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        final Map<String, String[]> requestProperties = this.cacheRequestPropertiesManager.getRequestProperties(httpServletRequest, portletWindow);
        
        EasyMock.verify(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
        
        assertEquals(1, requestProperties.size());
        final String[] cacheProperty = requestProperties.get(RenderResponse.EXPIRATION_CACHE);
        assertNotNull(cacheProperty);
        assertEquals(1, cacheProperty.length);
        assertEquals("1", cacheProperty[0]);
    }
    
    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)}.
     */
    public void testGetCacheFromWindow() throws Exception {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        portletWindow.setExpirationCache(-1);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        
        
        EasyMock.replay(portletEntityRegistry, portletDefinitionRegistry);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        final Map<String, String[]> requestProperties = this.cacheRequestPropertiesManager.getRequestProperties(httpServletRequest, portletWindow);
        
        EasyMock.verify(portletEntityRegistry, portletDefinitionRegistry);
        
        assertEquals(1, requestProperties.size());
        final String[] cacheProperty = requestProperties.get(RenderResponse.EXPIRATION_CACHE);
        assertNotNull(cacheProperty);
        assertEquals(1, cacheProperty.length);
        assertEquals("-1", cacheProperty[0]);
    }

    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)}.
     */
    public void testSetCacheWithDDCache() throws Exception  {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(1);
        EasyMock.replay(portletDD);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        
        EasyMock.replay(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        this.cacheRequestPropertiesManager.setResponseProperty(httpServletRequest, portletWindow, RenderResponse.EXPIRATION_CACHE, "30");
        
        EasyMock.verify(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
        
        final Integer expirationCache = portletWindow.getExpirationCache();
        assertEquals(Integer.valueOf(30), expirationCache);
    }

    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)}.
     */
    public void testSetCacheWithNoDDCache() throws Exception  {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        
        final IPortletDefinition portletDefinition = EasyMock.createMock(IPortletDefinition.class);
        EasyMock.expect(portletDefinition.getPortletDefinitionId()).andReturn(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDefinition portletDD = EasyMock.createMock(PortletDefinition.class);
        EasyMock.expect(portletDD.getExpirationCache()).andReturn(Integer.MIN_VALUE);
        EasyMock.replay(portletDD);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        
        
        EasyMock.replay(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        
        this.cacheRequestPropertiesManager.setResponseProperty(httpServletRequest, portletWindow, RenderResponse.EXPIRATION_CACHE, "30");
        
        EasyMock.verify(portletDefinition, portletEntityRegistry, portletDefinitionRegistry);
        
        final Integer expirationCache = portletWindow.getExpirationCache();
        assertNull(expirationCache);
    }

}
