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

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import javax.portlet.CacheControl;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.portlet.container.CacheControlImpl;
import org.jasig.portal.portlet.container.cache.CachedPortletData;
import org.jasig.portal.portlet.container.cache.IPortletCacheControlService;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.web.PortletHttpServletRequestWrapper;
import org.jasig.portal.utils.web.PortletHttpServletResponseWrapper;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link PortletRendererImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class PortletRendererImplTest {

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
		
		PortletRendererImpl portletRenderer = new PortletRendererImpl();
		IPortletWindowId portletWindowId = mock(IPortletWindowId.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		
		IPortletCacheControlService portletCacheControlService = mock(IPortletCacheControlService.class);
		portletRenderer.setPortletCacheControlService(portletCacheControlService);
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		portletRenderer.setPortletWindowRegistry(portletWindowRegistry);
		PortletContainer portletContainer = mock(PortletContainer.class);
		portletRenderer.setPortletContainer(portletContainer);
		PortletDelegationLocator portletDelegationLocator = mock(PortletDelegationLocator.class);
		portletRenderer.setPortletDelegationLocator(portletDelegationLocator);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
		when(portletCacheControlService.getPortletCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletData(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(portletWindowId, request)).thenReturn(false);
		when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
		
		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		// call 2 times
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		verify(portletContainer, times(2)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		verify(portletCacheControlService, never()).cachePortletResourceOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(byte[].class), isA(String.class), isA(CacheControl.class));

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
		
		PortletRendererImpl portletRenderer = new PortletRendererImpl();
		IPortletWindowId portletWindowId = mock(IPortletWindowId.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		
		IPortletCacheControlService portletCacheControlService = mock(IPortletCacheControlService.class);
		portletRenderer.setPortletCacheControlService(portletCacheControlService);
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		portletRenderer.setPortletWindowRegistry(portletWindowRegistry);
		PortletContainer portletContainer = mock(PortletContainer.class);
		portletRenderer.setPortletContainer(portletContainer);
		PortletDelegationLocator portletDelegationLocator = mock(PortletDelegationLocator.class);
		portletRenderer.setPortletDelegationLocator(portletDelegationLocator);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
		when(portletCacheControlService.getPortletCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletData(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(portletWindowId, request)).thenReturn(true);
		when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
		
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
		
		PortletRendererImpl portletRenderer = new PortletRendererImpl();
		IPortletWindowId portletWindowId = mock(IPortletWindowId.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		
		IPortletCacheControlService portletCacheControlService = mock(IPortletCacheControlService.class);
		portletRenderer.setPortletCacheControlService(portletCacheControlService);
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		portletRenderer.setPortletWindowRegistry(portletWindowRegistry);
		PortletContainer portletContainer = mock(PortletContainer.class);
		portletRenderer.setPortletContainer(portletContainer);
		PortletDelegationLocator portletDelegationLocator = mock(PortletDelegationLocator.class);
		portletRenderer.setPortletDelegationLocator(portletDelegationLocator);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
		when(portletCacheControlService.getPortletCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletData(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(portletWindowId, request)).thenReturn(true);
		when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
		
		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		
		verify(portletContainer, times(1)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		
		verify(portletCacheControlService, times(1)).cachePortletRenderOutput(isA(IPortletWindowId.class), isA(HttpServletRequest.class), isA(String.class), isA(CacheControl.class));
	}
	
	/**
	 * Mimic workflow when data cached portlet data using "expiration" method is available.
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
		
		PortletRendererImpl portletRenderer = new PortletRendererImpl();
		IPortletWindowId portletWindowId = mock(IPortletWindowId.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		IPortletCacheControlService portletCacheControlService = mock(IPortletCacheControlService.class);
		portletRenderer.setPortletCacheControlService(portletCacheControlService);
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		portletRenderer.setPortletWindowRegistry(portletWindowRegistry);
		PortletDelegationLocator portletDelegationLocator = mock(PortletDelegationLocator.class);
		portletRenderer.setPortletDelegationLocator(portletDelegationLocator);
		PortletContainer portletContainer = mock(PortletContainer.class);
		portletRenderer.setPortletContainer(portletContainer);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
		when(portletCacheControlService.getPortletCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletData(portletWindowId, request)).thenReturn(cachedPortletData);
		when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
		
		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		Assert.assertEquals("<p>Some content</p>", writer.toString());
		
		// verify we enter the first branch and never execute portletContainer#doRender
		verify(portletContainer, never()).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(portletWindowId, request);
	}
	
	/**
	 * Mimic workflow when data cached portlet data using "validation" method is available.
	 * 
	 * @throws PortletContainerException 
	 * @throws IOException 
	 * @throws PortletException 
	 */
	@Test
	public void doRenderMarkupCachedContentValidationMethodTest() throws PortletException, IOException, PortletContainerException {
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
		
		PortletRendererImpl portletRenderer = new PortletRendererImpl();
		IPortletWindowId portletWindowId = mock(IPortletWindowId.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		IPortletCacheControlService portletCacheControlService = mock(IPortletCacheControlService.class);
		portletRenderer.setPortletCacheControlService(portletCacheControlService);
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		portletRenderer.setPortletWindowRegistry(portletWindowRegistry);
		PortletDelegationLocator portletDelegationLocator = mock(PortletDelegationLocator.class);
		portletRenderer.setPortletDelegationLocator(portletDelegationLocator);
		PortletContainer portletContainer = mock(PortletContainer.class);
		portletRenderer.setPortletContainer(portletContainer);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
		when(portletCacheControlService.getPortletCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletData(portletWindowId, request)).thenReturn(cachedPortletData);
		when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
		
		StringWriter writer = new StringWriter();
		portletRenderer.doRenderMarkup(portletWindowId, request, response, writer);
		Assert.assertEquals("<p>Some content</p>", writer.toString());
		
		// presence of etag will skip the expiration cache branch and trigger a doRender
		verify(portletContainer, times(1)).doRender(isA(PortletWindow.class), isA(PortletHttpServletRequestWrapper.class), isA(PortletHttpServletResponseWrapper.class));
		// verify we never enter the other branch of the "should render cached output" if statement
		verify(portletCacheControlService, never()).shouldOutputBeCached(portletWindowId, request);
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
		
		PortletRendererImpl portletRenderer = new PortletRendererImpl();
		IPortletWindowId portletWindowId = mock(IPortletWindowId.class);
		IPortletWindow portletWindow = mock(IPortletWindow.class);
		IPortletCacheControlService portletCacheControlService = mock(IPortletCacheControlService.class);
		portletRenderer.setPortletCacheControlService(portletCacheControlService);
		IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
		portletRenderer.setPortletWindowRegistry(portletWindowRegistry);
		PortletDelegationLocator portletDelegationLocator = mock(PortletDelegationLocator.class);
		portletRenderer.setPortletDelegationLocator(portletDelegationLocator);
		PortletContainer portletContainer = mock(PortletContainer.class);
		
		portletRenderer.setPortletContainer(portletContainer);
		
		PortletWindow plutoPortletWindow = mock(PortletWindow.class);
		when(portletWindow.getPlutoPortletWindow()).thenReturn(plutoPortletWindow);
		when(portletCacheControlService.getPortletCacheControl(portletWindowId, request)).thenReturn(cacheControl);
		when(portletCacheControlService.getCachedPortletData(portletWindowId, request)).thenReturn(null);
		when(portletCacheControlService.shouldOutputBeCached(portletWindowId, request)).thenReturn(true);
		when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
		

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
}
