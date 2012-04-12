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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.portlet.CacheControl;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.commons.lang.time.DateUtils;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.portlet.container.CacheControlImpl;
import org.jasig.portal.portlet.container.cache.CachedPortletData;
import org.jasig.portal.portlet.container.cache.CachingPortletHttpServletResponseWrapper;
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
        when(portletWindow.getRenderParameters()).thenReturn(Collections.<String, String[]>emptyMap());
        when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
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
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(0);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
        when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(null);
        when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(false);
		when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);
		
		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		// call 2 times
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		verify(portletContainer, times(2)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		verify(portletCacheControlService, never()).cachePortletRenderOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(String.class), isA(CacheControl.class));

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
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(300);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		verify(portletContainer, times(1)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		
		verify(portletCacheControlService, times(1)).cachePortletRenderOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(String.class), isA(CacheControl.class));
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
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(-1);
		
		setupPortletExecutionMocks(request);
		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);
		
		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		verify(portletContainer, times(1)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		
		verify(portletCacheControlService, times(1)).cachePortletRenderOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(String.class), isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setStringData("<p>Some content</p>");
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(cachedPortletData);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		Assert.assertEquals("<p>Some content</p>", writer.toString());
		
		// verify we enter the first branch and never execute portletContainer#doRender
		verify(portletContainer, never()).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(cacheControl);
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setETag("123456");
		cacheControl.setExpirationTime(300);
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setStringData("<p>Some content</p>");
		cachedPortletData.setEtag("123456");
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(cachedPortletData);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		Assert.assertEquals("<p>Some content</p>", writer.toString());
		
		// verify we enter the first branch and never execute portletContainer#doRender
		verify(portletContainer, never()).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(cacheControl);
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		// by setting useCachedContent to true, we are saying even though content is expired, replay it anyways (since etag is still valid)
		cacheControl.setUseCachedContent(true);
		cacheControl.setETag("123456");
		cacheControl.setExpirationTime(300);
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setStringData("<p>Some content</p>");
		cachedPortletData.setEtag("123456");
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		// set the time stored to be a time that will trigger the content to be expired
		Date expiredTime = DateUtils.addSeconds(now, -301); 
		cachedPortletData.setTimeStored(expiredTime);
		
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(cachedPortletData);
        when(portalRequestInfo.getTargetedPortletWindowId()).thenReturn(portletWindowId);

		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		Assert.assertEquals("<p>Some content</p>", writer.toString());
		// verify the expiration time has been updated
		Assert.assertNotSame(expiredTime, cachedPortletData.getTimeStored());
		// context is expired, triggers doRender
		verify(portletContainer, times(1)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		
		cacheControl.setETag("123456");
		cacheControl.setExpirationTime(300);
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setStringData("<p>Some content</p>");
		cachedPortletData.setEtag("123456");
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletRenderCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);

		StringWriter writer = new StringWriter();
		
		// doAction will trigger purge
		portletRenderer.doAction(portletWindowId, request, response);
		verify(portletCacheControlService, times(1)).purgeCachedPortletData(portletWindowId, request, cacheControl);
		verify(portletContainer, times(1)).doAction(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		verify(portletContainer, times(1)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, times(1)).cachePortletRenderOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(String.class), isA(CacheControl.class));
		
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
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(0);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(false);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		
		// call 2 times
		portletRenderer.doServeResource(portletWindowId, request, response);
		
		verify(portletContainer, times(2)).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		verify(portletCacheControlService, never()).cachePortletResourceOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(CachedPortletData.class), isA(CacheControl.class));
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
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(300);
		
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		
		verify(portletContainer, times(1)).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		
		verify(portletCacheControlService, times(1)).cachePortletResourceOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(CachedPortletData.class), isA(CacheControl.class));
	}
	
	/**
	 * Set a bad status code and verify {@link PortletRendererImpl#cacheOutputIfNecessary(CacheControl, CachingPortletHttpServletResponseWrapper, IPortletWindowId, IPortletWindow, HttpServletRequest)}
	 * does not invoke {@link IPortletCacheControlService#cachePortletResourceOutput(IPortletWindowId, HttpServletRequest, CachedPortletData, CacheControl)}.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void testCacheOutputIfNecessaryBadStatus() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(300);
			
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
		
		
		CachingPortletHttpServletResponseWrapper responseWrapper = new CachingPortletHttpServletResponseWrapper(response, 100);
		PrintWriter writer = responseWrapper.getWriter();
		for(int i = 0; i <= 10; i++) {
			writer.print('a');
		}
		responseWrapper.setStatus(999);
		Assert.assertFalse(responseWrapper.isThresholdExceeded());
		portletRenderer.cacheOutputIfNecessary(cacheControl, responseWrapper, portletWindowId, portletWindow, request);
			
		// cachePortletResourceOutput MUST not be triggered since a bad status code was set
		verify(portletCacheControlService, times(0)).cachePortletResourceOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(CachedPortletData.class), isA(CacheControl.class));
	}
	
	/**
	 * Exceed the cache output threshold and verify {@link PortletRendererImpl#cacheOutputIfNecessary(CacheControl, CachingPortletHttpServletResponseWrapper, IPortletWindowId, IPortletWindow, HttpServletRequest)}
	 * does not invoke {@link IPortletCacheControlService#cachePortletResourceOutput(IPortletWindowId, HttpServletRequest, CachedPortletData, CacheControl)}.
	 * 
	 * @throws PortletException
	 * @throws IOException
	 * @throws PortletContainerException
	 */
	@Test
	public void testCacheOutputIfNecessaryThresholdExceeded() throws PortletException, IOException, PortletContainerException {	
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(300);
			
		setupPortletExecutionMocks(request);

		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
		
		CachingPortletHttpServletResponseWrapper responseWrapper = new CachingPortletHttpServletResponseWrapper(response, 100);
		PrintWriter writer = responseWrapper.getWriter();
		for(int i = 0; i <= 101; i++) {
			writer.print('a');
		}
		Assert.assertTrue(responseWrapper.isThresholdExceeded());
		portletRenderer.cacheOutputIfNecessary(cacheControl, responseWrapper, portletWindowId, portletWindow, request);
			
		// cachePortletResourceOutput MUST not be triggered since the threshold was exceeded
		verify(portletCacheControlService, times(0)).cachePortletResourceOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(CachedPortletData.class), isA(CacheControl.class));
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
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(false);
		cacheControl.setExpirationTime(-1);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(cacheControl)).thenReturn(true);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		
		verify(portletContainer, times(1)).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		
		verify(portletCacheControlService, times(1)).cachePortletResourceOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(CachedPortletData.class), isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setContentType("application/json");
		byte [] content = "{ \"hello\": \"world\" }".getBytes();
		cachedPortletData.setByteData(content);
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(cachedPortletData);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		// verify content matches what was in cache (no array support in Assert.assertEquals, check byte for byte)
		byte [] fromResponse = response.getContentAsByteArray();
		Assert.assertEquals(content.length, fromResponse.length);
		for(int i = 0; i < content.length; i++) {
			Assert.assertEquals(content[i], fromResponse[i]);
		}
		// verify we enter the first branch and never execute portletContainer#doServeResource
		verify(portletContainer, never()).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setContentType("application/json");
		byte [] content = "{ \"hello\": \"world\" }".getBytes();
		cachedPortletData.setByteData(content);
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setEtag("123456");
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(cachedPortletData);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		// verify content matches what was in cache (no array support in Assert.assertEquals, check byte for byte)
		byte [] fromResponse = response.getContentAsByteArray();
		Assert.assertEquals(content.length, fromResponse.length);
		for(int i = 0; i < content.length; i++) {
			Assert.assertEquals(content[i], fromResponse[i]);
		}
		Assert.assertEquals(200, response.getStatus());
		// verify we enter the first branch and never execute portletContainer#doServeResource
		verify(portletContainer, never()).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
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
	public void doServeResourceCachedContentValidationMethodNotModifiedTest() throws PortletException, IOException, PortletContainerException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("If-None-Match", "123456");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setContentType("application/json");
		byte [] content = "{ \"hello\": \"world\" }".getBytes();
		cachedPortletData.setByteData(content);
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setEtag("123456");
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(cachedPortletData);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		//byte [] fromResponse = response.getContentAsByteArray();
		
		Assert.assertEquals(0, response.getContentLength());
		Assert.assertEquals(304, response.getStatus());
		// verify we enter the first branch and never execute portletContainer#doServeResource
		verify(portletContainer, never()).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123457");
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setContentType("application/json");
		byte [] content = "{ \"hello\": \"world\" }".getBytes();
		cachedPortletData.setByteData(content);
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setEtag("123457");
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(cachedPortletData);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		// verify content matches what was in cache (no array support in Assert.assertEquals, check byte for byte)
		byte [] fromResponse = response.getContentAsByteArray();
		Assert.assertEquals(content.length, fromResponse.length);
		for(int i = 0; i < content.length; i++) {
			Assert.assertEquals(content[i], fromResponse[i]);
		}
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals("123457", response.getHeader("ETag"));
		// verify we enter the first branch and never execute portletContainer#doServeResource
		verify(portletContainer, never()).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		cacheControl.setETag("123456");
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setContentType("application/json");
		byte [] content = "{ \"hello\": \"world\" }".getBytes();
		cachedPortletData.setByteData(content);
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setEtag("123456");
		// set Time stored to a value prior to the expiration time
		cachedPortletData.setTimeStored(DateUtils.addSeconds(now, -310));
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(cachedPortletData);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		
		Assert.assertEquals(0, response.getContentLength());
		Assert.assertEquals(304, response.getStatus());
		// since the cached content is expired, a doServeResource is going to be invoked
		verify(portletContainer, times(1)).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// portlet said we should useCachedContent, so don't expect an attempt to "cache output"
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
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
		Date now = new Date();
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setUseCachedContent(true);
		cacheControl.setExpirationTime(300);
		CachedPortletData cachedPortletData = new CachedPortletData();
		cachedPortletData.setContentType("application/json");
		byte [] content = "{ \"hello\": \"world\" }".getBytes();
		Map<String, List<Object>> headers = ImmutableMap.<String, List<Object>>of(
		        "header1", Arrays.<Object>asList("value1"),
		        "header2", Arrays.<Object>asList("value2", "value3"));
		        
		cachedPortletData.setHeaders(headers);
		cachedPortletData.setByteData(content);
		cachedPortletData.setExpirationTimeSeconds(cacheControl.getExpirationTime());
		cachedPortletData.setTimeStored(now);
		
		setupPortletExecutionMocks(request);
		
		when(portletCacheControlService.getPortletResourceCacheControl(portletWindowId, request, response)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, request)).thenReturn(cachedPortletData);
		
		portletRenderer.doServeResource(portletWindowId, request, response);
		// verify content matches what was in cache (no array support in Assert.assertEquals, check byte for byte)
		byte [] fromResponse = response.getContentAsByteArray();
		Assert.assertEquals(content.length, fromResponse.length);
		for(int i = 0; i < content.length; i++) {
			Assert.assertEquals(content[i], fromResponse[i]);
		}
		Assert.assertEquals("value1", response.getHeader("header1"));
		Assert.assertEquals(Arrays.asList(new String[] {"value2", "value3"}), response.getHeaders("header2"));
		// verify we enter the first branch and never execute portletContainer#doServeResource
		verify(portletContainer, never()).doServeResource(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(isA(CacheControl.class));
	}
}
