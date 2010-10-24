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
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.registry.NotAPortletException;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.threading.TrackingThreadLocal;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
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
    public static final String REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID = PortletExecutionManager.class.getName() + ".CURRENT_FAILED_PORTLET_WINDOW_ID";
    public static final String REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE = PortletExecutionManager.class.getName() + ".CURRENT_EXCEPTION_CAUSE";
    public static final String DEFAULT_ERROR_PORTLET_FNAME = "error";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ApplicationEventPublisher applicationEventPublisher;
    
    private String errorPortletFname = DEFAULT_ERROR_PORTLET_FNAME;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private ExecutorService portletThreadPool;
    private IPortletRenderer portletRenderer;
    private IUserInstanceManager userInstanceManager;
    private EntityManagerFactory entityManagerFactory;
    private IPortletEventCoordinationService eventCoordinationService;
    
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

    @Autowired
    public void setPortletThreadPool(@Qualifier("portletThreadPool") ExecutorService portletThreadPool) {
        this.portletThreadPool = portletThreadPool;
    }

    @Autowired
    public void setPortletRenderer(IPortletRenderer portletRenderer) {
        this.portletRenderer = portletRenderer;
    }
    
    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

	/**
	 * @return the errorPortletFname
	 */
	public String getErrorPortletFname() {
		return errorPortletFname;
	}

	/**
	 * @param errorPortletFname the errorPortletFname to set
	 */
	public void setErrorPortletFname(String errorPortletFname) {
		this.errorPortletFname = errorPortletFname;
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
        
        final PortletActionExecutionWorker portletActionExecutionWorker = new PortletActionExecutionWorker(this.portletThreadPool, portletWindowId, request, response);
        portletActionExecutionWorker.submit();
        
        try {
			final Long actualExecutionTime = portletActionExecutionWorker.get(timeout);
			 //TODO publish portlet action event
		} catch (Exception e) {
			// put the exception into the error map for the session
	    	HttpSession session = request.getSession();
	    	Map<IPortletWindowId, Exception> portletFailureMap = safeRetrieveErrorMapFromSession(session);
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
        
        final Map<IPortletWindowId, PortletEventExecutionWorker> eventWorkers = new LinkedHashMap<IPortletWindowId, PortletEventExecutionWorker>();
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
                    final PortletEventExecutionWorker portletEventExecutionWorker = new PortletEventExecutionWorker(portletThreadPool, eventWindowId, request, response, event);
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
            final Set<Entry<IPortletWindowId, PortletEventExecutionWorker>> entrySet = eventWorkers.entrySet();
            for (final Iterator<Entry<IPortletWindowId, PortletEventExecutionWorker>> eventWorkerEntryItr = entrySet.iterator(); eventWorkerEntryItr.hasNext();) {
                final Entry<IPortletWindowId, PortletEventExecutionWorker> eventWorkerEntry = eventWorkerEntryItr.next();
                
                final PortletEventExecutionWorker eventWorker = eventWorkerEntry.getValue();
                final Future<Long> future = eventWorker.getFuture();
                if (future.isDone()) {
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
                final Iterator<Entry<IPortletWindowId, PortletEventExecutionWorker>> eventWorkerEntryItr = entrySet.iterator();
                final Entry<IPortletWindowId, PortletEventExecutionWorker> eventWorkerEntry = eventWorkerEntryItr.next();
                eventWorkerEntryItr.remove();
                
                final IPortletWindowId portletWindowId = eventWorkerEntry.getKey();
                final PortletEventExecutionWorker eventWorker = eventWorkerEntry.getValue();
                waitForEventWorker(request, eventQueue, eventWorker, portletWindowId);
            }
        }
    }

    protected void waitForEventWorker(
            HttpServletRequest request, PortletEventQueue eventQueue, 
            PortletEventExecutionWorker eventWorker, IPortletWindowId portletWindowId) {

        final int timeout = getPortletRenderTimeout(portletWindowId, request);
        
        try {
            //This should never actually wait since the future says it was done
            final Long actualExecutionTime = eventWorker.get(timeout);
             //TODO publish portlet event event
        }
        catch (Exception e) {
            // put the exception into the error map for the session
            //TODO event error handling?
            HttpSession session = request.getSession();
            Map<IPortletWindowId, Exception> portletFailureMap = safeRetrieveErrorMapFromSession(session);
            portletFailureMap.put(portletWindowId, e);
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
		
		PortletResourceExecutionWorker resourceWorker = new PortletResourceExecutionWorker(portletThreadPool, portletWindowId, request, response);
		resourceWorker.submit();
        
        try {
        	final PortletResourceResult resourceResult = resourceWorker.get(timeout);
			final Long actualExecutionTime = resourceResult.getRenderTime();
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
        final Map<IPortletWindowId, PortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        final PortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        
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
    	final PortletRenderExecutionWorker tracker = getRenderedPortlet(portletWindowId, request, response);
        final int timeout = getPortletRenderTimeout(portletWindowId, request);

		try {
			final PortletRenderResult portletRenderResult = tracker.get(timeout);
			 //TODO publish portlet render event - should actually be published from the portlet renderer impl
			final String output = tracker.getOutput();
			return output == null ? "" : output;
		} catch (Exception e) {
			return getErrorPortletOutput(portletWindowId, e, request, response);
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
	protected Map<IPortletWindowId, Exception> safeRetrieveErrorMapFromSession(HttpSession session) {
    	Object mutex = WebUtils.getSessionMutex(session);
    	synchronized(mutex) {
    		Map<IPortletWindowId, Exception> portletFailureMap = (Map<IPortletWindowId, Exception>) session.getAttribute("portletErrorMap");
    		if(portletFailureMap == null) {
    			portletFailureMap = new ConcurrentHashMap<IPortletWindowId, Exception>();
    			session.setAttribute(SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP, portletFailureMap);
    		}
    		return portletFailureMap;
    	}
    	
    }
    /**
     * 
     * @param failedPortletWindowId
     * @param request
     * @param response
     * @return
     */
    protected String getErrorPortletOutput(IPortletWindowId failedPortletWindowId, Exception cause, HttpServletRequest request, HttpServletResponse response) {
    	// place the cause in the session attached to the failed portletWindowId
    	HttpSession session = request.getSession();
    	Map<IPortletWindowId, Exception> portletFailureMap = safeRetrieveErrorMapFromSession(session);
		portletFailureMap.put(failedPortletWindowId, cause);
		
		// BEGIN code block required to find the IPortletWindowId for the portlet with this.errorPortletFname
		final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
		final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final String errorPortletSubscribeId = userLayoutManager.getSubscribeId(this.errorPortletFname);
		IPortletEntity errorPortletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(userInstance, errorPortletSubscribeId);
		final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, errorPortletEntity.getPortletEntityId());
    	IPortletWindowId errorPortletWindowId = portletWindow.getPortletWindowId();
    	// END code block required to find the IPortletWindowId for the portlet with this.errorPortletFname
    	
    	// dispatch to the error portlet and capture the output
    	final StringWriter writer = new StringWriter();
    	request.setAttribute(REQUEST_ATTRIBUTE__CURRENT_FAILED_PORTLET_WINDOW_ID, failedPortletWindowId);
    	request.setAttribute(REQUEST_ATTRIBUTE__CURRENT_EXCEPTION_CAUSE, cause);
    	this.portletRenderer.doRender(errorPortletWindowId, request, response, writer);
    	
    	// once we've grabbed the output, remove the throwable from the session map
    	portletFailureMap.remove(failedPortletWindowId);
    	
    	return writer.toString();
    }
    
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
            final PortletRenderExecutionWorker tracker = getRenderedPortlet(portletWindowId, request, response);
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

    protected PortletRenderExecutionWorker getRenderedPortlet(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final Map<IPortletWindowId, PortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        PortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        if (tracker == null) {
            tracker = this.startPortletRenderInternal(portletWindowId, request, response);
        }
        return tracker;
    }
    
    /**
     * create and submit the rendering job to the thread pool
     */
    protected PortletRenderExecutionWorker startPortletRenderInternal(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
    	// first check to see if there is a Throwable in the session for this IPortletWindowId
    	HttpSession session = request.getSession();
    	Map<IPortletWindowId, Exception> portletFailureMap = safeRetrieveErrorMapFromSession(session);
    	Exception cause = portletFailureMap.remove(portletWindowId);
    	
    	final PortletRenderExecutionWorker portletRenderExecutionWorker;
    	if(null != cause) {
    		// previous action failed, dispatch to errorPortlet immediately and return a dummy worker that already has it's output
    		String errorOutput = getErrorPortletOutput(portletWindowId, cause, request, response);
    		portletRenderExecutionWorker = new PortletFailureExecutionWorker(this.portletThreadPool, portletWindowId, request, response, cause, errorOutput);
    	} else {
    		portletRenderExecutionWorker = new PortletRenderExecutionWorker(this.portletThreadPool, portletWindowId, request, response);
            portletRenderExecutionWorker.submit();
    	}
    	
        final Map<IPortletWindowId, PortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        portletRenderingMap.put(portletWindowId, portletRenderExecutionWorker);
        
        return portletRenderExecutionWorker;
    }

    /**
     * Returns a request attribute scoped Map of portlets that are rendering for the current request.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, PortletRenderExecutionWorker> getPortletRenderingMap(HttpServletRequest request) {
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            Map<IPortletWindowId, PortletRenderExecutionWorker> portletRenderingMap = (Map<IPortletWindowId, PortletRenderExecutionWorker>)request.getAttribute(PORTLET_RENDERING_MAP);
            if (portletRenderingMap == null) {
                portletRenderingMap = new ConcurrentHashMap<IPortletWindowId, PortletRenderExecutionWorker>();
                request.setAttribute(PORTLET_RENDERING_MAP, portletRenderingMap);
            }
            return portletRenderingMap;
        }
    }
    
    //TODO move workers into their own package
    
    /**
     * Base Callable impl for portlet execution dispatching. Tracks the target, request, response objects as well as
     * submitted, started and completed timestamps.
     */
    private abstract class PortletExecutionWorker<V> {
        protected final Log logger = LogFactory.getLog(this.getClass());
        
        private final CountDownLatch startLatch = new CountDownLatch(1);
        private final ExecutorService executorService;
        final IPortletWindowId portletWindowId;
        final String fName;
        final HttpServletRequest request;
        final HttpServletResponse response;
        
        private Future<V> future;
        private volatile long submitted = 0;
        private volatile long started = 0;
        private volatile long complete = 0;
        
        public PortletExecutionWorker(ExecutorService executorService, IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
            //TODO wrap request & response with timeout tracking impl
            this.executorService = executorService;
            this.portletWindowId = portletWindowId;
            this.request = request;
            this.response = response;
            
            final IPortletEntity parentPortletEntity = portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
            final IPortletDefinition parentPortletDefinition = portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
            final IChannelDefinition channelDefinition = parentPortletDefinition.getChannelDefinition();
            this.fName = channelDefinition.getFName();
        }
        
        public final void submit() {
            this.submitted = System.currentTimeMillis();
            
            final Map<TrackingThreadLocal<Object>, Object> trackingThreadLocalData = TrackingThreadLocal.getCurrentData();
            final RequestAttributes requestAttributesFromHolder = RequestContextHolder.getRequestAttributes();
            final Locale localeFromHolder = LocaleContextHolder.getLocale();
            
            this.future = this.executorService.submit(new Callable<V>() {
                /* (non-Javadoc)
                 * @see java.util.concurrent.Callable#call()
                 */
                @Override
                public V call() throws Exception {
                    final Thread currentThread = Thread.currentThread();
                    final String threadName = currentThread.getName();
                    currentThread.setName(threadName + "-[" + fName + "]");
                    
                    TrackingThreadLocal.setCurrentData(trackingThreadLocalData);
                    RequestContextHolder.setRequestAttributes(requestAttributesFromHolder);
                    LocaleContextHolder.setLocale(localeFromHolder);
                    
                    final EntityManagerFactory emf = entityManagerFactory;
                    final boolean participate = this.openEntityManager(emf);
                    
                    started = System.currentTimeMillis();
                    //signal any threads waiting for the worker to start
                    startLatch.countDown();
                    try {
                        return callInternal();
                    }
                    catch (Exception e) {
                        logger.warn("Portlet '" + fName + "' failed with an exception", e);
                        throw e;
                    }
                    finally {
                        complete = System.currentTimeMillis();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Execution complete on portlet " + portletWindowId + " in " + getDuration() + "ms");
                        }
                        
                        this.closeEntityManager(emf, participate);
                        
                        TrackingThreadLocal.clearCurrentData();
                        LocaleContextHolder.resetLocaleContext();
                        RequestContextHolder.resetRequestAttributes();
                        
                        currentThread.setName(threadName);
                    }
                }

                private boolean openEntityManager(final EntityManagerFactory emf) {
                    if (TransactionSynchronizationManager.hasResource(emf)) {
                        // Do not modify the EntityManager: just set the participate flag.
                        return true;
                    }

                    logger.debug("Opening JPA EntityManager in PortletExecutionWorker");
                    try {
                        final EntityManager em = entityManagerFactory.createEntityManager();
                        TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em));
                    }
                    catch (PersistenceException ex) {
                        throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
                    }
                    
                    return false;
                }

                private void closeEntityManager(final EntityManagerFactory emf, final boolean participate) {
                    if (!participate) {
                        final EntityManagerHolder emHolder = (EntityManagerHolder)TransactionSynchronizationManager.unbindResource(emf);
                        logger.debug("Closing JPA EntityManager in PortletExecutionWorker");
                        EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
                    }
                }
            });
        }
        
        /**
         * @see Callable#call()
         */
        protected abstract V callInternal() throws Exception;
        
        public final boolean isStarted() {
            return this.started > 0;
        }
        
        /**
         * Wait for the worker to start, return the start time.
         */
        public final long waitForStart() throws InterruptedException {
            //Wait for start Callable to start
            this.startLatch.await();
            return this.started;
        }
        
        public V get(long timeout) throws Exception {
            try {
                //TODO we probably don't want to wait here forever for the worker to start, what is a reasonable wait time?
                final long startTime = this.waitForStart();
                return this.future.get(timeout - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // TODO ErrorPortlet handling an unhandled exception from the portlet
                this.logger.warn("Execution failed on portlet " + this.portletWindowId, e);
//                throw new RuntimeException("Portlet window id " + this.portletWindowId + " failed execution due to an exception.", e);
                //return null;
                throw e;
            }
            catch (ExecutionException e) {
                // TODO ErrorPortlet handling an unhandled exception from the portlet
                this.logger.warn("Execution failed on portlet " + this.portletWindowId, e);
//                throw new RuntimeException("Portlet window id " + this.portletWindowId + " failed execution due to an exception.", e);
                //return null;
                throw e;
            }
            catch (TimeoutException e) {
                // TODO ErrorPortlet handling a timeout from the portlet
                /*
                 * timeout handling
                 *  render ErrorPortlet
                 *  mark soft-timeout in request/response, this only allows content output
                 *  wait for configured grace period
                 *  mark hard-timeout in request/response, any API fails
                 *  call future.cancel(true)
                 */
                this.logger.warn("Execution failed on portlet " + this.portletWindowId, e);
                this.future.cancel(true);
//                throw new RuntimeException("Portlet window id " + this.portletWindowId + " failed execution due to timeout.", e);
                //return null;
                throw e;
            }
        }
        
        /**
         * @return The Future for the submited portlet execution task.
         * @throws IllegalStateException if {@link #submit()} hasn't been called yet.
         */
        public final Future<V> getFuture() {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before getFuture() can be called");
            }
            
            return this.future;
        }

        /**
         * @return time that the worker was submitted
         */
        public final long getSubmitted() {
            return this.submitted;
        }

        /**
         * @return time that {@link #callInternal()} was called 
         */
        public final long getStarted() {
            return this.started;
        }

        /**
         * @return time that {@link #callInternal()} completed
         */
        public final long getComplete() {
            return this.complete;
        }
        
        /**
         * @return time that the Worker had to wait from being submitted until being started
         */
        public final long getWait() {
            return this.started - this.submitted;
        }
        
        /**
         * @return time that the Worker took to execute {@link #callInternal()}
         */
        public final long getDuration() {
            return this.complete - this.started;
        }

        @Override
        public String toString() {
            return "PortletExecutionWorker [" +
                		"portletWindowId=" + this.portletWindowId + ", " +
        				"started=" + this.started + ", " +
    					"submitted=" + this.submitted + ", " +
						"complete=" + this.complete + ", " +
						"wait=" + this.getWait() + ", " +
                		"duration=" + this.getDuration() + "]";
        }
        
    }
    
    private class PortletActionExecutionWorker extends PortletExecutionWorker<Long> {
        public PortletActionExecutionWorker(ExecutorService executorService, IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
            super(executorService, portletWindowId, request, response);
        }

        @Override
        protected Long callInternal() throws Exception {
            return portletRenderer.doAction(portletWindowId, request, response);
        }
    }
    
    private class PortletEventExecutionWorker extends PortletExecutionWorker<Long> {
        private final Event event;
        
        public PortletEventExecutionWorker(
                ExecutorService executorService, IPortletWindowId portletWindowId, 
                HttpServletRequest request, HttpServletResponse response,
                Event event) {
            
            super(executorService, portletWindowId, request, response);
            
            this.event = event;
        }

        @Override
        protected Long callInternal() throws Exception {
            return portletRenderer.doEvent(portletWindowId, request, response, event);
        }
    }
    
    private class PortletRenderExecutionWorker extends PortletExecutionWorker<PortletRenderResult> {
        private String output = null;
        
        public PortletRenderExecutionWorker(ExecutorService executorService, IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
            super(executorService, portletWindowId, request, response);
        }
        
        @Override
        protected PortletRenderResult callInternal() throws Exception {
            final StringWriter writer = new StringWriter();
            final PortletRenderResult result = portletRenderer.doRender(portletWindowId, request, response, writer);
            this.output = writer.toString();
            return result;
        }

        //Buffer used to write portlet content to.
        public String getOutput() {
            return this.output;
        }
    }
    
    private class PortletResourceExecutionWorker extends PortletExecutionWorker<PortletResourceResult> {
    	
		public PortletResourceExecutionWorker(ExecutorService executorService,
				IPortletWindowId portletWindowId, HttpServletRequest request,
				HttpServletResponse response) {
			super(executorService, portletWindowId, request, response);
		}

		/* (non-Javadoc)
		 * @see org.jasig.portal.portlet.rendering.PortletExecutionManager.PortletExecutionWorker#callInternal()
		 */
		@Override
		protected PortletResourceResult callInternal() throws Exception {
            final PortletResourceResult result = portletRenderer.doServeResource(portletWindowId, request, response);
            return result;
		}
    	
    }
    
    private class PortletFailureExecutionWorker extends PortletRenderExecutionWorker {
		private final Exception cause;
		private String errorOutput;
    	
    	public PortletFailureExecutionWorker(ExecutorService executorService,
				IPortletWindowId portletWindowId, HttpServletRequest request,
				HttpServletResponse response, Exception cause, String errorOutput) {
			super(executorService, portletWindowId, request, response);
			this.cause = cause;
			this.errorOutput = errorOutput;
		}

		/* (non-Javadoc)
		 * @see org.jasig.portal.portlet.rendering.PortletExecutionManager.PortletExecutionWorker#callInternal()
		 */
		@Override
		protected PortletRenderResult callInternal() throws Exception {
			throw cause;
		}

		/* (non-Javadoc)
		 * @see org.jasig.portal.portlet.rendering.PortletExecutionManager.PortletRenderExecutionWorker#getOutput()
		 */
		@Override
		public String getOutput() {
			return this.errorOutput;
		}
		
    }
}
