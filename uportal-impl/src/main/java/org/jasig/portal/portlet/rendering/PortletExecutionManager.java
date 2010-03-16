/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.EventCoordinationService;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

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
public class PortletExecutionManager implements EventCoordinationService, ApplicationEventPublisherAware {
    private static final String PORTLET_RENDERING_MAP = PortletExecutionManager.class.getName() + ".PORTLET_RENDERING_MAP";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ApplicationEventPublisher applicationEventPublisher;
    
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private ExecutorService portletThreadPool;
    private IPortletRenderer portletRenderer;
    private IUserInstanceManager userInstanceManager;
    
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

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public void doPortletAction(IPortletEntityId portletEntityId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntityId);
        this.doPortletAction(portletWindow.getPortletWindowId(), request, response);
    }

    public void doPortletAction(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = this.portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
        final int timeout = parentPortletDefinition.getChannelDefinition().getTimeout();
        
        final PortletActionExecutionWorker portletActionExecutionWorker = new PortletActionExecutionWorker(this.portletThreadPool, portletWindowId, request, response);
        portletActionExecutionWorker.submit();
        
        /*
         * TODO an action will include generated event handling, need to make sure we don't timeout portlets whos actions have completed
         * but are waiting on event handlers to complete
         */
        final Long actualExecutionTime = this.waitForWorker(portletActionExecutionWorker, timeout);
        
        //TODO publish portlet action event
        
        //TODO on error redirect to appropriate render URL
    }
    
    /**
     * Starts the specified portlet rendering, returns immediately.
     */
    public void startPortletRender(String subscribeId, HttpServletRequest request, HttpServletResponse response) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(userInstance, subscribeId);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntity.getPortletEntityId());
        
        this.startPortletRenderInternal(portletWindow.getPortletWindowId(), request, response);
    }
    
    /**
     * Starts the specified portlet rendering, returns immediately.
     */
    public void startPortletRender(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        this.startPortletRenderInternal(portletWindowId, request, response);
    }
    
    /**
     * create and submit the rendering job to the thread pool
     */
    protected PortletRenderingTracker startPortletRenderInternal(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final StringWriter portletOutputBuffer = new StringWriter();
        final PortletRenderExecutionWorker portletRenderExecutionWorker = new PortletRenderExecutionWorker(this.portletThreadPool, portletWindowId, request, response, portletOutputBuffer);
        portletRenderExecutionWorker.submit();
        
        final PortletRenderingTracker tracker = new PortletRenderingTracker(portletOutputBuffer, portletRenderExecutionWorker, portletRenderFuture);
        
        final Map<IPortletWindowId, PortletRenderingTracker> portletRenderingMap = this.getPortletRenderingMap(request);
        portletRenderingMap.put(portletWindowId, tracker);
        
        return tracker;
    }
    
    /**
     * Writes the specified portlet content to the Writer. If the portlet was already rendering due to a previous call to
     * {@link #startPortletRender(IPortletWindowId, HttpServletRequest, HttpServletResponse)} the output from that render will
     * be used. If the portlet is not already rendering it will be started.
     */
    public void outputPortlet(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response, Writer writer) throws IOException {
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = this.portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
        final int timeout = parentPortletDefinition.getChannelDefinition().getTimeout();

        final Map<IPortletWindowId, PortletRenderingTracker> portletRenderingMap = this.getPortletRenderingMap(request);
        PortletRenderingTracker tracker = portletRenderingMap.get(portletWindowId);
        if (tracker == null) {
            tracker = this.startPortletRenderInternal(portletWindowId, request, response);
        }
        
        /*
         * TODO waitForWorker needs to track if the portlet has already completed for this request and just return the request
         * cached result object
         */
        final PortletRenderResult portletRenderResult = this.waitForWorker(tracker.portletRenderExecutionWorker, tracker.portletRenderFuture, timeout);
        writer.write(tracker.portletOutputBuffer.toString());
        
        //TODO publish portlet render event
    }
    
    /*
     * portlet execution Callable should track when the callable is submitted and then when
     * the callback is actually started and completed. That way we can measure thread pool
     * wait times.
     */

    @Override
    public void processEvents(PortletContainer container, PortletWindow portletWindow, HttpServletRequest request, HttpServletResponse response, List<Event> events) {
        throw new UnsupportedOperationException("Events are not supported yet");
        /*
         * Note: that processEvents can be re-entrant.
         * 
         * If the result of a portlet handling an event generates additional events then processEvents
         * is called immediately by the container. This could potentially result in a large tree of
         * threads being used as each thread calling processEvents has to wait for all child events
         * to complete.
         * 
         * when an event handling thread reenters processEvents the event handling should be marked as
         * 'done'
         * 
         * perhaps only the very first processEvents call should wait for child events. It can store a
         * request attribute (ConcurrentLinkedQueue?) that tracks all of the Future objects for the event calls. Then it can wait
         * for all of those Futures to complete and handle the timeouts for them. The Callable will need
         * to be stored as well so start/end execution times can be tracked for timeout handling.
         */
        
        /*
         *  create portlet event map: Map<Portlet, List<Event>>
         *  iterate over events
         *      get list of all portlets registered for event
         *          build portlet event map data
         *            
         *  iterate over portlet event map        
         *      for each event list create thread that fires doEvent once per event
         * 
         */
    }
    
    protected <V> V waitForWorker(PortletExecutionWorker<V> worker, long timeout) {
        try {
            //TODO we probably don't want to wait here forever for the worker to start, what is a reasonable wait time?
            final long startTime = worker.waitForStart();
            final V result = worker.get(timeout - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Execution complete on portlet " + worker.portletWindowId + " in " + worker.getDuration() + "ms");
            }
            return result;
        }
        catch (InterruptedException e) {
            // TODO ErrorPortlet handling an unhandled exception from the portlet
            this.logger.warn("Execution failed on portlet " + worker.portletWindowId, e);
            throw new RuntimeException("Portlet window id " + worker.portletWindowId + " failed execution due to an exception.", e);
        }
        catch (ExecutionException e) {
            // TODO ErrorPortlet handling an unhandled exception from the portlet
            this.logger.warn("Execution failed on portlet " + worker.portletWindowId, e);
            throw new RuntimeException("Portlet window id " + worker.portletWindowId + " failed execution due to an exception.", e);
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
            this.logger.warn("Execution failed on portlet " + worker.portletWindowId, e);
            worker.cancel(true);
            throw new RuntimeException("Portlet window id " + worker.portletWindowId + " failed execution due to timeout.", e);
        }
    }

    protected Map<IPortletWindowId, PortletRenderingTracker> getPortletRenderingMap(HttpServletRequest request) {
        Map<IPortletWindowId, PortletRenderingTracker> portletRenderingMap = (Map<IPortletWindowId, PortletRenderingTracker>)request.getAttribute(PORTLET_RENDERING_MAP);
        if (portletRenderingMap == null) {
            portletRenderingMap = new ConcurrentHashMap<IPortletWindowId, PortletRenderingTracker>();
            request.setAttribute(PORTLET_RENDERING_MAP, portletRenderingMap);
        }
        return portletRenderingMap;
    }
    
    private static class PortletRenderingTracker {
        public final StringWriter portletOutputBuffer;
        public final PortletRenderExecutionWorker portletRenderExecutionWorker;
        public final Future<PortletRenderResult> portletRenderFuture;
        
        public PortletRenderingTracker(StringWriter portletOutputBuffer,
                PortletRenderExecutionWorker portletRenderExecutionWorker,
                Future<PortletRenderResult> portletRenderFuture) {
            this.portletOutputBuffer = portletOutputBuffer;
            this.portletRenderExecutionWorker = portletRenderExecutionWorker;
            this.portletRenderFuture = portletRenderFuture;
        }
    }
    
    /**
     * Base Callable impl for portlet execution dispatching. Tracks the target, request, response objects as well as
     * submitted, started and completed timestamps.
     */
    private static abstract class PortletExecutionWorker<V> {
        private final Object startMutex = new Object();
        private final ExecutorService executorService;
        final IPortletWindowId portletWindowId;
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
        }
        
        public final void submit() {
            this.submitted = System.currentTimeMillis();
            this.future = this.executorService.submit(new Callable<V>() {
                /* (non-Javadoc)
                 * @see java.util.concurrent.Callable#call()
                 */
                @Override
                public V call() throws Exception {
                    synchronized (startMutex) {
                        //signal any threads waiting for the worker to start
                        started = System.currentTimeMillis();
                        startMutex.notifyAll();
                    }
                    try {
                        return callInternal();
                    }
                    finally {
                        complete = System.currentTimeMillis();
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
            synchronized (this.startMutex) {
                if (!this.isStarted()) {
                    this.startMutex.wait();
                }
            }
            
            return this.started;
        }
        
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before cancel(boolean) can be called");
            }
            
            return this.future.cancel(mayInterruptIfRunning);
        }

        public V get() throws InterruptedException, ExecutionException {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before get() can be called");
            }
            
            return this.future.get();
        }

        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before get(long, TimeUnit) can be called");
            }
            
            return this.future.get(timeout, unit);
        }

        public boolean isCancelled() {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before isCancelled() can be called");
            }
            
            return this.future.isCancelled();
        }

        public boolean isDone() {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before isDone() can be called");
            }
            
            return this.future.isDone();
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
    
    private class PortletRenderExecutionWorker extends PortletExecutionWorker<PortletRenderResult> {
        private final Writer writer;
        
        public PortletRenderExecutionWorker(ExecutorService executorService, IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response, Writer writer) {
            super(executorService, portletWindowId, request, response);
            this.writer = writer;
        }
        
        @Override
        protected PortletRenderResult callInternal() throws Exception {
            return portletRenderer.doRender(portletWindowId, request, response, writer);
        }
    }
}
