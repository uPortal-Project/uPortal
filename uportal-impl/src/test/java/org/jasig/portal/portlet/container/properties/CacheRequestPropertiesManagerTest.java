/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.Map;

import javax.portlet.RenderResponse;

import junit.framework.TestCase;

import org.apache.pluto.descriptors.portlet.PortletDD;
import org.easymock.EasyMock;
import org.jasig.portal.mock.portlet.om.MockPortletDefinition;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.url.AttributeScopingHttpServletRequestWrapper;
import org.jasig.portal.url.IPortalRequestUtils;
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
        httpServletRequest.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, httpServletRequest);
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinition portletDefinition = new MockPortletDefinition();
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        portletDefinition.setPortletDefinitionId(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDD portletDD = new PortletDD();
        portletDD.setExpirationCache(PortletDD.EXPIRATION_CACHE_UNSET);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(httpServletRequest)).andReturn(httpServletRequest);
        
        
        EasyMock.replay(portletEntityRegistry, portletDefinitionRegistry, portalRequestUtils);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.cacheRequestPropertiesManager.setPortalRequestUtils(portalRequestUtils);
        
        final Map<String, String[]> requestProperties = this.cacheRequestPropertiesManager.getRequestProperties(httpServletRequest, portletWindow);
        
        EasyMock.verify(portletEntityRegistry, portletDefinitionRegistry);
        
        assertEquals(0, requestProperties.size());
    }
    
    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)}.
     */
    public void testGetCacheFromDD() throws Exception {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, httpServletRequest);
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinition portletDefinition = new MockPortletDefinition();
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        portletDefinition.setPortletDefinitionId(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDD portletDD = new PortletDD();
        portletDD.setExpirationCache(1);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(httpServletRequest)).andReturn(httpServletRequest);
        
        
        EasyMock.replay(portletEntityRegistry, portletDefinitionRegistry, portalRequestUtils);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.cacheRequestPropertiesManager.setPortalRequestUtils(portalRequestUtils);
        
        final Map<String, String[]> requestProperties = this.cacheRequestPropertiesManager.getRequestProperties(httpServletRequest, portletWindow);
        
        EasyMock.verify(portletEntityRegistry, portletDefinitionRegistry);
        
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
        httpServletRequest.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, httpServletRequest);
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        portletWindow.setExpirationCache(-1);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(httpServletRequest)).andReturn(httpServletRequest);
        
        
        EasyMock.replay(portletEntityRegistry, portletDefinitionRegistry, portalRequestUtils);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.cacheRequestPropertiesManager.setPortalRequestUtils(portalRequestUtils);
        
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
        httpServletRequest.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, httpServletRequest);
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinition portletDefinition = new MockPortletDefinition();
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        portletDefinition.setPortletDefinitionId(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDD portletDD = new PortletDD();
        portletDD.setExpirationCache(1);
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(httpServletRequest)).andReturn(httpServletRequest);
        
        
        EasyMock.replay(portletEntityRegistry, portletDefinitionRegistry, portalRequestUtils);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.cacheRequestPropertiesManager.setPortalRequestUtils(portalRequestUtils);
        
        this.cacheRequestPropertiesManager.setResponseProperty(httpServletRequest, portletWindow, RenderResponse.EXPIRATION_CACHE, "30");
        
        EasyMock.verify(portletEntityRegistry, portletDefinitionRegistry);
        
        final Integer expirationCache = portletWindow.getExpirationCache();
        assertEquals(Integer.valueOf(30), expirationCache);
    }

    /**
     * Test method for {@link org.jasig.portal.portlet.container.properties.CacheRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)}.
     */
    public void testSetCacheWithNoDDCache() throws Exception  {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setAttribute(AttributeScopingHttpServletRequestWrapper.ATTRIBUTE__HTTP_SERVLET_REQUEST, httpServletRequest);
        
        final MockPortletWindow portletWindow = new MockPortletWindow();
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("1");
        portletWindow.setPortletEntityId(portletEntityId);
        
        final MockPortletDefinition portletDefinition = new MockPortletDefinition();
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("2");
        portletDefinition.setPortletDefinitionId(portletDefinitionId);
        
        final IPortletEntityRegistry portletEntityRegistry = EasyMock.createMock(IPortletEntityRegistry.class);
        EasyMock.expect(portletEntityRegistry.getParentPortletDefinition(portletEntityId)).andReturn(portletDefinition);
        
        final PortletDD portletDD = new PortletDD();
        
        final IPortletDefinitionRegistry portletDefinitionRegistry = EasyMock.createMock(IPortletDefinitionRegistry.class);
        EasyMock.expect(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).andReturn(portletDD);
        
        final IPortalRequestUtils portalRequestUtils = EasyMock.createMock(IPortalRequestUtils.class);
        EasyMock.expect(portalRequestUtils.getOriginalPortletAdaptorRequest(httpServletRequest)).andReturn(httpServletRequest);
        
        
        EasyMock.replay(portletEntityRegistry, portletDefinitionRegistry, portalRequestUtils);
            
        this.cacheRequestPropertiesManager.setPortletEntityRegistry(portletEntityRegistry);
        this.cacheRequestPropertiesManager.setPortletDefinitionRegistry(portletDefinitionRegistry);
        this.cacheRequestPropertiesManager.setPortalRequestUtils(portalRequestUtils);
        
        this.cacheRequestPropertiesManager.setResponseProperty(httpServletRequest, portletWindow, RenderResponse.EXPIRATION_CACHE, "30");
        
        EasyMock.verify(portletEntityRegistry, portletDefinitionRegistry);
        
        final Integer expirationCache = portletWindow.getExpirationCache();
        assertNull(expirationCache);
    }

}
