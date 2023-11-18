/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.rendering;

import java.io.IOException;
import javax.portlet.CacheControl;
import javax.portlet.Event;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apereo.portal.AuthorizationException;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.api.portlet.PortletDelegationLocator;
import org.apereo.portal.events.IPortletExecutionEventFactory;
import org.apereo.portal.portlet.PortletDispatchException;
import org.apereo.portal.portlet.container.cache.CacheControlImpl;
import org.apereo.portal.portlet.container.cache.CacheState;
import org.apereo.portal.portlet.container.cache.CachedPortletData;
import org.apereo.portal.portlet.container.cache.CachedPortletResourceData;
import org.apereo.portal.portlet.container.cache.CachingPortletOutputHandler;
import org.apereo.portal.portlet.container.cache.CachingPortletResourceOutputHandler;
import org.apereo.portal.portlet.container.cache.HeaderSettingCacheControl;
import org.apereo.portal.portlet.container.cache.IPortletCacheControlService;
import org.apereo.portal.portlet.container.cache.PortletCachingHeaderUtils;
import org.apereo.portal.portlet.container.services.AdministrativeRequestListenerController;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.session.PortletSessionAdministrativeRequestListener;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.url.IPortalRequestInfo;
import org.apereo.portal.url.IUrlSyntaxProvider;
import org.apereo.portal.url.ParameterMap;
import org.apereo.portal.utils.web.PortletHttpServletRequestWrapper;
import org.apereo.portal.utils.web.PortletHttpServletResponseWrapper;
import org.apereo.portal.utils.web.PortletMimeHttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Executes methods on portlets using Pluto */
@Service
public class PortletRendererImpl implements IPortletRenderer {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IPersonManager personManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private PortletContainer portletContainer;
    private PortletDelegationLocator portletDelegationLocator;
    private IPortletCacheControlService portletCacheControlService;
    private IPortletExecutionEventFactory portalEventFactory;
    private IUrlSyntaxProvider urlSyntaxProvider;

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    @Autowired
    public void setPortalEventFactory(IPortletExecutionEventFactory portalEventFactory) {
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
    /** @param portletCacheControlService the portletCacheControlService to set */
    @Autowired
    public void setPortletCacheControlService(
            IPortletCacheControlService portletCacheControlService) {
        this.portletCacheControlService = portletCacheControlService;
    }

    /*
     * PLT 22.1 If the content of a portlet is cached and the portlet is target of request
     * with an action-type semantic (e.g. an action or event call), the portlet container should discard the cache and
     * invoke the corresponding request handling methods of the portlet like processAction,or processEvent.
     */
    @Override
    public long doAction(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        this.portletCacheControlService.purgeCachedPortletData(portletWindowId, httpServletRequest);

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        enforceConfigPermission(httpServletRequest, portletWindow);

        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse =
                new PortletHttpServletResponseWrapper(httpServletResponse, portletWindow);

        // Execute the action,
        this.logger.debug("Executing portlet action for window '{}'", portletWindow);

        final long start = System.nanoTime();
        try {
            this.portletContainer.doAction(
                    portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        } catch (PortletException pe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing action.",
                    portletWindow,
                    pe);
        } catch (PortletContainerException pce) {
            throw new PortletDispatchException(
                    "The portlet container threw an exception while executing action on portlet window '"
                            + portletWindow
                            + "'.",
                    portletWindow,
                    pce);
        } catch (IOException ioe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing action.",
                    portletWindow,
                    ioe);
        }

        final long executionTime = System.nanoTime() - start;

        this.portalEventFactory.publishPortletActionExecutionEvent(
                httpServletRequest, this, portletWindowId, executionTime);

        return executionTime;
    }

