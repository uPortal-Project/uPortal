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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.CacheControl;
import javax.portlet.Event;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.portlet.OutputCapturingHttpServletResponseWrapper;
import org.jasig.portal.portlet.PortletDispatchException;
import org.jasig.portal.portlet.container.cache.CachedPortletData;
import org.jasig.portal.portlet.container.cache.CachingPortletHttpServletResponseWrapper;
import org.jasig.portal.portlet.container.cache.IPortletCacheControlService;
import org.jasig.portal.portlet.container.cache.LimitedBufferStringWriter;
import org.jasig.portal.portlet.container.cache.TeeServletOutputStream;
import org.jasig.portal.portlet.container.cache.TeeWriter;
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
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	private static final String IF_NONE_MATCH = "If-None-Match";

	protected final Log logger = LogFactory.getLog(this.getClass());
    
    protected static final String PORTLET_OUTPUT_CACHE_NAME = PortletRendererImpl.class.getName() + ".portletOutputCache";
    protected static final String PUBLIC_SCOPE_PORTLET_OUTPUT_CACHE_NAME = PortletRendererImpl.class.getName() + ".publicScopePortletOutputCache";
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
    	CacheControl cacheControl = this.portletCacheControlService.getPortletRenderCacheControl(portletWindowId, httpServletRequest);
    	this.portletCacheControlService.purgeCachedPortletData(portletWindowId, httpServletRequest, cacheControl);
    	
    	final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet action for window '" + portletWindow + "'");
        }
        
        final long start = System.currentTimeMillis();
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
        
        final long executionTime = System.currentTimeMillis() - start;
        
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, false);
        this.portalEventFactory.publishPortletActionExecutionEvent(httpServletRequest, this, fname, executionTime, parameters);
        
        return executionTime;
    }
    
    /**
     * Get the parameter value corresponding to the {@link ActionRequest#ACTION_NAME} parameter
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
    	CacheControl cacheControl = this.portletCacheControlService.getPortletRenderCacheControl(portletWindowId, httpServletRequest);
    	this.portletCacheControlService.purgeCachedPortletData(portletWindowId, httpServletRequest, cacheControl);
    	
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Executing portlet event for window '" + portletWindow + "'");
        }
        
        final long start = System.currentTimeMillis();
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
        
        final long executionTime = System.currentTimeMillis() - start;
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, false);
        this.portalEventFactory.publishPortletEventExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, event.getQName());
        
        return executionTime;
    }
    
   /**
    * Interacts with the {@link IPortletCacheControlService} to determine if the markup should come from cache or not.
    * If cached data doesn't exist or is expired, this delegates to {@link #doRenderMarkupInternal(IPortletWindowId, HttpServletRequest, HttpServletResponse, Writer)}.
    * 
    * (non-Javadoc)
    * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doRenderMarkup(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.Writer)
    */
    @Override
    public PortletRenderResult doRenderMarkup(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Writer writer) {
    	CachedPortletData cachedPortletData = this.portletCacheControlService.getCachedPortletRenderOutput(portletWindowId, httpServletRequest);
    	if(cachedPortletData != null && !cachedPortletData.isExpired()) {
    		// regardless if etag is set or not, we need to replay cachedPortlet Data if it's not expired
    		return doRenderMarkupReplayCachedContent(portletWindowId, httpServletRequest, writer, cachedPortletData);
    	}
    	
    	// cached data is either null or expired
    	// have to invoke PortletContainer#doRender
    	
    	// check cacheControl AFTER portlet render to see if the portlet said "useCachedContent"
        CacheControl cacheControl = this.portletCacheControlService.getPortletRenderCacheControl(portletWindowId, httpServletRequest);   
        // alter writer argument to capture output
        LimitedBufferStringWriter captureWriter = new LimitedBufferStringWriter(this.portletCacheControlService.getCacheSizeThreshold());
        TeeWriter teeWriter = new TeeWriter(writer, captureWriter);
        PortletRenderResult result = doRenderMarkupInternal(portletWindowId, httpServletRequest, httpServletResponse, teeWriter);
        
        boolean useCachedContent = cacheControl.useCachedContent();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
		if(useCachedContent && cachedPortletData == null) {
			throw new PortletDispatchException("The portlet window '"+ portletWindow + "' indicated via CacheControl#useCachedContent that the portal should render cached content, however there is no cached content to return. This is a portlet bug.", portletWindow);
		}
        
        if (useCachedContent) {
        	cachedPortletData.updateExpirationTime(cacheControl.getExpirationTime());
    		return doRenderMarkupReplayCachedContent(portletWindowId, httpServletRequest, writer, cachedPortletData);
        } else {
        	boolean shouldCache = this.portletCacheControlService.shouldOutputBeCached(cacheControl);
        	if(shouldCache && !captureWriter.isLimitExceeded()) {
        		this.portletCacheControlService.cachePortletRenderOutput(portletWindowId, httpServletRequest, captureWriter.toString(), cacheControl);
        	}
        }
    	return result;
    }
    
    @Override
    public HungWorkerAnalyzer getHungWorkerAnalyzer() {
        return hungWorkerAnalyzer;
    }
    
    /**
     * Replay the cached content inside the {@link CachedPortletData} as the response to a doRenderMarkup.
     * 
     * @param httpServletRequest
     * @param writer
     * @param cachedPortletData
     * @param portletWindow
     * @return the {@link PortletRenderResult}
     */
    protected PortletRenderResult doRenderMarkupReplayCachedContent(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, Writer writer, CachedPortletData cachedPortletData) {
    	// generate PortletRenderResult from cachedPortletData		
    	final long renderStartTime = System.currentTimeMillis();
    	PrintWriter printWriter = new PrintWriter(writer);
        // send cached String data
    	if(null != cachedPortletData.getStringData()) {
    		printWriter.write(cachedPortletData.getStringData().toCharArray());
    	}		
    	
    	final long executionTime = System.currentTimeMillis() - renderStartTime;

    	final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final boolean targeted = portletWindowId.equals(portalRequestInfo.getTargetedPortletWindowId());
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, true);
        this.portalEventFactory.publishPortletRenderExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, targeted, true);

        return constructPortletRenderResult(httpServletRequest, executionTime);
    }
    /**
     * Internal method to invoke {@link PortletContainer#doRender(org.apache.pluto.container.PortletWindow, HttpServletRequest, HttpServletResponse)}.
     * 
     * @param portletWindowId
     * @param httpServletRequest
     * @param httpServletResponse
     * @param writer
     * @return
     */
    protected PortletRenderResult doRenderMarkupInternal(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Writer writer) {
    	final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
    	//Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);
        
    	//Set the writer to capture the response if not exclusive
        //exclusive state writes the content directly to the response
        if (!EXCLUSIVE.equals(portletWindow.getWindowState())) {
            httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_PRINT_WRITER, new PrintWriter(writer));
        }
        
        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet body for window '" + portletWindow + "'");
        }

        final long renderStartTime = System.currentTimeMillis();
        try {
        	httpServletRequest.setAttribute(PortletRequest.RENDER_PART, PortletRequest.RENDER_MARKUP);
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
        
        final long executionTime = System.currentTimeMillis() - renderStartTime;
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final boolean targeted = portletWindowId.equals(portalRequestInfo.getTargetedPortletWindowId());
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, true);
        this.portalEventFactory.publishPortletRenderExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, targeted, false);
        
        return constructPortletRenderResult(httpServletRequest, executionTime);
    }
    
    /**
     * Construct a {@link PortletRenderResult} from information in the {@link HttpServletRequest}.
     * The second argument is how long the render action took.
     * 
     * @param httpServletRequest
     * @param renderTime
     * @return an appropriate {@link PortletRenderResult}, never null
     */
    private PortletRenderResult constructPortletRenderResult(HttpServletRequest httpServletRequest, long renderTime) {
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
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doRenderHeader(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.Writer)
	 */
	@Override
	public PortletRenderResult doRenderHeader(IPortletWindowId portletWindowId,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Writer writer) {
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        //Setup the request and response
        httpServletRequest = this.setupPortletRequest(httpServletRequest);
        httpServletResponse = this.setupPortletResponse(httpServletResponse);

        //Set the writer to capture the response
        httpServletRequest.setAttribute(ATTRIBUTE__PORTLET_PRINT_WRITER, new PrintWriter(writer));

        //Execute the action, 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Rendering portlet header for window '" + portletWindow + "'");
        }

        final long start = System.currentTimeMillis();
        try {
        	httpServletRequest.setAttribute(PortletRequest.RENDER_PART, PortletRequest.RENDER_HEADERS);
            this.portletContainer.doRender(portletWindow.getPlutoPortletWindow(), httpServletRequest, httpServletResponse);
            // check cachecontrols to see what we should do
        }
        catch (PortletException pe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderHeader.", portletWindow, pe);
        }
        catch (PortletContainerException pce) {
            throw new PortletDispatchException("The portlet container threw an exception while executing renderHeader on portlet window '" + portletWindow + "'.", portletWindow, pce);
        }
        catch (IOException ioe) {
            throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing renderHeader.", portletWindow, ioe);
        }
        
        
        final String title = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_TITLE);
        final String newItemCountString = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_NEW_ITEM_COUNT);
        final int newItemCount;
        if (newItemCountString != null && StringUtils.isNumeric(newItemCountString)) {
            newItemCount = Integer.parseInt(newItemCountString);
        } else {
            newItemCount = 0;
        }
        final String externalLink = (String)httpServletRequest.getAttribute(IPortletRenderer.ATTRIBUTE__PORTLET_LINK);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for: " + portletWindow);
        }
        
        final long executionTime = System.currentTimeMillis() - start;
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final boolean targeted = portletWindowId.equals(portalRequestInfo.getTargetedPortletWindowId());
        
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindowId, true);
        this.portalEventFactory.publishPortletRenderHeaderExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, targeted);
        
        return new PortletRenderResult(title, externalLink, newItemCount, executionTime);
	}
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletRenderer#doServeResource(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.io.Writer)
	 */
	@Override
	public long doServeResource(
			IPortletWindowId portletWindowId,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		final long start = System.currentTimeMillis();
		boolean servedFromCache = false;
		final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
		CachedPortletData cachedPortletData = this.portletCacheControlService.getCachedPortletResourceOutput(portletWindowId, httpServletRequest);
		if(cachedPortletData != null && !cachedPortletData.isExpired()) {
			if(logger.isDebugEnabled()) {
				logger.debug("cached content available and not expired for portletWindowId " + portletWindowId );
			}
			doServeResourceCachedOutput(portletWindowId, httpServletRequest, httpServletResponse, cachedPortletData, portletWindow);
			servedFromCache = true;
		} else {
			try {
				servedFromCache = doServeResourceInternal(portletWindowId, portletWindow, cachedPortletData, httpServletRequest, httpServletResponse);
			} catch (PortletException pe) {
				throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing serveResource.", portletWindow, pe);
			} catch (PortletContainerException pce) {
				throw new PortletDispatchException("The portlet container threw an exception while executing serveResource on portlet window '" + portletWindow + "'.", portletWindow, pce);
			}
			catch (IOException ioe) {
				throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while executing serveResource.", portletWindow, ioe);
			}	
		}

		final long executionTime = System.currentTimeMillis() - start;
		publishResourceExecutionEvent(httpServletRequest, portletWindow, executionTime, servedFromCache); 
		return executionTime;
	}
	/**
	 * Invoke {@link PortletContainer#doServeResource(PortletWindow, HttpServletRequest, HttpServletResponse)}.
	 * May invoke {@link #doServeResourceCachedOutput(IPortletWindowId, HttpServletRequest, HttpServletResponse, CachedPortletData, IPortletWindow)}
	 * if the portlet indicates to do so (and we can fulfill the request).
	 * 
	 * @param portletWindow
	 * @param cachedPortletData
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return true if the response served cached content
	 * @throws IOException
	 * @throws PortletException
	 * @throws PortletContainerException
	 */
	protected boolean doServeResourceInternal(IPortletWindowId portletWindowId, IPortletWindow portletWindow, CachedPortletData cachedPortletData,
			HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, PortletException, PortletContainerException {
		CacheControl cacheControl = this.portletCacheControlService.getPortletResourceCacheControl(portletWindowId, httpServletRequest, httpServletResponse);

		httpServletRequest = this.setupPortletRequest(httpServletRequest);
		CachingPortletHttpServletResponseWrapper responseWrapper = this.setupCachingPortletResponse(httpServletResponse, this.portletCacheControlService.getCacheSizeThreshold());
		this.portletContainer.doServeResource(portletWindow.getPlutoPortletWindow(), httpServletRequest, responseWrapper);
		// check cacheControl AFTER portlet serveResource to see if the portlet said "useCachedContent"
		boolean useCachedContent = cacheControl.useCachedContent();
		if(useCachedContent && cachedPortletData == null) {
			throw new PortletDispatchException("The portlet window '"+ portletWindow + "' indicated via CacheControl#useCachedContent that the portal should render cached content, however there is no cached content to return. This is a portlet bug.", portletWindow);
		}

		if(useCachedContent) {
			if(responseWrapper.isCommitted()) {
				throw new PortletDispatchException("The portlet window '"+ portletWindow + "' indicated it wanted the portlet container to send the cached content, however the portlet wrote content anyways. This is a bug in the portlet; if it sets an etag on the response and sets useCachedContent to true it should not commit the response.", portletWindow);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("expired cached content deemed still valid by portletWindowId " + portletWindowId + ", updated expiration time");
			}
			cachedPortletData.updateExpirationTime(cacheControl.getExpirationTime());
			doServeResourceCachedOutput(portletWindowId, httpServletRequest, responseWrapper, cachedPortletData, portletWindow);
			return true;
		}

		cacheOutputIfNecessary(cacheControl, responseWrapper, portletWindowId, portletWindow, httpServletRequest);
		
		return false;
	}
	
	/**
	 * Invoke {@link IPortletCacheControlService#cachePortletResourceOutput(IPortletWindowId, HttpServletRequest, CachedPortletData, CacheControl)} if
	 * appropriate.
	 * 
	 * @param cacheControl
	 * @param responseWrapper
	 * @param portletWindowId
	 * @param portletWindow
	 * @param httpServletRequest
	 */
	protected void cacheOutputIfNecessary(CacheControl cacheControl, CachingPortletHttpServletResponseWrapper responseWrapper,
			IPortletWindowId portletWindowId, IPortletWindow portletWindow, HttpServletRequest httpServletRequest) {
		boolean shouldCache = this.portletCacheControlService.shouldOutputBeCached(cacheControl);
		if(shouldCache && !responseWrapper.isThresholdExceeded()) {
			CachedPortletData justCachedPortletData = responseWrapper.getCachedPortletData();
			if(justCachedPortletData == null) {
				// we were told to cache, but we can't either due to a bad statusCode or content exceeding cache size threshold
				logger.warn("PortletRendererImpl#doServeResource was told to cache output for " + portletWindow + ", however the CachedPortletData returned from the response was null. This means either a bad status code was set by the portlet, or the portlet's output exceeds the cache threshold.");
			} else {
				this.portletCacheControlService.cachePortletResourceOutput(portletWindowId, httpServletRequest, responseWrapper.getCachedPortletData(), cacheControl);
				String etag = cacheControl.getETag();
				if (etag != null) {
					responseWrapper.setHeader("ETag", etag);
				}
			}
		}
	}
	
	/**
	 * Helper method to invoke {@link IPortalEventFactory#publishPortletResourceExecutionEvent(HttpServletRequest, Object, String, long, Map, String, boolean)}.
	 * 
	 * @param httpServletRequest
	 * @param portletWindow
	 * @param executionTime
	 * @param cached
	 */
	private void publishResourceExecutionEvent(HttpServletRequest httpServletRequest, IPortletWindow portletWindow, long executionTime, boolean cached) {
		final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final String resourceId = getResourceId(portletWindow.getPortletWindowId(), portalRequestInfo);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();
        final Map<String, List<String>> parameters = this.getParameters(httpServletRequest, portletWindow.getPortletWindowId(), false);
        this.portalEventFactory.publishPortletResourceExecutionEvent(httpServletRequest, this, fname, executionTime, parameters, resourceId, cached);
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
	 * Mimic {@link PortletContainer#doServeResource(PortletWindow, HttpServletRequest, HttpServletResponse)} and write
	 * the cached content out to the response.
	 * 
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @param cachedPortletData
	 * @param portletWindow
	 * @return the milliseconds 
	 */
	protected void doServeResourceCachedOutput(IPortletWindowId portletWindowId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, CachedPortletData cachedPortletData, IPortletWindow portletWindow) {	
		//If there is an etag and it matches the IF_NONE_MATCH header return a 304
		final String etag = cachedPortletData.getEtag();
		if(StringUtils.isNotBlank(etag)) {
			final String ifNoneMatch = httpServletRequest.getHeader(IF_NONE_MATCH);
			if(etag.equals(ifNoneMatch)) {
				// browser already has the content! send a 304
				if(logger.isDebugEnabled()) {
					logger.debug("returning 304 for portletWindowId " + portletWindowId + ", ifNoneMatch header=" + ifNoneMatch + ", " + cachedPortletData.getEtag() + ", cachedPortletData#expired=" + cachedPortletData.isExpired());
				}
				httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		
		//If the cached portlet data is not expired and the last mod date is within the IF_MODIFIED_SINCE window return a 304 
		if(!cachedPortletData.isExpired()) {
			long ifModifiedSince = httpServletRequest.getDateHeader(IF_MODIFIED_SINCE);
			if(cachedPortletData.getTimeStored().getTime() == ifModifiedSince) {
				// browser already has the content! send a 304
				if(logger.isDebugEnabled()) {
					logger.debug("returning 304 for portletWindowId " + portletWindowId + ", ifModifiedSince header=" + ifModifiedSince);
				}
				httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		
		//********* Browser does NOT have the content, replay the cached response *********//
		
		//If provided set the status code
        final Integer sc = cachedPortletData.getStatus();
        if (sc != null) {
            final String sm = cachedPortletData.getStatusMessage();
            
            if (sm != null) {
                httpServletResponse.setStatus(sc, sm);
            }
            else {
                httpServletResponse.setStatus(sc);
            }
        }
        
        final String characterEncoding = cachedPortletData.getCharacterEncoding();
        if (characterEncoding != null) {
            httpServletResponse.setCharacterEncoding(characterEncoding);
        }
        
        final Integer contentLength = cachedPortletData.getContentLength();
        if (contentLength != null) {
            //We could derive this from the cached data but lets try to faithfully replay the cached response
            httpServletResponse.setContentLength(contentLength);
        }
        
        final String contentType = cachedPortletData.getContentType();
        if (contentType != null) {
            httpServletResponse.setContentType(contentType);
        }
        
        final Locale locale = cachedPortletData.getLocale();
        if (locale != null) {
            httpServletResponse.setLocale(locale);
        }
		
		//Replay headers
		httpServletResponse.setContentType(contentType);
		for(Entry<String, List<Object>> header: cachedPortletData.getHeaders().entrySet()) {
			final String headerName = header.getKey();
			for(final Object value: header.getValue()) {
			    if (value instanceof Long) {
			        httpServletResponse.addDateHeader(headerName, (Long)value);
			    }
			    else if (value instanceof Integer) {
                    httpServletResponse.addIntHeader(headerName, (Integer)value);
                }
			    else {
			        httpServletResponse.addHeader(headerName, (String)value);
			    }
			}
		}
		
		//Set the ETag again
		if (etag != null) {
			httpServletResponse.setHeader("ETag", etag);
		} else {
			httpServletResponse.setDateHeader("Last-Modified", cachedPortletData.getTimeStored().getTime());
		}
		
		//Replay content
		final byte[] byteData = cachedPortletData.getByteData();
		if (byteData != null) {
    		try {
    			ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
                servletOutputStream.write(byteData);
    		} catch (IOException e) {
    			 throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while writing cached resource content.", portletWindow, e);
    		} 
		}
		else {
    		final String stringData = cachedPortletData.getStringData();
            if (stringData != null) {
                try {
                    final PrintWriter writer = httpServletResponse.getWriter();
                    writer.append(stringData);
                } catch (IOException e) {
                     throw new PortletDispatchException("The portlet window '" + portletWindow + "' threw an exception while writing cached resource content.", portletWindow, e);
                } 
            }
		}
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
