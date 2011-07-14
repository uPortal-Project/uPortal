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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.Event;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.om.portlet.ContainerRuntimeOption;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.jasig.portal.portlet.rendering.worker.IPortletFailureExecutionWorker;
import org.jasig.portal.portlet.rendering.worker.IPortletRenderExecutionWorker;
import org.jasig.portal.portlet.rendering.worker.IPortletWorkerFactory;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

/**
 * requests always end up targeting a portlet window
 *  for portlets within the UI (min, norm, max) the entity/subscription = the window, the window is persistent but its ID would be based on the entity ID
 *  for stand-alone portlets (exclusive, detached) the window only needs to exist for the duration of the request, all request data should be stored on the URL
 *  for config portlets the window is persistent and the ID would be stored in the parent code that is dispatching to the config mode portlet
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletExecutionManager")
public class PortletExecutionManager implements ApplicationEventPublisherAware, IPortletExecutionManager {
    private static final String PORTLET_HEADER_RENDERING_MAP = PortletExecutionManager.class.getName() + ".PORTLET_HEADER_RENDERING_MAP";
	private static final String PORTLET_RENDERING_MAP = PortletExecutionManager.class.getName() + ".PORTLET_RENDERING_MAP";
    
    protected static final String SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP = PortletExecutionManager.class.getName() + ".PORTLET_FAILURE_CAUSE_MAP";
    
    /**
     * 'javax.portlet.renderHeaders' is the name of a container runtime option a JSR-286 portlet can enable to trigger header output
     */
    protected static final String PORTLET_RENDER_HEADERS_OPTION = "javax.portlet.renderHeaders";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ApplicationEventPublisher applicationEventPublisher;
    
    private int maxEventIterations = 100;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEventCoordinationService eventCoordinationService;
    private IPortletWorkerFactory portletWorkerFactory;
    
    /**
     * @param maxEventIterations The maximum number of iterations to spend dispatching events. Defaults to 100
     */
    public void setMaxEventIterations(int maxEventIterations) {
        this.maxEventIterations = maxEventIterations;
    }

    @Autowired
    public void setPortletWorkerFactory(IPortletWorkerFactory portletWorkerFactory) {
        this.portletWorkerFactory = portletWorkerFactory;
    }

    @Autowired
    public void setEventCoordinationService(IPortletEventCoordinationService eventCoordinationService) {
        this.eventCoordinationService = eventCoordinationService;
    }


    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#doPortletAction(org.jasig.portal.portlet.om.IPortletEntityId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPortletAction(IPortletEntityId portletEntityId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntityId);
        this.doPortletAction(portletWindow.getPortletWindowId(), request, response);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#doPortletAction(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPortletAction(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final int timeout = getPortletActionTimeout(portletWindowId, request);
        
        final IPortletExecutionWorker<Long> portletActionExecutionWorker = this.portletWorkerFactory.createActionWorker(request, response, portletWindowId);
        portletActionExecutionWorker.submit();
        
        try {
			portletActionExecutionWorker.get(timeout);
		} catch (Exception e) {
			// put the exception into the error map for the session
	    	final Map<IPortletWindowId, Exception> portletFailureMap = getPortletErrorMap(request);
			portletFailureMap.put(portletWindowId, e);
		}
		
		final PortletEventQueue portletEventQueue = this.eventCoordinationService.getPortletEventQueue(request);
        this.doPortletEvents(portletEventQueue, request, response);
    }
    
    public void doPortletEvents(PortletEventQueue eventQueue, HttpServletRequest request, HttpServletResponse response) {
        if (eventQueue.getUnresolvedEvents().isEmpty()) {
            return;
        }
        
        final Map<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkers = new LinkedHashMap<IPortletWindowId, IPortletExecutionWorker<Long>>();
        

        //TODO what to do if we hit the max iterations?
        int iteration = 0;
        for (; iteration < this.maxEventIterations; iteration++) {
            //Make sure all queued events have been resolved
            this.eventCoordinationService.resolvePortletEvents(request, eventQueue);
            
            //Create and submit an event worker for each window with a queued event
            for (final IPortletWindowId eventWindowId : eventQueue) {
                if (eventWorkers.containsKey(eventWindowId)) {
                    /* 
                     * PLT.15.2.5 says that event processing per window must be serialized, if there
                     * is already a working in the map for the window ID skip it for now. we'll get back to it eventually
                     */
                    continue;
                }
                
                final Event event = eventQueue.pollEvent(eventWindowId);
                
                if (event != null) {
                    final IPortletExecutionWorker<Long> portletEventExecutionWorker = this.portletWorkerFactory.createEventWorker(request, response, eventWindowId, event);
                    eventWorkers.put(eventWindowId, portletEventExecutionWorker);
                    portletEventExecutionWorker.submit();
                }
            }
            
            //If no event workers exist we're done with event processing!
            if (eventWorkers.isEmpty()) {
                return;
            }
            
            //See if any of the events have completed
            int completedEventWorkers = 0;
            final Set<Entry<IPortletWindowId, IPortletExecutionWorker<Long>>> entrySet = eventWorkers.entrySet();
            for (final Iterator<Entry<IPortletWindowId, IPortletExecutionWorker<Long>>> eventWorkerEntryItr = entrySet.iterator(); eventWorkerEntryItr.hasNext();) {
                final Entry<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkerEntry = eventWorkerEntryItr.next();
                
                final IPortletExecutionWorker<Long> eventWorker = eventWorkerEntry.getValue();
                if (eventWorker.isComplete()) {
                    final IPortletWindowId portletWindowId = eventWorkerEntry.getKey();
                    //TODO return number of new queued events, use to break the loop earlier
                    waitForEventWorker(request, eventQueue, eventWorker, portletWindowId);
                    
                    eventWorkerEntryItr.remove();
                    completedEventWorkers++;
                }
            }
            
            /*
             * If no event workers have completed without waiting wait for the first one and then loop again
             * Not waiting for all events since each event may spawn more events and we want to start them
             * processing as soon as possible
             */
            if (completedEventWorkers == 0) {
                final Iterator<Entry<IPortletWindowId, IPortletExecutionWorker<Long>>> eventWorkerEntryItr = entrySet.iterator();
                final Entry<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkerEntry = eventWorkerEntryItr.next();
                eventWorkerEntryItr.remove();
                
                final IPortletWindowId portletWindowId = eventWorkerEntry.getKey();
                final IPortletExecutionWorker<Long> eventWorker = eventWorkerEntry.getValue();
                waitForEventWorker(request, eventQueue, eventWorker, portletWindowId);
            }
        }
        
        if (iteration == this.maxEventIterations) {
            this.logger.error("The Event dispatching iteration maximum of " + this.maxEventIterations + " was hit, consider either raising this limit or reviewing the portlets that use events to reduce the number of events spawned");
        }
    }

    protected void waitForEventWorker(
            HttpServletRequest request, PortletEventQueue eventQueue, 
            IPortletExecutionWorker<Long> eventWorker, IPortletWindowId portletWindowId) {

        final int timeout = getPortletEventTimeout(portletWindowId, request);
        
        try {
            eventWorker.get(timeout);
        }
        catch (Exception e) {
            // put the exception into the error map for the session
            //TODO event error handling?
            logger.warn("Portlet '" + portletWindowId + "' threw an execption while executing an event. This chain of event handling will terminate.", e);
        }
    }
    
    
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#startPortletHeadRender(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void startPortletHeadRender(String subscribeId,
			HttpServletRequest request, HttpServletResponse response) {
		Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        
        if(portletWindow != null) {
        	this.startPortletHeadRender(portletWindow.getPortletWindowId(), request, response);
        } else {
        	this.logger.debug("ignoring startPortletHeadRender since getDefaultPortletWindow returned null for subscribeId " + subscribeId);
        }
        
	}
    /**
     * Only actually starts rendering the head if the portlet has the 'javax.portlet.renderHeaders' container-runtime-option
     * present and set to "true."
     * 
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#startPortletHeadRender(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void startPortletHeadRender(IPortletWindowId portletWindowId,
			HttpServletRequest request, HttpServletResponse response) {
		if(doesPortletNeedHeaderWorker(portletWindowId, request)) {
			this.startPortletHeaderRenderInternal(portletWindowId, request, response);
		} else {
			this.logger.debug("ignoring startPortletHeadRender request since containerRuntimeOption is not present for portletWindowId " + portletWindowId);
		}
	}

	/**
	 * 
	 * @param portletWindowId
	 * @param request
	 * @return
	 */
	protected boolean doesPortletNeedHeaderWorker(IPortletWindowId portletWindowId, HttpServletRequest request) {
		IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
		PortletDefinition portletDefinition = portletWindow.getPlutoPortletWindow().getPortletDefinition();
		ContainerRuntimeOption renderHeaderOption = portletDefinition.getContainerRuntimeOption(PORTLET_RENDER_HEADERS_OPTION);
		boolean result = false;
		if(renderHeaderOption != null) {
			result = renderHeaderOption.getValues().contains(Boolean.TRUE.toString());
		}
		return result;
	}

	/* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#startPortletRender(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void startPortletRender(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        
        if(null != portletWindow) {
        	this.startPortletRenderInternal(portletWindow.getPortletWindowId(), request, response);
        } else {
        	this.logger.debug("skipping startPortletRender due to null result from getDefaultPortletWindow for subscribeId " + subscribeId);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#startPortletRender(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void startPortletRender(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        this.startPortletRenderInternal(portletWindowId, request, response);
    }
    
    

    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#serveResource(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPortletServeResource(IPortletWindowId portletWindowId,
			HttpServletRequest request, HttpServletResponse response) {
		final int timeout = getPortletResourceTimeout(portletWindowId, request);
		
		final IPortletExecutionWorker<Long> resourceWorker = this.portletWorkerFactory.createResourceWorker(request, response, portletWindowId);
		resourceWorker.submit();
        
        try {
            resourceWorker.get(timeout);
		} catch (Exception e) {
			//log the exception
			this.logger.error("resource worker failed with exception", e);
			// render generic serveResource error
			try {
				if(!response.isCommitted()) {
					response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "resource unavailable");
				}
			} catch (IOException e1) {
				logger.fatal("caught IOException trying to send error response for failed resource worker", e);
			}
		}
	}
	

	

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#isPortletHeaderRenderRequested(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean isPortletRenderHeaderRequested(
			IPortletWindowId portletWindowId, HttpServletRequest request,
			HttpServletResponse response) {
		final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = this.getPortletHeaderRenderingMap(request);
        final IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        
        return tracker != null;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#isPortletHeaderRenderRequested(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean isPortletRenderHeaderRequested(String subscribeId,
			HttpServletRequest request, HttpServletResponse response) {
	    final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);

		if(portletWindow == null) {
			this.logger.debug("returning false since getDefaultPortletWindow returned null for subscribeId " + subscribeId); 
			return false;
		}
		return this.isPortletRenderHeaderRequested(portletWindow.getPortletWindowId(), request, response);
	}

	/* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#isPortletRenderRequested(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isPortletRenderRequested(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        
        if (portletWindow == null) {
        	this.logger.warn("returning false for isPortletRenderRequested due to null result for getDefaultPortletWindow on subscribeId " + subscribeId);
        	return false;
        }
        
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        return this.isPortletRenderRequested(portletWindowId, request, response);
        
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#isPortletRenderRequested(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isPortletRenderRequested(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        final IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        
        return tracker != null;
    }
    
    /* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletHeadOutput(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    @Override
    public String getPortletHeadOutput(IPortletWindowId portletWindowId,
    		HttpServletRequest request, HttpServletResponse response) {
    	if(doesPortletNeedHeaderWorker(portletWindowId, request)) {
    		final IPortletRenderExecutionWorker tracker = getRenderedPortletHeader(portletWindowId, request, response);
    		final int timeout = getPortletRenderTimeout(portletWindowId, request);
    		try {
    			final String output = tracker.getOutput(timeout);
    			return output == null ? "" : output;
    		} catch (Exception e) {
    			logger.error("failed to render header output for " + portletWindowId, e);
    			return "";
    		}
    	} 
    	
		logger.debug(portletWindowId + " does not produce output for header");
		return "";
    }

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletHeadOutput(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String getPortletHeadOutput(String subscribeId,
			HttpServletRequest request, HttpServletResponse response) {
	    final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
		
		if (portletWindow == null) {
		    this.logger.warn("Could not find portlet window for layout node id, empty header content will be returned: " + subscribeId);
            return "";
		}

		final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

		return this.getPortletHeadOutput(portletWindowId, request, response);
	}

	/* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletOutput(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletOutput(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
            Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        
        if (portletWindow == null) {
            this.logger.warn("Could not find portlet window for layout node id, empty missing content text will be returned: " + subscribeId);
            return "This portlet does not exist or is not deployed correctly.";
        }

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        return this.getPortletOutput(portletWindowId, request, response);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletOutput(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletOutput(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
    	final IPortletRenderExecutionWorker tracker = getRenderedPortletBody(portletWindowId, request, response);
        final int timeout = getPortletRenderTimeout(portletWindowId, request);

		try {
//			final PortletRenderResult portletRenderResult = tracker.get(timeout);
			 //TODO publish portlet render event - should actually be published from the portlet renderer impl
			final String output = tracker.getOutput(timeout);
			return output == null ? "" : output;
		} catch (Exception e) {
		    final IPortletFailureExecutionWorker failureWorker = this.portletWorkerFactory.createFailureWorker(request, response, portletWindowId, e);
		    // TODO publish portlet error event?
		    try {
	            failureWorker.submit();
		        return failureWorker.getOutput(timeout);
            }
            catch (Exception e1) {
                logger.error("Failed to render error portlet for: " + portletWindowId, e1);
                return "Error Portlet Unavailable. Please contact your portal adminstrators.";
            }
		}
    }

    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletTitle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletTitle(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        
        if (portletWindow == null) {
            this.logger.warn("Could not find portlet window for layout node id, empty title content will be returned: " + subscribeId);
            return "";
        }

        return this.getPortletTitle(portletWindow.getPortletWindowId(), request, response);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletTitle(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletTitle(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final IPortletDefinitionParameter disableDynamicTitle = portletDefinition.getParameter("disableDynamicTitle");
        
        if (disableDynamicTitle == null || !Boolean.parseBoolean(disableDynamicTitle.getValue())) {
            final IPortletRenderExecutionWorker tracker = getRenderedPortletBody(portletWindowId, request, response);
            final int timeout = getPortletRenderTimeout(portletWindowId, request);
            
    		try {
    			final PortletRenderResult portletRenderResult = tracker.get(timeout);
    			if (portletRenderResult != null) {
        	        final String title = portletRenderResult.getTitle();
        	        if (title != null) {
        	            return title;
        	        }
    			}
    		}
    		catch (Exception e) {
    			logger.warn("unable to get portlet title, falling back to title defined in channel definition for portletWindowId " + portletWindowId);
    		}
        }
        
		// return portlet title from channel definition
        return portletDefinition.getTitle();
    }

    @Override
    public int getPortletNewItemCount(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        if (portletWindow == null) {
            return 0;
        }

        return this.getPortletNewItemCount(portletWindow.getPortletWindowId(), request, response);
    }

    @Override
    public int getPortletNewItemCount(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletRenderExecutionWorker tracker = getRenderedPortletBody(portletWindowId, request, response);
        final int timeout = getPortletRenderTimeout(portletWindowId, request);
        
        try {
            final PortletRenderResult portletRenderResult = tracker.get(timeout);
            if (portletRenderResult != null) {
                final int newItemCount = portletRenderResult.getNewItemCount();
                return newItemCount;
            }
        }
        catch (Exception e) {
            logger.warn("unable to get portlet new item count for portletWindowId " + portletWindowId);
        }
        
        return 0;
    }
    
    @Override
    public String getPortletLink(IPortletWindowId portletWindowId, String defaultPortletUrl, HttpServletRequest request, HttpServletResponse response) {
        final IPortletRenderExecutionWorker tracker = getRenderedPortletBody(portletWindowId, request, response);
        final int timeout = getPortletRenderTimeout(portletWindowId, request);
        
        try {
            final PortletRenderResult portletRenderResult = tracker.get(timeout);
            if (portletRenderResult != null) {
                final String link = portletRenderResult.getExternalLink();
                if (StringUtils.isNotBlank(link)) {
                    return link;
                } else {
                    return defaultPortletUrl;
                }
            }
        }
        catch (Exception e) {
            logger.warn("unable to get portlet new item count for portletWindowId " + portletWindowId);
        }
        
        return null;
    }
    
    protected int getPortletActionTimeout(IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer actionTimeout = portletDefinition.getActionTimeout();
        if (actionTimeout != null) {
            return actionTimeout;
        }
        
        return portletDefinition.getTimeout();
    }
    
    protected int getPortletEventTimeout(IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer eventTimeout = portletDefinition.getEventTimeout();
        if (eventTimeout != null) {
            return eventTimeout;
        }
        
        return portletDefinition.getTimeout();
    }
    
    protected int getPortletRenderTimeout(IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer renderTimeout = portletDefinition.getRenderTimeout();
        if (renderTimeout != null) {
            return renderTimeout;
        }
        
        return portletDefinition.getTimeout();
    }
    
    protected int getPortletResourceTimeout(IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer resourceTimeout = portletDefinition.getResourceTimeout();
        if (resourceTimeout != null) {
            return resourceTimeout;
        }
        
        return portletDefinition.getTimeout();
    }

    protected IPortletDefinition getPortletDefinition(IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity parentPortletEntity = portletWindow.getPortletEntity();
        return parentPortletEntity.getPortletDefinition();
    }

    protected IPortletRenderExecutionWorker getRenderedPortletHeader(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
    	final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletHeaderRenderingMap = this.getPortletHeaderRenderingMap(request);
    	IPortletRenderExecutionWorker portletHeaderRenderWorker = portletHeaderRenderingMap.get(portletWindowId);
    	if (portletHeaderRenderWorker == null) {
            portletHeaderRenderWorker = this.startPortletHeaderRenderInternal(portletWindowId, request, response);
        }
        return portletHeaderRenderWorker;
    }
    protected IPortletRenderExecutionWorker getRenderedPortletBody(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        if (tracker == null) {
            tracker = this.startPortletRenderInternal(portletWindowId, request, response);
            
        }
        return tracker;
    }
    
    /**
     * create and submit the portlet header rendering job to the thread pool
     * 
     * @param portletWindowId
     * @param request
     * @param response
     * @return
     */
    protected IPortletRenderExecutionWorker startPortletHeaderRenderInternal(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
    	IPortletRenderExecutionWorker portletHeaderRenderWorker = this.portletWorkerFactory.createRenderHeaderWorker(request, response, portletWindowId);
    	portletHeaderRenderWorker.submit();
    	
    	final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletHeaderRenderingMap = this.getPortletHeaderRenderingMap(request);
    	portletHeaderRenderingMap.put(portletWindowId, portletHeaderRenderWorker);
           
        return portletHeaderRenderWorker;
    }
    
    /**
     * create and submit the portlet content rendering job to the thread pool
     */
    protected IPortletRenderExecutionWorker startPortletRenderInternal(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
    	// first check to see if there is a Throwable in the session for this IPortletWindowId
    	final Map<IPortletWindowId, Exception> portletFailureMap = getPortletErrorMap(request);
    	final Exception cause = portletFailureMap.remove(portletWindowId);
    	
    	final IPortletRenderExecutionWorker portletRenderExecutionWorker;
    	if (null != cause) {
    		// previous action failed, dispatch to errorPortlet immediately
    		portletRenderExecutionWorker = this.portletWorkerFactory.createFailureWorker(request, response, portletWindowId, cause);
    	} else {
    		portletRenderExecutionWorker = this.portletWorkerFactory.createRenderWorker(request, response, portletWindowId);
    	}
    	
    	portletRenderExecutionWorker.submit();
    	
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        portletRenderingMap.put(portletWindowId, portletRenderExecutionWorker);
        
        return portletRenderExecutionWorker;
    }

    /**
     * Returns a request attribute scoped Map of portlets that are rendering for the current request.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, IPortletRenderExecutionWorker> getPortletHeaderRenderingMap(HttpServletRequest request) {
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = (Map<IPortletWindowId, IPortletRenderExecutionWorker>)request.getAttribute(PORTLET_HEADER_RENDERING_MAP);
            if (portletRenderingMap == null) {
                portletRenderingMap = new ConcurrentHashMap<IPortletWindowId, IPortletRenderExecutionWorker>();
                request.setAttribute(PORTLET_HEADER_RENDERING_MAP, portletRenderingMap);
            }
            return portletRenderingMap;
        }
    }
    /**
     * Returns a request attribute scoped Map of portlets that are rendering for the current request.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, IPortletRenderExecutionWorker> getPortletRenderingMap(HttpServletRequest request) {
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = (Map<IPortletWindowId, IPortletRenderExecutionWorker>)request.getAttribute(PORTLET_RENDERING_MAP);
            if (portletRenderingMap == null) {
                portletRenderingMap = new ConcurrentHashMap<IPortletWindowId, IPortletRenderExecutionWorker>();
                request.setAttribute(PORTLET_RENDERING_MAP, portletRenderingMap);
            }
            return portletRenderingMap;
        }
    }
    
    /**
     * Null safe means for retrieving the {@link Map} from the specified session
     * keyed by {@link #SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP}.
     * 
     * @param session
     * @return a never null {@link Map} in the session for storing portlet failure causes.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, Exception> getPortletErrorMap(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        synchronized(WebUtils.getSessionMutex(session)) {
            Map<IPortletWindowId, Exception> portletFailureMap = (Map<IPortletWindowId, Exception>) session.getAttribute(SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP);
            if(portletFailureMap == null) {
                portletFailureMap = new ConcurrentHashMap<IPortletWindowId, Exception>();
                session.setAttribute(SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP, portletFailureMap);
            }
            return portletFailureMap;
        }
        
    }
}
