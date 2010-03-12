/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.rendering;

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
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

/**
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
    
    
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void doPortletAction(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = this.portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
        final int timeout = parentPortletDefinition.getChannelDefinition().getTimeout();
        
        final PortletActionExecutionWorker portletActionExecutionWorker = new PortletActionExecutionWorker(portletWindowId, request, response);
        final Future<Long> portletActionFuture = this.portletThreadPool.submit(portletActionExecutionWorker);
        
        /*
         * TODO an action will include generated event handling, need to make sure we don't timeout portlets whos actions have completed
         * but are waiting on event handlers to complete
         */
        final Long actualExecutionTime = this.waitForWorker(portletActionExecutionWorker, portletActionFuture, timeout);
        
        //TODO publish portlet action event
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
        final PortletRenderExecutionWorker portletRenderExecutionWorker = new PortletRenderExecutionWorker(portletWindowId, request, response, portletOutputBuffer);
        final Future<PortletRenderResult> portletRenderFuture = this.portletThreadPool.submit(portletRenderExecutionWorker);
        
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
    public void outputPortlet(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response, Writer writer) {
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final IPortletDefinition parentPortletDefinition = this.portletEntityRegistry.getParentPortletDefinition(parentPortletEntity.getPortletEntityId());
        final int timeout = parentPortletDefinition.getChannelDefinition().getTimeout();

        final Map<IPortletWindowId, PortletRenderingTracker> portletRenderingMap = this.getPortletRenderingMap(request);
        PortletRenderingTracker tracker = portletRenderingMap.get(portletWindowId);
        if (tracker == null) {
            tracker = this.startPortletRenderInternal(portletWindowId, request, response);
        }
        
        final PortletRenderResult portletRenderResult = this.waitForWorker(tracker.portletRenderExecutionWorker, tracker.portletRenderFuture, timeout);
        
        //TODO publish portlet render event
    }
    
    /*
     * portlet execution Callable should track when the callable is created and then when
     * the callback is actually started and completed. That way we can measure thread pool
     * wait times.
     */

    @Override
    public void processEvents(PortletContainer container, PortletWindow portletWindow, HttpServletRequest request, HttpServletResponse response, List<Event> events) {
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
         * request attribute that tracks all of the Future objects for the event calls. Then it can wait
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
    
    protected <V> V waitForWorker(PortletExecutionWorker<V> worker, Future<V> future, long timeout) {
        try {
            //TODO we probably don't want to wait here forever for the worker to start, what is a reasonable wait time?
            final long startTime = worker.waitForStart();
            final V result = future.get(timeout - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
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
            future.cancel(true);
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
     * created, started and completed timestamps.
     */
    private abstract class PortletExecutionWorker<V> implements Callable<V> {
        private final Object startMutex = new Object();
        private final long created = System.currentTimeMillis();
        final IPortletWindowId portletWindowId;
        final HttpServletRequest request;
        final HttpServletResponse response;
        private volatile long started = 0;
        private volatile long complete = 0;
        
        public PortletExecutionWorker(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
            //TODO wrap request & response with timeout tracking impl
            this.portletWindowId = portletWindowId;
            this.request = request;
            this.response = response;
        }

        @Override
        public final V call() throws Exception {
            synchronized (this.startMutex) {
                //signal any threads waiting for the worker to start
                this.started = System.currentTimeMillis();
                this.startMutex.notifyAll();
            }
            try {
                return this.callInternal();
            }
            finally {
                this.complete = System.currentTimeMillis();
            }
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

        /**
         * @return time that the worker was created
         */
        public final long getCreated() {
            return this.created;
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
         * @return time that the Worker had to wait from being created until being started
         */
        public final long getWait() {
            return this.started - this.created;
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
    					"created=" + this.created + ", " +
						"complete=" + this.complete + ", " +
						"wait=" + this.getWait() + ", " +
                		"duration=" + this.getDuration() + "]";
        }
        
    }
    
    private class PortletActionExecutionWorker extends PortletExecutionWorker<Long> {
        public PortletActionExecutionWorker(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
            super(portletWindowId, request, response);
        }

        @Override
        protected Long callInternal() throws Exception {
            return portletRenderer.doAction(portletWindowId, request, response);
        }
    }
    
    private class PortletRenderExecutionWorker extends PortletExecutionWorker<PortletRenderResult> {
        private final Writer writer;
        
        public PortletRenderExecutionWorker(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response, Writer writer) {
            super(portletWindowId, request, response);
            this.writer = writer;
        }
        
        @Override
        protected PortletRenderResult callInternal() throws Exception {
            return portletRenderer.doRender(portletWindowId, request, response, writer);
        }
    }
}
