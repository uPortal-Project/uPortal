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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.registry.NotAPortletException;
import org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.jasig.portal.portlet.rendering.worker.IPortletFailureExecutionWorker;
import org.jasig.portal.portlet.rendering.worker.IPortletRenderExecutionWorker;
import org.jasig.portal.portlet.rendering.worker.IPortletWorkerFactory;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.DataRetrievalFailureException;
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
    private static final String PORTLET_RENDERING_MAP = PortletExecutionManager.class.getName() + ".PORTLET_RENDERING_MAP";
    
    protected static final String SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP = PortletExecutionManager.class.getName() + ".PORTLET_FAILURE_CAUSE_MAP";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ApplicationEventPublisher applicationEventPublisher;
    
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IUserInstanceManager userInstanceManager;
    private IPortletEventCoordinationService eventCoordinationService;
    private IPortletWorkerFactory portletWorkerFactory;
    
    @Autowired
    public void setPortletWorkerFactory(IPortletWorkerFactory portletWorkerFactory) {
        this.portletWorkerFactory = portletWorkerFactory;
    }

    @Autowired
    public void setEventCoordinationService(IPortletEventCoordinationService eventCoordinationService) {
        this.eventCoordinationService = eventCoordinationService;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
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
        final int timeout = getPortletRenderTimeout(portletWindowId, request);
        
        final IPortletExecutionWorker<Long> portletActionExecutionWorker = this.portletWorkerFactory.createActionWorker(request, response, portletWindowId);
        portletActionExecutionWorker.submit();
        
        try {
			final Long actualExecutionTime = portletActionExecutionWorker.get(timeout);
			 //TODO publish portlet action event
		} catch (Exception e) {
			// put the exception into the error map for the session
	    	final Map<IPortletWindowId, Exception> portletFailureMap = getPortletErrorMap(request);
			portletFailureMap.put(portletWindowId, e);
		}
		
		final Queue<Event> queuedEvents = this.eventCoordinationService.getQueuedEvents(request);
		final List<Event> events = new LinkedList<Event>();
		this.drainTo(queuedEvents, events);
        this.doPortletEvents(events, request, response);
    }
    
    public <E> void drainTo(Queue<E> queue, Collection<E> dest) {
        while (!queue.isEmpty()) {
            final E e = queue.poll();
            if (e == null) {
                break;
            }
            dest.add(e);
        }
    }
    
    public void doPortletEvents(List<Event> events, HttpServletRequest request, HttpServletResponse response) {
        final PortletEventQueue eventQueue = new PortletEventQueue();
        
        final Map<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkers = new LinkedHashMap<IPortletWindowId, IPortletExecutionWorker<Long>>();
        this.eventCoordinationService.resolveQueueEvents(eventQueue, events, request);

        /*
         * TODO limit the number of iterations we'll take to do event processing
         */
        while (true) {
            //Create and submit an event worker for each window with a queued event
            for (final IPortletWindowId eventWindowId : eventQueue) {
                if (eventWorkers.containsKey(eventWindowId)) {
                    /* 
                     * PLT.15.2.5 says that event processing per window must be serialized, if there
                     * is already a working in the map for the window ID skip it for now.
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
    }

    protected void waitForEventWorker(
            HttpServletRequest request, PortletEventQueue eventQueue, 
            IPortletExecutionWorker<Long> eventWorker, IPortletWindowId portletWindowId) {

        final int timeout = getPortletRenderTimeout(portletWindowId, request);
        
        try {
            //This should never actually wait since the future says it was done
            final Long actualExecutionTime = eventWorker.get(timeout);
             //TODO publish portlet event event
        }
        catch (Exception e) {
            // put the exception into the error map for the session
            //TODO event error handling?
            logger.warn("Portlet '" + portletWindowId + "' threw an execption while executing an event. This chain of event handling will terminate.", e);
        }
        
        //Get any additional events that the event that just completed may have generated and add them to the queuedEvents structure
        final Queue<Event> queuedEvents = this.eventCoordinationService.getQueuedEvents(request);
        final List<Event> events = new LinkedList<Event>();
        this.drainTo(queuedEvents, events);
        this.eventCoordinationService.resolveQueueEvents(eventQueue, events, request);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#startPortletRender(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void startPortletRender(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow;
        try {
            portletWindow = this.getDefaultPortletWindow(subscribeId, request);
        }
        catch (NotAPortletException nape) {
            this.logger.warn("Channel with subscribeId '" + subscribeId + "' is not a portlet");
            return;
        }
        catch (DataRetrievalFailureException e) {
            this.logger.warn("Failed to start portlet rendering: " + subscribeId, e);
            return;
        }
        
        this.startPortletRenderInternal(portletWindow.getPortletWindowId(), request, response);
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
		final int timeout = getPortletRenderTimeout(portletWindowId, request);
		
		final IPortletExecutionWorker<Long> resourceWorker = this.portletWorkerFactory.createResourceWorker(request, response, portletWindowId);
		resourceWorker.submit();
        
        try {
            final Long actualExecutionTime = resourceWorker.get(timeout);
			// TODO publish portlet resource event
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
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#isPortletRenderRequested(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isPortletRenderRequested(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletWindow portletWindow;
        try {
            portletWindow = this.getDefaultPortletWindow(subscribeId, request);
        }
        catch (NotAPortletException nape) {
            this.logger.warn("Channel with subscribeId '" + subscribeId + "' is not a portlet");
            return false;
        }
        catch (DataRetrievalFailureException e) {
            this.logger.warn("Failed to start portlet rendering: " + subscribeId, e);
            return false;
        }
        
        return this.isPortletRenderRequested(portletWindow.getPortletWindowId(), request, response);
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
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletOutput(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletOutput(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
            Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow;
        try {
            portletWindow = this.getDefaultPortletWindow(subscribeId, request);
        }
        catch (NotAPortletException nape) {
            this.logger.warn("Channel with subscribeId '" + subscribeId + "' is not a portlet");
            return "";
        }
        catch (DataRetrievalFailureException e) {
            this.logger.warn("Failed to output portlet: " + subscribeId, e);
            return "";
        }

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        return this.getPortletOutput(portletWindowId, request, response);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletOutput(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletOutput(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
    	final IPortletRenderExecutionWorker tracker = getRenderedPortlet(portletWindowId, request, response);
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
    /**
     * 
     * @param failedPortletWindowId
     * @param request
     * @param response
     * @return
     */
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletTitle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletTitle(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(subscribeId, "subscribeId cannot be null");
        
        final IPortletWindow portletWindow;
        try {
            portletWindow = this.getDefaultPortletWindow(subscribeId, request);
        }
        catch (NotAPortletException nape) {
            this.logger.warn("Channel with subscribeId '" + subscribeId + "' is not a portlet");
            return "";
        }
        catch (DataRetrievalFailureException e) {
            this.logger.warn("Failed to get portlet title: " + subscribeId, e);
            return "";
        }

        return this.getPortletTitle(portletWindow.getPortletWindowId(), request, response);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletTitle(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletTitle(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletEntity parentPortletEntity = portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
        final IChannelDefinition channelDefinition = parentPortletDefinition.getChannelDefinition();
        final IChannelParameter disableDynamicTitle = channelDefinition.getParameter("disableDynamicTitle");
        
        if (disableDynamicTitle == null || !Boolean.parseBoolean(disableDynamicTitle.getValue())) {
            final IPortletRenderExecutionWorker tracker = getRenderedPortlet(portletWindowId, request, response);
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
        return channelDefinition.getTitle();
    }

    protected IPortletWindow getDefaultPortletWindow(String subscribeId, HttpServletRequest request) {
        try {
            final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
            final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(userInstance, subscribeId);
            final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntity.getPortletEntityId());
            return portletWindow;
        }
        catch (NotAPortletException nape) {
            throw nape;
        }
        catch (RuntimeException re) {
            throw new DataRetrievalFailureException("Could not find IPortletWindow for subscribe id '" + subscribeId + "'", re);
        }
    }
    
    protected int getPortletRenderTimeout(IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = this.portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
//        return parentPortletDefinition.getChannelDefinition().getTimeout();
        //TODO just for debugging
        return (int)TimeUnit.MINUTES.toMillis(5);
    }

    protected IPortletRenderExecutionWorker getRenderedPortlet(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        if (tracker == null) {
            tracker = this.startPortletRenderInternal(portletWindowId, request, response);
        }
        return tracker;
    }
    
    /**
     * create and submit the rendering job to the thread pool
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
