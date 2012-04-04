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

import java.io.Serializable;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.container.CacheControlImpl;
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
@ContextConfiguration(locations={"/portletCacheControlServiceTestContext.xml"})
public class PortletCacheControlServiceImplTest {

	
	private CacheManager cacheManager;
	@Autowired
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private Cache privateScopeRenderCache;
	private Cache publicScopeRenderCache;
	private Cache privateScopeResourceCache;
	private Cache publicScopeResourceCache;
	
	@Before
	public void getCaches() {
		privateScopeRenderCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletRenderOutputCache");
		publicScopeRenderCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletRenderOutputCache");
		
		privateScopeResourceCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletResourceOutputCache");
		publicScopeResourceCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletResourceOutputCache");
	}
	@After
	public void clearCaches() {
		privateScopeRenderCache.removeAll(true);
		publicScopeRenderCache.removeAll(true);
		
		privateScopeResourceCache.removeAll(true);
		publicScopeResourceCache.removeAll(true);
	}
	@Test
	public void testGetCacheControlDefault() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn(null);
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletRenderOutputCache(privateScopeRenderCache);
		cacheControlService.setPublicScopePortletRenderOutputCache(publicScopeRenderCache);
		cacheControlService.setPrivateScopePortletResourceOutputCache(privateScopeResourceCache);
		cacheControlService.setPublicScopePortletResourceOutputCache(publicScopeResourceCache);
		
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
		
		CacheControl control = cacheControlService.getPortletRenderCacheControl(portletWindowId, httpRequest);
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
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		// use private cache
		when(portletDefinition.getCacheScope()).thenReturn("private");
		when(portletDefinition.getExpirationCache()).thenReturn(300);
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletRenderOutputCache(privateScopeRenderCache);
		cacheControlService.setPublicScopePortletRenderOutputCache(publicScopeRenderCache);
		cacheControlService.setPrivateScopePortletResourceOutputCache(privateScopeResourceCache);
		cacheControlService.setPublicScopePortletResourceOutputCache(publicScopeResourceCache);
		
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
		
		CacheControl control = cacheControlService.getPortletRenderCacheControl(portletWindowId, httpRequest);
		assertFalse(control.isPublicScope());
		
		control.setETag("123456");
		cacheControlService.cachePortletRenderOutput(portletWindowId, httpRequest, "<p>Cached content</p>", control);
		
		
		// retrieve cachecontrol again, and return should have etag set
		// note using 'nextHttpRequest' 
		CacheControl afterCache = cacheControlService.getPortletRenderCacheControl(portletWindowId, nextHttpRequest);
		Assert.assertEquals("123456", afterCache.getETag());
	}
	
	@Test
	public void testDetermineCacheScopePortletDefinitionPrivate() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn("private");
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletRenderOutputCache(privateScopeRenderCache);
		cacheControlService.setPublicScopePortletRenderOutputCache(publicScopeRenderCache);
		cacheControlService.setPrivateScopePortletResourceOutputCache(privateScopeResourceCache);
		cacheControlService.setPublicScopePortletResourceOutputCache(publicScopeResourceCache);
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
		
		CacheControl control = cacheControlService.getPortletRenderCacheControl(portletWindowId, httpRequest);
		assertFalse(control.isPublicScope());
	}
	
	@Test
	public void testDetermineCacheScopePortletDefinitionPublic() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletEntityId portletEntityId = new MockPortletEntityId("456");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn(MimeResponse.PUBLIC_SCOPE);
		
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		cacheControlService.setPrivateScopePortletRenderOutputCache(privateScopeRenderCache);
		cacheControlService.setPublicScopePortletRenderOutputCache(publicScopeRenderCache);
		cacheControlService.setPrivateScopePortletResourceOutputCache(privateScopeResourceCache);
		cacheControlService.setPublicScopePortletResourceOutputCache(publicScopeResourceCache);
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
		
		CacheControl control = cacheControlService.getPortletRenderCacheControl(portletWindowId, httpRequest);
		assertTrue(control.isPublicScope());
	}
	
	@Test
	public void testConstructCacheElement() {
		PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
		CacheConfiguration cacheConfig = new CacheConfiguration();
		cacheConfig.setTimeToLiveSeconds(1800L);
		CacheControl cacheControl = new CacheControlImpl();
		
		Serializable key = new Serializable() {
			private static final long serialVersionUID = 1L;
		};
		CachedPortletData data = new CachedPortletData();
		
		// cacheControl expiration time unset, fail safe to cache config value
		Element cacheElement = cacheControlService.constructCacheElement(key, data, cacheConfig, cacheControl);
		Assert.assertEquals(0, cacheElement.getTimeToLive());
		
		cacheControl.setExpirationTime(60);
		// cacheControl expiration time lesser, use it
		cacheElement = cacheControlService.constructCacheElement(key, data, cacheConfig, cacheControl);
		Assert.assertEquals(60, cacheElement.getTimeToLive());
		cacheControl.setExpirationTime(1800);
		// identical
		cacheElement = cacheControlService.constructCacheElement(key, data, cacheConfig, cacheControl);
		Assert.assertEquals(1800, cacheElement.getTimeToLive());
		// cacheControl expiration time greater than cache, use cache config value
		cacheConfig.setTimeToLiveSeconds(900L);
		cacheElement = cacheControlService.constructCacheElement(key, data, cacheConfig, cacheControl);
		Assert.assertEquals(900, cacheElement.getTimeToLive());
		
		// cacheControl indicates use validation method, defer to cache configuration
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
		cacheElement = cacheControlService.constructCacheElement(key, data, cacheConfig, cacheControl);
		Assert.assertEquals(0, cacheElement.getTimeToLive());
	}
}
