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

import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.EventCoordinationService;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;
import org.jasig.portal.channel.IChannelDefinition;
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
public class PortletExecutionManager implements EventCoordinationService, ApplicationEventPublisherAware, IPortletExecutionManager {
    private static final String PORTLET_RENDERING_MAP = PortletExecutionManager.class.getName() + ".PORTLET_RENDERING_MAP";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ApplicationEventPublisher applicationEventPublisher;
    
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private ExecutorService portletThreadPool;
    private IPortletRenderer portletRenderer;
    private IUserInstanceManager userInstanceManager;
    private EntityManagerFactory entityManagerFactory;
    
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
        
        /*
         * TODO an action will include generated event handling, need to make sure we don't timeout portlets whos actions have completed
         * but are waiting on event handlers to complete
         */
        final Long actualExecutionTime = portletActionExecutionWorker.get(timeout);
        
        //TODO publish portlet action event
        
        //TODO on error redirect to appropriate render URL
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
            return null;
        }
        catch (DataRetrievalFailureException e) {
            this.logger.warn("Failed to output portlet: " + subscribeId, e);
            return null;
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

        final PortletRenderResult portletRenderResult = tracker.get(timeout);
        //TODO publish portlet render event - should actually be published from the portlet renderer impl

        final String output = tracker.getOutput();
        return output != null ? output : "";
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
            return null;
        }
        catch (DataRetrievalFailureException e) {
            this.logger.warn("Failed to get portlet title: " + subscribeId, e);
            return null;
        }

        return this.getPortletTitle(portletWindow.getPortletWindowId(), request, response);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.IPortletExecutionManager#getPortletTitle(org.jasig.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletTitle(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final PortletRenderExecutionWorker tracker = getRenderedPortlet(portletWindowId, request, response);
        final int timeout = getPortletRenderTimeout(portletWindowId, request);
        final PortletRenderResult portletRenderResult = tracker.get(timeout);
        
        if (portletRenderResult == null) {
            return null;
        }
        
        return portletRenderResult.getTitle();
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
    
    /**
     * create and submit the rendering job to the thread pool
     */
    protected PortletRenderExecutionWorker startPortletRenderInternal(IPortletWindowId portletWindowId, HttpServletRequest request, HttpServletResponse response) {
        final PortletRenderExecutionWorker portletRenderExecutionWorker = new PortletRenderExecutionWorker(this.portletThreadPool, portletWindowId, request, response);
        portletRenderExecutionWorker.submit();
        
        final Map<IPortletWindowId, PortletRenderExecutionWorker> portletRenderingMap = this.getPortletRenderingMap(request);
        portletRenderingMap.put(portletWindowId, portletRenderExecutionWorker);
        
        return portletRenderExecutionWorker;
    }

    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, PortletRenderExecutionWorker> getPortletRenderingMap(HttpServletRequest request) {
        Map<IPortletWindowId, PortletRenderExecutionWorker> portletRenderingMap = (Map<IPortletWindowId, PortletRenderExecutionWorker>)request.getAttribute(PORTLET_RENDERING_MAP);
        if (portletRenderingMap == null) {
            portletRenderingMap = new ConcurrentHashMap<IPortletWindowId, PortletRenderExecutionWorker>();
            request.setAttribute(PORTLET_RENDERING_MAP, portletRenderingMap);
        }
        return portletRenderingMap;
    }
    
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
        
        public V get(long timeout) {
            try {
                //TODO we probably don't want to wait here forever for the worker to start, what is a reasonable wait time?
                final long startTime = this.waitForStart();
                return this.future.get(timeout - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // TODO ErrorPortlet handling an unhandled exception from the portlet
                this.logger.warn("Execution failed on portlet " + this.portletWindowId, e);
//                throw new RuntimeException("Portlet window id " + this.portletWindowId + " failed execution due to an exception.", e);
                return null;
            }
            catch (ExecutionException e) {
                // TODO ErrorPortlet handling an unhandled exception from the portlet
                this.logger.warn("Execution failed on portlet " + this.portletWindowId, e);
//                throw new RuntimeException("Portlet window id " + this.portletWindowId + " failed execution due to an exception.", e);
                return null;
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
                return null;
            }
        }
        
        /**
         * @return The Future for the submited portlet execution task.
         * @throws IllegalStateException if {@link #submit()} hasn't been called yet.
         */
        public final Future<V> getFuture() {
            if (this.future == null) {
                throw new IllegalStateException("submit() must be called before cancel(boolean) can be called");
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
}
