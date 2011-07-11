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
package org.jasig.portal.portlet.container.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/properties/contexts/cacheContext.xml"})
public class PortletCacheControlServiceImplTest {

	
	private CacheManager cacheManager;
	@Autowired
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private Cache privateScopeCache;
	private Cache publicScopeCache;
	
	@Before
	public void getCaches() {
		privateScopeCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletOutputCache");
		publicScopeCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletOutputCache");
	}
	@After
	public void clearCaches() {
		privateScopeCache.removeAll(true);
		publicScopeCache.removeAll(true);
	}
	@Test
	public void testGetCacheControlDefault() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("789");
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn(null);
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletOutputCache(privateScopeCache);
		cacheControlService.setPublicScopePortletDataCache(publicScopeCache);
		
		final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		final IPortletWindow portletWindow = mock(IPortletWindow.class);
		final IPortletEntity portletEntity = mock(IPortletEntity.class);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		final IPortletEntityRegistry portletEntityRegistry = mock(IPortletEntityRegistry.class);
		when(portletEntityRegistry.getPortletEntity(httpRequest, portletEntityId)).thenReturn(portletEntity);
		final IPortletDefinitionRegistry portletDefinitionRegistry = mock(IPortletDefinitionRegistry.class);
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDefinition);
		
		cacheControlService.setPortletWindowRegistry(portletWindowRegistry);
		cacheControlService.setPortletDefinitionRegistry(portletDefinitionRegistry);
		cacheControlService.setPortletEntityRegistry(portletEntityRegistry);
		
		CacheControl control = cacheControlService.getPortletCacheControl(portletWindowId, httpRequest);
		assertFalse(control.isPublicScope());
		assertNull(control.getETag());
	}
	
	@Test
	public void testGetCacheControlDataExistsInPrivateCache() {
		// mock 2 requests, have to share sessionId for private cache
		MockHttpSession mockSession = new MockHttpSession();
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setSession(mockSession);
		MockHttpServletRequest nextHttpRequest = new MockHttpServletRequest();
		nextHttpRequest.setSession(mockSession);
		
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("789");
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		// use private cache
		when(portletDefinition.getCacheScope()).thenReturn("private");
		when(portletDefinition.getExpirationCache()).thenReturn(300);
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletOutputCache(privateScopeCache);
		cacheControlService.setPublicScopePortletDataCache(publicScopeCache);
		
		
		final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		final IPortletWindow portletWindow = mock(IPortletWindow.class);
		final IPortletEntity portletEntity = mock(IPortletEntity.class);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindowRegistry.getPortletWindow(nextHttpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		final IPortletEntityRegistry portletEntityRegistry = mock(IPortletEntityRegistry.class);
		when(portletEntityRegistry.getPortletEntity(httpRequest, portletEntityId)).thenReturn(portletEntity);
		when(portletEntityRegistry.getPortletEntity(nextHttpRequest, portletEntityId)).thenReturn(portletEntity);
		final IPortletDefinitionRegistry portletDefinitionRegistry = mock(IPortletDefinitionRegistry.class);
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDefinition);
		
		cacheControlService.setPortletWindowRegistry(portletWindowRegistry);
		cacheControlService.setPortletDefinitionRegistry(portletDefinitionRegistry);
		cacheControlService.setPortletEntityRegistry(portletEntityRegistry);
		
		CacheControl control = cacheControlService.getPortletCacheControl(portletWindowId, httpRequest);
		assertFalse(control.isPublicScope());
		
		control.setETag("123456");
		cacheControlService.cachePortletRenderOutput(portletWindowId, httpRequest, "<p>Cached content</p>", control);
		
		
		// retrieve cachecontrol again, and return should have etag set
		// note using 'nextHttpRequest' 
		CacheControl afterCache = cacheControlService.getPortletCacheControl(portletWindowId, nextHttpRequest);
		Assert.assertEquals("123456", afterCache.getETag());
	}
	
	@Test
	public void testDetermineCacheScopePortletDefinitionPrivate() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("789");
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn("private");
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletOutputCache(privateScopeCache);
		cacheControlService.setPublicScopePortletDataCache(publicScopeCache);
		final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		final IPortletWindow portletWindow = mock(IPortletWindow.class);
		final IPortletEntity portletEntity = mock(IPortletEntity.class);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		final IPortletEntityRegistry portletEntityRegistry = mock(IPortletEntityRegistry.class);
		when(portletEntityRegistry.getPortletEntity(httpRequest, portletEntityId)).thenReturn(portletEntity);
		final IPortletDefinitionRegistry portletDefinitionRegistry = mock(IPortletDefinitionRegistry.class);
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDefinition);
		
		cacheControlService.setPortletWindowRegistry(portletWindowRegistry);
		cacheControlService.setPortletDefinitionRegistry(portletDefinitionRegistry);
		cacheControlService.setPortletEntityRegistry(portletEntityRegistry);
		
		CacheControl control = cacheControlService.getPortletCacheControl(portletWindowId, httpRequest);
		assertFalse(control.isPublicScope());
	}
	
	@Test
	public void testDetermineCacheScopePortletDefinitionPublic() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("789");
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn(MimeResponse.PUBLIC_SCOPE);
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletOutputCache(privateScopeCache);
		cacheControlService.setPublicScopePortletDataCache(publicScopeCache);
		final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		final IPortletWindow portletWindow = mock(IPortletWindow.class);
		final IPortletEntity portletEntity = mock(IPortletEntity.class);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		final IPortletEntityRegistry portletEntityRegistry = mock(IPortletEntityRegistry.class);
		when(portletEntityRegistry.getPortletEntity(httpRequest, portletEntityId)).thenReturn(portletEntity);
		final IPortletDefinitionRegistry portletDefinitionRegistry = mock(IPortletDefinitionRegistry.class);
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDefinition);
		
		cacheControlService.setPortletWindowRegistry(portletWindowRegistry);
		cacheControlService.setPortletDefinitionRegistry(portletDefinitionRegistry);
		cacheControlService.setPortletEntityRegistry(portletEntityRegistry);
		
		CacheControl control = cacheControlService.getPortletCacheControl(portletWindowId, httpRequest);
		assertTrue(control.isPublicScope());
	}
}
