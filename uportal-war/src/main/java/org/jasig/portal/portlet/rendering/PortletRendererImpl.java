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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.CacheControl;
import javax.portlet.Event;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.portlet.OutputCapturingHttpServletResponseWrapper;
import org.jasig.portal.portlet.PortletDispatchException;
import org.jasig.portal.portlet.container.cache.CacheState;
import org.jasig.portal.portlet.container.cache.CachedPortletData;
import org.jasig.portal.portlet.container.cache.CachedPortletResourceData;
import org.jasig.portal.portlet.container.cache.CachingPortletHttpServletResponseWrapper;
import org.jasig.portal.portlet.container.cache.CachingPortletOutputHandler;
import org.jasig.portal.portlet.container.cache.CachingPortletResourceOutputHandler;
import org.jasig.portal.portlet.container.cache.HeaderSettingCacheControl;
import org.jasig.portal.portlet.container.cache.IPortletCacheControlService;
import org.jasig.portal.portlet.container.cache.PortletCachingHeaderUtils;
import org.jasig.portal.portlet.container.services.AdministrativeRequestListenerController;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.worker.HungWorkerAnalyzer;
import org.jasig.portal.portlet.session.PortletSessionAdministrativeRequestListener;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.url.ParameterMap;
import org.jasig.portal.utils.web.PortletHttpServletRequestWrapper;
import org.jasig.portal.utils.web.PortletHttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Executes methods on portlets using Pluto
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletRendererImpl implements IPortletRenderer {
	protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPersonManager personManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private PortletContainer portletContainer;
    private PortletDelegationLocator portletDelegationLocator;
    private IPortletCacheControlService portletCacheControlService;
    private IPortalEventFactory portalEventFactory;
    private IUrlSyntaxProvider urlSyntaxProvider;
    private HungWorkerAnalyzer hungWorkerAnalyzer;

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
    @Autowired
    public void setPortalEventFactory(IPortalEventFactory portalEventFactory) {
        this.portalEventFactory = portalEventFactory;
    }
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    @Autowired
    public void setPortletContainer(PortletContainer portletContainer) {
        this.portletContainer = portletContainer;
    }
    @Autowired
    public void setPortletDelegationLocator(PortletDelegationLocator portletDelegationLocator) {
        this.portletDelegationLocator = portletDelegationLocator;
    }
    /**
	 * @param portletCacheControlService the portletCacheControlService to set
	 */
    @Autowired
	public void setPortletCacheControlService(
			IPortletCacheControlService portletCacheControlService) {
		this.portletCacheControlService = portletCacheControlService;
	}
    @Autowired
    public void setHungWorkerAnalyzer(HungWorkerAnalyzer hungWorkerAnalyzer) {
        this.hungWorkerAnalyzer = hungWorkerAnalyzer;
    }
	
	
	/**
	 * PLT 22.1 If the content of a portlet is cached and the portlet is target of request 
     * with an action-type semantic (e.g. an action or event call), the portlet container should discard the cache and
     * invoke the corresponding request handling methods of the portlet like processAction,or processEvent.
     * 
	 *  (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.IPortletRenderer#doAction(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public long doAction(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    	this.portletCacheControlService.purgeCachedPortletData(portletWindowId, httpServletRequest);
    	
    	final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "'");
        }
        
        final long start = System.nanoTime();
        try {
            this.portletContainer.doAction(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing action on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing action.", portletWindow, ioe);
        }
        
        final long executionTime = System.nanoTime() - start;
        
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, false);
        this.portalEventFactory.publishPortletActionExecutionEvent(httpServletRequest, this, fname, executionTime, parameters);
        
        return executionTime;
    }
    
    /**
     * PLT 22.1 If the content of a portlet is cached and the portlet is target of request 
     * with an action-type semantic (e.g. an action or event call), the portlet container should discard the cache and
     * invoke the corresponding request handling methods of the portlet like processAction,or processEvent.
     * 
     * (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doEvent(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.portlet.Event)
     */
    @Override
    public long doEvent(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Event event) {
    	this.portletCacheControlService.purgeCachedPortletData(portletWindowId, httpServletRequest);
    	
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet event for window '" + portletWindow + "'");
        }
        
        final long start = System.nanoTime();
        try {
            this.portletContainer.doEvent(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse, event);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing event.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing event on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing event.", portletWindow, ioe);
        }
        
        final long executionTime = System.nanoTime() - start;
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, false);
        this.portalEventFactory.publishPortletEventExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, event.getQName());
        
        return executionTime;
    }
    
    @Override
    public PortletRenderResult doRenderHeader(IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, PortletOutputHandler portletOutputHandler) throws IOException  {
        
        return doRender(portletWindowId,
                httpServletRequest,
                httpServletResponse,
                portletOutputHandler,
                RenderPart.HEADERS);
    }
    
    
    /**
     * Interacts with the {@link IPortletCacheControlService} to determine if the markup should come from cache or not.
     * If cached data doesn't exist or is expired, this delegates to {@link #doRenderMarkupInternal(IPortletWindowId, HttpServletRequest, HttpServletResponse, Writer)}.
     * @throws IOException 
     */
    @Override
    public PortletRenderResult doRenderMarkup(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, PortletOutputHandler portletOutputHandler) throws IOException {
        
        return doRender(portletWindowId,
                httpServletRequest,
                httpServletResponse,
                portletOutputHandler,
                RenderPart.MARKUP);
    }
    
    /**
     * Describes the part of the render request and defines the part specific behaviors
     */
    protected enum RenderPart {
        HEADERS(PortletRequest.RENDER_HEADERS) {
            @Override
            public CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> getCacheState(
                    IPortletCacheControlService portletCacheControlService, HttpServletRequest request,
                    IPortletWindowId portletWindowId) {
                return portletCacheControlService.getPortletRenderHeaderState(request, portletWindowId);
            }

            @Override
            public void cachePortletOutput(IPortletCacheControlService portletCacheControlService,
                    IPortletWindowId portletWindowId, HttpServletRequest request,
                    CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData) {

                portletCacheControlService.cachePortletRenderHeaderOutput(portletWindowId,
                        request,
                        cacheState,
                        cachedPortletData);
            }

            @Override
            public void publishRenderExecutionEvent(IPortalEventFactory portalEventFactory, PortletRendererImpl source,
                    HttpServletRequest request, String fname, long executionTime, Map<String, List<String>> parameters,
                    boolean targeted, boolean cached) {
                portalEventFactory.publishPortletRenderHeaderExecutionEvent(request,
                        source,
                        fname,
                        executionTime,
                        parameters,
                        targeted,
                        cached);
            }
        },
        MARKUP(PortletRequest.RENDER_MARKUP) {
            @Override
            public CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> getCacheState(
                    IPortletCacheControlService portletCacheControlService, HttpServletRequest request,
                    IPortletWindowId portletWindowId) {
                return portletCacheControlService.getPortletRenderState(request, portletWindowId);
            }

            @Override
            public void cachePortletOutput(IPortletCacheControlService portletCacheControlService,
                    IPortletWindowId portletWindowId, HttpServletRequest request,
                    CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData) {

                portletCacheControlService.cachePortletRenderOutput(portletWindowId,
                        request,
                        cacheState,
                        cachedPortletData);
            }

            @Override
            public void publishRenderExecutionEvent(IPortalEventFactory portalEventFactory, PortletRendererImpl source,
                    HttpServletRequest request, String fname, long executionTime, Map<String, List<String>> parameters,
                    boolean targeted, boolean cached) {
                portalEventFactory.publishPortletRenderExecutionEvent(request,
                        source,
                        fname,
                        executionTime,
                        parameters,
                        targeted,
                        cached);
            }
        };
        
        private final String renderPart;
        
        private RenderPart(String renderPart) {
            this.renderPart = renderPart;
        }
        
        /**
         * Get the cache state for the request
         */
        public abstract CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> getCacheState(
                IPortletCacheControlService portletCacheControlService, HttpServletRequest request,
                IPortletWindowId portletWindowId);
        
        /**
         * Cache the portlet output
         */
        public abstract void cachePortletOutput(IPortletCacheControlService portletCacheControlService,
                IPortletWindowId portletWindowId, HttpServletRequest request,
                CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState, CachedPortletData<PortletRenderResult> cachedPortletData);
        
        /**
         * Public portlet event
         */
        public abstract void publishRenderExecutionEvent(IPortalEventFactory portalEventFactory,
                PortletRendererImpl source, HttpServletRequest request, String fname, long executionTime,
                Map<String, List<String>> parameters, boolean targeted, boolean cached);

        /**
         * @return The {@link PortletRequest#RENDER_PART} name
         */
        public final String getRenderPart() {
            return renderPart;
        }

        /**
         * Determine the {@link RenderPart} from the {@link PortletRequest#RENDER_PART}
         */
        public static RenderPart getRenderPart(String renderPart) {
            if (PortletRequest.RENDER_HEADERS.equals(renderPart)) {
                return HEADERS;
            }
            
            if (PortletRequest.RENDER_MARKUP.equals(renderPart)) {
                return MARKUP;
            }

            throw new IllegalArgumentException("Unknown " + PortletRequest.RENDER_PART + " specified: " + renderPart);
        }
    }
        
    protected PortletRenderResult doRender(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, PortletOutputHandler portletOutputHandler, RenderPart renderPart)
            throws IOException {
        
        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState = renderPart
                .getCacheState(this.portletCacheControlService, httpServletRequest, portletWindowId);

        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        /*
         * If the portlet is rendering in EXCLUSIVE WindowState ignore the provided PortletOutputHandler and
         * write directly to the response.
         * 
         * THIS IS VERY BAD AND SHOULD BE DEPRECATED ALONG WITH EXCLUSIVE WINDOW STATE
         */
        if (EXCLUSIVE.equals(portletWindow.getWindowState())) {
            portletOutputHandler = new ResourcePortletOutputHandler(httpServletResponse);
        }

        if (cacheState.isUseCachedData()) {
    		return doRenderReplayCachedContent(portletWindow, httpServletRequest, cacheState, portletOutputHandler, renderPart, 0);
    	}
        
        final int cacheSizeThreshold = this.portletCacheControlService.getCacheSizeThreshold();
        final CachingPortletOutputHandler cachingPortletOutputHandler = new CachingPortletOutputHandler(portletOutputHandler, cacheSizeThreshold);

        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        final CacheControl cacheControl = cacheState.getCacheControl();
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_CACHE_CONTROL, cacheControl);
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_OUTPUT_HANDLER, cachingPortletOutputHandler);
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet body for window '" + portletWindow + "'");
        }

        final long renderStartTime = System.nanoTime();
        try {
            httpServletRequest.setAttribute(PortletRequest.RENDER_PART, renderPart.getRenderPart());
            this.portletContainer.doRender(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);     
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderMarkup.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing renderMarkup on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderMarkup.", portletWindow, ioe);
        }
        
        final long executionTime = System.nanoTime() - renderStartTime;
        
        //See if the portlet signaled to use the cached content
        final boolean useCachedContent = cacheControl.useCachedContent();
        if (useCachedContent) {
            final CachedPortletData<PortletRenderResult> cachedPortletData = cacheState.getCachedPortletData();
            if (cachedPortletData == null) {
                throw new PortletDispatchException(
                        "The portlet window '" + portletWindow + "' indicated via CacheControl#useCachedContent " +
                		"that the portal should render cached content, however there is no cached content to return. " +
                		"This is a portlet bug.",
                        portletWindow);
            }
            
            //Update the expiration time and re-store in the cache
            cachedPortletData.updateExpirationTime(cacheControl.getExpirationTime());
            
            renderPart.cachePortletOutput(portletCacheControlService, portletWindowId, httpServletRequest, cacheState, cachedPortletData);
            
            return doRenderReplayCachedContent(portletWindow, httpServletRequest, cacheState, portletOutputHandler, renderPart, executionTime);
        }
        
        publishRenderEvent(portletWindow, httpServletRequest, renderPart, executionTime, false);

        //Build the render result
        final PortletRenderResult portletRenderResult = constructPortletRenderResult(httpServletRequest, executionTime);

        //Check if the portlet's output should be cached
        if (cacheState != null) {
            boolean shouldCache = this.portletCacheControlService.shouldOutputBeCached(cacheControl);

            if (shouldCache) {
                final CachedPortletData<PortletRenderResult> cachedPortletData = cachingPortletOutputHandler
                        .getCachedPortletData(portletRenderResult, cacheControl);

                if (cachedPortletData != null) {
                    renderPart.cachePortletOutput(portletCacheControlService,
                            portletWindowId,
                            httpServletRequest,
                            cacheState,
                            cachedPortletData);
                }
            }
        }
        
        return portletRenderResult;
    }
    
    /**
     * Replay the cached content inside the {@link CachedPortletData} as the response to a doRender.
     */
    protected PortletRenderResult doRenderReplayCachedContent(IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest, CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState,
            PortletOutputHandler portletOutputHandler, RenderPart renderPart, long baseExecutionTime) throws IOException {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Replaying cached content for Render " + renderPart + " request to " + portletWindow);
        }
        
    	final long renderStartTime = System.nanoTime();
    	
    	final CachedPortletData<PortletRenderResult> cachedPortletData = cacheState.getCachedPortletData();
        cachedPortletData.replay(portletOutputHandler);
    	
    	final long executionTime = baseExecutionTime + (System.nanoTime() - renderStartTime);

        publishRenderEvent(portletWindow, httpServletRequest, renderPart, executionTime, true);
        
        final PortletRenderResult portletResult = cachedPortletData.getPortletResult();
        return new PortletRenderResult(portletResult, executionTime);
    }

    /**
     * Publish the portlet render event
     */
    protected void publishRenderEvent(IPortletWindow portletWindow, HttpServletRequest httpServletRequest,
            RenderPart renderPart, long executionTime, boolean cached) {

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        //Determine if the portlet was targeted
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final boolean targeted = portletWindowId.equals(portalRequestInfo.getTargetedPortletWindowId());

        //Get the portlet's fname
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();

        //Get the portlet's parameters
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, true);

        renderPart.publishRenderExecutionEvent(this.portalEventFactory,
                this,
                httpServletRequest,
                fname,
                executionTime,
                parameters,
                targeted,
                cached);
    }
    
    /**
     * Construct a {@link PortletRenderResult} from information in the {@link HttpServletRequest}.
     * The second argument is how long the render action took.
     * 
     * @param httpServletRequest
     * @param renderTime
     * @return an appropriate {@link PortletRenderResult}, never null
     */
    protected PortletRenderResult constructPortletRenderResult(HttpServletRequest httpServletRequest, long renderTime) {
    	 final String title = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE);
         final String newItemCountString = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_NEW_ITEM_COUNT);
         final int newItemCount;
         if (newItemCountString != null && StringUtils.isNumeric(newItemCountString)) {
             newItemCount = Integer.parseInt(newItemCountString);
         } else {
             newItemCount = 0;
         }
         final String link = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_LINK);
         
         return new PortletRenderResult(title, link, newItemCount, renderTime);
    }
	
	@Override
    public long doServeResource(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, PortletResourceOutputHandler portletOutputHandler) throws IOException {
	    
        final CacheState<CachedPortletResourceData<Long>, Long> cacheState = this.portletCacheControlService
                .getPortletResourceState(httpServletRequest, portletWindowId);

        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        if (cacheState.isUseBrowserData()) {
            return doResourceReplayBrowserContent(portletWindow, httpServletRequest, cacheState, portletOutputHandler);
        }
        
        if (cacheState.isUseCachedData()) {
            return doResourceReplayCachedContent(portletWindow, httpServletRequest, cacheState, portletOutputHandler, 0);
        }
        
        final int cacheSizeThreshold = this.portletCacheControlService.getCacheSizeThreshold();
        final CachingPortletResourceOutputHandler cachingPortletOutputHandler = new CachingPortletResourceOutputHandler(portletOutputHandler, cacheSizeThreshold);

        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //TODO? CachingPortletHttpServletResponseWrapper responseWrapper = this.setupCachingPortletResponse(httpServletResponse, this.portletCacheControlService.getCacheSizeThreshold());
        
        CacheControl cacheControl = cacheState.getCacheControl();
        //Wrap the cache control so it immediately sets the caching related response headers
        cacheControl = new HeaderSettingCacheControl(cacheControl, cachingPortletOutputHandler);
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_CACHE_CONTROL, cacheControl);
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_OUTPUT_HANDLER, cachingPortletOutputHandler);
        
        //Add PrintWriter/ServletOutputStream wrapper
        httpServletResponse = new ResourceHttpServletResponseWrapper(httpServletResponse, cacheControl);
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing resource request for window '" + portletWindow + "'");
        }
	    
		final long start = System.nanoTime();
		try {
            this.portletContainer.doServeResource(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing serveResource.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing serveResource on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing serveResource.", portletWindow, ioe);
        }
        final long executionTime = System.nanoTime() - start;
		
        //See if the portlet signaled to use the cached content
        final boolean useCachedContent = cacheControl.useCachedContent();
        if (useCachedContent) {
            final CachedPortletResourceData<Long> cachedPortletResourceData = cacheState.getCachedPortletData();
            
            if (cachedPortletResourceData != null) {
                //Update the expiration time and re-store in the cache
                final CachedPortletData<Long> cachedPortletData = cachedPortletResourceData.getCachedPortletData();
                cachedPortletData.updateExpirationTime(cacheControl.getExpirationTime());
                this.portletCacheControlService.cachePortletResourceOutput(portletWindowId, httpServletRequest, cacheState, cachedPortletResourceData);
            }
            
            if (cacheState.isBrowserSetEtag()) {
                //Browser-side content matches, send a 304
                return doResourceReplayBrowserContent(portletWindow, httpServletRequest, cacheState, portletOutputHandler);
            }
            
            return doResourceReplayCachedContent(portletWindow, httpServletRequest, cacheState, cachingPortletOutputHandler, executionTime);
        }
        
		publishResourceEvent(portletWindow, httpServletRequest, executionTime, false, false);
		
        if (cacheState != null) {
            boolean shouldCache = this.portletCacheControlService.shouldOutputBeCached(cacheControl);

            if (shouldCache) {
                final CachedPortletResourceData<Long> cachedPortletResourceData = cachingPortletOutputHandler
                        .getCachedPortletResourceData(executionTime, cacheControl);

                if (cachedPortletResourceData != null) {
                    this.portletCacheControlService.cachePortletResourceOutput(portletWindowId,
                            httpServletRequest,
                            cacheState,
                            cachedPortletResourceData);
                }
            }
        }
		
		return executionTime;
	}
    
    protected long doResourceReplayBrowserContent(IPortletWindow portletWindow, HttpServletRequest httpServletRequest,
            CacheState<CachedPortletResourceData<Long>, Long> cacheState,
            PortletResourceOutputHandler portletOutputHandler) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Sending 304 for resource request to " + portletWindow);
        }
        
        if (portletOutputHandler.isCommitted()) {
            throw new IllegalStateException("Attempting to send 304 but response is already committed");
        }

        final long start = System.nanoTime();
    
        portletOutputHandler.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        
        final CachedPortletResourceData<Long> cachedPortletResourceData = cacheState.getCachedPortletData();
        if (cachedPortletResourceData != null) {
            //Freshen up the various caching related headers
            final CachedPortletData<Long> cachedPortletData = cachedPortletResourceData.getCachedPortletData();
            PortletCachingHeaderUtils.setCachingHeaders(cachedPortletData, portletOutputHandler);
        }
        
        final long executionTime = System.nanoTime() - start;
        
        publishResourceEvent(portletWindow, httpServletRequest, executionTime, true, false);
        
        return executionTime;
    }
    
    /**
     * Replay the cached content inside the {@link CachedPortletData} as the response to a doResource.
     */
    protected Long doResourceReplayCachedContent(IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest, CacheState<CachedPortletResourceData<Long>, Long> cacheState,
            PortletResourceOutputHandler portletOutputHandler, long baseExecutionTime) throws IOException {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Replaying cached content for resource request to " + portletWindow);
        }
        
        final long renderStartTime = System.nanoTime();
        
        final CachedPortletResourceData<Long> cachedPortletResourceData = cacheState.getCachedPortletData();
        if (cachedPortletResourceData == null) {
            throw new PortletDispatchException(
                    "The portlet window '" + portletWindow + "' indicated via CacheControl#useCachedContent " +
                    "that the portal should render cached content, however there is no cached content to return. " +
                    "This is a portlet bug.",
                    portletWindow);
        }
        
        cachedPortletResourceData.replay(portletOutputHandler);
        
        final long executionTime = baseExecutionTime + (System.nanoTime() - renderStartTime);

        publishResourceEvent(portletWindow, httpServletRequest, executionTime, false, true);
        
        return executionTime;
    }
    
    /**
     * The portlet resource request resourceId
     */
    protected String getResourceId(IPortletWindowId portletWindowId, final IPortalRequestInfo portalRequestInfo) {
        final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getPortletRequestInfo(portletWindowId);
        if (portletRequestInfo == null) {
            return null;
        }
        
        return portletRequestInfo.getResourceId();
    }

    /**
     * Publish the portlet resource event
     */
    protected void publishResourceEvent(IPortletWindow portletWindow, HttpServletRequest httpServletRequest,
            long executionTime, boolean usedBrowserCache, boolean usedPortalCache) {

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        //Get the portlet's fname
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();

        //Get the portlet's parameters
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, true);

        //Get the resource Id
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final String resourceId = getResourceId(portletWindowId, portalRequestInfo);

        this.portalEventFactory.publishPortletResourceExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, resourceId, usedBrowserCache, usedPortalCache);
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doReset(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doReset(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
		if(portletWindow != null) {
			portletWindow.setPortletMode(PortletMode.VIEW);
			portletWindow.setRenderParameters(new ParameterMap());
			portletWindow.setExpirationCache(null);

			final StringWriter responseOutput = new StringWriter();

			httpServletRequest = this.setupPortletRequest(httpServletRequest);
			httpServletResponse = new OutputCapturingHttpServletResponseWrapper(httpServletResponse, new PrintWriter(responseOutput));

			httpServletRequest.setAttribute(AdministrativeRequestListenerController.DEFAULT_LISTENER_KEY_ATTRIBUTE, "sessionActionListener");
			httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.ACTION, PortletSessionAdministrativeRequestListener.SessionAction.CLEAR);
			httpServletRequest.setAttribute(PortletSessionAdministrativeRequestListener.SCOPE, PortletSession.PORTLET_SCOPE);

			try {
				this.portletContainer.doAdmin(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
			}
			catch (PortletException pe) {
				throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing admin command to clear session.", portletWindow, pe);
			}
			catch (PortletContainerException pce) {
				throw new PortletDispatchException("The portlet container threw an exception while executing admin command to clear session on portlet window '" + portletWindow + "'.", portletWindow, pce);
			}
			catch (IOException ioe) {
				throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing admin command to clear session.", portletWindow, ioe);
			}

			final StringBuffer initResults = responseOutput.getBuffer();
			if (initResults.length() > 0) {
				throw new PortletDispatchException("Content was written to response during reset of portlet window '" + portletWindow + "'. Response Content: " + initResults, portletWindow);
			}
		} else {
			logger.debug("ignoring doReset as portletWindowRegistry#getPortletWindow returned a null result for portletWindowId " + portletWindowId);
		}
    }
    
    @Override
    public HungWorkerAnalyzer getHungWorkerAnalyzer() {
        return hungWorkerAnalyzer;
    }
    
    /**
     * Get the portlet parameters
     */
    protected Map<String, List<String>> getParameters(HttpServletRequest httpServletRequest, IPortletWindowId portletWindowId, 
            boolean renderRequest) {
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getPortletRequestInfo(portletWindowId);
        
        if (portletRequestInfo != null) {
            return portletRequestInfo.getPortletParameters();
        }
        
        //Only re-use render parameters on a render request
        if (renderRequest) {
            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
            final Map<String, String[]> parameters = portletWindow.getRenderParameters();
            return ParameterMap.immutableCopyOfArrayMap(parameters);
        }
        
        return Collections.emptyMap();
    }

    protected HttpServletRequest setupPortletRequest(HttpServletRequest httpServletRequest) {
        final PortletHttpServletRequestWrapper portletHttpServletRequestWrapper = new PortletHttpServletRequestWrapper(httpServletRequest);
        portletHttpServletRequestWrapper.setAttribute(PortletDelegationLocator.PORTLET_DELECATION_LOCATOR_ATTR, this.portletDelegationLocator);
        
        return portletHttpServletRequestWrapper;
    }

    /**
     * Wrap the {@link HttpServletResponse} in a {@link PortletHttpServletResponseWrapper}.
     * @param httpServletResponse
     * @return the wrapped response
     */
    protected HttpServletResponse setupPortletResponse(HttpServletResponse httpServletResponse) {
        final PortletHttpServletResponseWrapper portletHttpServletResponseWrapper = new PortletHttpServletResponseWrapper(httpServletResponse);
        return portletHttpServletResponseWrapper;
    }
    
    /**
     * Wrap the {@link HttpServletResponse} like {@link #setupPortletResponse(HttpServletResponse)}, additionally override
     * the response's outputstream with a {@link TeeServletOutputStream}.
     * 
     * @param httpServletResponse
     * @param toTee
     * @param captureHeaders set to true if you expect to capture headers
     * @return the wrapepd response.
     * @throws IOException 
     */
    protected CachingPortletHttpServletResponseWrapper setupCachingPortletResponse(HttpServletResponse httpServletResponse, int cacheThresholdSize) throws IOException {
        final CachingPortletHttpServletResponseWrapper portletHttpServletResponseWrapper = 
                new CachingPortletHttpServletResponseWrapper(httpServletResponse, cacheThresholdSize);
        return portletHttpServletResponseWrapper;
    }
    
    protected void setupPortletWindow(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, IPortletUrlBuilder portletUrl) {
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            if (IPortletRenderer.CONFIG.equals(portletMode)) {
                final IPerson person = this.personManager.getPerson(httpServletRequest);
                
                final EntityIdentifier ei = person.getEntityIdentifier();
                final AuthorizationService authorizationService = AuthorizationService.instance();
                final IAuthorizationPrincipal ap = authorizationService.newPrincipal(ei.getKey(), ei.getType());
                
                final IPortletEntity portletEntity = portletWindow.getPortletEntity();
                final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
                
                if (!ap.canConfigure(portletDefinition.getPortletDefinitionId().getStringId())) {
                    throw new AuthorizationException(person.getUserName() + " does not have permission to render '" + portletDefinition.getFName() + "' in " + portletMode + " PortletMode");
                }
            }
        }
    }
    
}