    /*
     * PLT 22.1 If the content of a portlet is cached and the portlet is target of request
     * with an action-type semantic (e.g. an action or event call), the portlet container should discard the cache and
     * invoke the corresponding request handling methods of the portlet like processAction,or processEvent.
     *
     * (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletRenderer#doEvent(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.portlet.Event)
     */
    @Override
    public long doEvent(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Event event) {
        this.portletCacheControlService.purgeCachedPortletData(portletWindowId, httpServletRequest);

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        enforceConfigPermission(httpServletRequest, portletWindow);

        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse =
                new PortletHttpServletResponseWrapper(httpServletResponse, portletWindow);

        // Execute the action,
        this.logger.debug("Executing portlet event for window '{}'", portletWindow);

        final long start = System.nanoTime();
        try {
            this.portletContainer.doEvent(
                    portletWindow.getPlutoPortletWindow(),
                    httpServletRequest,
                    httpServletResponse,
                    event);
        } catch (PortletException pe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing event.",
                    portletWindow,
                    pe);
        } catch (PortletContainerException pce) {
            throw new PortletDispatchException(
                    "The portlet container threw an exception while executing event on portlet window '"
                            + portletWindow
                            + "'.",
                    portletWindow,
                    pce);
        } catch (IOException ioe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing event.",
                    portletWindow,
                    ioe);
        }

        final long executionTime = System.nanoTime() - start;

        this.portalEventFactory.publishPortletEventExecutionEvent(
                httpServletRequest, this, portletWindowId, executionTime, event.getQName());

        return executionTime;
    }

    @Override
    public PortletRenderResult doRenderHeader(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            PortletOutputHandler portletOutputHandler)
            throws IOException {

        // enforce CONFIG mode restriction through its enforcement in doRender();

        return doRender(
                portletWindowId,
                httpServletRequest,
                httpServletResponse,
                portletOutputHandler,
                RenderPart.HEADERS);
    }

    /**
     * Interacts with the {@link IPortletCacheControlService} to determine if the markup should come
     * from cache or not. If cached data doesn't exist or is expired, this delegates to {@link
     * #doRender(IPortletWindowId, HttpServletRequest, HttpServletResponse, PortletOutputHandler,
     * RenderPart)}.
     *
     * @throws IOException
     */
    @Override
    public PortletRenderResult doRenderMarkup(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            PortletOutputHandler portletOutputHandler)
            throws IOException {

        // enforce CONFIG mode access restriction through its enforcement in the doRender() impl.
        return doRender(
                portletWindowId,
                httpServletRequest,
                httpServletResponse,
                portletOutputHandler,
                RenderPart.MARKUP);
    }

    /** Describes the part of the render request and defines the part specific behaviors */
    protected enum RenderPart {
        HEADERS(PortletRequest.RENDER_HEADERS) {
            @Override
            public CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>
                    getCacheState(
                            IPortletCacheControlService portletCacheControlService,
                            HttpServletRequest request,
                            IPortletWindowId portletWindowId) {
                return portletCacheControlService.getPortletRenderHeaderState(
                        request, portletWindowId);
            }

            @Override
            public void cachePortletOutput(
                    IPortletCacheControlService portletCacheControlService,
                    IPortletWindowId portletWindowId,
                    HttpServletRequest request,
                    CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>
                            cacheState,
                    CachedPortletData<PortletRenderResult> cachedPortletData) {

                portletCacheControlService.cachePortletRenderHeaderOutput(
                        portletWindowId, request, cacheState, cachedPortletData);
            }

            @Override
            public void publishRenderExecutionEvent(
                    IPortletExecutionEventFactory portalEventFactory,
                    PortletRendererImpl source,
                    HttpServletRequest request,
                    IPortletWindowId portletWindowId,
                    long executionTime,
                    boolean targeted,
                    boolean cached) {

                portalEventFactory.publishPortletRenderHeaderExecutionEvent(
                        request, source, portletWindowId, executionTime, targeted, cached);
            }
        },
        MARKUP(PortletRequest.RENDER_MARKUP) {
            @Override
            public CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>
                    getCacheState(
                            IPortletCacheControlService portletCacheControlService,
                            HttpServletRequest request,
                            IPortletWindowId portletWindowId) {
                return portletCacheControlService.getPortletRenderState(request, portletWindowId);
            }

            @Override
            public void cachePortletOutput(
                    IPortletCacheControlService portletCacheControlService,
                    IPortletWindowId portletWindowId,
                    HttpServletRequest request,
                    CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>
                            cacheState,
                    CachedPortletData<PortletRenderResult> cachedPortletData) {

                portletCacheControlService.cachePortletRenderOutput(
                        portletWindowId, request, cacheState, cachedPortletData);
            }

            @Override
            public void publishRenderExecutionEvent(
                    IPortletExecutionEventFactory portalEventFactory,
                    PortletRendererImpl source,
                    HttpServletRequest request,
                    IPortletWindowId portletWindowId,
                    long executionTime,
                    boolean targeted,
                    boolean cached) {
                portalEventFactory.publishPortletRenderExecutionEvent(
                        request, source, portletWindowId, executionTime, targeted, cached);
            }
        };

        private final String renderPart;

        private RenderPart(String renderPart) {
            this.renderPart = renderPart;
        }

        /** Get the cache state for the request */
        public abstract CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult>
                getCacheState(
                        IPortletCacheControlService portletCacheControlService,
                        HttpServletRequest request,
                        IPortletWindowId portletWindowId);

        /** Cache the portlet output */
        public abstract void cachePortletOutput(
                IPortletCacheControlService portletCacheControlService,
                IPortletWindowId portletWindowId,
                HttpServletRequest request,
                CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState,
                CachedPortletData<PortletRenderResult> cachedPortletData);

        /** Public portlet event */
        public abstract void publishRenderExecutionEvent(
                IPortletExecutionEventFactory portalEventFactory,
                PortletRendererImpl source,
                HttpServletRequest request,
                IPortletWindowId portletWindowId,
                long executionTime,
                boolean targeted,
                boolean cached);

        /** @return The {@link PortletRequest#RENDER_PART} name */
        public final String getRenderPart() {
            return renderPart;
        }
    }

    protected PortletRenderResult doRender(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            PortletOutputHandler portletOutputHandler,
            RenderPart renderPart)
            throws IOException {

        final CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState =
                renderPart.getCacheState(
                        this.portletCacheControlService, httpServletRequest, portletWindowId);

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        enforceConfigPermission(httpServletRequest, portletWindow);

        /*
         * If the portlet is rendering in EXCLUSIVE WindowState ignore the provided PortletOutputHandler and
         * write directly to the response.
         *
         * THIS IS VERY BAD AND SHOULD BE DEPRECATED ALONG WITH EXCLUSIVE WINDOW STATE
         */
        if (IPortletRenderer.EXCLUSIVE.equals(portletWindow.getWindowState())) {
            portletOutputHandler = new ResourcePortletOutputHandler(httpServletResponse);
        }

        if (cacheState.isUseCachedData()) {
            return doRenderReplayCachedContent(
                    portletWindow,
                    httpServletRequest,
                    cacheState,
                    portletOutputHandler,
                    renderPart,
                    0);
        }

        final int cacheSizeThreshold = this.portletCacheControlService.getCacheSizeThreshold();
        final CachingPortletOutputHandler cachingPortletOutputHandler =
                new CachingPortletOutputHandler(portletOutputHandler, cacheSizeThreshold);

        final CacheControl cacheControl = cacheState.getCacheControl();

        // Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse =
                new PortletMimeHttpServletResponseWrapper(
                        httpServletResponse, portletWindow, portletOutputHandler, cacheControl);

        httpServletRequest.setAttribute(
                IPortletRenderer.ATTRIBUTE__PORTLET_CACHE_CONTROL, cacheControl);
        httpServletRequest.setAttribute(
                IPortletRenderer.ATTRIBUTE__PORTLET_OUTPUT_HANDLER, cachingPortletOutputHandler);

        logger.debug("Rendering portlet {} for window {}", renderPart.name(), portletWindow);

        final long renderStartTime = System.nanoTime();
        try {
            httpServletRequest.setAttribute(PortletRequest.RENDER_PART, renderPart.getRenderPart());
            this.portletContainer.doRender(
                    portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        } catch (PortletException pe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing renderMarkup.",
                    portletWindow,
                    pe);
        } catch (PortletContainerException pce) {
            throw new PortletDispatchException(
                    "The portlet container threw an exception while executing renderMarkup on portlet window '"
                            + portletWindow
                            + "'.",
                    portletWindow,
                    pce);
        } catch (IOException ioe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing renderMarkup.",
                    portletWindow,
                    ioe);
        }

        final long executionTime = System.nanoTime() - renderStartTime;

        // See if the portlet signaled to use the cached content
        final boolean useCachedContent = cacheControl.useCachedContent();
        if (useCachedContent) {
            final CachedPortletData<PortletRenderResult> cachedPortletData =
                    cacheState.getCachedPortletData();
            if (cachedPortletData == null) {
                throw new PortletDispatchException(
                        "The portlet window '"
                                + portletWindow
                                + "' indicated via CacheControl#useCachedContent "
                                + "that the portal should render cached content, however there is no cached content to return. "
                                + "This is a portlet bug.",
                        portletWindow);
            }

            // Update the expiration time and re-store in the cache
            cachedPortletData.updateExpirationTime(cacheControl.getExpirationTime());

            renderPart.cachePortletOutput(
                    portletCacheControlService,
                    portletWindowId,
                    httpServletRequest,
                    cacheState,
                    cachedPortletData);

            return doRenderReplayCachedContent(
                    portletWindow,
                    httpServletRequest,
                    cacheState,
                    portletOutputHandler,
                    renderPart,
                    executionTime);
        }

        publishRenderEvent(portletWindow, httpServletRequest, renderPart, executionTime, false);

        // Build the render result
        final PortletRenderResult portletRenderResult =
                constructPortletRenderResult(httpServletRequest, executionTime);

        // Check if the portlet's output should be cached
        if (cacheState != null) {
            boolean shouldCache =
                    this.portletCacheControlService.shouldOutputBeCached(cacheControl);

            if (shouldCache) {
                final CachedPortletData<PortletRenderResult> cachedPortletData =
                        cachingPortletOutputHandler.getCachedPortletData(
                                portletRenderResult, cacheControl);

                if (cachedPortletData != null) {
                    renderPart.cachePortletOutput(
                            portletCacheControlService,
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
    protected PortletRenderResult doRenderReplayCachedContent(
            IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest,
            CacheState<CachedPortletData<PortletRenderResult>, PortletRenderResult> cacheState,
            PortletOutputHandler portletOutputHandler,
            RenderPart renderPart,
            long baseExecutionTime)
            throws IOException {

        enforceConfigPermission(httpServletRequest, portletWindow);

        logger.debug(
                "Replaying cached content for Render {} request to {}", renderPart, portletWindow);

        final long renderStartTime = System.nanoTime();

        final CachedPortletData<PortletRenderResult> cachedPortletData =
                cacheState.getCachedPortletData();
        cachedPortletData.replay(portletOutputHandler);

        final long executionTime = baseExecutionTime + (System.nanoTime() - renderStartTime);

        publishRenderEvent(portletWindow, httpServletRequest, renderPart, executionTime, true);

        final PortletRenderResult portletResult = cachedPortletData.getPortletResult();
        return new PortletRenderResult(portletResult, executionTime);
    }

    /** Publish the portlet render event */
    protected void publishRenderEvent(
            IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest,
            RenderPart renderPart,
            long executionTime,
            boolean cached) {

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        // Determine if the portlet was targeted
        final IPortalRequestInfo portalRequestInfo =
                this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final boolean targeted =
                portletWindowId.equals(portalRequestInfo.getTargetedPortletWindowId());

        renderPart.publishRenderExecutionEvent(
                this.portalEventFactory,
                this,
                httpServletRequest,
                portletWindowId,
                executionTime,
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
    protected PortletRenderResult constructPortletRenderResult(
            HttpServletRequest httpServletRequest, long renderTime) {
        final String title =
                (String) httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE);
        final String newItemCountString =
                (String)
                        httpServletRequest.getAttribute(
                                IPortletRenderer.ATTRIBUTE__PORTLET_NEW_ITEM_COUNT);
        final int newItemCount;
        if (newItemCountString != null && StringUtils.isNumeric(newItemCountString)) {
            newItemCount = Integer.parseInt(newItemCountString);
        } else {
            newItemCount = 0;
        }
        final String link =
                (String) httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_LINK);

        return new PortletRenderResult(title, link, newItemCount, renderTime);
    }

    @Override
    public long doServeResource(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            PortletResourceOutputHandler portletOutputHandler)
            throws IOException {

        final CacheState<CachedPortletResourceData<Long>, Long> cacheState =
                this.portletCacheControlService.getPortletResourceState(
                        httpServletRequest, portletWindowId);

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        enforceConfigPermission(httpServletRequest, portletWindow);

        if (cacheState.isUseBrowserData()) {
            logger.trace("doServeResource-Reusing browser data");
            return doResourceReplayBrowserContent(
                    portletWindow, httpServletRequest, cacheState, portletOutputHandler);
        }

        if (cacheState.isUseCachedData()) {
            logger.trace("doServeResource-Reusing cached data");
            return doResourceReplayCachedContent(
                    portletWindow, httpServletRequest, cacheState, portletOutputHandler, 0);
        }

        final int cacheSizeThreshold = this.portletCacheControlService.getCacheSizeThreshold();
        final CachingPortletResourceOutputHandler cachingPortletOutputHandler =
                new CachingPortletResourceOutputHandler(portletOutputHandler, cacheSizeThreshold);

        CacheControl cacheControl = cacheState.getCacheControl();
        // Wrap the cache control so it immediately sets the caching related response headers
        cacheControl = new HeaderSettingCacheControl(cacheControl, cachingPortletOutputHandler);

        // Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse =
                new PortletResourceHttpServletResponseWrapper(
                        httpServletResponse, portletWindow, portletOutputHandler, cacheControl);

        httpServletRequest.setAttribute(
                IPortletRenderer.ATTRIBUTE__PORTLET_CACHE_CONTROL, cacheControl);
        httpServletRequest.setAttribute(
                IPortletRenderer.ATTRIBUTE__PORTLET_OUTPUT_HANDLER, cachingPortletOutputHandler);

        this.logger.debug("Executing resource request for window {}", portletWindow);

        final long start = System.nanoTime();
        try {
            this.portletContainer.doServeResource(
                    portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
        } catch (PortletException pe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing serveResource.",
                    portletWindow,
                    pe);
        } catch (PortletContainerException pce) {
            throw new PortletDispatchException(
                    "The portlet container threw an exception while executing serveResource on portlet window '"
                            + portletWindow
                            + "'.",
                    portletWindow,
                    pce);
        } catch (IOException ioe) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' threw an exception while executing serveResource.",
                    portletWindow,
                    ioe);
        }
        final long executionTime = System.nanoTime() - start;

        // See if the portlet signaled to use the cached content
        final boolean useCachedContent = cacheControl.useCachedContent();
        if (useCachedContent) {
            final CachedPortletResourceData<Long> cachedPortletResourceData =
                    cacheState.getCachedPortletData();

            if (cachedPortletResourceData != null) {
                // Update the expiration time and re-store in the cache
                final CachedPortletData<Long> cachedPortletData =
                        cachedPortletResourceData.getCachedPortletData();
                cachedPortletData.updateExpirationTime(cacheControl.getExpirationTime());
                this.portletCacheControlService.cachePortletResourceOutput(
                        portletWindowId, httpServletRequest, cacheState, cachedPortletResourceData);
            }

            if (cacheState.isBrowserSetEtag()) {
                logger.trace("doServeResource-useCachedContent, Reusing browser data");
                // Browser-side content matches, send a 304
                return doResourceReplayBrowserContent(
                        portletWindow, httpServletRequest, cacheState, portletOutputHandler);
            }
            logger.trace("doServeResource-useCachedContent, Reusing cached data");

            return doResourceReplayCachedContent(
                    portletWindow,
                    httpServletRequest,
                    cacheState,
                    cachingPortletOutputHandler,
                    executionTime);
        }

        publishResourceEvent(portletWindow, httpServletRequest, executionTime, false, false);

        if (cacheState != null) {
            boolean shouldCache =
                    this.portletCacheControlService.shouldOutputBeCached(cacheControl);

            if (shouldCache) {
                final CachedPortletResourceData<Long> cachedPortletResourceData =
                        cachingPortletOutputHandler.getCachedPortletResourceData(
                                executionTime, cacheControl);

                if (cachedPortletResourceData != null) {
                    this.portletCacheControlService.cachePortletResourceOutput(
                            portletWindowId,
                            httpServletRequest,
                            cacheState,
                            cachedPortletResourceData);
                }
            }
        }

        return executionTime;
    }

    protected long doResourceReplayBrowserContent(
            IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest,
            CacheState<CachedPortletResourceData<Long>, Long> cacheState,
            PortletResourceOutputHandler portletOutputHandler) {

        enforceConfigPermission(httpServletRequest, portletWindow);

        logger.debug("Sending 304 for resource request to {}", portletWindow);

        if (portletOutputHandler.isCommitted()) {
            throw new IllegalStateException(
                    "Attempting to send 304 but response is already committed");
        }

        final long start = System.nanoTime();

        portletOutputHandler.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

        final CachedPortletResourceData<Long> cachedPortletResourceData =
                cacheState.getCachedPortletData();
        if (cachedPortletResourceData != null) {
            // Freshen up the various caching related headers
            final CachedPortletData<Long> cachedPortletData =
                    cachedPortletResourceData.getCachedPortletData();
            PortletCachingHeaderUtils.setCachingHeaders(cachedPortletData, portletOutputHandler);
        }

        final long executionTime = System.nanoTime() - start;

        publishResourceEvent(portletWindow, httpServletRequest, executionTime, true, false);

        return executionTime;
    }

    /**
     * Replay the cached content inside the {@link CachedPortletData} as the response to a
     * doResource.
     */
    protected Long doResourceReplayCachedContent(
            IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest,
            CacheState<CachedPortletResourceData<Long>, Long> cacheState,
            PortletResourceOutputHandler portletOutputHandler,
            long baseExecutionTime)
            throws IOException {

        enforceConfigPermission(httpServletRequest, portletWindow);

        logger.debug("Replaying cached content for resource request to {}", portletWindow);

        final long renderStartTime = System.nanoTime();

        final CachedPortletResourceData<Long> cachedPortletResourceData =
                cacheState.getCachedPortletData();
        if (cachedPortletResourceData == null) {
            throw new PortletDispatchException(
                    "The portlet window '"
                            + portletWindow
                            + "' indicated via CacheControl#useCachedContent "
                            + "that the portal should render cached content, however there is no cached content to return. "
                            + "This is a portlet bug.",
                    portletWindow);
        }

        cachedPortletResourceData.replay(portletOutputHandler);

        final long executionTime = baseExecutionTime + (System.nanoTime() - renderStartTime);

        publishResourceEvent(portletWindow, httpServletRequest, executionTime, false, true);

        return executionTime;
    }

    /** Publish the portlet resource event */
    protected void publishResourceEvent(
            IPortletWindow portletWindow,
            HttpServletRequest httpServletRequest,
            long executionTime,
            boolean usedBrowserCache,
            boolean usedPortalCache) {

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        this.portalEventFactory.publishPortletResourceExecutionEvent(
                httpServletRequest,
                this,
                portletWindowId,
                executionTime,
                usedBrowserCache,
                usedPortalCache);
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletRenderer#doReset(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doReset(
            IPortletWindowId portletWindowId,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {

        // Don't enforce config mode restriction because this is going to snap the portlet window
        // back into view mode.

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        if (portletWindow != null) {
            portletWindow.setPortletMode(PortletMode.VIEW);
            portletWindow.setRenderParameters(new ParameterMap());
            portletWindow.setExpirationCache(null);

            httpServletRequest = this.setupPortletRequest(httpServletRequest);
            httpServletResponse =
                    new PortletHttpServletResponseWrapper(httpServletResponse, portletWindow);

            httpServletRequest.setAttribute(
                    AdministrativeRequestListenerController.DEFAULT_LISTENER_KEY_ATTRIBUTE,
                    "sessionActionListener");
            httpServletRequest.setAttribute(
                    PortletSessionAdministrativeRequestListener.ACTION,
                    PortletSessionAdministrativeRequestListener.SessionAction.CLEAR);
            httpServletRequest.setAttribute(
                    PortletSessionAdministrativeRequestListener.SCOPE,
                    PortletSession.PORTLET_SCOPE);

            // TODO modify PortletContainer.doAdmin to create a specific "admin" req/res object and
            // context so we don't have to fake it with a render req
            // These are required for a render request to be created and admin requests use a render
            // request under the hood
            final String characterEncoding = httpServletResponse.getCharacterEncoding();
            httpServletRequest.setAttribute(
                    IPortletRenderer.ATTRIBUTE__PORTLET_OUTPUT_HANDLER,
                    new RenderPortletOutputHandler(characterEncoding));
            httpServletRequest.setAttribute(
                    IPortletRenderer.ATTRIBUTE__PORTLET_CACHE_CONTROL, new CacheControlImpl());

            try {
                this.portletContainer.doAdmin(
                        portletWindow.getPlutoPortletWindow(),
                        httpServletRequest,
                        httpServletResponse);
            } catch (PortletException pe) {
                throw new PortletDispatchException(
                        "The portlet window '"
                                + portletWindow
                                + "' threw an exception while executing admin command to clear session.",
                        portletWindow,
                        pe);
            } catch (PortletContainerException pce) {
                throw new PortletDispatchException(
                        "The portlet container threw an exception while executing admin command to clear session on portlet window '"
                                + portletWindow
                                + "'.",
                        portletWindow,
                        pce);
            } catch (IOException ioe) {
                throw new PortletDispatchException(
                        "The portlet window '"
                                + portletWindow
                                + "' threw an exception while executing admin command to clear session.",
                        portletWindow,
                        ioe);
            }
        } else {
            logger.debug(
                    "ignoring doReset as portletWindowRegistry#getPortletWindow returned a null result for portletWindowId {}",
                    portletWindowId);
        }
    }

    protected HttpServletRequest setupPortletRequest(HttpServletRequest httpServletRequest) {
        final PortletHttpServletRequestWrapper portletHttpServletRequestWrapper =
                new PortletHttpServletRequestWrapper(httpServletRequest);
        portletHttpServletRequestWrapper.setAttribute(
                PortletDelegationLocator.PORTLET_DELEGATION_LOCATOR_ATTR,
                this.portletDelegationLocator);

        return portletHttpServletRequestWrapper;
    }

    /**
     * Enforces config mode access control. If requesting user does not have CONFIG permission, and
     * the PortletWindow specifies config mode, throws AuthorizationException. Otherwise does
     * nothing.
     *
     * @param httpServletRequest the non-null current HttpServletRequest (for determining requesting
     *     user)
     * @param portletWindow a non-null portlet window that might be in config mode
     * @throws AuthorizationException if the user is not permitted to access config mode yet portlet
     *     window specifies config mode
     * @throws java.lang.IllegalArgumentException if the request or window are null
     * @since 4.0.13.1, 4.0.14, 4.1.
     */
    protected void enforceConfigPermission(
            final HttpServletRequest httpServletRequest, final IPortletWindow portletWindow) {

        Validate.notNull(
                httpServletRequest, "Servlet request must not be null to determine remote user.");
        Validate.notNull(portletWindow, "Portlet window must not be null to determine its mode.");

        final PortletMode portletMode = portletWindow.getPortletMode();
        if (portletMode != null) {
            if (IPortletRenderer.CONFIG.equals(portletMode)) {
                final IPerson person = this.personManager.getPerson(httpServletRequest);

                final EntityIdentifier ei = person.getEntityIdentifier();
                final AuthorizationServiceFacade authorizationServiceFacade =
                        AuthorizationServiceFacade.instance();
                final IAuthorizationPrincipal ap =
                        authorizationServiceFacade.newPrincipal(ei.getKey(), ei.getType());

                final IPortletEntity portletEntity = portletWindow.getPortletEntity();
                final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();

                if (!ap.canConfigure(portletDefinition.getPortletDefinitionId().getStringId())) {

                    logger.error(
                            "User {} attempted to use portlet {} in {} but lacks permission to use that mode.  "
                                    + "THIS MAY BE AN ATTEMPT TO EXPLOIT A HISTORICAL SECURITY FLAW.  "
                                    + "You should probably figure out who this user is and why they are trying to access "
                                    + "unauthorized portlet modes.",
                            person.getUserName(),
                            portletDefinition.getFName(),
                            portletMode);

                    throw new AuthorizationException(
                            person.getUserName()
                                    + " does not have permission to render '"
                                    + portletDefinition.getFName()
                                    + "' in "
                                    + portletMode
                                    + " PortletMode.");
                }
            }
        }
    }
}
