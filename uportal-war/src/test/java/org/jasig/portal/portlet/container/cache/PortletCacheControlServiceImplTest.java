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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.portlet.CacheControl;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.PortletRenderResult;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class PortletCacheControlServiceImplTest {
    @InjectMocks private PortletCacheControlServiceImpl cacheControlService = new PortletCacheControlServiceImpl();
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Mock private IUrlSyntaxProvider urlSyntaxProvider;
    @Mock private IPortletWindow portletWindow;
    @Mock private IPortletEntity portletEntity;
    @Mock private PortletDefinition portletDescriptor;
    @Mock private IPortalRequestInfo portalRequestInfo;

    
	private CacheManager cacheManager;

	private Cache privateScopeRenderHeaderCache;
    private Cache publicScopeRenderHeaderCache;
    private Cache privateScopeRenderCache;
	private Cache publicScopeRenderCache;
	private Cache privateScopeResourceCache;
	private Cache publicScopeResourceCache;
	
	@Before
	public void getCaches() {
	    cacheManager = new CacheManager(this.getClass().getResource("/portletCacheControlServiceTestEhcache.xml"));
	    
	    privateScopeRenderHeaderCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletRenderHeaderOutputCache");
	    publicScopeRenderHeaderCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletRenderHeaderOutputCache");

        privateScopeRenderCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletRenderOutputCache");
		publicScopeRenderCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletRenderOutputCache");
		
		privateScopeResourceCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.privateScopePortletResourceOutputCache");
		publicScopeResourceCache = cacheManager.getCache("org.jasig.portal.portlet.container.cache.PortletCacheControlServiceImpl.publicScopePortletResourceOutputCache");
		
		
        cacheControlService.setPrivateScopePortletRenderHeaderOutputCache(privateScopeRenderHeaderCache);
        cacheControlService.setPublicScopePortletRenderHeaderOutputCache(publicScopeRenderHeaderCache);
        
        cacheControlService.setPrivateScopePortletRenderOutputCache(privateScopeRenderCache);
        cacheControlService.setPublicScopePortletRenderOutputCache(publicScopeRenderCache);
        
        cacheControlService.setPrivateScopePortletResourceOutputCache(privateScopeResourceCache);
        cacheControlService.setPublicScopePortletResourceOutputCache(publicScopeResourceCache);
	}
	@After
	public void clearCaches() {
	    privateScopeRenderHeaderCache.removeAll(true);
	    publicScopeRenderHeaderCache.removeAll(true);
        
		privateScopeRenderCache.removeAll(true);
		publicScopeRenderCache.removeAll(true);
		
		privateScopeResourceCache.removeAll(true);
		publicScopeResourceCache.removeAll(true);
		
		cacheManager.shutdown();
	}
	
	@Test
	public void testGetCacheControlDefault() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		when(portletDescriptor.getCacheScope()).thenReturn(null);
		
		final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		final IPortletWindow portletWindow = mock(IPortletWindow.class);
		final IPortletEntity portletEntity = mock(IPortletEntity.class);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
		when(portletWindow.getWindowState()).thenReturn(WindowState.NORMAL);
		when(portletWindow.getPortletMode()).thenReturn(PortletMode.VIEW);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDescriptor);

        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = cacheControlService.getPortletRenderState(httpRequest, portletWindowId);
        final CacheControl cacheControl = cacheState.getCacheControl();
		assertFalse(cacheControl.isPublicScope());
		assertNull(cacheControl.getETag());
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
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		// use private cache
		when(portletDescriptor.getCacheScope()).thenReturn("private");
		when(portletDescriptor.getExpirationCache()).thenReturn(300);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindowRegistry.getPortletWindow(nextHttpRequest, portletWindowId)).thenReturn(portletWindow);
		when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletWindow.getWindowState()).thenReturn(WindowState.NORMAL);
        when(portletWindow.getPortletMode()).thenReturn(PortletMode.VIEW);
        when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDescriptor);
		
		final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = cacheControlService.getPortletRenderState(httpRequest, portletWindowId);
		final CacheControl cacheControl = cacheState.getCacheControl();
		assertFalse(cacheControl.isPublicScope());
		
        cacheControl.setETag("123456");
        
        final PortletRenderResult portletResult = new PortletRenderResult("title", null, 0, 1);
        final String content = "<p>Cached content</p>";
        
        final CachedPortletData<PortletRenderResult> cachedPortletData = new CachedPortletData<PortletRenderResult>(
                portletResult, content, null, null, cacheControl.isPublicScope(),
                cacheControl.getETag(), -2);
        
        cacheControlService.cachePortletRenderOutput(portletWindowId, nextHttpRequest, cacheState, cachedPortletData);
		
        
        
        
        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> afterCacheState = cacheControlService.getPortletRenderState(nextHttpRequest, portletWindowId);
		// retrieve cachecontrol again, and return should have etag set
		// note using 'nextHttpRequest' 
		Assert.assertEquals("123456", afterCacheState.getCacheControl().getETag());
	}
	
	@Test
	public void testDetermineCacheScopePortletDefinitionPrivate() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		PortletDefinition portletDefinition = mock(PortletDefinition.class);
		when(portletDefinition.getCacheScope()).thenReturn("private");
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletWindow.getWindowState()).thenReturn(WindowState.NORMAL);
        when(portletWindow.getPortletMode()).thenReturn(PortletMode.VIEW);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDefinition);

		cacheControlService.setPortletWindowRegistry(portletWindowRegistry);
		cacheControlService.setPortletDefinitionRegistry(portletDefinitionRegistry);
		
		final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = cacheControlService.getPortletRenderState(httpRequest, portletWindowId);
		final CacheControl cacheControl = cacheState.getCacheControl();
        assertFalse(cacheControl.isPublicScope());
	}
	
	@Test
	public void testDetermineCacheScopePortletDefinitionPublic() {
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
		MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
		
		when(portletDescriptor.getCacheScope()).thenReturn(MimeResponse.PUBLIC_SCOPE);
		
		when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletWindow.getWindowState()).thenReturn(WindowState.NORMAL);
        when(portletWindow.getPortletMode()).thenReturn(PortletMode.VIEW);
		when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
		
		when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDescriptor);

		cacheControlService.setPortletWindowRegistry(portletWindowRegistry);
		cacheControlService.setPortletDefinitionRegistry(portletDefinitionRegistry);

        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = cacheControlService.getPortletRenderState(httpRequest, portletWindowId);
        final CacheControl cacheControl = cacheState.getCacheControl();
        assertTrue(cacheControl.isPublicScope());
	}
    
    @Test
    public void testCachePrivateRenderRoundTrip() {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
        MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
        
        when(portletDescriptor.getCacheScope()).thenReturn(MimeResponse.PUBLIC_SCOPE);
        
        when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletWindow.getWindowState()).thenReturn(WindowState.NORMAL);
        when(portletWindow.getPortletMode()).thenReturn(PortletMode.VIEW);
        when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
        
        when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDescriptor);

        when(this.urlSyntaxProvider.getPortalRequestInfo(httpRequest)).thenReturn(portalRequestInfo);
        when(portalRequestInfo.getPortletRequestInfoMap()).thenReturn(Collections.EMPTY_MAP);

        //Get the initial cache state
        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> firstCacheState = cacheControlService.getPortletRenderState(httpRequest, portletWindowId);
        
        //Fake Render execution
        final CacheControl cacheControl = firstCacheState.getCacheControl();
        cacheControl.setExpirationTime(300);
        
        final PortletRenderResult renderResult = new PortletRenderResult("title", null, 0, 1000l);
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<PortletRenderResult> cachedPortletData = new CachedPortletData<PortletRenderResult>(
                renderResult, output, null, null, false, cacheControl.getETag(), cacheControl.getExpirationTime());
        firstCacheState.setCachedPortletData(cachedPortletData);

        
        assertTrue(cacheControlService.shouldOutputBeCached(cacheControl));
        
        //Cache the results
        cacheControlService.cachePortletRenderOutput(portletWindowId, httpRequest, firstCacheState, cachedPortletData);
        
        //Check the cached results
        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> secondCacheState = cacheControlService.getPortletRenderState(httpRequest, portletWindowId);

        assertNotNull(secondCacheState);
        final CachedPortletData<PortletRenderResult> actualCachedPortletData = secondCacheState.getCachedPortletData();
        assertNotNull(actualCachedPortletData);
    }
    
    @Test
    public void testCachePrivateResourceRoundTrip() {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        MockPortletWindowId portletWindowId = new MockPortletWindowId("123");
        MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(789);
        
        when(portletDescriptor.getCacheScope()).thenReturn(MimeResponse.PUBLIC_SCOPE);
        
        when(portletWindowRegistry.getPortletWindow(httpRequest, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletWindow.getWindowState()).thenReturn(WindowState.NORMAL);
        when(portletWindow.getPortletMode()).thenReturn(PortletMode.VIEW);
        when(portletEntity.getPortletDefinitionId()).thenReturn(portletDefinitionId);
        
        when(portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDescriptor);

        when(this.urlSyntaxProvider.getPortalRequestInfo(httpRequest)).thenReturn(portalRequestInfo);
        when(portalRequestInfo.getPortletRequestInfoMap()).thenReturn(Collections.EMPTY_MAP);

        //Get the initial cache state
        final CacheState<CachedPortletResourceData<Long>, Long> firstCacheState = cacheControlService.getPortletResourceState(httpRequest, portletWindowId);
        
        //Fake resource execution
        final CacheControl cacheControl = firstCacheState.getCacheControl();
        cacheControl.setExpirationTime(300);
        
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        firstCacheState.setCachedPortletData(cachedPortletResourceData);

        
        assertTrue(cacheControlService.shouldOutputBeCached(cacheControl));
        
        //Cache the results
        cacheControlService.cachePortletResourceOutput(portletWindowId, httpRequest, firstCacheState, cachedPortletResourceData);
        
        //Check the cached results
        final CacheState<CachedPortletResourceData<Long>, Long> secondCacheState = cacheControlService.getPortletResourceState(httpRequest, portletWindowId);

        assertNotNull(secondCacheState);
        final CachedPortletResourceData<Long> actualCachedPortletData = secondCacheState.getCachedPortletData();
        assertNotNull(actualCachedPortletData);
    }
}
