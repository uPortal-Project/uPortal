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
package org.jasig.portal.portlet.rendering;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.CacheControl;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.portlet.container.cache.CacheState;
import org.jasig.portal.portlet.container.cache.CachedPortletData;
import org.jasig.portal.portlet.container.cache.CachedPortletResourceData;
import org.jasig.portal.portlet.container.cache.IPortletCacheControlService;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.utils.web.PortletHttpServletRequestWrapper;
import org.jasig.portal.utils.web.PortletHttpServletResponseWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.ImmutableMap;

/**
 * Tests for {@link PortletRendererImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class PortletRendererImplTest {
    @InjectMocks private PortletRendererImpl portletRenderer = new PortletRendererImpl();
    @Mock private IUrlSyntaxProvider urlSyntaxProvider;
    @Mock private IPortalEventFactory portalEventFactory;
    @Mock private IPortletCacheControlService portletCacheControlService;
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private PortletContainer portletContainer;
    @Mock private PortletDelegationLocator portletDelegationLocator;
    @Mock private IPortletWindowId portletWindowId;
    @Mock private IPortletWindow portletWindow;
    @Mock private IPortletEntity portletEntity;
    @Mock private IPortletDefinition portletDefinition;
    @Mock private PortletWindow plutoPortletWindow;
    @Mock private IPortalRequestInfo portalRequestInfo;

    private final String portletFname = "MyPortlet";
    
    /**
     * Does common setup of mock options needed for portlet execution
     */
    protected void setupPortletExecutionMocks(MockHttpServletRequest request) {
        when(portletWindowRegistry.getPortletWindow(isA(HttpServletRequest.class), eq(portletWindowId))).thenReturn(portletWindow);
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
        
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        when(portletWindow.getRenderParameters()).thenReturn(Collections.<String, String[]>emptyMap());
        when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        
        when(portletEntity.getPortletDefinition()).thenReturn(portletDefinition);
        
        when(portletDefinition.getFName()).thenReturn(portletFname);
        
        when(urlSyntaxProvider.getPortalRequestInfo(isA(HttpServletRequest.class))).thenReturn(portalRequestInfo);
    }
    
	/**
	 * {@link CacheControl} says don't cache, make sure no caching.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doRenderMarkupNoCacheControl() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = new TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
		CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(0);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletRenderState(request, portletWindowId)).thenReturn(cacheState);
        when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(false);
        when(portletCacheControlService.getCacheSizeThreshold()).thenReturn(102400);
		when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);
		
		RenderPortletOutputHandler handler = new RenderPortletOutputHandler("UTF-8");
		portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);
		
		// call 2 times
		handler = new RenderPortletOutputHandler("UTF-8");
		portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);
		
		verify(portletContainer, times(2)).doRender(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		verify(portletCacheControlService, times(2)).getPortletRenderState(request, portletWindowId);
		verify(portletCacheControlService, times(2)).getCacheSizeThreshold();
		verify(portletCacheControlService, times(2)).shouldOutputBeCached(cacheControl);
		
		verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * No cached data exists, but mock a {@link CacheControl} that will trigger the portletContainer#doRender, 
	 * capture the output, and give to the portlet cachecontrol service.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doRenderMarkupCapture() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
        
        CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = new TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
        CacheControl cacheControl = cacheState.getCacheControl();
        cacheControl.setUseCachedContent(false);
        cacheControl.setExpirationTime(300);
		
		setupPortletExecutionMocks(request);
        
        when(portletCacheControlService.getPortletRenderState(request, portletWindowId)).thenReturn(cacheState);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

        RenderPortletOutputHandler handler = new RenderPortletOutputHandler("UTF-8");
		portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);
		
		verify(portletContainer, times(1)).doRender(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
        verify(portletCacheControlService, times(1)).getPortletRenderState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletCacheControlService, times(1)).shouldOutputBeCached(cacheControl);
        verify(portletCacheControlService, times(1)).cachePortletRenderOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), isA(CachedPortletData.class));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * No cached data exists, but mock a {@link CacheControl} with a negative value for expirationtime.
	 * Will trigger the portletContainer#doRender, 
	 * capture the output, and give to the portlet cachecontrol service.
	 * 
	 * negative value for cacheControl expiration time means "cache forever."
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doRenderMarkupCaptureNegativeExpirationTime() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = new TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(-1);
		
		setupPortletExecutionMocks(request);
		when(portletCacheControlService.getPortletRenderState(request, portletWindowId)).thenReturn(cacheState);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);
		
        RenderPortletOutputHandler handler = new RenderPortletOutputHandler("UTF-8");
        portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);

        
        verify(portletContainer, times(1)).doRender(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
        verify(portletCacheControlService, times(1)).getPortletRenderState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletCacheControlService, times(1)).shouldOutputBeCached(cacheControl);
        verify(portletCacheControlService, times(1)).cachePortletRenderOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), isA(CachedPortletData.class));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Mimic workflow when cached portlet data using "expiration" method is available.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doRenderMarkupCachedContentExpirationMethodTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = new TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
		cacheState.setUseCachedData(true);
        CacheControl cacheControl = cacheState.getCacheControl();
		
        final PortletRenderResult portletResult = new PortletRenderResult("title", null, 0, 100);
        final String output = "<p>Some content</p>";
        CachedPortletData<PortletRenderResult> cachedPortletData = new CachedPortletData<PortletRenderResult>(
                portletResult, output, null, null, false, null, cacheControl.getExpirationTime());
        cacheState.setCachedPortletData(cachedPortletData);
		
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.getPortletRenderState(request, portletWindowId)).thenReturn(cacheState);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

		RenderPortletOutputHandler handler = new RenderPortletOutputHandler("UTF-8");
        portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);
		Assert.assertEquals(output, handler.getOutput());
		
		verify(portletCacheControlService, times(1)).getPortletRenderState(request, portletWindowId);
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	/**
	 * Mimic workflow when data cached portlet data using "validation" method is available.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doRenderMarkupCachedContentValidationNotExpiredMethodTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = new TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
		cacheState.setUseCachedData(true);
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setETag("123456");
		cacheControl.setExpirationTime(300);
		
        final PortletRenderResult portletResult = new PortletRenderResult("title", null, 0, 100);
        final String output = "<p>Some content</p>";
        CachedPortletData<PortletRenderResult> cachedPortletData = new CachedPortletData<PortletRenderResult>(
                portletResult, output, null, null, false, cacheControl.getETag(), cacheControl.getExpirationTime());
        cacheState.setCachedPortletData(cachedPortletData);

		setupPortletExecutionMocks(request);
		when(portletCacheControlService.getPortletRenderState(request, portletWindowId)).thenReturn(cacheState);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

        RenderPortletOutputHandler handler = new RenderPortletOutputHandler("UTF-8");
        portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);
        Assert.assertEquals(output, handler.getOutput());
        
        verify(portletCacheControlService, times(1)).getPortletRenderState(request, portletWindowId);
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Mimic workflow when data cached portlet data using "validation" method is available.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doRenderMarkupCachedContentValidationMethodExpiredTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = new TestingCacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>();
        CacheControl cacheControl = cacheState.getCacheControl();
		// by setting useCachedContent to true, we are saying even though content is expired, replay it anyways (since etag is still valid)
		cacheControl.setUseCachedContent(true);
		cacheControl.setETag("123456");
		cacheControl.setExpirationTime(300);

        
        final PortletRenderResult portletResult = new PortletRenderResult("title", null, 0, 100);
        final String output = "<p>Some content</p>";
        CachedPortletData<PortletRenderResult> cachedPortletData = new CachedPortletData<PortletRenderResult>(
                portletResult, output, null, null, false, cacheControl.getETag(), 1);
        cacheState.setCachedPortletData(cachedPortletData);
		
        final long expTime = cachedPortletData.getExpirationTime();
        
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.getPortletRenderState(request, portletWindowId)).thenReturn(cacheState);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

		RenderPortletOutputHandler handler = new RenderPortletOutputHandler("UTF-8");
        portletRenderer.doRenderMarkup(portletWindowId, request, response, handler);
        Assert.assertEquals(output, handler.getOutput());
        
		// verify the expiration time has been updated
		Assert.assertNotSame(expTime, cachedPortletData.getTimeStored());

		verify(portletCacheControlService, times(1)).getPortletRenderState(request, portletWindowId);
		verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
		verify(portletContainer, times(1)).doRender(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		verify(portletCacheControlService, times(1)).cachePortletRenderOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), isA(CachedPortletData.class));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	/**
	 * Verify invoking portletRenderer#doAction removes cached content.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doActionPurgesCachedContent() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
        
		setupPortletExecutionMocks(request);

		// doAction will trigger purge
		portletRenderer.doAction(portletWindowId, request, response);
		
		verify(portletCacheControlService, times(1)).purgeCachedPortletData(portletWindowId, request);
		verify(portletContainer, times(1)).doAction(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * {@link CacheControl} says don't cache, make sure no caching for doServeResource.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDoServeResourceNoCache() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(0);
		
		setupPortletExecutionMocks(request);
		

		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(false);
		
		ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
		
		// call 2 times
		portletRenderer.doServeResource(portletWindowId, request, response, handler);
		
		verify(portletCacheControlService, times(2)).getCacheSizeThreshold();
		verify(portletCacheControlService, times(2)).getPortletResourceState(request, portletWindowId);
		verify(portletContainer, times(2)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
		verify(portletCacheControlService, times(2)).shouldOutputBeCached(isA(CacheControl.class));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * No cached data exists, but mock a {@link CacheControl} that will trigger the portletContainer#doServeResource, 
	 * capture the output, and give to the portlet cachecontrol service.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doServeResourceCapture() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setContentType("application/octet-stream");
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(300);
		
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
        when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
		
        ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
        
        
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletContainer, times(1)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
        verify(portletCacheControlService, times(1)).shouldOutputBeCached(isA(CacheControl.class));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * No cached data exists, but mock a {@link CacheControl} with a negative value for expirationtime.
	 * Will trigger the portletContainer#doServeResource, 
	 * capture the output, and give to the portlet cachecontrol service.
	 * 
	 * negative value for cacheControl expiration time means "cache forever."
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doServeResourceMarkupCaptureNegativeExpirationTime() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setContentType("application/octet-stream");
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(-1);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
		
		ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
		
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletContainer, times(1)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
        verify(portletCacheControlService, times(1)).shouldOutputBeCached(isA(CacheControl.class));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Mimic workflow when cached portlet data using "expiration" method is available.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doServeResourceCachedContentExpirationMethodTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        final long expirationTime = System.currentTimeMillis() - 60000;
        cachedPortletData.updateExpirationTime(50000);
        cacheState.setCachedPortletData(cachedPortletResourceData);
        
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);

        ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
        
		
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletContainer, times(1)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
        verify(portletCacheControlService).cachePortletResourceOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), eq(cachedPortletResourceData));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Mimic workflow when cached portlet data using "validation" method is available.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doServeResourceCachedContentValidationMethodTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
        
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        cacheState.setCachedPortletData(cachedPortletResourceData);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);


        ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
        
		byte [] fromResponse = response.getContentAsByteArray();
		assertArrayEquals(output.getBytes(), fromResponse);
        
        
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletContainer, times(1)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
        verify(portletCacheControlService).cachePortletResourceOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), eq(cachedPortletResourceData));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Same as {@link #doServeResourceCachedContentValidationMethodTest()}, but simulate browser
	 * sending If-None-Match header that matches the etag. Verify no content returned and a 304 status code.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doServeResourceUseBrowserContentTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("If-None-Match", "123456");
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
		cacheState.setUseBrowserData(true);
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
        
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        cacheState.setCachedPortletData(cachedPortletResourceData);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
		
		ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
		//byte [] fromResponse = response.getContentAsByteArray();
		
		Assert.assertEquals(0, response.getContentLength());
		Assert.assertEquals(304, response.getStatus());

        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
    @Test
    public void doServeResourceCachedContentValidationMethodNotModifiedTest() throws PortletException, IOException, PortletContainerException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("If-None-Match", "123456");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
        cacheState.setBrowserSetEtag(true);
        CacheControl cacheControl = cacheState.getCacheControl();
        cacheControl.setUseCachedContent(true);
        cacheControl.setExpirationTime(300);
        cacheControl.setETag("123456");
        
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        cacheState.setCachedPortletData(cachedPortletResourceData);
        
        setupPortletExecutionMocks(request);
        
        when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
        
        ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
        //byte [] fromResponse = response.getContentAsByteArray();
        
        Assert.assertEquals(0, response.getContentLength());
        Assert.assertEquals(304, response.getStatus());
        
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletContainer, times(1)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
        verify(portletCacheControlService).cachePortletResourceOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), eq(cachedPortletResourceData));
        
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
    }
	
	/**
	 * Same as {@link #doServeResourceCachedContentValidationMethodTest()}, but simulate browser
	 * sending If-None-Match header with mismatched etag. Response is 200 with content and new etag
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doServeResourceCachedContentValidationMethodIfNoneMatchInvalidTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("If-None-Match", "123456");
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
		cacheState.setUseCachedData(true);
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123457");
        
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        cacheState.setCachedPortletData(cachedPortletResourceData);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
		
		ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
        
        byte [] fromResponse = response.getContentAsByteArray();
        assertArrayEquals(output.getBytes(), fromResponse);
        
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals("123457", response.getHeader("ETag"));
		
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Same as {@link #doServeResourceCachedContentValidationMethodNotModifiedTest()}, however the CachedPortletData
	 * is older than it's expiration time. Verify the renderer still detects the etag and returns 304 not modified.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void doServeResourceCachedContentValidationMethodNotModifiedInternalCacheExpiredTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("If-None-Match", "123456");
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
		cacheState.setBrowserSetEtag(true);
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
        
        final String output = "{ \"hello\": \"world\" }";
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, Collections.EMPTY_MAP, null, null, null, null);
        cacheState.setCachedPortletData(cachedPortletResourceData);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
		
		ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
		
		Assert.assertEquals(0, response.getContentLength());
		Assert.assertEquals(304, response.getStatus());
        
        
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        verify(portletCacheControlService, times(1)).getCacheSizeThreshold();
        verify(portletContainer, times(1)).doServeResource(eq(plutoPortletWindow), isA(PortletHttpServletRequestWrapper.class), isA(ResourceHttpServletResponseWrapper.class));
        verify(portletCacheControlService).cachePortletResourceOutput(eq(portletWindowId), isA(PortletHttpServletRequestWrapper.class), eq(cacheState), eq(cachedPortletResourceData));
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
	
	/**
	 * Verify headers stored in cache are replayed on the response for cached doServeResource content.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doServeResourceCachedContentReplayHeadersTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		TestingCacheState<CachedPortletResourceData<Long>, Long> cacheState = new TestingCacheState<CachedPortletResourceData<Long>, Long>();
		cacheState.setUseCachedData(true);
        CacheControl cacheControl = cacheState.getCacheControl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
        
        final String output = "{ \"hello\": \"world\" }";
        final Map<String, List<Serializable>> headers = ImmutableMap.<String, List<Serializable>>of(
                "header1", Arrays.<Serializable>asList("value1"),
                "header2", Arrays.<Serializable>asList("value2", "value3"));
        
        final CachedPortletData<Long> cachedPortletData = new CachedPortletData<Long>(
                1000l, output, null, "application/json", false, cacheControl.getETag(), cacheControl.getExpirationTime());
        final CachedPortletResourceData<Long> cachedPortletResourceData = new CachedPortletResourceData<Long>(
                cachedPortletData, headers, null, null, null, null);
        cacheState.setCachedPortletData(cachedPortletResourceData);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceState(request, portletWindowId)).thenReturn(cacheState);
		
		ResourcePortletOutputHandler handler = new ResourcePortletOutputHandler(response);
        portletRenderer.doServeResource(portletWindowId, request, response, handler);
        byte [] fromResponse = response.getContentAsByteArray();
        assertArrayEquals(output.getBytes(), fromResponse);
		Assert.assertEquals("value1", response.getHeader("header1"));
		Assert.assertEquals(Arrays.asList(new String[] {"value2", "value3"}), response.getHeaders("header2"));
        
        
        verify(portletCacheControlService, times(1)).getPortletResourceState(request, portletWindowId);
        
        verifyNoMoreInteractions(portletContainer, portletCacheControlService);
	}
}
